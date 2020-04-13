package com.example.utilisateur.uvexposureapp;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.app.DatePickerDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
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
import com.jjoe64.graphview.DefaultLabelFormatter;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.GridLabelRenderer;
import com.jjoe64.graphview.LegendRenderer;
import com.jjoe64.graphview.helper.DateAsXAxisLabelFormatter;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/*
* An activity that displays the UV exposure graph depending on the time chosen.
* Data from the database will have to be fetched in order to view them.
*/

// TODO FIX: Delete last live exposure before making another one for today otherwise graphs overlaps

public class GraphActivity extends AppCompatActivity {
    protected SharedPreferencesHelper sharedPreferencesHelper;
    BroadcastReceiver mBroadcastReceiver;
    GraphView graph;
    TextView uvIndexTextView;
    DatePickerDialog datePicker;
    EditText datePick;
    DataPoint[] liveValues;
    String username;
    int counter = 0;
    int maxLivePoints = 1000;
    int databasePoints = 10000;
    LineGraphSeries<DataPoint> series;
    FirebaseFirestore fireStore;
    static final double ALPHA = 0.50; // If ALPHA = 0 or 1, no filter applies

    String lastDate = ""; // To keep track of the last date entered
    Boolean toggleLivePastData = false; // If false: live data, if true: past data
    Menu menu;
    SimpleDateFormat sdf = new SimpleDateFormat("mm:ss");

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
        sharedPreferencesHelper = new SharedPreferencesHelper(GraphActivity.this);
        fireStore = FirebaseFirestore.getInstance();
        setDate();
    }

    @Override
    protected void onStart() {
        super.onStart();

        try {
            String profileName = sharedPreferencesHelper.getProfile().getUsername();
            if (profileName == null || profileName.isEmpty())
                goToActivity(LoginActivity.class); // Send back to login
            else
                username = profileName;
        } catch(Exception exception) {
            Log.d("Error: ", exception.toString());
        }
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
        this.menu = menu;
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int menuId = item.getItemId();
        if(menuId == R.id.toggleLivePastExposure) { // If we click on the ... button
            toggleLivePastData = !toggleLivePastData; // Toggle
            if (toggleLivePastData) { // past data
                menu.findItem(R.id.toggleLivePastExposure).setTitle("See Live Exposure");
                switchMode(false, "Past Data", "Pick a date!");
            } else { // live data
                menu.findItem(R.id.toggleLivePastExposure).setTitle("See Past Exposure");
                switchMode(true, "Live Exposure", "Today");
            }
        }
        return super.onOptionsItemSelected(item);
    }

    /* Checks if we have a wifi or LTE connection */
    /* Will be used if we are not connected to internet and want to use local db */
    private boolean haveNetworkConnection() {
        boolean haveConnectedWifi = false;
        boolean haveConnectedMobile = false;

        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo[] netInfo = cm.getAllNetworkInfo();
        for (NetworkInfo ni : netInfo) {
            if (ni.getTypeName().equalsIgnoreCase("WIFI"))
                if (ni.isConnected())
                    haveConnectedWifi = true;
            if (ni.getTypeName().equalsIgnoreCase("MOBILE"))
                if (ni.isConnected())
                    haveConnectedMobile = true;
        }
        return haveConnectedWifi || haveConnectedMobile;
    }

    /* Lowpass filter used to filter out noise */
    protected double lowPass(double input, double output) {
        output = output + ALPHA * (input - output);
        return output;
    }

    protected double averageValue(double prev, double current) {
        double output = 0;
        output = (prev + current) / 2;
        return output;
    }

    protected void switchMode(Boolean status, String title, String dateText) {
        series.resetData(new DataPoint[] {}); // Reset previous series
        counter = 0; // Reset the time counter
        series.setTitle(title);
        uvIndexTextView.setEnabled(status);
        datePick.setText(dateText);
        datePick.setEnabled(!status);
        graph.getViewport().setScalable(!status);
        graph.getViewport().setScalableY(!status);
        graph.getViewport().setScrollable(!status);
        graph.getViewport().setScrollableY(!status);
    }

    protected void setDate() { // Used to date the date with calendar
        datePick.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Calendar calendar = Calendar.getInstance();
                int day = calendar.get(Calendar.DAY_OF_MONTH);
                int month = calendar.get(Calendar.MONTH);
                int year = calendar.get(Calendar.YEAR);

                datePicker = new DatePickerDialog(GraphActivity.this, new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                        int monthAdjusted = monthOfYear + 1;

                        String zeroMonth = "";
                        String zeroDay = "";
                        if (monthAdjusted < 10) {
                            zeroMonth = "0"+String.valueOf(monthAdjusted);
                        } else {
                            zeroMonth = String.valueOf(monthAdjusted);
                        }
                        if (dayOfMonth < 10) {
                            zeroDay = "0"+String.valueOf(dayOfMonth);
                        } else {
                            zeroDay = String.valueOf(dayOfMonth);
                        }

                        String spinDate = year + "-" + zeroMonth + "-" + zeroDay;
                        datePick.setText("Date: "+ spinDate);
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
        graph.getGridLabelRenderer().setNumHorizontalLabels(3);
        graph.getGridLabelRenderer().setNumVerticalLabels(5);
        graph.getGridLabelRenderer().setLabelFormatter(new DateAsXAxisLabelFormatter(this));
        // Axis
        GridLabelRenderer gridLabel = graph.getGridLabelRenderer();
        gridLabel.setHorizontalAxisTitle("Time"); // 5t is 5 times the actual time to reject fluctuations
        gridLabel.setVerticalAxisTitle("UV Exposure");
    }

    public void fetchUVDataByDate(final String date) {
        // From sharedPrefs, get the username logged in
        int userId = 1;

        Log.d("user:", username);

        /* Read user from database */
        if (haveNetworkConnection()) {
            if (!lastDate.equals(date) && toggleLivePastData) { // We check if the last date chosen isn't the same otherwise don't fetch new data
                series.resetData(new DataPoint[]{}); // Reset previous series
                CollectionReference users = fireStore.collection(DatabaseConfig.USER_TABLE_NAME);
                users.whereEqualTo(DatabaseConfig.COLUMN_USERNAME, username).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document_user : task.getResult()) {
                               constructUvDataByDate(document_user, date);
                            }
                        } else {
                            Log.d("DB:", "Error getting documents: ", task.getException());
                        }
                    }
                });
                lastDate = date;
            }
        } else {
            List<UV> uvList;
            DatabaseHelper databaseHelper = new DatabaseHelper(GraphActivity.this);
            //String userID = User.getUserId();    // Fetch current users ID
            uvList = databaseHelper.getAllUVData(userId, date); // fetch all UV values by date

            series.resetData(new DataPoint[]{}); // Reset previous series
            DataPoint[] newPoints = new DataPoint[databasePoints];
            int newCounter = 0;
            for (int i = 0; i < uvList.size(); i++) {
                double x = uvList.get(i).getUvTime();
                double y = uvList.get(i).getUv();
                DataPoint point = new DataPoint(x, y);
                newPoints[newCounter] = point;
                series.appendData(new DataPoint(point.getX(), point.getY()), false, maxLivePoints);
                newCounter = newCounter + 1;
            }
        }
        lastDate = date;
    }

    protected void constructUvDataByDate(QueryDocumentSnapshot document_user, String date) {
        /* Read all uv (time, uv) values of that user */
        CollectionReference uvData = fireStore.collection(DatabaseConfig.UV_TABLE_NAME);
        // We want to filter by userID and date chosen from uv data
        uvData.whereEqualTo("userId", document_user.getId()).whereEqualTo("date", date).orderBy("uvTime").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                DataPoint[] newPoints = new DataPoint[databasePoints];
                int newCounter = 0;
                if (task.isSuccessful()) {
                    for (QueryDocumentSnapshot document_uv_data : task.getResult()) { // Fetch every point and create new series
                        double x = Double.parseDouble(document_uv_data.getData().get("uvTime").toString());
                        double y = Double.parseDouble(document_uv_data.getData().get("uv").toString());
                        DataPoint point = new DataPoint(x, y);
                        newPoints[newCounter] = point;
                        series.appendData(new DataPoint(point.getX(), point.getY()), false, maxLivePoints);
                        newCounter = newCounter + 1;
                    }
            } else {
                Log.d("DB:", "Error getting documents: ", task.getException());
            }
            }
        });
    }

    protected void addDataToDatabase(final double dataX, final double dataY, final LocalDate date) { // Should store every 5 seconds ? otherwise we may have too many data in database
        // From sharedPrefs, get the username logged in
        String userID = "1";

        if (haveNetworkConnection()) { // If connected wifi or LTE
            CollectionReference users = fireStore.collection("user_info");
            users.whereEqualTo("username", username).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                @Override
                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot document_user : task.getResult()) {

                            /* Add a uv (time, uv) value for that user */
                            CollectionReference uvData = fireStore.collection("uv_data");
                            UV uv = new UV(document_user.getId(), dataX, dataY, date.toString());
                            uvData.add(uv); // Add a new uv value
                        }
                    } else {
                        Log.d("DB:", "Error getting documents: ", task.getException());
                    }
                }
            });
        } else {
            DatabaseHelper databaseHelper = new DatabaseHelper(GraphActivity.this);
            UV uv = new UV(userID, dataX, dataY, date.toString());
            databaseHelper.insertUV(uv);    // Add the current UV value to the database
        }
    }

    double previousY = 0;
    @RequiresApi(api = Build.VERSION_CODES.O)
    protected void buildLiveExposureGraph(String data) {
        Date today = new Date();
        double currentTime = today.getTime();
        double x = counter; // Should be divided by 10 for real second values but we get lots of fluctuation (5 times faster)
        double y = Double.parseDouble(data);
        double weight = 0.20;
        double filterEWMA = (1-weight)*previousY + weight*y;
        series.appendData(new DataPoint(currentTime, liveValues[counter].getY()), false, maxLivePoints); // Send new data to the graph with 5 times less in time to get real time
        addDataToDatabase(currentTime, filterEWMA, LocalDate.now());
        counter = counter + 1; // Increment by 1
        previousY = y;
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

        uvIndexTextView.setText("UV Index (0-11): " + uvIndex);
    }

    //The Arduino Map function but for doubles for UV intensity
    //From: http://forum.arduino.cc/index.php?topic=3922.0
    double mapIntensity(double x, double in_min, double in_max, double out_min, double out_max) {
        return (x - in_min) * (out_max - out_min) / (in_max - in_min) + out_min;
    }

    void goToActivity(Class page) { // Function that goes from the main activity to another one
        Intent intent = new Intent(GraphActivity.this, page); // from the main activity to the profile class
        startActivity(intent);
    }

}
