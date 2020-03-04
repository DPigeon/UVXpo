package com.example.utilisateur.uvexposureapp;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.EditText;
import android.widget.TextView;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.concurrent.ExecutionException;

public class WeatherAcitvity extends AppCompatActivity {
    protected TextView Weather;
    protected EditText City;
    protected TextView Country;
    protected TextView Status;
    static String cityName = "London";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_weather_acitvity);
        String main = "";
        String desc = "";
        String temps = "";
        setupUI(main,desc,temps);
        Weather weather = new Weather();
        try {
            String content = weather.execute(weatherInfo()).get();
             //Log.i("contentData", content);
            JSONObject jsonObject = new JSONObject(content);
            String weatherData = jsonObject.getString("weather");
            //Log.i("WeatherData",weatherData);
            JSONArray array = new JSONArray(weatherData);


            String temperature = "";
            temperature = jsonObject.getString("main");
            for (int i = 0;i<array.length();i++){
                JSONObject weatherPt = array.getJSONObject(i);
                main = weatherPt.getString("main");
                desc = weatherPt.getString("description");

            }
            JSONObject temp = new JSONObject(temperature);
            temps = temp.getString("temp");
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
        Country = findViewById(R.id.weather);
        Status = findViewById(R.id.Status);
        cityName = City.getText().toString();
        Weather.setText("Status: "+ main +"\n"+"Description: "+desc+"\n"+"Temperature: "+temps);

    }

    public String weatherInfo() {
        String URL = "https://openweathermap.org/data/2.5/weather?q="+cityName+","+"uk"+"&appid=b6907d289e10d714a6e88b30761fae22";
        return URL;


    }
}
