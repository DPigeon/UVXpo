package com.example.utilisateur.uvexposureapp;

import androidx.appcompat.app.AppCompatActivity;

import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class BluetoothActivity extends AppCompatActivity {
    private static final int REQUEST_ENABLE_BT = 0;
    private static final int REQUEST_DISCOVER_BT = 1;

    Button buttonOn, buttonOff, buttonDiscoverable, buttonPairedDevices;
    TextView bluetoothStatusTextView, pairedTextView;

    BluetoothAdapter bluetoothAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bluetooth);

        setupUI();
    }

    protected void setupUI() {
        bluetoothStatusTextView = findViewById(R.id.bluetoothStatusTextView);
        buttonOn = findViewById(R.id.buttonOn);
        buttonOff = findViewById(R.id.buttonOff);
        buttonDiscoverable = findViewById(R.id.buttonDiscoverable);
        buttonPairedDevices = findViewById(R.id.pairedDevicesButton);
        pairedTextView = findViewById(R.id.pairedDeviceTextView);

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter(); // Initialize bluetooth
        bluetoothSetStatus();
        bluetoothButtons();
    }

    protected void bluetoothSetStatus() { // Sets the status of the bluetooth connection
        if (bluetoothAdapter == null) {
            Log.i("BT", "Bluetooth not supported on Virtual Devices! Use a real device.");
            displayToast("Bluetooth is not available!");
            finish();
        } else
            displayToast("Bluetooth is available!");

            if (bluetoothAdapter.isEnabled())
                displayToast("Bluetooth is on!");
            else
                displayToast("Bluetooth is off!");
    }

    protected void bluetoothButtons() {
        buttonOn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!bluetoothAdapter.isEnabled()) {
                    displayToast("Turning on Bluetooth...");
                    Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                    startActivityForResult(intent, REQUEST_ENABLE_BT);
                } else
                    displayToast("Bluetooth is already on!");
            }
        });
    }

    protected void displayToast(String message) {
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG);
    }

}
