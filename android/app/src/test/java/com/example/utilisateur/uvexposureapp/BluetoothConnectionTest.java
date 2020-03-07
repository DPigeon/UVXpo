package com.example.utilisateur.uvexposureapp;

import android.util.Log;

import org.junit.Test;

import java.util.concurrent.ExecutionException;

import static org.junit.Assert.*;

/**
 * Bluetooth Connection Unit Test. Should return Success to pass.
 */
public class BluetoothConnectionTest {
    @Test
    public void bluetooth_Connection_ReturnsSuccess() {
        BluetoothAsyncTask bluetoothAsyncTask = new BluetoothAsyncTask();
        bluetoothAsyncTask.execute();
        try {
            String status = (String)bluetoothAsyncTask.get().get(0); // Get the status from the async task
            assertEquals(status, "Success");
        } catch (InterruptedException | ExecutionException exception) {
            Log.e("BluetoothThread: ", "Error ", exception);
            bluetoothAsyncTask.cancel(true); // Cancel the connection if exception
        }
    }
}