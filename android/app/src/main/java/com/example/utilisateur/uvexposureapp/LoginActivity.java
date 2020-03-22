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

import java.util.List;

public class LoginActivity extends AppCompatActivity {

    Button loginButton; /**log in button that checks for username and password*/
    Button newregisterButton;
    EditText usernameEditText; /**user input for username*/
    EditText passwordEditText; /**user input for password*/


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        loginButton = findViewById(R.id.loginButton); /**assign to actual button*/
        newregisterButton = findViewById(R.id.registerButton);
        usernameEditText = findViewById(R.id.usernameEditText); /**assign to edittext*/
        passwordEditText = findViewById(R.id.passwordEditText); /**assign to edittext*/


        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DatabaseHelper dbhelper = new DatabaseHelper(LoginActivity.this);
                List<User> userIDcheck = dbhelper.getAllUserData();
                int ivalueCheck = -1;
                for (int i = 0; i < userIDcheck.size(); i++)
                {
                    if (userIDcheck.get(i).getUsername().equals(usernameEditText.getText().toString()) && userIDcheck.get(i).getPassword().equals(passwordEditText.getText().toString()))
                    {
                        /**currently set the password and username to 'abc' but it should be attached to database*/
                        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                        intent.removeExtra("username");
                        intent.removeExtra("checknewuser");
                        intent.putExtra("username", usernameEditText.getText().toString());
                        intent.putExtra("checknewuser", false);
                        startActivity(intent); /**if correct, open mainactivity*/
                        finish();
                        ivalueCheck = i;
                    }
                }
                if (ivalueCheck == -1)
                {
                    Toast.makeText(LoginActivity.this,"Invalid Username and/or Password", Toast.LENGTH_SHORT).show();
                }

            }
        });
        newregisterButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                RegisterUserFragment dialog = new RegisterUserFragment();
                dialog.show(getSupportFragmentManager(), "RegisterUserFragment");
            }
        });

        setupAction();
    }

    protected void setupAction() { // No action bar for the main activity
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(false);
    }
}
