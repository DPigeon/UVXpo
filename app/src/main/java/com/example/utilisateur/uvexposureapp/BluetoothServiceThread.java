package com.example.utilisateur.uvexposureapp;

import android.bluetooth.BluetoothSocket;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;

/* https://developer.android.com/guide/topics/connectivity/bluetooth
 * A thread used to listen to the incoming stream input from bluetooth
 */

public class BluetoothServiceThread extends Thread {
    private static final String TAG = "BluetoothService";
    private Handler handler; // handler that gets info from Bluetooth service
    private BluetoothSocket socket;
    private InputStream inStream;
    private byte[] buffer; // To store the stream

    private interface MessageConstants {
        public static final int MESSAGE_READ = 0;
        public static final int MESSAGE_WRITE = 1;
        public static final int MESSAGE_TOAST = 2;
    }

    public BluetoothServiceThread(BluetoothSocket socket) {
        socket = socket;
        InputStream tempIn = null;

        // Get the incoming stream
        try {
            tempIn = socket.getInputStream();
        } catch (IOException e) {
            Log.e(TAG, "Error occurred when creating input stream!", e);
        }

        inStream = tempIn;
    }

    public void run() {
        Log.d(TAG, "Listening to data...");
        buffer = new byte[1024];
        int bytes; // bytes returned from read()

        // Listens to data and brings them to the UI Thread
        while (true) {
            try {
                // Read from the InputStream.
                bytes = inStream.read(buffer);
                // Send the obtained bytes to the UI activity.
                Message readMsg = handler.obtainMessage(MessageConstants.MESSAGE_READ, bytes, -1, buffer);
                readMsg.sendToTarget();
            } catch (IOException e) {
                Log.d(TAG, "Input stream was disconnected!", e);
                break;
            }
        }
    }

    // Call this method from the main activity to shut down the connection
    public void cancel() {
        try {
            socket.close();
        } catch (IOException e) {
            Log.e(TAG, "Could not close the connect socket!", e);
        }
    }
}
