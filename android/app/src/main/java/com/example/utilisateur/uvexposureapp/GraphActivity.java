package com.example.utilisateur.uvexposureapp;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.app.DatePickerDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;
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

import java.time.LocalDateTime;
import java.util.Calendar;

/*
* An activity that displays the UV exposure graph depending on the time chosen.
* Data from the database will have to be fetched in order to view them.
*/

public class GraphActivity extends AppCompatActivity {
    BroadcastReceiver mBroadcastReceiver;
    GraphView graph;
    TextView uvIndexTextView;
    DatePickerDialog datePicker;
    EditText datePick;
    DataPoint[] liveValues;
    int counter = 0;
    int maxLivePoints = 1000;
    LineGraphSeries<DataPoint> series;
    FirebaseFirestore fireStore;

    String lastDate = ""; // To keep track of the last date entered
    Boolean toggleLivePastData = false; // If false: live data, if true: past data

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_graph);

        graph = findViewById(R.id.graph);
        uvIndexTextView = findViewById(R.id.uvIndexTextView);
        datePick = findViewById(R.id.datePicker);
        datePick.setInputType(InputType.TYPE_NULL);
        datePick.setEnabled(false);
        liveValues = new DataPoint[maxLivePoints];
        series = new LineGraphSeries<DataPoint>();
        setupGraph();
        fireStore = FirebaseFirestore.getInstance();

        setDate();
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
                        if (preciseData > 0 && !toggleLivePastData) { // Rejecting all 0's and negative values & enable if toggle is on live data
                            buildLiveExposureGraph(convertVoltageToIntensity(preciseData));
                            convertVoltageToUvIndex(preciseData);
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) { // Creates the three dot action menu
        getMenuInflater().inflate(R.menu.graph_options, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int menuId = item.getItemId();
        if(menuId == R.id.toggleLivePastExposure) { // If we click on the ... button
            toggleLivePastData = !toggleLivePastData; // Toggle
            if (toggleLivePastData) // past data
                switchMode(false, "Past Data");
            else // live data
                switchMode(true, "Live Exposure");
        }
        return super.onOptionsItemSelected(item);
    }

    protected void switchMode(Boolean status, String title) {
        series.resetData(new DataPoint[] {}); // Reset previous series
        counter = 0; // Reset the time counter
        series.setTitle(title);
        uvIndexTextView.setEnabled(status);
        datePick.setEnabled(!status);
    }

    protected void setDate() { // Used to date the date with calendar
        datePick.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Calendar calendar = Calendar.getInstance();
                int day = calendar.get(Calendar.DAY_OF_MONTH);
                int month = calendar.get(Calendar.MONTH);
                int year = calendar.get(Calendar.YEAR);
                // date picker dialog
                datePicker = new DatePickerDialog(GraphActivity.this, new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                        int monthAdjusted = monthOfYear + 1;
                        String spinDate = year + "-" + monthAdjusted + "-" + dayOfMonth;
                        datePick.setText(spinDate);
                        fetchUVDataByDate(spinDate); // send the format to the database (year - month - day)
                    }
                }, year, month, day);
                datePicker.show();
            }
        });
    }

    protected void setupGraph() {
        graph.addSeries(series);

        // Legend
        series.setTitle("Live Exposure");
        series.setThickness(5);
        graph.getLegendRenderer().setVisible(true);
        graph.getLegendRenderer().setAlign(LegendRenderer.LegendAlign.TOP);
        // Axis
        GridLabelRenderer gridLabel = graph.getGridLabelRenderer();
        gridLabel.setHorizontalAxisTitle("Time"); // 5t is 5 times the actual time to reject fluctuations
        gridLabel.setVerticalAxisTitle("Intensity (mW/cm^2)");
    }

    public void fetchUVDataByDate(final String date) {
        // From sharedPrefs, get the username logged in
        String name = "Marc";

        /* Read user from database */
        if (!lastDate.equals(date) && toggleLivePastData) { // We check if the last date chosen isn't the same otherwise don't fetch new data
            series.resetData(new DataPoint[] {}); // Reset previous series
            CollectionReference users = fireStore.collection("user_info");
            users.whereEqualTo("username", name).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                @Override
                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot document_user : task.getResult()) {

                            /* Read all uv (time, uv) values of that user */
                            CollectionReference uvData = fireStore.collection("uv_data");
                            // We want to filter by userID and date chosen from uv data
                            uvData.whereEqualTo("uv_user_id", document_user.getId()).whereEqualTo("date", date).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                    DataPoint[] newPoints = new DataPoint[10000];
                                    int newCounter = 0;
                                    if (task.isSuccessful()) {
                                        for (QueryDocumentSnapshot document_uv_data : task.getResult()) { // Fetch every point and create new series
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
                        }
                    } else {
                        Log.d("DB:", "Error getting documents: ", task.getException());
                    }
                }
            });
            lastDate = date;
        }
    }

    protected void addDataToDatabase(final double dataX, final double dataY, final LocalDateTime date) { // Should store every 5 seconds ? otherwise we may have too many data in database
        // From sharedPrefs, get the username logged in
        String name = "Marc";

        CollectionReference users = fireStore.collection("user_info");
        users.whereEqualTo("username", name).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    for (QueryDocumentSnapshot document_user : task.getResult()) {

                        /* Add a uv (time, uv) value for that user */
                        CollectionReference uvData = fireStore.collection("uv_data");
                        //UV uv = new UV(document_user.getId(), dataX, dataY, date.toString());
                        //uvData.add(uv); // Add a new uv value
                    }
                } else {
                    Log.d("DB:", "Error getting documents: ", task.getException());
                }
            }
        });
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    protected void buildLiveExposureGraph(String data) {
        double x = counter / 2; // Should be divided by 10 for real second values but we get lots of fluctuation (5 times faster)
        double y = Double.parseDouble(data);
        DataPoint point = new DataPoint(x, y);
        liveValues[counter] = point;

        series.appendData(new DataPoint(liveValues[counter].getX() / 5, liveValues[counter].getY()), false, maxLivePoints); // Send new data to the graph with 5 times less in time to get real time
        addDataToDatabase(x / 5, y, LocalDateTime.now());
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

    //The Arduino Map function but for doubles for UV intensity
    //From: http://forum.arduino.cc/index.php?topic=3922.0
    double mapIntensity(double x, double in_min, double in_max, double out_min, double out_max) {
        return (x - in_min) * (out_max - out_min) / (in_max - in_min) + out_min;
    }

}
