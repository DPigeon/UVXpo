package com.example.utilisateur.uvexposureapp;

import android.content.Intent;
import android.graphics.Point;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

public class StoreLocatorFragment extends DialogFragment {
    Button cancelButton;
    ListView storeListView;
    protected ArrayAdapter adapter;
    List<String> stores;
    List<StoreCoordinates> coordinates;
    String currentLatitude = "";
    String currentLongtitude = "";
    String latFound = "";
    String longFound = "";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_store_locator, container, false);

        Bundle coordinates = getArguments();
        currentLatitude = coordinates.getString("lat");
        currentLongtitude = coordinates.getString("long");

        setupUI(view);
        getNearestStores(currentLatitude, currentLongtitude);

        return view;
    }

    protected void setupUI(View view) {
        stores = new ArrayList<String>();
        coordinates = new ArrayList<StoreCoordinates>();
        storeListView = view.findViewById(R.id.storesListView);

        adapter = new ArrayAdapter<String>(view.getContext(), android.R.layout.simple_list_item_1, stores);
        storeListView.setAdapter(adapter);

        storeListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) { // As soon as we click an item in the course view list, we want google maps to open to that store
                Double coordinateLat = coordinates.get(position).getLatitude();
                Double coordinateLong = coordinates.get(position).getLongitude();

                String uri = "http://maps.google.com/maps?saddr=" + currentLatitude + "," + currentLongtitude + "&daddr=" + coordinateLat + "," + coordinateLong;
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
                startActivity(intent);
            }
        });
        cancelButton = view.findViewById(R.id.locatorStoreCancelButton);
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getDialog().dismiss();
            }
        });
    }

    public void getNearestStores(String latitude, String longitude) {
        StoreLocator storeLocator = new StoreLocator();
        try {
            String json = storeLocator.execute(latitude, longitude).get();
            if (!json.isEmpty()) {
                JSONArray jsonArray = new JSONArray(json);
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject store = jsonArray.getJSONObject(i);
                    String name = store.getString("n");
                    String address = store.getString("ad");
                    String distanceFromYou = store.getString("dist");
                    latFound = store.getString("lat");
                    longFound = store.getString("lng");

                    String storeMessage = name + "\n" + "(" + address + ")" + "\n[Distance]: " + distanceFromYou + " km from you\n";
                    coordinates.add(new StoreCoordinates(Double.valueOf(latFound), Double.valueOf(longFound)));
                    stores.add(storeMessage);
                    adapter.notifyDataSetChanged();
                }
            }
        } catch (InterruptedException | ExecutionException | JSONException exception) {
            exception.printStackTrace();
        }
    }

    @Override
    public void onResume() {
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
}
