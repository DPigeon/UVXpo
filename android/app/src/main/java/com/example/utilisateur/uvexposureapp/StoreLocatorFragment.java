package com.example.utilisateur.uvexposureapp;

import android.Manifest;
import android.graphics.Point;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.concurrent.ExecutionException;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

public class StoreLocatorFragment extends DialogFragment implements OnMapReadyCallback {
    Button cancelButton;
    MapView mapView;
    GoogleMap map;
    String currentLatitude = "";
    String currentLongtitude = "";
    String latFound = "";
    String longFound = "";
    Boolean responseDone = false;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_store_locator, container, false);

        Bundle coordinates = getArguments();
        currentLatitude = coordinates.getString("latitude");
        currentLongtitude = coordinates.getString("longtitude");
        getNearestStores(currentLatitude, currentLongtitude);
        Log.d("hi:", currentLatitude);

        cancelButton = view.findViewById(R.id.locatorStoreCancelButton);
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getDialog().dismiss();
            }
        });
        mapView = view.findViewById(R.id.mapView);
        mapView.onCreate(savedInstanceState);

        return view;
    }

    public void getNearestStores(String latitude, String longitude) {
        StoreLocator storeLocator = new StoreLocator();
        storeLocator.parseComments(this.getActivity());
        try {
            String json = storeLocator.execute(latitude, longitude).get();
            if (!json.isEmpty()) {
                JSONArray jsonArray = new JSONArray(json);
                JSONObject store1 = jsonArray.getJSONObject(0);
                String name = store1.getString("n");
                String address = store1.getString("ad");
                String distanceFromYou = store1.getString("dist");
                latFound = store1.getString("lat");
                longFound = store1.getString("lng");
            }
        } catch (InterruptedException | ExecutionException | JSONException exception) {
            exception.printStackTrace();
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        map = googleMap;
        map.getUiSettings().setMyLocationButtonEnabled(false);
        map.setMyLocationEnabled(true);
       /*
       //in old Api Needs to call MapsInitializer before doing any CameraUpdateFactory call
        try {
            MapsInitializer.initialize(this.getActivity());
        } catch (GooglePlayServicesNotAvailableException e) {
            e.printStackTrace();
        }
       */

        // Updates the location and zoom of the MapView
        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(new LatLng(Integer.valueOf(currentLatitude), Integer.valueOf(currentLongtitude)), 10);
        map.animateCamera(cameraUpdate);
        map.moveCamera(CameraUpdateFactory.newLatLng(new LatLng(Integer.valueOf(latFound), Integer.valueOf(longFound))));

    }

    @Override
    public void onResume() {
        mapView.onResume();
        // Store access variables for window and blank point
        Window window = getDialog().getWindow();
        Point size = new Point();

        // Store dimensions of the screen in `size`
        Display display = window.getWindowManager().getDefaultDisplay();
        display.getSize(size);

        //resize
        window.setLayout((int) (size.x * 0.98), (int) (size.y * 0.9));
        window.setGravity(Gravity.CENTER);
        super.onResume();

    }


    @Override
    public void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }
}
