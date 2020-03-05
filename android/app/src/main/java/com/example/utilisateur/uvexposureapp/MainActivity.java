package com.example.utilisateur.uvexposureapp;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.TargetApi;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

/*
 * The MainActivity where the bluetooth connection is made and the data is fetched.
 */


import java.util.concurrent.ExecutionException;

public class MainActivity extends AppCompatActivity {
    Handler incomingHandler;
    static TextView testDataTextView;
    protected Button weatherButton, bluetoothActivityButton, graphButton;

    @TargetApi(Build.VERSION_CODES.CUPCAKE)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        incomingHandler = new Handler(new Handler.Callback() {
            @Override
            public boolean handleMessage(Message message) {
                testDataTextView.setText(String.valueOf(message));
                return true;
            }
        });
        connectAndListen();
    }


    protected void setupUI() {
        bluetoothActivityButton = findViewById(R.id.bluetoothButton);
        testDataTextView = findViewById(R.id.testDataTextView);
        graphButton = findViewById(R.id.graphButton);
        weatherButton = findViewById(R.id.weather);
        bluetoothActivityButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                goToActivity(BluetoothActivity.class);
            }
        });
        weatherButton.setText("Get Current Weather Data");
        weatherButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                goToActivity(WeatherActivity.class);
            }
        });
        graphButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                goToActivity(GraphActivity.class);
            }
        });
    }

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
                String status = (String)bluetoothAsyncTask.get().get(0); // Get the status from the async task
                BluetoothSocket socket = (BluetoothSocket)bluetoothAsyncTask.get().get(1); // Get the socket from the async task
                BluetoothServiceThread service;
                if (status == "Success") { // If connected to the device
                    service = new BluetoothServiceThread(socket);
                    try {
                        service.start(); // Start the service
                    } catch (Exception exception) {
                        Log.e("BluetoothThread: ", "Error ", exception);
                        service.cancel(); // Cancel the service if exception
                    }
                }
            } catch (InterruptedException | ExecutionException exception) {
                Log.e("BluetoothThread: ", "Error ", exception);
                bluetoothAsyncTask.cancel(true); // Cancel the connection if exception
            }
        }
    }
}
