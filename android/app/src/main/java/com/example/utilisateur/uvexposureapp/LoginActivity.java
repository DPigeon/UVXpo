package com.example.utilisateur.uvexposureapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCanceledListener;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.List;

public class LoginActivity extends AppCompatActivity {

    FirebaseFirestore fireStore;
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
        usernameEditText.setText(null);
        passwordEditText.setText(null);
        fireStore = FirebaseFirestore.getInstance();

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DatabaseHelper dbhelper = new DatabaseHelper(LoginActivity.this);
                List<User> userIDcheck = dbhelper.getAllUserData();
                int ivalueCheck = -1;

                if (!haveNetworkConnection()) { // Locally, no internet
                    for (int i = 0; i < userIDcheck.size(); i++) {
                        if (userIDcheck.get(i).getUsername().equals(usernameEditText.getText().toString()) && userIDcheck.get(i).getPassword().equals(passwordEditText.getText().toString())) {
                            ivalueCheck = i;
                            proceedLogin();

                        } else
                            Toast.makeText(LoginActivity.this, "Invalid Username and/or Password", Toast.LENGTH_SHORT).show();
                    }
                } else // Online, no internet
                    loginOnline(usernameEditText.getText().toString(), passwordEditText.getText().toString());
            }
        });
        newregisterButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                RegisterUserFragment dialog = new RegisterUserFragment();
                Bundle bundle = new Bundle();
                bundle.putBoolean("hasInternet", haveNetworkConnection());
                dialog.setArguments(bundle);
                dialog.show(getSupportFragmentManager(), "RegisterUserFragment");
            }
        });

        setupAction();
    }

    protected void setupAction() { // No action bar for the main activity
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(false);
    }

    public void proceedLogin() {
        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
        intent.removeExtra("username");
        intent.removeExtra("checknewuser");
        intent.putExtra("username", usernameEditText.getText().toString());
        intent.putExtra("checknewuser", false);
        Toast.makeText(LoginActivity.this, "Logged In!", Toast.LENGTH_SHORT).show();
        startActivity(intent); /**if correct, open mainactivity*/
        finish();
    }

    public void loginOnline(String username, final String password) {
        CollectionReference users = fireStore.collection(DatabaseConfig.USER_TABLE_NAME);
        // We look if username and password matches at same time
        Query query;
        query = users.whereEqualTo(DatabaseConfig.COLUMN_USERNAME, username);
        /* Have to look if this query fails to output message */
        query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    for (QueryDocumentSnapshot document_user : task.getResult()) {
                        if (document_user.getData().get(DatabaseConfig.COLUMN_PASSWORD).toString().equals(password))
                            proceedLogin();
                        else
                            Toast.makeText(LoginActivity.this, "Wrong password!", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(LoginActivity.this, "Invalid Username and/or Password", Toast.LENGTH_SHORT).show();
                }
            }
        });
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

}
