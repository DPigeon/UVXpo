package com.example.utilisateur.uvexposureapp;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.EditText;
import android.widget.TextView;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.concurrent.ExecutionException;

public class WeatherActivity extends AppCompatActivity {
    protected TextView Weather;
    protected EditText City;
    protected TextView montreal;
    protected TextView Status;
    static String cityName = "London";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_weather_activity);
        String main = "";
        String desc = "";
        String temps = "";

        Weather weather = new Weather();
        try {
            String content = weather.execute("https://openweathermap.org/data/2.5/weather?q=Montreal,ca&appid=b6907d289e10d714a6e88b30761fae22").get();
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
    }

    void setupUI(String main,String desc,String temps) {
        Weather = findViewById(R.id.Weather);
        City = findViewById(R.id.City);
        montreal = findViewById(R.id.textView5);
        Status = findViewById(R.id.Status);
        cityName = City.getText().toString();
        Weather.setText("Description: "+desc+"\n"+"Temperature: "+temps);
        Status.setText("Status: "+ main +"\n");
    }

    public String weatherInfo() {
        String URL = "https://openweathermap.org/data/2.5/weather?q="+cityName+","+"ca"+"&appid=b6907d289e10d714a6e88b30761fae22";
        return URL;
    }
}

