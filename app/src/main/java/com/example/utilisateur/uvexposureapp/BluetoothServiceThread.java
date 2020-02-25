package com.example.utilisateur.uvexposureapp;

import android.bluetooth.BluetoothSocket;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/* https://developer.android.com/guide/topics/connectivity/bluetooth
 * A thread used to listen to the incoming stream input from bluetooth
 */

public class BluetoothServiceThread extends Thread {
    private static final String TAG = "BluetoothService";
    private Handler handler; // handler that gets info from Bluetooth service
    private BluetoothSocket socket;
    private InputStream inStream;
    private OutputStream outStream;
    private byte[] buffer; // To store the stream

    private interface MessageConstants {
        public static final int MESSAGE_READ = 0;
        public static final int MESSAGE_WRITE = 1;
        public static final int MESSAGE_TOAST = 2;
    }

    public BluetoothServiceThread(BluetoothSocket socket) {
        socket = socket;
        InputStream tempIn = null;
        OutputStream tempOut = null;

        // Get the incoming stream
        try {
            tempIn = socket.getInputStream();
        } catch (IOException e) {
            Log.e(TAG, "Error occurred when creating input stream!", e);
        }

        try {
            tempOut = socket.getOutputStream();
        } catch (IOException e) {
            Log.e(TAG, "Error occurred when creating input stream!", e);
        }

        inStream = tempIn;
        outStream = tempOut;
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
                Message readMessage = handler.obtainMessage(MessageConstants.MESSAGE_READ, bytes, -1, buffer);
                readMessage.sendToTarget();
            } catch (IOException e) {
                Log.d(TAG, "Input stream was disconnected!", e);
                break;
            }
        }
    }

    // Used to send data from phone to microcontroller (may be needed for integration checks)
    public void write(byte[] bytes) {
        buffer = new byte[1024];
        try {
            outStream.write(bytes);

            // Share the sent message with the UI activity
            Message writtenMessage = handler.obtainMessage(MessageConstants.MESSAGE_WRITE, -1, -1);
            writtenMessage.sendToTarget();
        } catch (IOException e) {
            Log.e(TAG, "Error occurred when sending data!", e);

            // Send a failure message back to the activity
            Message writeErrorMsg = handler.obtainMessage(MessageConstants.MESSAGE_TOAST);
            Bundle bundle = new Bundle();
            bundle.putString("Toast", "Couldn't send data to the other device!");
            writeErrorMsg.setData(bundle);
            handler.sendMessage(writeErrorMsg);
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
