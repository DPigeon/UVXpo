package com.example.utilisateur.uvexposureapp;

import androidx.appcompat.app.AppCompatActivity;

import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {
    TextView testDataTextView;
    Button bluetoothActivityButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setupUI();
        fetchData();
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

    void goToActivity(Class page) { // Function that goes from the main activity to another one
        Intent intent = new Intent(MainActivity.this, page); // from the main activity to the profile class
        startActivity(intent);
    }

    protected void fetchData() {
        if (BluetoothAdapter.getDefaultAdapter() == null) {
            Log.i("BT", "Bluetooth not supported on Virtual Devices! Use a real device.");
            finish(); // Allowing to skip the exception
        } else {
            BluetoothAsyncTask bluetoothAsyncTask = new BluetoothAsyncTask();
            bluetoothAsyncTask.execute();

            //testDataTextView.setText(bluetoothAsyncTask.getData());
        }
    }
}
