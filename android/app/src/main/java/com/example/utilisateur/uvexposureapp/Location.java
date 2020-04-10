package com.example.utilisateur.uvexposureapp;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.concurrent.ExecutionException;

public class Location extends AppCompatActivity  {

    private static final int REQUEST_LOCATION=1;

    Button getLocationBtn;
    TextView showLocationTxt;

    LocationManager locationManager;
    String latitude, longitude;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_location);

        ///add permissions //

        ActivityCompat.requestPermissions(this,new String[]
                {Manifest.permission.ACCESS_FINE_LOCATION},REQUEST_LOCATION);

        showLocationTxt=findViewById(R.id.show_location);
        getLocationBtn=findViewById(R.id.getLocation);

        getLocationBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

                //Check if GPS or on or not
                if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)){
                    /// write function to enable GPS
                    OnGPS();
                }

                else {
                    //gps is already on
                    getLocation();

                    // Make http call to Blipstar API to fetch nearest store that has sunscreen (Downtown MTL only for now)
                    getNearestStores();
                }
            }
        });

    }
    private void getLocation(){

        //check permission again
        if (ActivityCompat.checkSelfPermission(Location.this,Manifest.permission.ACCESS_FINE_LOCATION)!= PackageManager.PERMISSION_GRANTED &&ActivityCompat.checkSelfPermission(Location.this,Manifest.permission.ACCESS_COARSE_LOCATION)!=PackageManager.PERMISSION_GRANTED)

        {
            ActivityCompat.requestPermissions(this,new String[]
                    {Manifest.permission.ACCESS_FINE_LOCATION},REQUEST_LOCATION);
        }

        else{
            android.location.Location LocationGps=locationManager.getLastKnownLocation(locationManager.GPS_PROVIDER);
            android.location.Location LocationNetwork=locationManager.getLastKnownLocation(locationManager.NETWORK_PROVIDER);
            android.location.Location LocationPassive=locationManager.getLastKnownLocation(locationManager.PASSIVE_PROVIDER);

            if(LocationGps!=null){

                double lat=LocationGps.getLatitude();
                double longi=LocationGps.getLongitude();
                latitude= String.valueOf(lat);
                longitude=String.valueOf(longi);

                showLocationTxt.setText("Your location"+"\n"+"Latitude="+latitude+"\n"+"Longitude="+longitude);
            }
            else if(LocationNetwork!=null){

                double lat=LocationNetwork.getLatitude();
                double longi=LocationNetwork.getLongitude();
                latitude= String.valueOf(lat);
                longitude=String.valueOf(longi);

                showLocationTxt.setText("Your location"+"\n"+"Latitude="+latitude+"\n"+"Longitude="+longitude);
            }
            else if(LocationPassive!=null){

                double lat=LocationPassive.getLatitude();
                double longi=LocationPassive.getLongitude();
                latitude= String.valueOf(lat);
                longitude=String.valueOf(longi);

                showLocationTxt.setText("Your location"+"\n"+"Latitude="+latitude+"\n"+"Longitude="+longitude);
            }
            else{

                Toast.makeText(this,"Can't get your location",Toast.LENGTH_SHORT).show();
            }
        }
    }

    public void getNearestStores() {
        StoreLocator storeLocator = new StoreLocator();
        try {
            String json = storeLocator.execute(latitude, longitude).get();
            if (!json.isEmpty()) {
                JSONArray jsonArray = new JSONArray(json);
                JSONObject store1 = jsonArray.getJSONObject(0);
                String name = store1.getString("n");
                String address = store1.getString("ad");
                String distanceFromYou = store1.getString("dist");
                String lat = store1.getString("lat");
                String lng = store1.getString("lng");
                Log.d("Location:", name + " " + address);

            }
        } catch (InterruptedException | ExecutionException | JSONException exception) {
            exception.printStackTrace();
        }
    }

    private void OnGPS(){

        final AlertDialog.Builder builder= new AlertDialog.Builder(this);
        builder.setMessage("Enable GPS").setCancelable(false).setPositiveButton("YES", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        }).setNegativeButton("NO", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        final AlertDialog alertDialog=builder.create();
        alertDialog.show();
    }
}
