package com.example.utilisateur.uvexposureapp;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.TargetApi;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

/*
 * The MainActivity where the bluetooth connection is made and the data is fetched.
 */

import java.util.Set;
import java.util.UUID;

import static android.bluetooth.BluetoothAdapter.STATE_CONNECTED;

public class MainActivity extends AppCompatActivity {
    TextView testDataTextView;
    protected Button weatherButton, bluetoothActivityButton, graphButton, settingsButton, faqButton;
    String FaqURL = "https://www.ccohs.ca/oshanswers/phys_agents/ultravioletradiation.html?fbclid=IwAR05zwUhYrQqcc0bNr-nSeWcbN7J1LUsjgW3K7Bs5oT49s_O9XrgfFpZybY";

    @TargetApi(Build.VERSION_CODES.CUPCAKE)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setupUI();
        connectAndListen();
    }

    protected void setupAction() { // No action bar for the main activity
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(false);
    }

    protected void setupUI() {
        setupAction();
        bluetoothActivityButton = findViewById(R.id.bluetoothButton);
        testDataTextView = findViewById(R.id.testDataTextView);
        graphButton = findViewById(R.id.graphButton);
        weatherButton = findViewById(R.id.weatherButton);
        settingsButton = findViewById(R.id.settingsButton);
        bluetoothActivityButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                goToActivity(BluetoothActivity.class);
            }
        });
        weatherButton.setText("Get Current Weather Data");
        weatherButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                goToActivity(WeatherActivity.class);
            }
        });
        graphButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                goToActivity(GraphActivity.class);
            }
        });
        settingsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                goToActivity(UserActivity.class);
            }
        });
        faqButton = findViewById(R.id.faqButton);
        faqButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openFaqWebsite(view);
            }
        });
    }

    /* Linking website to FAQ button */
    public void openFaqWebsite(View view){
        Intent browserIntent=new Intent(Intent.ACTION_VIEW, Uri.parse(FaqURL));
        startActivity(browserIntent);
    }

    void goToActivity(Class page) { // Function that goes from the main activity to another one
        Intent intent = new Intent(MainActivity.this, page); // from the main activity to the profile class
        startActivity(intent);
    }

    // Connects to the Bluetooth device & starts the service to listen to inputs
    protected void connectAndListen() {
        final UUID SERVICE_UUID = UUID.fromString("b923eeab-9473-4b86-8607-5068911b18fe"); // First layer
        final UUID CHARACTERISTIC_UUID = UUID.fromString("aba24047-b36f-4646-92ce-3d5c0c75bd20"); // Second layer
        final UUID CLIENT_CHARACTERISTIC_CONFIG_UUID = convertFromInteger(0x2902); // Used to send data from phone --> microcontroller
        boolean foundDevice = false;

        if (BluetoothAdapter.getDefaultAdapter() == null) {
            Log.i("BT", "Bluetooth not supported on Virtual Devices! Use a real device.");
            finish(); // Allowing to skip the exception
        } else {
            BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

            BluetoothDevice bluetoothDevice = null;
            Set<BluetoothDevice> devices;
            devices = bluetoothAdapter.getBondedDevices(); // We get the devices
            for (BluetoothDevice device : devices) {
                if (device.getAddress().equals("24:0A:C4:05:C6:8A")) { // We find the Arduino device server
                    bluetoothDevice = device;
                    foundDevice = true;
                    break;
                }
            }
            if (foundDevice) { // If found connect, otherwise don't
                BluetoothGatt gatt; // We're using a low energy bluetooth microcontroller. Must use Gatt.

                /* This is the callback where all the receiving and sending happens. Once connected, this callback thread runs in background */
                BluetoothGattCallback gattCallback = new BluetoothGattCallback() {
                    @Override
                    public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) { // If connection state changes
                        if (newState == STATE_CONNECTED)
                            gatt.discoverServices(); // Discover the services to update
                    }

                    @Override
                    public void onServicesDiscovered(BluetoothGatt gatt, int status) { // As soon as we discover new services
                        super.onServicesDiscovered(gatt, status);
                        BluetoothGattCharacteristic characteristic = gatt.getService(SERVICE_UUID).getCharacteristic(CHARACTERISTIC_UUID);
                        gatt.setCharacteristicNotification(characteristic, true); // Set notifications on

                        BluetoothGattDescriptor descriptor = characteristic.getDescriptor(CLIENT_CHARACTERISTIC_CONFIG_UUID); // Set descriptor on

                        descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                        gatt.writeDescriptor(descriptor);
                    }

                    @Override
                    public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) { // Used to send data phone --> microcontroller
                        BluetoothGattCharacteristic characteristic =
                                gatt.getService(SERVICE_UUID).getCharacteristic(CHARACTERISTIC_UUID);
                        String data = "Hello Microcontroller !";
                        byte[] byteArray = data.getBytes();
                        characteristic.setValue(byteArray); // Send data here example
                        gatt.writeCharacteristic(characteristic);
                    }

                    @Override
                    public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) { // Listen to data here from the characteristic
                        String data = new String(characteristic.getValue()); // Converting from byte[] to string
                        processDataOnScreen(data);
                    }
                };
                gatt = bluetoothDevice.connectGatt(this, true, gattCallback); // Connect with a callback
            }
        }
    }

    public void processDataOnScreen(final String data) { // Processes the data from Bluetooth Thread --> UI thread
        final String str = data;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                testDataTextView.setText(data);
            }
        });
    }

    public UUID convertFromInteger(int i) { // Used to get the right UUID for descriptor and characteristic of Gatt Bluetooth
        final long MSB = 0x0000000000001000L;
        final long LSB = 0x800000805f9b34fbL;
        long value = i & 0xFFFFFFFF;
        return new UUID(MSB | (value << 32), LSB);
    }

}
