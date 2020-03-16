package com.example.utilisateur.uvexposureapp;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.DatePicker;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.GridLabelRenderer;
import com.jjoe64.graphview.LegendRenderer;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import java.time.LocalDate;
import java.util.Calendar;

/*
* An activity that displays the UV exposure graph depending on the time chosen.
* Data from the database will have to be fetched in order to view them.
*/

public class GraphActivity extends AppCompatActivity {
    BroadcastReceiver mBroadcastReceiver;
    GraphView graph;
    TextView uvIndexTextView;
    DatePicker datePicker;
    DataPoint[] liveValues;
    int counter = 0;
    int maxLivePoints = 1000;
    LineGraphSeries<DataPoint> series;
    FirebaseFirestore fireStore;

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_graph);

        graph = findViewById(R.id.graph);
        uvIndexTextView = findViewById(R.id.uvIndexTextView);
        datePicker = findViewById(R.id.datePicker1);
        liveValues = new DataPoint[maxLivePoints];
        series = new LineGraphSeries<DataPoint>();
        setupGraph();
        fireStore = FirebaseFirestore.getInstance();

        setSpinner();
    }

    /* Used to get the broadcasted message from main activity of bluetooth data */
    @Override
    protected void onResume() {
        super.onResume();
        mBroadcastReceiver = new BroadcastReceiver() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                switch (action) { // Several actions may be added here later for different activities
                    case "graph-activity":
                        String data = intent.getStringExtra("uv-live-data");
                        double preciseData = Double.parseDouble(data);
                        if (preciseData > 0) { // Rejecting all 0's and negative values
                            buildLiveExposureGraph(convertVoltageToIntensity(preciseData));
                            convertVoltageToUvIndex(preciseData);
                            //addUvValue(preciseData, LocalDateTime.now());
                        }
                        break;
                }
            }
        };

        IntentFilter filter = new IntentFilter("graph-activity");
        registerReceiver(mBroadcastReceiver, filter);
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(mBroadcastReceiver); // Unregister once paused
    }

    public void fetchUVDataByDate(final double value, final String date) {
        // From sharedPrefs, get the username logged in
        String name = "Marc";

        /* Read user from database */
        CollectionReference users = fireStore.collection("user_info");
        users.whereEqualTo("username", name).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    for (QueryDocumentSnapshot document_user : task.getResult()) {

                        /* Read all uv (time, uv) values of that user */
                        CollectionReference uvData = fireStore.collection("uv_data");
                        uvData.whereEqualTo("uv_user_id", document_user.getId()).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                DataPoint[] newPoints = new DataPoint[10000];
                                int newCounter = 0;
                                if (task.isSuccessful()) {
                                    for (QueryDocumentSnapshot document_uv_data : task.getResult()) {
                                        double x = Double.parseDouble(document_uv_data.getData().get("uv_time").toString());
                                        double y = Double.parseDouble(document_uv_data.getData().get("uv_value").toString());
                                        DataPoint point = new DataPoint(x, y);
                                        newPoints[newCounter] = point;
                                        series.appendData(new DataPoint(point.getX(), point.getY()), false, document_uv_data.getData().size());
                                        newCounter = newCounter + 1;
                                    }
                                } else {
                                    Log.d("DB:", "Error getting documents: ", task.getException());
                                }
                            }
                        });
                        //UV uv = new UV(value, date);
                        //uvData.add()
                    }
                } else {
                    Log.d("DB:", "Error getting documents: ", task.getException());
                }
            }
        });
    }

    protected void setSpinner() {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        datePicker.init(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH), new DatePicker.OnDateChangedListener() {
            @Override
            public void onDateChanged(DatePicker datePicker, int year, int month, int dayOfMonth) {
                String spinDate = year + "-" + month + "-" + dayOfMonth;
                fetchUVDataByDate(20.0, spinDate); // year - month- day
            }
        });
    }

    protected void setupGraph() {
        graph.addSeries(series);

        // Legend
        series.setTitle("Live UV Exposure");
        series.setThickness(5);
        graph.getLegendRenderer().setVisible(true);
        graph.getLegendRenderer().setAlign(LegendRenderer.LegendAlign.TOP);
        // Axis
        GridLabelRenderer gridLabel = graph.getGridLabelRenderer();
        gridLabel.setHorizontalAxisTitle("Time"); // 5t is 5 times the actual time to reject fluctuations
        gridLabel.setVerticalAxisTitle("Intensity (mW/cm^2)");
    }

    protected void buildLiveExposureGraph(String data) {
        double x = counter / 2; // Should be divided by 10 for real second values but we get lots of fluctuation (5 times faster)
        double y = Double.parseDouble(data);
        DataPoint point = new DataPoint(x, y);
        liveValues[counter] = point;

        series.appendData(new DataPoint(liveValues[counter].getX() / 5, liveValues[counter].getY()), false, maxLivePoints); // Send new data to the graph with 5 times less in time to get real time
        counter = counter + 1; // Increment by 1
    }

    protected String convertVoltageToIntensity(double data) {
        double voltage = (data * 3.3) / 1023; // Convert analog values to voltage out of 3.3V (should be done on arduino side later)
        double intensity = mapIntensity(voltage, 0, 2.8, 0.0, 15.0); // Mapping from 0V at 0 mW/cm^2 and from 2.8V max to 15 mW/cm^2
        String stringIntensity = Double.toString(intensity);

        return stringIntensity;
    }

    protected void convertVoltageToUvIndex(double data) {
        String uvIndex = "0";
        double voltage = data * (3.3 / 1023) * 1000; // using 3.3 mV

        // From http://educ8s.tv/arduino-uv-meter-project/ with converting of 3.3V output
        if (voltage < 33)
            uvIndex = "0";
        else if (voltage > 33 && voltage <= 150)
            uvIndex = "1";
        else if (voltage > 150 && voltage <= 210)
            uvIndex = "2";
        else if (voltage > 210 && voltage <= 269)
            uvIndex = "3";
        else if (voltage > 269 && voltage <= 332)
            uvIndex = "4";
        else if (voltage > 332 && voltage <= 400)
            uvIndex = "5";
        else if (voltage > 400 && voltage <= 459)
            uvIndex = "6";
        else if (voltage > 459 && voltage <= 525)
            uvIndex = "7";
        else if (voltage > 525 && voltage <= 581)
            uvIndex = "8";
        else if (voltage > 581 && voltage <= 644)
            uvIndex = "9";
        else if (voltage > 644 && voltage <= 712)
            uvIndex = "10";
        else if (voltage > 712)
            uvIndex = "11";

        uvIndexTextView.setText("UV Index: " + uvIndex);
    }

    protected void fetchData() {
        // Will be used to fetch some UV exposure points <time, UV> from the database
    }

    //The Arduino Map function but for doubles for UV intensity
    //From: http://forum.arduino.cc/index.php?topic=3922.0
    double mapIntensity(double x, double in_min, double in_max, double out_min, double out_max) {
        return (x - in_min) * (out_max - out_min) / (in_max - in_min) + out_min;
    }

}
