package com.example.utilisateur.uvexposureapp;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import android.annotation.TargetApi;
import android.app.Notification;
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

import java.util.Set;
import java.util.UUID;

import static android.bluetooth.BluetoothAdapter.STATE_CONNECTED;
import static com.example.utilisateur.uvexposureapp.Notifications.CHANNELID_1;
import static com.example.utilisateur.uvexposureapp.Notifications.CHANNELID_2;

/*
 * The MainActivity where the bluetooth connection is made and the data is fetched.
 */

public class MainActivity extends AppCompatActivity {
    private NotificationManagerCompat notificationManagerCompat;

    protected Button weatherButton, graphButton, settingsButton, faqButton;
    String FaqURL = "https://www.ccohs.ca/oshanswers/phys_agents/ultravioletradiation.html?fbclid=IwAR05zwUhYrQqcc0bNr-nSeWcbN7J1LUsjgW3K7Bs5oT49s_O9XrgfFpZybY";
    String TAG = "MainActivity";
    String usernameIntentExtra;
    Boolean newusercheck;

    @TargetApi(Build.VERSION_CODES.CUPCAKE)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Bundle userNameIntent = getIntent().getExtras(); /**GETS USER INTENTS SO DATA COULD BE RETRIEVED*/
        usernameIntentExtra = userNameIntent.getString("username");
        newusercheck = userNameIntent.getBoolean("checknewuser");
      
