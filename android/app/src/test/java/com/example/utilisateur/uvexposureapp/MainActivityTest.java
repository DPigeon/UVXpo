package com.example.utilisateur.uvexposureapp;

import android.widget.Button;

import org.junit.*;

/* A simple test for buttons on main activity */

public class MainActivityTest {
    private MainActivity mainActivity;
    private Button weatherButton;

    @Before
    public void setup() {
        mainActivity = new MainActivity();
        weatherButton = mainActivity.findViewById(R.id.weatherButton);
    }

    @Test
    public void test_weather_button() {
        String expected = "Get Current Weather Data";
        Assert.assertEquals(expected, weatherButton.getText());
    }
}
