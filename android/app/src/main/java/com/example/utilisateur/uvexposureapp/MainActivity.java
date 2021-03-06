package com.example.utilisateur.uvexposureapp;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Notification;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

import static android.bluetooth.BluetoothAdapter.STATE_CONNECTED;
import static com.example.utilisateur.uvexposureapp.Notifications.CHANNELID_1;
import static com.example.utilisateur.uvexposureapp.Notifications.CHANNELID_2;
import static java.lang.Integer.parseInt;

/*
 * The MainActivity where the bluetooth connection is made and the data is fetched.
 */

public class MainActivity extends AppCompatActivity {
    private static final int REQUEST_LOCATION = 1;
    private NotificationManagerCompat notificationManagerCompat;
    protected SharedPreferencesHelper sharedPreferencesHelper;
    FirebaseFirestore fireStore;

    TextView test;

    protected TextView welcomeUserTextView;
    protected Button  graphButton, settingsButton, faqButton;
    String FaqURL = "https://www.ccohs.ca/oshanswers/phys_agents/ultravioletradiation.html?fbclid=IwAR05zwUhYrQqcc0bNr-nSeWcbN7J1LUsjgW3K7Bs5oT49s_O9XrgfFpZybY";
    String TAG = "MainActivity";
    String usernameIntentExtra;
    String passwordIntent;
    Boolean newusercheck;
    LocationManager locationManager;
    String latitude, longitude;
    Boolean storeLocationShown = false; // we show only once
    protected ImageButton newWeatherButton;

    String usernameOffline = null;
    String passwordOffline = null;
    int ageOffline = 0;
    int skinOffline = 0;
    boolean notifsOffline = true;

