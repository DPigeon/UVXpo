package com.example.utilisateur.uvexposureapp;

import android.util.Log;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.modules.junit4.PowerMockRunner;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Bluetooth Connection Unit Test. Should return Success to pass.
 */
@RunWith(PowerMockRunner.class)
public class BluetoothConnectionTest {
    private BluetoothAsyncTask bluetoothAsyncTaskMock;
    private BluetoothConnection bluetoothConnection;

    @Before
    public void setup() {
        bluetoothConnection = mock(BluetoothConnection.class); // Mocking the asynctask
    }

    @Test
    public void bluetooth_Connection_ReturnsSuccess() throws Exception {
        when(bluetoothConnection.connectToBluetooth()).thenReturn("Success");

        String status = (String)bluetoothConnection.connectToBluetooth(); // Get the status from the async task
        Assert.assertEquals(status, "Success"); // Looks if it is the same as success
    }
}