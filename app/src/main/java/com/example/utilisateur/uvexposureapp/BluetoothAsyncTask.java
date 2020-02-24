package com.example.utilisateur.uvexposureapp;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.AsyncTask;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Set;
import java.util.UUID;

/*
 * An asynchronous task that connects the Bluetooth client (phone) to the Android Bluetooth server
 * It uses its device address along with its UUID to instantiate a proper connection
 */

public class BluetoothAsyncTask extends AsyncTask<Void, Void, String> {
    private final String DEVICE_ADDRESS = "F8:59:71:70:5D:5E"; // To be determined with Arduino
    private final UUID BT_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    private String TAG = "BluetoothService";
    private BluetoothAdapter bluetoothAdapter;
    private BluetoothDevice bluetoothDevice;
    private BluetoothSocket socket;
    protected InputStream inputStream;
    byte buffer[];

    @Override
    public void onPreExecute() {
        Set<BluetoothDevice> devices;
        BluetoothSocket tempSocket = null;
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        Log.d(TAG, "Connecting...");

        try {
            devices = bluetoothAdapter.getBondedDevices(); // We get the devices
            for (BluetoothDevice device : devices) {
                if(device.getAddress().equals(DEVICE_ADDRESS))  { // We find the Arduino device server
                    bluetoothDevice = device;
                    break;
                }
            }
            // We get the temporary socket and run the device by instantiating a socket with port 1
            tempSocket = (BluetoothSocket) bluetoothDevice.getClass().getMethod("createRfcommSocket", new Class[] {int.class}).invoke(bluetoothDevice,1);
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException exception) {
            Log.e(TAG, "Socket's onPreExecute() method failed!", exception);
        }
        socket = tempSocket;
    }

    @Override
    protected String doInBackground(Void... args) {
        bluetoothAdapter.cancelDiscovery(); // To have a better performance

        try {
            socket.connect(); // Connect
            inputStream = socket.getInputStream(); // We get the data
            return "Success";
        } catch (IOException exception) {
            Log.e(TAG, "Error: ",exception);
            try {
                socket.close();
            } catch (IOException closeException) {
                Log.e(TAG, "Could not close the client socket: ", closeException);
            }
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

    public String getData() {
        buffer = new byte[1024];
        String data = "";

        while(true) {
            try {
                int byteCount = inputStream.available();
                if(byteCount > 0) {
                    byte[] rawBytes = new byte[byteCount];
                    inputStream.read(rawBytes);
                    data = new String(rawBytes,"UTF-8");
                    return data;
                }
            }
            catch (IOException ex) {
            }
        }
    }
}
