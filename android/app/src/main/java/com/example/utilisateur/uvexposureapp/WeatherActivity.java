package com.example.utilisateur.uvexposureapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.concurrent.ExecutionException;

public class WeatherActivity extends AppCompatActivity {
    protected TextView Weather;
    protected TextView montreal;
    protected TextView Status;
    protected ListView citiesView;
    protected Button locationButton;
    protected ImageView statuspic;
    static String cityName = "Montreal";
    String main = "";
    String desc = "";
    String temps = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_weather_activity);
       // String main = "";
        //String desc = "";
        //String temps = "";


        Weather weather = new Weather();                //call weather activity thats responsible to deal witht he weather API
        try {
            String content = weather.execute(weatherInfo()).get();          //http call
            //Log.i("contentData", content);
            if (content != null) {
                JSONObject jsonObject = new JSONObject(content);          //going through the JSON formatted data

                String weatherData = jsonObject.getString("weather");
                //Log.i("WeatherData",weatherData);
                JSONArray array = new JSONArray(weatherData);


                String temperature = "";
                temperature = jsonObject.getString("main");

                for (int i = 0; i < array.length(); i++) {                      //filtering out relevant data in this case weather in celsius, the status of the sun
                    JSONObject weatherPt = array.getJSONObject(i);
                    main = weatherPt.getString("main");
                    desc = weatherPt.getString("description");

                }
                JSONObject temp = new JSONObject(temperature);
                temps = temp.getString("temp");
                setupUI(main,desc,temps);                         //setup ui

            }
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    void setupUI(String main,String desc,String temps) {
        Weather = findViewById(R.id.Weather);
        montreal = findViewById(R.id.textView5);
        citiesView = findViewById(R.id.Cities);
        Status = findViewById(R.id.Status);
        statuspic=findViewById(R.id.imageView2);
        cityList();
        Weather.setText("Description: "+desc+"\n"+"Temperature: "+temps);
        Status.setText("Status: "+ main +"\n");
        montreal.setText(cityName);
        locationButton = findViewById(R.id.Location);
        locationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                open(view);
            }
        });

        if (main.equals("clear sky")){                                                 //Deals with the image view depending on what the status is outside
        statuspic.setImageResource(R.drawable.clear_sky);
        }else if(main.equals("few clouds")){
            statuspic.setImageResource(R.drawable.few_clouds);

        }else if(main.equals("scattered clouds")){
            statuspic.setImageResource(R.drawable.scattered_clouds);

        }else if(main.equals("broken clouds")){
            statuspic.setImageResource(R.drawable.scattered_clouds);

        }else if(main.equals("shower rain")){
            statuspic.setImageResource(R.drawable.shower_rain);

        }else if(main.equals("rain")){
            statuspic.setImageResource(R.drawable.rain);

        }else if(main.equals("thunderstorm")){
            statuspic.setImageResource(R.drawable.thunder_storm);

        }else if(main.equals("snow")){
            statuspic.setImageResource(R.drawable.snow);

        }else if(main.equals("mist")){
            statuspic.setImageResource(R.drawable.mist);

        }










    }
    void cityList() {
        final String[] cities = {"   Montreal", "   Vancouver", "   Toronto", "   Calgary", "   Edmonton","   Quebec"};
        final ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, cities);
        citiesView.setAdapter(adapter);

        citiesView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
                cityName = (String) parent.getItemAtPosition(position);
                Toast.makeText(WeatherActivity.this,cities[position],Toast.LENGTH_LONG).show();
                Weather weather = new Weather();
                try {
                    String content = weather.execute(weatherInfo()).get();
                    //Log.i("contentData", content);
                    if (content != null) {
                        JSONObject jsonObject = new JSONObject(content);

                        String weatherData = jsonObject.getString("weather");
                        //Log.i("WeatherData",weatherData);
                        JSONArray array = new JSONArray(weatherData);


                        String temperature = "";
                        temperature = jsonObject.getString("main");

                        for (int i = 0; i < array.length(); i++) {
                            JSONObject weatherPt = array.getJSONObject(i);
                            main = weatherPt.getString("main");
                            desc = weatherPt.getString("description");

                        }
                        JSONObject temp = new JSONObject(temperature);
                        temps = temp.getString("temp");
                        setupUI(main,desc,temps);

                    }
                } catch (ExecutionException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                montreal.setText(cityName);
                Weather.setText("Description: "+desc+"\n"+"Temperature: "+temps);
                Status.setText("Status: "+ main +"\n");
            }
        });
    }


    static String weatherInfo() {
        String URL = "https://openweathermap.org/data/2.5/weather?q="+cityName+","+"ca"+"&appid=b6907d289e10d714a6e88b30761fae22";
        return URL;
    }

    //////to open location activity///
    public void  open(View view) {
        Intent intent= new Intent(this, Location.class);
        startActivity(intent);
    }

}

