package com.example.utilisateur.uvexposureapp;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.AsyncTask;
import android.os.Handler;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Set;
import java.util.UUID;

/*
 * An asynchronous task that connects the Bluetooth client (phone) to the Android Bluetooth server
 * It uses its device address along with its UUID to instantiate a proper connection
 */

public class BluetoothAsyncTask extends AsyncTask<Void, Void, ArrayList> {
    boolean foundDevice = false;
    private final String DEVICE_ADDRESS = "24:0A:C4:05:C6:8A";
    private final UUID BT_UUID = UUID.fromString("b923eeab-9473-4b86-8607-5068911b18fe");
    //    private final UUID BT_UUID = UUID.fromString("aba24047-b36f-4646-92ce-3d5c0c75bd20");
    private String TAG = "BluetoothService";
    private BluetoothAdapter bluetoothAdapter;
    private BluetoothDevice bluetoothDevice;
    private BluetoothSocket socket;
    private InputStream inputStream;
    private Handler handler;
    private byte buffer[];

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
                    foundDevice = true;
                    break;
                }
            }
            // We get the temporary socket and run the device by instantiating a socket with port 1
            if (foundDevice)
                tempSocket = (BluetoothSocket) bluetoothDevice.getClass().getMethod("createRfcommSocket", new Class[] {int.class}).invoke(bluetoothDevice,1);

        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException exception) {
            Log.e(TAG, "Socket's onPreExecute() method failed!", exception);
        }
        socket = tempSocket;
    }

    @Override
    protected ArrayList doInBackground(Void... args) {
        bluetoothAdapter.cancelDiscovery(); // To have a better performance
        InputStream tempIn = null;
        String stringToReturn = "";

        try {
            if (foundDevice) {
                socket.connect(); // Connect
                tempIn = socket.getInputStream(); // We get the data
                stringToReturn = "Success";
                Log.d(TAG, "Connected to the Bluetooth device!");
            } else
                stringToReturn = "Failed";
        } catch (IOException exception) {
            Log.e(TAG, "Error: ",exception);
            try {
                socket.close();
            } catch (IOException closeException) {
                Log.e(TAG, "Could not close the client socket: ", closeException);
            }
            stringToReturn = "Failed";
        }
        inputStream = tempIn;
        ArrayList arrayToReturn = new ArrayList<>(); // Returning an array with the status and socket
        arrayToReturn.add(stringToReturn);
        arrayToReturn.add(socket);
        return arrayToReturn;
    }

    @Override
    public void onPostExecute(ArrayList result) {
        if (result.get(0) == "Success") {
            Log.d(TAG, "Asynchronous task completed!");
        }
        else if (result.get(0) == "Failed")
            Log.d(TAG, "Failed to connect! Check if you are connecting to the device address " + DEVICE_ADDRESS + ".");
    }
}
