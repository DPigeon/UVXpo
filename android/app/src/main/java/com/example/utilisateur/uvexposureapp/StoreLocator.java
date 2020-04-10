package com.example.utilisateur.uvexposureapp;

import android.app.Activity;
import android.os.AsyncTask;

import com.google.android.gms.maps.SupportMapFragment;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import androidx.appcompat.app.AppCompatActivity;

public class StoreLocator extends AsyncTask<String, Void, String> {
    AppCompatActivity activity;

    public void parseComments(AppCompatActivity activity){
        this.activity = activity;
    }

    @Override
    protected String doInBackground(String... coordinates) {
        String TAG = "StoreLocator";
        String lat = coordinates[0];
        String lng = coordinates[1];
        String BLIPSTAR_ACCOUNT_ID = "5093053"; // Blipstar API (good for 15 days trial)
        String results = "2"; // Lets limit ourselves with 2 for now
        String radius = "1"; // kilometers
        String storesUrl = "https://viewer.blipstar.com/api/?uid=" + BLIPSTAR_ACCOUNT_ID + "&lat=" + lat + "&lng=" + lng + "&results=" + results + "&radius=" + radius;

        try {
            URL url = new URL(storesUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.connect();

            InputStream inputStream = connection.getInputStream();
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
            int data = inputStreamReader.read();
            String json = "";
            char c;
            while (data != -1){
                c = (char) data;
                json = json + c;
                data = inputStreamReader.read();
            }
            return json;
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    protected void onPostExecute(String result) {
       /* SupportMapFragment mapFragment = (SupportMapFragment)activity.getSupportFragmentManager().find;
        mapFragment.getMapAsync(this);*/
    }

}
