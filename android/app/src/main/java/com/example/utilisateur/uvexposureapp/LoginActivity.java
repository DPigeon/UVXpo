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
    protected SharedPreferencesHelper sharedPreferencesHelper;
    Button loginButton; /**log in button that checks for username and password*/
    Button newregisterButton;
    EditText usernameEditText; /**user input for username*/
    EditText passwordEditText; /**user input for password*/
    String persistentUsername;
    String persistentPassword;
    Boolean persistentCheckNewUser;
    DatabaseHelper dbhelperCheck;

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
        sharedPreferencesHelper = new SharedPreferencesHelper(LoginActivity.this);


        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DatabaseHelper dbhelper = new DatabaseHelper(LoginActivity.this);
                List<User> userIDcheck = dbhelper.getAllUserData();
                int ivalueCheck = -1;

                if (!haveNetworkConnection()) { // Locally, no internet
                    for (int i = 0; i < userIDcheck.size(); i++) {
                        if (userIDcheck.get(i).getUsername().equals(usernameEditText.getText().toString())) {
                            ivalueCheck = i;
                        }
                    }
                    if (usernameEditText.equals("")){
                        Toast.makeText(LoginActivity.this, "Invalid Username.", Toast.LENGTH_SHORT).show();
                    }
                    else if (ivalueCheck == -1) {
                        Toast.makeText(LoginActivity.this, "No account found, either because its not registered or not on your device.", Toast.LENGTH_SHORT).show();
                    }
                    else if (userIDcheck.get(ivalueCheck).getUsername().equals(usernameEditText.getText().toString()) && userIDcheck.get(ivalueCheck).getPassword().equals(passwordEditText.getText().toString()))
                    {
                        proceedLogin(usernameEditText.getText().toString(), passwordEditText.getText().toString(), userIDcheck.get(ivalueCheck).getNewUser());
                    }
                    else {
                        Toast.makeText(LoginActivity.this, "Invalid Password.", Toast.LENGTH_SHORT).show();
                    }
                }
                else { // Online, no internet
                    
                    loginOnline(usernameEditText.getText().toString(), passwordEditText.getText().toString());
                }
            }
        });
        newregisterButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try
                {
                    if (haveNetworkConnection())
                    {
                        RegisterUserFragment dialog = new RegisterUserFragment();
                        Bundle bundle = new Bundle();
                        bundle.putBoolean("hasInternet", haveNetworkConnection());
                        dialog.setArguments(bundle);
                        dialog.show(getSupportFragmentManager(), "RegisterUserFragment");
                    }
                    else
                    {
                        Toast.makeText(LoginActivity.this, "Internet required for registration.", Toast.LENGTH_SHORT).show();
                    }
                }
                catch (Exception e){
                    Log.d("Exception", e.toString());
                }

            }
        });

        setupAction();
    }

    @Override
    protected void onStart() {
        super.onStart();
        // Persistent Login here: allows the user to stay logged in and skip login activity
        try {
            persistentUsername = sharedPreferencesHelper.getProfile().getUsername();
            persistentPassword = sharedPreferencesHelper.getProfile().getPassword();
            persistentCheckNewUser = sharedPreferencesHelper.getProfile().getNewUser();
            if (!persistentUsername.isEmpty() && !persistentPassword.isEmpty())
                proceedLogin(persistentUsername, persistentPassword, persistentCheckNewUser);
        } catch (Exception exception) {
            Log.d("Login", exception.toString());
        }
    }

    protected void setupAction() { // No action bar for the main activity
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(false);
    }

    public void proceedLogin(String username, String password, Boolean newUserCheck) {
        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
        intent.removeExtra("username");
        intent.removeExtra("checknewuser");
        intent.putExtra("username", username);
        intent.putExtra("password", password);
        intent.putExtra("checknewuser", newUserCheck);
        Toast.makeText(LoginActivity.this, "Logged In!", Toast.LENGTH_SHORT).show();
        startActivity(intent); /**if correct, open mainactivity*/
        finish();
    }

    public void loginOnline(final String username, final String password) {
        CollectionReference users = fireStore.collection(DatabaseConfig.USER_TABLE_NAME);
        dbhelperCheck = new DatabaseHelper(this);
        final List<User> userLocalDBCheck = dbhelperCheck.getAllUserData();
        // We look if username and password matches at same time
        Query query;
        query = users.whereEqualTo(DatabaseConfig.COLUMN_USERNAME, username);
        /* Have to look if this query fails to output message */
        Toast.makeText(LoginActivity.this, "Logging in...", Toast.LENGTH_SHORT).show();
        query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    if (!task.getResult().getDocuments().isEmpty() && task.getResult().getDocuments().get(0).getData().get(DatabaseConfig.COLUMN_PASSWORD).toString().equals(password)) { // If user exists and password matches
                        Boolean checkNewUser = Boolean.valueOf(task.getResult().getDocuments().get(0).getData().get("newUser").toString());
                        proceedLogin(usernameEditText.getText().toString(), passwordEditText.getText().toString(), checkNewUser);


                        boolean userInLocalDB = false;
                        for (int i = 0; i < userLocalDBCheck.size(); i++)
                        {
                            if (username.equals(userLocalDBCheck.get(i).getUsername())){
                                userInLocalDB = true;
                            }
                        }
                        if (!userInLocalDB){
                            int age = Integer.parseInt(task.getResult().getDocuments().get(0).getData().get("age").toString());
                            int skin = Integer.parseInt(task.getResult().getDocuments().get(0).getData().get("skin").toString());

                            dbhelperCheck.insertUser(new User(username, password, age, skin, true, true));
                        }
                        return;
                    }
                }
                else {
                    Toast.makeText(LoginActivity.this, "Invalid Username and/or Password", Toast.LENGTH_SHORT).show();
                }
                Toast.makeText(LoginActivity.this, "Invalid Username and/or Password", Toast.LENGTH_SHORT).show();
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
