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

import org.w3c.dom.Text;

/*
* An activity that displays the UV exposure graph depending on the time chosen.
* Data from the database will have to be fetched in order to view them.
*/

public class GraphActivity extends AppCompatActivity {
    BroadcastReceiver mBroadcastReceiver;
    GraphView graph;
    TextView uvIndexTextView;
    DataPoint[] liveValues;
    int counter = 0;
    int maxLivePoints = 1000;
    LineGraphSeries<DataPoint> series;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_graph);

        graph = findViewById(R.id.graph);
        uvIndexTextView = findViewById(R.id.uvIndexTextView);
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
                        if (preciseData > 0) { // Rejecting all 0's and negative values
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
