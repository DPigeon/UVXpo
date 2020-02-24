package com.example.utilisateur.uvexposureapp;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.AsyncTask;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.util.Set;
import java.util.UUID;

/*
 * An asynchronous task that connects the Bluetooth client (phone) to the Android Bluetooth server
 * It uses its device address along with its UUID to instantiate a proper connection
 */

public class BluetoothAsyncTask extends AsyncTask<String, Void, String> {
    private final String DEVICE_ADDRESS = ""; // To be determined with Arduino
    private final UUID BT_UUID = UUID.fromString("aa4e5899-f305-44f0-a7a8-d45061c9659b"); // Randomly generated
    private String TAG = "BluetoothService";
    private BluetoothAdapter bluetoothAdapter;
    private BluetoothDevice bluetoothDevice;
    private BluetoothSocket socket;
    protected InputStream inputStream;

    @Override
    public void onPreExecute() {
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        Log.d(TAG, "Connecting...");
    }

    @Override
    protected String doInBackground(String... params) {
        bluetoothAdapter.cancelDiscovery(); // To have a better performance
        Set<BluetoothDevice> devices;

        try {
            devices = bluetoothAdapter.getBondedDevices(); // We get the devices
            for (BluetoothDevice device : devices) {
                if(device.getAddress().equals(DEVICE_ADDRESS))  { // We find the Arduino device server
                    bluetoothDevice = device;
                    break;
                }
            }
            socket = bluetoothDevice.createRfcommSocketToServiceRecord(BT_UUID); // Get a socket instance with proper UUID
            socket.connect(); // Connect
            inputStream = socket.getInputStream(); // We get the data
            return "Success";
        } catch (IOException exception) {
            exception.printStackTrace();
            return "Failed";
        }
    }

    @Override
    public void onPostExecute(String result) {
        if (result == "Success")
            Log.d(TAG, "Connected & asynchronous task done!");
        else
            Log.d(TAG, "Failed connection...");
    }
}
