package com.example.utilisateur.uvexposureapp;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class LoginActivity extends AppCompatActivity {

    Button loginButton; /**log in button that checks for username and password*/
    EditText usernameEditText; /**user input for username*/
    EditText passwordEditText; /**user input for password*/


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        loginButton = findViewById(R.id.loginButton); /**assign to actual button*/
        usernameEditText = findViewById(R.id.usernameEditText); /**assign to edittext*/
        passwordEditText = findViewById(R.id.passwordEditText); /**assign to edittext*/

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (usernameEditText.getText().toString().equals("abc") && passwordEditText.getText().toString().equals("abc"))
                { /**currently set the password and username to 'abc' but it should be attached to database*/
                    Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                    startActivity(intent); /**if correct, open mainactivity*/

                }
                else
                { /**if password is incorrect, clear inputs and toast message*/
                    Toast.makeText(LoginActivity.this, "Invalid username and/or password" , Toast.LENGTH_SHORT).show();
                    usernameEditText.setText(null);
                    passwordEditText.setText(null);
                }
            }
        });
        setupAction();
    }

    protected void setupAction() { // No action bar for the main activity
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(false);
    }


}
