package com.example.utilisateur.uvexposureapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.GridLabelRenderer;
import com.jjoe64.graphview.LegendRenderer;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

/*
* An activity that displays the UV exposure graph depending on the time chosen.
* Data from the database will have to be fetched in order to view them.
*/

public class GraphActivity extends AppCompatActivity {
    BroadcastReceiver mBroadcastReceiver;
    GraphView graph;
    DataPoint[] liveValues;
    int counter = 0;
    int maxLivePoints = 1000;
    LineGraphSeries<DataPoint> series;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_graph);

        graph = findViewById(R.id.graph);
        liveValues = new DataPoint[maxLivePoints];
        series = new LineGraphSeries<DataPoint>();
        setupGraph();
    }

    /* Used to get the broadcasted message from main activity of bluetooth data */
    @Override
    protected void onResume() {
        super.onResume();
        mBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                switch (action) { // Several actions may be added here later for different activities
                    case "graph-activity":
                        String data = intent.getStringExtra("uv-live-data");
                        double preciseData = Double.parseDouble(data);
                        if (preciseData > 0) // Rejecting all 0's and negative values
                            buildLiveExposureGraph(convertVoltageToIntensity(preciseData));
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

    protected void setupGraph() {
        graph.addSeries(series);

        // Legend
        series.setTitle("Live UV Exposure");
        series.setThickness(5);
        graph.getLegendRenderer().setVisible(true);
        graph.getLegendRenderer().setAlign(LegendRenderer.LegendAlign.TOP);
        // Axis
        GridLabelRenderer gridLabel = graph.getGridLabelRenderer();
        gridLabel.setHorizontalAxisTitle("Time");
        gridLabel.setVerticalAxisTitle("Intensity (mW/cm^2)");
    }

    protected void buildLiveExposureGraph(String data) {
        double x = counter / 2; // Should be divided by 10 for real second values but we get lots of fluctuation
        double y = Double.parseDouble(data);
        DataPoint point = new DataPoint(x, y);
        liveValues[counter] = point;

        series.appendData(new DataPoint(liveValues[counter].getX(), liveValues[counter].getY()), false, maxLivePoints); // Send new data to the graph
        counter = counter + 1; // Increment by 1
    }

    protected String convertVoltageToIntensity(double data) {
        double voltage = (data * 3.3) / 1023; // Convert analog values to voltage out of 3.3V (should be done on arduino side later)
        double intensity = mapIntensity(voltage, 0.0, 2.8, 0.0, 15.0);
        String stringIntensity = Double.toString(intensity);

        return stringIntensity;
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
