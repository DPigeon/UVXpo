package com.example.utilisateur.uvexposureapp;

import androidx.appcompat.app.AppCompatActivity;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

public class MainActivity extends AppCompatActivity {
    static TextView testDataTextView;
    Button bluetoothActivityButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setupUI();
        connectAndListen();
    }

    protected void setupUI() {
        bluetoothActivityButton = findViewById(R.id.bluetoothButton);
        testDataTextView = findViewById(R.id.testDataTextView);
        bluetoothActivityButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                goToActivity(BluetoothActivity.class);
            }
        });
    }

    /*static Handler handler = new Handler() {
        @Override
        public void handleMessage(Message message) {
            testDataTextView.setText(String.valueOf(message.what));
        }
    };*/

    void goToActivity(Class page) { // Function that goes from the main activity to another one
        Intent intent = new Intent(MainActivity.this, page); // from the main activity to the profile class
        startActivity(intent);
    }

    protected void connectAndListen() { // Connects to the Bluetooth device & starts the service to listen to inputs
        if (BluetoothAdapter.getDefaultAdapter() == null) {
            Log.i("BT", "Bluetooth not supported on Virtual Devices! Use a real device.");
            finish(); // Allowing to skip the exception
        } else {
            BluetoothAsyncTask bluetoothAsyncTask = new BluetoothAsyncTask();
            bluetoothAsyncTask.execute();
            try {
                BluetoothSocket socket = (BluetoothSocket)bluetoothAsyncTask.get().get(1); // Get the socket from the async task
                BluetoothServiceThread service = new BluetoothServiceThread(socket);
                try {
                    service.start(); // Start the service
                } catch (Exception exception) {
                    Log.e("BluetoothThread: ", "Error ", exception);
                    service.cancel(); // Cancel the service if exception
                }
            } catch (InterruptedException | ExecutionException exception) {
                Log.e("BluetoothThread: ", "Error ", exception);
                bluetoothAsyncTask.cancel(true); // Cancel the connection if exception
            }
        }
    }
}
