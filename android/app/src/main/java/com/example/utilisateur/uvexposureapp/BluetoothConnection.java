package com.example.utilisateur.uvexposureapp;

import java.util.concurrent.ExecutionException;

public class BluetoothConnection {
    BluetoothAsyncTask asyncTask;

    public String connectToBluetooth() {
        String status = "";
        try {
           status = (String)asyncTask.execute().get().get(0);
        } catch (InterruptedException | ExecutionException exception) {

        }
        return status;
    }
}
