package com.example.utilisateur.uvexposureapp;

import android.util.Log;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.concurrent.ExecutionException;

import static org.junit.Assert.*;

/**
 * Bluetooth Connection Unit Test. Should return Success to pass.
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest(BluetoothAsyncTask.class)
public class BluetoothConnectionTest {
    @Test
    public void bluetooth_Connection_ReturnsSuccess() {
        BluetoothAsyncTask bluetoothAsyncTask = PowerMockito.mock(BluetoothAsyncTask.class);
        PowerMockito.when(bluetoothAsyncTask.execute()).thenReturn(bluetoothAsyncTask);
        try {
            String status = (String)bluetoothAsyncTask.get().get(0); // Get the status from the async task
            Assert.assertEquals(status, "Success"); // Looks if it is the same as success
        } catch (InterruptedException | ExecutionException exception) {
            Log.e("BluetoothThread: ", "Error ", exception);
            bluetoothAsyncTask.cancel(true); // Cancel the connection if exception
        }
    }
}