    @TargetApi(Build.VERSION_CODES.CUPCAKE)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION);
        test = findViewById(R.id.textView);
        setupUI();

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        //Check if GPS or on or not
        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            /// write function to enable GPS
            OnGPS();
        }
        else {
            //gps is already on
            getLocation();
        }

        sharedPreferencesHelper = new SharedPreferencesHelper(MainActivity.this);
        fireStore = FirebaseFirestore.getInstance();
        try {
            Bundle userIntent = getIntent().getExtras(); /**GETS USER INTENTS SO DATA COULD BE RETRIEVED*/

            usernameIntentExtra = userIntent.getString("username");
            passwordIntent = userIntent.getString("password");
            newusercheck = userIntent.getBoolean("checknewuser");

            sharedPreferencesHelper.saveProfile(new User(usernameIntentExtra, passwordIntent, 0, 1, true, newusercheck)); // We save the profile
        } catch(Exception exception) {
            Log.d("Error: ", exception.toString());
        }
      
        setupUI();
        connectAndListen();
        notificationManagerCompat = NotificationManagerCompat.from(this);
    }


    float x1, y1, x2, y2;
    @Override
    public boolean onTouchEvent(MotionEvent touchEvent) {
        switch(touchEvent.getAction()){
            case MotionEvent.ACTION_DOWN:
                x1 = touchEvent.getX();
                y1 = touchEvent.getY();
                break;
            case MotionEvent.ACTION_UP:
                x2 = touchEvent.getX();
                y2 = touchEvent.getY();
                if(y1 >= y2){
                Intent i = new Intent(MainActivity.this, GraphActivity.class);
                startActivity(i);
                overridePendingTransition(R.anim.slide_in, R.anim.slide_out);
            }
            break;
        }
        return false;
    }

    @Override
    protected void onStart() {
        super.onStart();
        DatabaseHelper dbhelper = new DatabaseHelper(this);
        List<User> tutorialNewUserCheck = dbhelper.getAllUserData();
        int ivalue = -1;


        try {
            checkInternetForUpdates();
        }
        catch(Exception e){
            Log.d("Error:", e.toString());
        }

        try {
            String profileName = sharedPreferencesHelper.getProfile().getUsername();

            if (profileName == null || profileName.isEmpty()) {
                //goToActivity(LoginActivity.class); // Send back to login
                Toast.makeText(MainActivity.this, "Welcome!", Toast.LENGTH_SHORT).show();
            }
            else
                welcomeUserTextView.setText("Welcome, " + profileName + "!"); // Otherwise just set the stored name
        } catch(Exception exception) {
            Log.d("Error: ", exception.toString());
        }


        /* Tutorial */
        try {

            if (newusercheck && !haveNetworkConnection()) {
                for (int i = 0; i < tutorialNewUserCheck.size(); i++) {
                    if (usernameIntentExtra.equals(tutorialNewUserCheck.get(i).getUsername())) {
                        ivalue = i;
                    }
                }
                    TutorialFragment dialog = new TutorialFragment();
                    dialog.show(getSupportFragmentManager(), "TutorialFragment");
                    newusercheck = false;
                    String id = Integer.toString(tutorialNewUserCheck.get(ivalue).getUserId());
                    dbhelper.updateData(id, tutorialNewUserCheck.get(ivalue).getUsername(),
                            tutorialNewUserCheck.get(ivalue).getPassword(), tutorialNewUserCheck.get(ivalue).getAge(),
                            tutorialNewUserCheck.get(ivalue).getSkin(), tutorialNewUserCheck.get(ivalue).getNotifications(), newusercheck);

            }
            else if (newusercheck && haveNetworkConnection()){
                TutorialFragment dialog = new TutorialFragment();
                dialog.show(getSupportFragmentManager(), "TutorialFragment");
                newusercheck = false;

                final CollectionReference users = fireStore.collection(DatabaseConfig.USER_TABLE_NAME);
                users.whereEqualTo(DatabaseConfig.COLUMN_USERNAME, usernameIntentExtra).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document_user : task.getResult()) {
                                users.document(document_user.getId()).update("newUser", newusercheck);
                            }
                        } else {
                            Toast.makeText(MainActivity.this, "Error storing newUser value!", Toast.LENGTH_SHORT).show();
                        }

                    }
                });
            }
        } catch (Exception exception) {
            Log.d("New User Check", exception.toString());
        }
    }


    protected void setupAction() { // No action bar for the main activity
        //ActionBar actionBar = getSupportActionBar();
        //actionBar.setDisplayHomeAsUpEnabled(false);
    }

    protected void setupUI() {
        setupAction();
        welcomeUserTextView = findViewById(R.id.welcomeUserTextView);
        graphButton = findViewById(R.id.graphButton);

        newWeatherButton = findViewById(R.id.imageButton);
        settingsButton = findViewById(R.id.settingsButton);
        weatherbutton();
        newWeatherButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                checkInternetForUpdates();
                goToActivity(WeatherActivity.class);

            }
        });
        graphButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                checkInternetForUpdates();
                goToActivity(GraphActivity.class);
            }
        });
        settingsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                checkInternetForUpdates();
                Intent intent = new Intent(MainActivity.this, UserActivity.class);
                intent.removeExtra("username");
                intent.removeExtra("checknewuser");
                intent.putExtra("username", usernameIntentExtra);/**ADDING INTENT SO USER DATA CAN BE RETRIEVED*/
                intent.putExtra("password", passwordIntent);
                intent.putExtra("checknewuser", newusercheck);
                startActivity(intent);
            }
        });
        faqButton = findViewById(R.id.faqButton);
        faqButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                checkInternetForUpdates();
                openFaqWebsite(view);
            }
        });

    }

    /* Linking website to FAQ button */
    public void openFaqWebsite(View view) {
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(FaqURL));
        startActivity(browserIntent);
    }

    void goToActivity(Class page) { // Function that goes from the main activity to another one
        Intent intent = new Intent(MainActivity.this, page); // from the main activity to the profile class
        intent.removeExtra("username");
        intent.removeExtra("checknewuser");
        intent.putExtra("username", usernameIntentExtra);/**ADDING INTENT SO USER DATA CAN BE RETRIEVED*/
        intent.putExtra("password", passwordIntent);
        intent.putExtra("checknewuser", newusercheck);
        startActivity(intent);
        overridePendingTransition(R.anim.slide_in, R.anim.slide_out);
    }

    // Connects to the Bluetooth device & starts the service to listen to inputs
    protected void connectAndListen() {
        final String DEVICE_ADDRESS = sharedPreferencesHelper.getBluetoothAddress(); // Gets the device paired from bluetooth activity
        final UUID SERVICE_UUID = UUID.fromString("b923eeab-9473-4b86-8607-5068911b18fe"); // First layer
        final UUID CHARACTERISTIC_UUID = UUID.fromString("aba24047-b36f-4646-92ce-3d5c0c75bd20"); // Second layer
        final UUID CLIENT_CHARACTERISTIC_CONFIG_UUID = convertFromInteger(0x2902); // Used to send data from phone --> microcontroller
        boolean foundDevice = false;

        if (BluetoothAdapter.getDefaultAdapter() == null) {
            Log.i(TAG, "Bluetooth not supported on Virtual Devices! Use a real device.");
            finish(); // Allowing to skip the exception
        } else {
            BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

            BluetoothDevice bluetoothDevice = null;
            Set<BluetoothDevice> devices;
            devices = bluetoothAdapter.getBondedDevices(); // We get the devices
            for (BluetoothDevice device : devices) {
                if (device.getAddress().equals(DEVICE_ADDRESS)) { // We find the Arduino device server
                    bluetoothDevice = device;
                    foundDevice = true;
                    break;
                }
            }
            if (foundDevice) { // If found connect, otherwise don't
                BluetoothGatt gatt; // We're using a low energy bluetooth microcontroller. Must use Gatt.

                /* This is the callback where all the receiving and sending happens. Once connected, this callback thread runs in background */
                BluetoothGattCallback gattCallback = new BluetoothGattCallback() {
                    @Override
                    public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) { // If connection state changes
                        if (newState == STATE_CONNECTED)
                            gatt.discoverServices(); // Discover the services to update
                    }

                    @Override
                    public void onServicesDiscovered(BluetoothGatt gatt, int status) { // As soon as we discover new services
                        super.onServicesDiscovered(gatt, status);
                        BluetoothGattCharacteristic characteristic = gatt.getService(SERVICE_UUID).getCharacteristic(CHARACTERISTIC_UUID);
                        gatt.setCharacteristicNotification(characteristic, true); // Set notifications on

                        BluetoothGattDescriptor descriptor = characteristic.getDescriptor(CLIENT_CHARACTERISTIC_CONFIG_UUID); // Set descriptor on

                        descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                        gatt.writeDescriptor(descriptor);
                    }

                    @Override
                    public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) { // Used to send data phone --> microcontroller
                        BluetoothGattCharacteristic characteristic =
                                gatt.getService(SERVICE_UUID).getCharacteristic(CHARACTERISTIC_UUID);
                        String data = "Hello Microcontroller !";
                        byte[] byteArray = data.getBytes();
                        characteristic.setValue(byteArray); // Send data here example
                        gatt.writeCharacteristic(characteristic);
                    }

                    @RequiresApi(api = Build.VERSION_CODES.O)
                    @Override
                    public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) { // Listen to data here from the characteristic
                        String data = new String(characteristic.getValue()); // Converting from byte[] to string
                        Log.d("hi:", data);
                        double val = Double.parseDouble(data);
                        notificationFunction(val);
                        /* Here we broadcast data to other activities using a specific action */
                        Intent intent = new Intent("graph-activity"); // New intent to send called live-data
                        intent.setPackage(getPackageName()); // Setup package
                        intent.putExtra("uv-live-data", data); // Put a string message with it
                        getApplicationContext().sendBroadcast(intent); // Send the message over broadcast
                    }
                };
                gatt = bluetoothDevice.connectGatt(this, true, gattCallback); // Connect with a callback
            }
        }
    }

    public UUID convertFromInteger(int i) { // Used to get the right UUID for descriptor and characteristic of Gatt Bluetooth
        final long MSB = 0x0000000000001000L;
        final long LSB = 0x800000805f9b34fbL;
        long value = i & 0xFFFFFFFF;
        return new UUID(MSB | (value << 32), LSB);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public void notificationFunction(double data) {
        int uvIndex = 0;
        double voltage = data * (3.3 / 1023) * 1000; // using 3.3 mV

        ImageView uvIndicator= (ImageView) findViewById(R.id.imageView5);
        uvIndicator.setImageResource(R.drawable.uv_none);

        // From http://educ8s.tv/arduino-uv-meter-project/ with converting of 3.3V output
        if (voltage < 5) {
            uvIndex = 0;
            uvIndicator.setImageResource(R.drawable.uv_none);
        }
        else if (voltage > 5 && voltage <= 33){
            uvIndex = 1;
            uvIndicator.setImageResource(R.drawable.uv_low);
        }
        else if (voltage > 33 && voltage <= 150) {
            uvIndex = 2;
            uvIndicator.setImageResource(R.drawable.uv_medium);
        }
        else if (voltage > 150 && voltage <= 210) {
            uvIndex = 3;
            uvIndicator.setImageResource(R.drawable.uv_high);
        }
        else if (voltage > 210 && voltage <= 269) {
            uvIndex = 4;
            uvIndicator.setImageResource(R.drawable.uv_high);
        }
        else if (voltage > 269 && voltage <= 332) {
            uvIndex = 5;
            uvIndicator.setImageResource(R.drawable.uv_high);
        }
        else if (voltage > 332 && voltage <= 400) {
            uvIndex = 6;
            uvIndicator.setImageResource(R.drawable.uv_high);
        }
        else if (voltage > 400 && voltage <= 459) {
            uvIndex = 7;
            uvIndicator.setImageResource(R.drawable.uv_high);
        }
        else if (voltage > 459 && voltage <= 525) {
            uvIndex = 8;
            uvIndicator.setImageResource(R.drawable.uv_high);
        }
        else if (voltage > 525 && voltage <= 681) {
            uvIndex = 9;
            uvIndicator.setImageResource(R.drawable.uv_high);
        }
        else if (voltage > 581 && voltage <= 644) {
            uvIndex = 10;
            uvIndicator.setImageResource(R.drawable.uv_high);
        }
        else if (voltage > 644) {
            uvIndex = 11;
            uvIndicator.setImageResource(R.drawable.uv_high);
        }
        //if (UserActivity.flag == true) {
            if (uvIndex >= 1 && uvIndex <= 2) {
                channel2Notifmedium();
            } else if (uvIndex > 2 && uvIndex <= 5) {
                channel2Notifhigh();
            } else if (uvIndex > 5 && uvIndex <= 7) {
                channel2Notifhigh();
            } else if (uvIndex > 7 && uvIndex <= 10) {
                channel2Notifveryhigh();
            } else if (uvIndex > 11) {
                channel2Notifextremelyhigh();
            }
        // }
        //test.setText("Voltage: " + String.valueOf(voltage) + "\n" + "UV Index: " + String.valueOf(uvIndex));
    }

    public void showStoreLocator() {
        if (!storeLocationShown && haveNetworkConnection()) { // Showing once and with internet
            StoreLocatorFragment dialog = new StoreLocatorFragment();
            Bundle bundle = new Bundle();
            bundle.putString("lat", latitude);
            bundle.putString("long", longitude);
            dialog.setArguments(bundle);
            dialog.show(getSupportFragmentManager(), "StoreLocatorFragment");
            storeLocationShown = true;
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public void channel1Notif() {
    Notification notifications = new NotificationCompat.Builder(this,CHANNELID_1)
            .setSmallIcon(R.drawable.ic_sentiment_satisfied_black_24dp)
            .setContentTitle("It is pretty sunny out there!")
            .setContentText("Stay in shade, apply sunscreen and wear sunglasses!")
            .build();
        notificationManagerCompat.notify(1,notifications);
    }

    public void channel2Notif() {
        Notification notifications = new NotificationCompat.Builder(this,CHANNELID_2)
                .setSmallIcon(R.drawable.ic_sentiment_satisfied_black_24dp)
                .setContentTitle("Wow its hot! ")
                .setContentText("Make sure to protect yourself with a hat!")
                .build();
        notificationManagerCompat.notify(2,notifications);
    }
    public void channel2Notifmedium() {
        Notification notifications = new NotificationCompat.Builder(this,CHANNELID_2)
                .setSmallIcon(R.drawable.ic_sun)
                .setContentTitle("Yikes! The sun is strong today!")
                .setContentText("A little sunscreen wouldn't hurt!")
                .build();
        notificationManagerCompat.notify(2,notifications);

        // Opens a fragment that proposes you store around you with sunscreen
        showStoreLocator();
    }
    public void channel2Notifhigh() {
        Notification notifications = new NotificationCompat.Builder(this,CHANNELID_2)
                .setSmallIcon(R.drawable.ic_sun)
                .setContentTitle("UV app")
                .setContentText("Sunscreen, Shade and a hat would be nice to combat UV exposure today!")
                .build();
        notificationManagerCompat.notify(2,notifications);
    }
    public void channel2Notifveryhigh() {
        Notification notifications = new NotificationCompat.Builder(this,CHANNELID_2)
                .setSmallIcon(R.drawable.ic_notif1)
                .setContentTitle("WARNING! Dangerous levels of UV detected! ")
                .setContentText("Make sure to be careful out there and apply lots of suncreen!")
                .build();
        notificationManagerCompat.notify(2,notifications);
    }
    public void channel2Notifextremelyhigh() {
        Notification notifications = new NotificationCompat.Builder(this,CHANNELID_2)
                .setSmallIcon(R.drawable.ic_warning)
                .setContentTitle("WARNING!!!")
                .setContentText("Best to stay indoors today, dangerous levels of UV detected.")
                .build();
        notificationManagerCompat.notify(2,notifications);
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
  
    public void checkInternetForUpdates(){
        try {
            if (haveNetworkConnection()) {
                String profileName = sharedPreferencesHelper.getProfile().getUsername();
                DatabaseHelper dbhelper = new DatabaseHelper(this);
                List<User> userLocaltoOnline = dbhelper.getAllUserData();
                final CollectionReference users = fireStore.collection(DatabaseConfig.USER_TABLE_NAME);


                for (int i = 0; i < userLocaltoOnline.size(); i++) {
                    if (profileName.equals(userLocaltoOnline.get(i).getUsername())) {
                        usernameOffline = userLocaltoOnline.get(i).getUsername();
                        passwordOffline = userLocaltoOnline.get(i).getPassword();
                        ageOffline = userLocaltoOnline.get(i).getAge();
                        skinOffline = userLocaltoOnline.get(i).getSkin();
                        notifsOffline = userLocaltoOnline.get(i).getNotifications();
                    }
                }

                users.whereEqualTo(DatabaseConfig.COLUMN_USERNAME, profileName).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document_user : task.getResult()) {
                                users.document(document_user.getId()).update("notifications", notifsOffline, "password", passwordOffline);
                                users.document(document_user.getId()).update("age", ageOffline,  "skin", skinOffline);
                            }

                        }
                    }
                });
            }
        }
        catch(Exception exception){
            Log.d("Exception: ", exception.toString());
        }
    }

   void weatherbutton(){
        String main = "clear sky";
    String desc = "rain";

       Weather weather = new Weather();
       try {
           String content = weather.execute(WeatherActivity.weatherInfo()).get();
           //Log.i("contentData", content);
           if (content != null) {
               JSONObject jsonObject = new JSONObject(content);

               String weatherData = jsonObject.getString("weather");
               //Log.i("WeatherData",weatherData);
               JSONArray array = new JSONArray(weatherData);
               for (int i = 0; i < array.length(); i++) {
                   JSONObject weatherPt = array.getJSONObject(i);
                   main = weatherPt.getString("main");
                   desc = weatherPt.getString("description");

               }}}
    catch (InterruptedException e) {
                   e.printStackTrace();
               } catch (ExecutionException e) {
                   e.printStackTrace();
               } catch (JSONException e) {
                   e.printStackTrace();
               }




       if (desc.equals("clear sky")){                                                 //Deals with the image view depending on what the status is outside
           newWeatherButton.setImageResource(R.drawable.clear_sky);
       }else if(desc.equals("clouds")||desc.equals("overcast clouds")){
           newWeatherButton.setImageResource(R.drawable.scattered_clouds);

       }else if(desc.equals("scattered clouds")){
           newWeatherButton.setImageResource(R.drawable.few_clouds);

       }else if(desc.equals("broken clouds")||main.equals("Clouds")){
           newWeatherButton.setImageResource(R.drawable.few_clouds);

       }else if(desc.equals("shower rain")||main.equals("Rain")){
           newWeatherButton.setImageResource(R.drawable.shower_rain);

       }else if(desc.equals("rain")||main.equals("Drizzle")){
           newWeatherButton.setImageResource(R.drawable.rain);

       }else if(desc.equals("thunderstorm")||main.equals("Thunderstorm")){
           newWeatherButton.setImageResource(R.drawable.thunder_storm);

       }else if(desc.equals("snow")||main.equals("Snow")){
           newWeatherButton.setImageResource(R.drawable.snow);

       }else if(desc.equals("mist")){
           newWeatherButton.setImageResource(R.drawable.mist);

       }else {
           newWeatherButton.setImageResource(R.drawable.few_clouds);
       }
   }

    private void getLocation(){

        //check permission again
        if (ActivityCompat.checkSelfPermission(MainActivity.this,Manifest.permission.ACCESS_FINE_LOCATION)!= PackageManager.PERMISSION_GRANTED &&ActivityCompat.checkSelfPermission(MainActivity.this,Manifest.permission.ACCESS_COARSE_LOCATION)!=PackageManager.PERMISSION_GRANTED)

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
            }
            else if(LocationNetwork!=null){
                double lat=LocationNetwork.getLatitude();
                double longi=LocationNetwork.getLongitude();
                latitude= String.valueOf(lat);
                longitude=String.valueOf(longi);
            }
            else if(LocationPassive!=null){
                double lat=LocationPassive.getLatitude();
                double longi=LocationPassive.getLongitude();
                latitude= String.valueOf(lat);
                longitude=String.valueOf(longi);
            }
            else{

                Toast.makeText(this,"Can't get your location",Toast.LENGTH_SHORT).show();
            }
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