        setupUI();
        connectAndListen();
        notificationManagerCompat = NotificationManagerCompat.from(this);
    }

    protected void setupAction() { // No action bar for the main activity
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(false);
    }

    protected void setupUI() {
        setupAction();
        graphButton = findViewById(R.id.graphButton);
        weatherButton = findViewById(R.id.weatherButton);
        settingsButton = findViewById(R.id.settingsButton);
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
                Intent intent = new Intent(MainActivity.this, UserActivity.class);
                intent.removeExtra("username");
                intent.removeExtra("checknewuser");
                intent.putExtra("username", usernameIntentExtra);/**ADDING INTENT SO USER DATA CAN BE RETRIEVED*/
                intent.putExtra("checknewuser", newusercheck);
                startActivity(intent);
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
    public void openFaqWebsite(View view) {
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(FaqURL));
        startActivity(browserIntent);
    }

    void goToActivity(Class page) { // Function that goes from the main activity to another one
        Intent intent = new Intent(MainActivity.this, page); // from the main activity to the profile class
        startActivity(intent);
    }

    // Connects to the Bluetooth device & starts the service to listen to inputs
    protected void connectAndListen() {
        final String DEVICE_ADDRESS = "24:0A:C4:05:C6:8A"; // We will have to allow more devices later
        final UUID SERVICE_UUID = UUID.fromString("b923eeab-9473-4b86-8607-5068911b18fe"); // First layer
        final UUID CHARACTERISTIC_UUID = UUID.fromString("aba24047-b36f-4646-92ce-3d5c0c75bd20"); // Second layer
        final UUID CLIENT_CHARACTERISTIC_CONFIG_UUID = convertFromInteger(0x2902); // Used to send data from phone --> microcontroller
        boolean foundDevice = false;

        if (BluetoothAdapter.getDefaultAdapter() == null) {
            Log.i(TAG, "Bluetooth not supported on Virtual Devices! Use a real device.");
            finish(); // Allowing to skip the exception
        } else {
            BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

            BluetoothDevice bluetoothDevice = null;
            Set<BluetoothDevice> devices;
            devices = bluetoothAdapter.getBondedDevices(); // We get the devices
            for (BluetoothDevice device : devices) {
                if (device.getAddress().equals(DEVICE_ADDRESS)) { // We find the Arduino device server
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

                    @RequiresApi(api = Build.VERSION_CODES.O)
                    @Override
                    public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) { // Listen to data here from the characteristic
                        String data = new String(characteristic.getValue()); // Converting from byte[] to string
                        double val = Double.parseDouble(data);
                        notificationFunction(val);
                        /* Here we broadcast data to other activities using a specific action */
                        Intent intent = new Intent("graph-activity"); // New intent to send called live-data
                        intent.setPackage(getPackageName()); // Setup package
                        intent.putExtra("uv-live-data", data); // Put a string message with it
                        getApplicationContext().sendBroadcast(intent); // Send the message over broadcast
                    }
                };
                gatt = bluetoothDevice.connectGatt(this, true, gattCallback); // Connect with a callback
            }
        }
    }

    public UUID convertFromInteger(int i) { // Used to get the right UUID for descriptor and characteristic of Gatt Bluetooth
        final long MSB = 0x0000000000001000L;
        final long LSB = 0x800000805f9b34fbL;
        long value = i & 0xFFFFFFFF;
        return new UUID(MSB | (value << 32), LSB);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public void notificationFunction(double data) {
        int uvIndex = 0;
        double voltage = data * (3.3 / 1023) * 1000; // using 3.3 mV

        // From http://educ8s.tv/arduino-uv-meter-project/ with converting of 3.3V output
        if (voltage < 33)
            uvIndex = 0;
        else if (voltage > 33 && voltage <= 150)
            uvIndex = 1;
        else if (voltage > 150 && voltage <= 210)
            uvIndex = 2;
        else if (voltage > 210 && voltage <= 269)
            uvIndex = 3;
        else if (voltage > 269 && voltage <= 332)
            uvIndex = 4;
        else if (voltage > 332 && voltage <= 400)
            uvIndex = 5;
        else if (voltage > 400 && voltage <= 459)
            uvIndex = 6;
        else if (voltage > 459 && voltage <= 525)
            uvIndex = 7;
        else if (voltage > 525 && voltage <= 581)
            uvIndex = 8;
        else if (voltage > 581 && voltage <= 644)
            uvIndex = 9;
        else if (voltage > 644 && voltage <= 712)
            uvIndex = 10;
        else if (voltage > 712)
            uvIndex = 11;

        if  (uvIndex >=1 && uvIndex <= 2){
            channel2Notifmedium();
        } else if (uvIndex > 2 &&uvIndex <= 5) {
            channel2Notifhigh();
        } else if (uvIndex > 5&& uvIndex <=7) {
            channel2Notifhigh();
        } else if (uvIndex > 7&&uvIndex <= 10) {
            channel2Notifveryhigh();
        } else if (uvIndex > 11) {
            channel2Notifextremelyhigh();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public void channel1Notif() {
    Notification notifications = new NotificationCompat.Builder(this,CHANNELID_1)
            .setSmallIcon(R.drawable.ic_sentiment_satisfied_black_24dp)
            .setContentTitle("It is pretty sunny out there!")
            .setContentText("Stay in shade, apply sunscreen and wear sunglasses!")
            .build();
        notificationManagerCompat.notify(1,notifications);
    }

    public void channel2Notif() {
        Notification notifications = new NotificationCompat.Builder(this,CHANNELID_2)
                .setSmallIcon(R.drawable.ic_sentiment_satisfied_black_24dp)
                .setContentTitle("Wow its hot! ")
                .setContentText("Make sure to protect yourself with a hat!")
                .build();
        notificationManagerCompat.notify(2,notifications);
    }
    public void channel2Notifmedium() {
        Notification notifications = new NotificationCompat.Builder(this,CHANNELID_2)
                .setSmallIcon(R.drawable.ic_sun)
                .setContentTitle("Yikes! The sun is strong today!")
                .setContentText("A little sunscreen wouldn't hurt!")
                .build();
        notificationManagerCompat.notify(2,notifications);
    }
    public void channel2Notifhigh() {
        Notification notifications = new NotificationCompat.Builder(this,CHANNELID_2)
                .setSmallIcon(R.drawable.ic_sun)
                .setContentTitle("UV app")
                .setContentText("Sunscreen, Shade and a hat would be nice to combat UV exposure today!")
                .build();
        notificationManagerCompat.notify(2,notifications);
    }
    public void channel2Notifveryhigh() {
        Notification notifications = new NotificationCompat.Builder(this,CHANNELID_2)
                .setSmallIcon(R.drawable.ic_notif1)
                .setContentTitle("WARNING! Dangerous levels of UV detected! ")
                .setContentText("Make sure to be careful out there and apply lots of suncreen!")
                .build();
        notificationManagerCompat.notify(2,notifications);
    }
    public void channel2Notifextremelyhigh() {
        Notification notifications = new NotificationCompat.Builder(this,CHANNELID_2)
                .setSmallIcon(R.drawable.ic_warning)
                .setContentTitle("WARNING!!!")
                .setContentText("Best to stay indoors today, dangerous levels of UV detected.")
                .build();
        notificationManagerCompat.notify(2,notifications);
    }
}
