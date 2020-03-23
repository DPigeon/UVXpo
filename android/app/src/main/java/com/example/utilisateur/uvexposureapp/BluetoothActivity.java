package com.example.utilisateur.uvexposureapp;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Set;

public class BluetoothActivity extends AppCompatActivity {
    private static final int REQUEST_ENABLE_BT = 0;
    private static final int REQUEST_DISCOVER_BT = 1;

    Button toggleButton, discoverableButton, pairedDevicesButton;
    TextView pairedTextView;

    BluetoothAdapter bluetoothAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bluetooth);

        setupUI();
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    public void onBackPressed()
    {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }

    protected void setupUI() {
        toggleButton = findViewById(R.id.toggleButton);
        discoverableButton = findViewById(R.id.discoverableButton);
        pairedDevicesButton = findViewById(R.id.pairedDevicesButton);
        pairedTextView = findViewById(R.id.pairedDeviceTextView);

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter(); // Initialize bluetooth
        bluetoothSetStatus();
        bluetoothButtons();
    }

    protected void bluetoothSetStatus() { // Sets the status of the bluetooth connection
        if (bluetoothAdapter == null) {
            Log.i("BT", "Bluetooth not supported on Virtual Devices! Use a real device.");
            finish(); // Allowing to skip the exception
        } else
            displayToast("Bluetooth is available!");

            if (bluetoothAdapter.isEnabled())
                displayToast("Bluetooth is on!");
            else
                displayToast("Bluetooth is off!");
    }

    protected void bluetoothButtons() {
        // Toggle button
        toggleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!bluetoothAdapter.isEnabled()) {
                    displayToast("Turning on Bluetooth...");
                    // Start a new bluetooth activity
                    Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                    startActivityForResult(intent, REQUEST_ENABLE_BT);
                    toggleButton.setText("On");
                    pairedTextView.setText(""); // Clearing the paired devices list
                } else {
                    displayToast("Turning Bluetooth off...");
                    bluetoothAdapter.disable();
                    toggleButton.setText("Off");
                    pairedTextView.setText(""); // Clearing the paired devices list
                }
            }
        });

        discoverableButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!bluetoothAdapter.isDiscovering()) {
                    displayToast("Making the device discoverable...");
                    // Creates a new intent for allowing bluetooth to be discoverable
                    Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
                    startActivityForResult(intent, REQUEST_DISCOVER_BT);
                }
            }
        });

        pairedDevicesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (bluetoothAdapter.isEnabled()) { // Shows all devices that are paired in a simple list
                    Set<BluetoothDevice> devices = bluetoothAdapter.getBondedDevices();
                    if (devices.size() != 0) {
                        pairedTextView.setText("Paired Devices: \n");
                        for (BluetoothDevice device : devices)
                            pairedTextView.append("\nDevice" + device.getName() + ", " + device);
                    } else
                        pairedTextView.setText("Paired Devices: None");
                } else {
                    displayToast("No paired devices to be shown.");
                    pairedTextView.setText(""); // Clearing the list
                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        // Used to show an action after sent to a new activity
        switch(resultCode) {
            case REQUEST_ENABLE_BT:
                if (resultCode == RESULT_OK)
                    displayToast("Bluetooth is on!");
                else
                    displayToast("Could not turn Bluetooth on!");
                break;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    protected void displayToast(String message) { // Used to display toasts
        Toast toast = Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG);
        toast.show();
    }

}
