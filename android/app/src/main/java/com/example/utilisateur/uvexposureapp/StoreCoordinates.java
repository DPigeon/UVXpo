package com.example.utilisateur.uvexposureapp;

public class StoreCoordinates {
    public double latitude = 0;
    public double longitude = 0;

    public StoreCoordinates(double lat, double lon) {
        latitude = lat;
        longitude = lon;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }
}
