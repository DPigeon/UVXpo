package com.example.utilisateur.uvexposureapp;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.LegendRenderer;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

/*
* An activity that displays the UV exposure graph depending on the time chosen.
* Data from the database will have to be fetched in order to view them.
*/

public class GraphActivity extends AppCompatActivity {
    GraphView graph;
    LineGraphSeries<DataPoint> series;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_graph);

        graph = findViewById(R.id.graph);
        series = new LineGraphSeries<DataPoint>();
        setupGraph();
    }

    protected void setupGraph() {
        double x, y;
        x = -5.0;

        for (int i = 0; i < 500; i++) {
            x = x + 0.04;
            y = Math.tanh(x); // A simple example
            series.appendData(new DataPoint(x, y), true, 500);
        }
        graph.addSeries(series);

        // Legend
        series.setTitle("UV Exposure Against Time");
        graph.getLegendRenderer().setVisible(true);
        graph.getLegendRenderer().setAlign(LegendRenderer.LegendAlign.TOP);
    }

    protected void fetchData() {
        // Will be used to fetch some UV exposure points <time, UV>
    }
}
