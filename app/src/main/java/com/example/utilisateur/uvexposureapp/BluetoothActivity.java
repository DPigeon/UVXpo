package com.example.utilisateur.uvexposureapp;

import androidx.appcompat.app.AppCompatActivity;

import android.bluetooth.BluetoothAdapter;
import android.os.Bundle;
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
    }

    protected void bluetoothSetStatus() { // Sets the status of the bluetooth connection
        if (bluetoothAdapter == null)
            Toast.makeText(getApplicationContext(), "Bluetooth is not available!", Toast.LENGTH_LONG);
        else
            Toast.makeText(getApplicationContext(), "Bluetooth is available!", Toast.LENGTH_LONG);

        if (bluetoothAdapter.isEnabled())
            bluetoothStatusTextView.setText("Bluetooth is on!");
        else
            bluetoothStatusTextView.setText("Bluetooth is off!");
    }

}
