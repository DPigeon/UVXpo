package com.example.utilisateur.uvexposureapp;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
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
    private final String DEVICE_ADDRESS = "F8:59:71:70:5D:5E"; // To be determined with Arduino
    private final UUID BT_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    private String TAG = "BluetoothService";
    private BluetoothAdapter bluetoothAdapter;
    private BluetoothDevice bluetoothDevice;
    private BluetoothSocket socket;
    private InputStream inputStream;
    private Handler handler;
    private byte buffer[];

    private interface MessageConstants {
        public static final int MESSAGE_READ = 0;
        public static final int MESSAGE_WRITE = 1;
        public static final int MESSAGE_TOAST = 2;
    }

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
            Log.d(TAG, "Connected to the Bluetooth Device!");
            connectedDevice((BluetoothSocket)result.get(1));
        }
        else if (result.get(0) == "Failed")
            Log.d(TAG, "Failed connection...");
    }

    public void connectedDevice(BluetoothSocket socket) { ;
            InputStream tempIn = null;
            try {
                if (foundDevice)
                    tempIn = socket.getInputStream();
            } catch (IOException e) {
                Log.e(TAG, "Error occurred when creating input stream", e);
            }

            inputStream = tempIn;
            getData(inputStream);
    }

    public void getData(InputStream inputStream) {
        Log.d(TAG, "Listening to data...");
        buffer = new byte[1024];
        int numBytes; // bytes returned from read()

        if (foundDevice) {
            while (true) {
                try {
                    // Read from the InputStream
                    numBytes = inputStream.read(buffer);
                    String incomingMessage = new String(buffer, 0, numBytes);
                    Log.d(TAG, "data: " + incomingMessage);
                    // Send the obtained bytes to the UI activity.
                    Message readMessage = handler.obtainMessage(MessageConstants.MESSAGE_READ, numBytes, -1, buffer);
                    readMessage.sendToTarget();
                } catch (IOException exception) {
                    Log.d(TAG, "Input stream was disconnected!", exception);
                    break;
                }
            }
        }
    }
}
