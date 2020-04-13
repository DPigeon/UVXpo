package com.example.utilisateur.uvexposureapp;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import javax.annotation.Nullable;
import android.os.Handler;

public class splashScreen extends AppCompatActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        //startActivity(new Intent( this, LoginActivity.class));

        new Handler().postDelayed (new Runnable() {
            @Override
            public void run() {
                Intent i = new Intent (splashScreen.this, LoginActivity.class);
                startActivity(i);

                finish ();
            }
        }, 1000);

    }

}
