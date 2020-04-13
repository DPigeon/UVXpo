package com.example.utilisateur.uvexposureapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ActionBar;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import static java.lang.Math.toIntExact;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import static java.lang.Integer.parseInt;

public class UserActivity extends AppCompatActivity {

    RadioGroup radioGroup;
    RadioButton radioButton;
    EditText editTextAge;
    Switch notifSwitch;
    Button saveButton;
    RadioButton radioSkintype1;
    RadioButton radioSkintype2;
    RadioButton radioSkintype3;
    RadioButton radioSkintype4;
    RadioButton radioSkintype5;
    RadioButton radioSkintype6;
    Button bluetoothButton;
    int getDOBday;
    int getDOBmonth;
    int getDOByear;
    int ageChecker;


    Boolean newuserregcheck = false;
    String usernameIntent;
    String passwordIntent;
    DatabaseHelper dbhelper;
    FirebaseFirestore fireStore;
    protected SharedPreferencesHelper sharedPreferencesHelper;
    Cursor userInfoAllData;
    List<User> userInfo;
    DatePickerDialog.OnDateSetListener date;
    final Calendar myCalendar = Calendar.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user);

        /**SET ALL OBJECTS TO VARIABLES*/
        radioGroup = findViewById(R.id.radiogroupSkinColor); /**CHECKBOXES FOR SKIN COLOR*/
        editTextAge = findViewById(R.id.editTextAge); /**AGE INPUT*/
        notifSwitch = findViewById(R.id.switchNotif); /**NOTIFICATIONS SWITCH*/
        saveButton = findViewById(R.id.saveButton); /**SAVE BUTTON*/
        radioSkintype1 = findViewById(R.id.radioSkinTypeone);
        radioSkintype2 = findViewById(R.id.radioSkinTypetwo);
        radioSkintype3 = findViewById(R.id.radioSkinTypethree);
        radioSkintype4 = findViewById(R.id.radioSkinTypefour);
        radioSkintype5 = findViewById(R.id.radioSkinTypefive);
        radioSkintype6 = findViewById(R.id.radioSkinTypesix);
        bluetoothButton = findViewById(R.id.bluetoothButton);

        dbhelper = new DatabaseHelper(this);
        fireStore = FirebaseFirestore.getInstance();
        sharedPreferencesHelper = new SharedPreferencesHelper(UserActivity.this);
        userInfoAllData = dbhelper.getData();
        userInfo = dbhelper.getAllUserData();

        //set calendar format for date of birth entry once user inputs age
        date = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                myCalendar.set(Calendar.YEAR, year );
                myCalendar.set(Calendar.MONTH, month);
                myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                getDOBday = dayOfMonth;
                getDOBmonth = month;
                getDOByear = year;
                ageChecker = getAge(getDOByear, getDOBmonth, getDOBday);

                updateLabel();
            }
        };
        //opens calendar format for user input
        editTextAge.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new DatePickerDialog(UserActivity.this, date, myCalendar.get(Calendar.YEAR), myCalendar.get(Calendar.MONTH), myCalendar.get(Calendar.DAY_OF_MONTH)).show();
            }
        });

        //attempt to grab bundle intents sent by previous activity
        try {
            Bundle bndset = getIntent().getExtras();
            newuserregcheck = bndset.getBoolean("checknewuser"); /**INTENT RETRIEVAL*/
            usernameIntent = bndset.getString("username");
            passwordIntent = bndset.getString("password");
        } catch (Exception exception) {
            Log.d("Error: ", exception.toString());
        }

        if (!newuserregcheck) /**IF USER IS NOT NEW DATA WILL BE ADDED TO OBJECTS*/
        {

            //setAllValues();
            if (!haveNetworkConnection()) { // Local, offline db fetch for user info
                for (int i = 0; i < userInfo.size(); i++) {
                    if (userInfo.get(i).getUsername().equals(usernameIntent)) {
                        int numintcheck = userInfo.get(i).getAge();
                        String numintToString = Integer.toString(numintcheck);
                        editTextAge.setText("Age: " + numintToString);
                        fetchSkinType(userInfo.get(i).getSkin());
                        fetchNotifs(userInfo.get(i).getNotifications());

                        Toast.makeText(UserActivity.this, "Accessed User Account " + usernameIntent + " Age " + numintcheck + " Skin " +  userInfo.get(i).getSkin() +  " Notifs " + userInfo.get(i).getNotifications(), Toast.LENGTH_SHORT).show();
                    }
                }
            } else { // Online db fetch for user info
                CollectionReference users = fireStore.collection(DatabaseConfig.USER_TABLE_NAME);
                users.whereEqualTo(DatabaseConfig.COLUMN_USERNAME, usernameIntent).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document_user : task.getResult()) {
                                editTextAge.setText("Age: " + document_user.getData().get("age").toString());
                                fetchSkinType(Integer.parseInt(document_user.getData().get("skin").toString()));
                                boolean notifChecker = Boolean.valueOf(document_user.getData().get("notifications").toString());
                                if (notifChecker){
                                    notifSwitch.setChecked(true);
                                }
                                else if (!notifChecker){
                                    notifSwitch.setChecked(false);
                                }
                            }
                        } else {
                            Toast.makeText(UserActivity.this, "Invalid Age and/or Skin Type", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
            setAllObjectsFalse();

            getSupportActionBar().setTitle("App Settings");
        }

        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean catcherCheck = true;
                /**THIS IS WHERE THE NEW VALUES SHOULD BE ENTERED IF CHANGES ARE MADE, (DATABASE)*/

                if (catcherCheck) {
                    if (ageChecker <= 0 || ageChecker > 100) {
                        Toast.makeText(UserActivity.this, "INVALID AGE", Toast.LENGTH_SHORT).show();
                        editTextAge.setText(null);
                        /**SET ALL CHANGES BACK TO ORIGINAL SINCE AGE INPUT IS INVALID*/

                    } else if (!radioSkintype1.isChecked() && !radioSkintype2.isChecked() && !radioSkintype3.isChecked() &&
                            !radioSkintype4.isChecked() && !radioSkintype5.isChecked() && !radioSkintype6.isChecked()) {
                        Toast.makeText(UserActivity.this, "Please select a skin tone.", Toast.LENGTH_SHORT).show();
                    } else {
                        if (!haveNetworkConnection()) { // Locally, no internet
                            List<User> userAgeChange = dbhelper.getAllUserData();
                            for (int i = 0; i < userAgeChange.size(); i++) {
                                if (userAgeChange.get(i).getUsername().equals(usernameIntent)) {

                                    String userID = Integer.toString(userAgeChange.get(i).getUserId());
                                    String userUsername = userAgeChange.get(i).getUsername();
                                    String userPassword = userAgeChange.get(i).getPassword();
                                    int ageInteger = ageChecker;
                                    int skin_type = getSkinType();
                                    boolean userNotifications = getNotifs();
                                    boolean newUser = userAgeChange.get(i).getNewUser();

                                    dbhelper.updateData(userID, userUsername, userPassword, ageInteger, skin_type, userNotifications, newUser);

                                    setAllObjectsFalse();
                                    if (newuserregcheck == true) {
                                        newuserregcheck = true;
                                    } else if (newuserregcheck == false) {
                                        newuserregcheck = false;
                                    }
                                    if (!newuserregcheck) // user is not new so go back to main activity
                                        changeActivityWithIntent();
                                    else // user is new so he must login after registering
                                        goBackToLogin();
                                }
                            }
                        }
                        else { // Online
                            final CollectionReference users = fireStore.collection(DatabaseConfig.USER_TABLE_NAME);
                            users.whereEqualTo(DatabaseConfig.COLUMN_USERNAME, usernameIntent).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                    if (task.isSuccessful()) {
                                        for (QueryDocumentSnapshot document_user : task.getResult()) {
                                            users.document(document_user.getId()).update("notifications", getNotifs());
                                            users.document(document_user.getId()).update("age", ageChecker, "skin", getSkinType());

                                            List<User> userAgeChange = dbhelper.getAllUserData();
                                            for (int i = 0; i < userAgeChange.size(); i++) {
                                                if (userAgeChange.get(i).getUsername().equals(document_user.getData().get("username").toString())) {

                                                    String userID = Integer.toString(userAgeChange.get(i).getUserId());
                                                    String userUsername = userAgeChange.get(i).getUsername();
                                                    String userPassword = userAgeChange.get(i).getPassword();
                                                    int ageInteger = ageChecker;
                                                    int skin_type = getSkinType();
                                                    boolean userNotifications = getNotifs();
                                                    boolean newUser = userAgeChange.get(i).getNewUser();
                                                    dbhelper.updateData(userID, userUsername, userPassword, ageInteger, skin_type, userNotifications, newUser);
                                                }
                                            }
                                        }
                                    } else {
                                        Toast.makeText(UserActivity.this, "Invalid Age and/or Skin Type", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                            setAllObjectsFalse();
                            if (newuserregcheck == true) {
                                newuserregcheck = true;
                            } else if (newuserregcheck == false) {
                                newuserregcheck = false;
                            }

                            if (!newuserregcheck) // user is not new so go back to main activity
                                changeActivityWithIntent();
                            else // user is new so he must login after registering
                                goBackToLogin();
                        }
                    }
                }

            }
        });
        bluetoothButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                goToActivity(BluetoothActivity.class);
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) { /**OPTIONS ACTION BAR*/
        if (newuserregcheck == false) {
            MenuInflater inflater = getMenuInflater(); /**SHOWS 'BACK' AND 'EDIT MODE'*/
            inflater.inflate(R.menu.user_options, menu);

        }
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) /**FOR OPENING THE ACTION BAR*/
    {
        int menuId = item.getItemId();
        if (menuId == R.id.EditUserProfileItem) { // If we click on the ... button
            Toast.makeText(this, "Edit Mode Enabled", Toast.LENGTH_SHORT).show();
            setAllObjectsTrue();
        }
        else if (menuId == R.id.setTutorialOn) {
            int offlineTutorial = -1;
            for (int i = 0; i < userInfo.size(); i++){
                if (usernameIntent.equals(userInfo.get(i).getUsername())){
                    offlineTutorial = i;
                }
            }
            dbhelper.updateData(Integer.toString(userInfo.get(offlineTutorial).getUserId()), userInfo.get(offlineTutorial).getUsername(),
                    userInfo.get(offlineTutorial).getPassword(), userInfo.get(offlineTutorial).getAge(),userInfo.get(offlineTutorial).getSkin(),
                    userInfo.get(offlineTutorial).getNotifications(),true);
            newuserregcheck = true;
            changeActivityWithIntent();
        }
        else if (menuId == R.id.EditUserPassword) {
            if (haveNetworkConnection()) {
                ChangePasswordFragment dialog = new ChangePasswordFragment();
                Bundle bundle = new Bundle();
                bundle.putString("username", usernameIntent);
                bundle.putString("password", passwordIntent);
                bundle.putBoolean("hasInternet", haveNetworkConnection());
                dialog.setArguments(bundle);
                dialog.show(getSupportFragmentManager(), "ChangePasswordFragment");
            }
            else{
                Toast.makeText(this, "Internet is required for password change.", Toast.LENGTH_SHORT).show();
            }
        }
        else if (menuId == R.id.userLogOut) {
            Intent intent = new Intent(this, LoginActivity.class);
            intent.removeExtra("username");
            intent.removeExtra("password");
            intent.removeExtra("checknewuser");
            sharedPreferencesHelper.deleteProfile();
            Toast.makeText(UserActivity.this, "Logging out...", Toast.LENGTH_SHORT).show();
            startActivity(intent);
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    public void onBackPressed()
    {
        changeActivityWithIntent();
    }

    public void changeActivityWithIntent() {
        Intent intent = new Intent(UserActivity.this, MainActivity.class);
        intent.removeExtra("username");
        intent.removeExtra("password");
        intent.removeExtra("checknewuser");
        intent.putExtra("username", usernameIntent);
        intent.putExtra("password", passwordIntent);
        intent.putExtra("checknewuser", newuserregcheck);
        startActivity(intent);
        finish();
    }

    protected void goBackToLogin() {
        Intent intent = new Intent(UserActivity.this, LoginActivity.class);
        Toast.makeText(UserActivity.this, "Account created! You may now login.", Toast.LENGTH_SHORT).show();
        startActivity(intent);
        finish();
    }

    public void checkButton(View v) /**CHECKS WHICH SKIN COLOR HAS BEEN PRESSED*/
    {
        int radioId = radioGroup.getCheckedRadioButtonId();

        radioButton = findViewById(radioId); /**FINDS WHAT RADIO BUTTON (SKIN COLOR) HAS BEEN SELECTED*/
        radioButton.setId(radioGroup.getCheckedRadioButtonId()); /**GETS THIS VALUE AS ID SO IT CAN BE CHECKED AFTER SAVE*/

    }

    public void goToActivity(Class page) { // Function that goes from the main activity to another one
        Intent intent = new Intent(UserActivity.this, page); // from the main activity to the profile class
        startActivity(intent);
        /**I JUST SET IT TO MAIN ACTIVITY FOR NOW BUT IT SHOULD BE CHANGED TO THE HOME INTERFACE*/
    }
  
    public void setAllObjectsFalse()
    {
        radioGroup.setEnabled(false); /**SET TO UNEDITABLE */
        editTextAge.setEnabled(false);
        radioGroup.setClickable(false);
        notifSwitch.setClickable(false);
        notifSwitch.setEnabled(false);
        saveButton.setEnabled(false);
        radioSkintype1.setClickable(false);
        radioSkintype2.setClickable(false);
        radioSkintype3.setClickable(false);
        radioSkintype4.setClickable(false);
        radioSkintype5.setClickable(false);
        radioSkintype6.setClickable(false);
        radioSkintype1.setEnabled(false);
        radioSkintype2.setEnabled(false);
        radioSkintype3.setEnabled(false);
        radioSkintype4.setEnabled(false);
        radioSkintype5.setEnabled(false);
        radioSkintype6.setEnabled(false);
    }
  
    public void setAllObjectsTrue()
    {
        radioGroup.setEnabled(true); /**SET ALL BUTTONS AND INPUTS TO ENABLED*/
        radioGroup.setClickable(true);
        notifSwitch.setClickable(true);
        editTextAge.setEnabled(true);
        notifSwitch.setEnabled(true);
        saveButton.setEnabled(true);
        radioSkintype1.setClickable(true);
        radioSkintype2.setClickable(true);
        radioSkintype3.setClickable(true);
        radioSkintype4.setClickable(true);
        radioSkintype5.setClickable(true);
        radioSkintype6.setClickable(true);
        radioSkintype1.setEnabled(true);
        radioSkintype2.setEnabled(true);
        radioSkintype3.setEnabled(true);
        radioSkintype4.setEnabled(true);
        radioSkintype5.setEnabled(true);
        radioSkintype6.setEnabled(true);
    }

    protected boolean getNotifs(){
        boolean notifsReturn;
        notifsReturn = notifSwitch.isChecked();
        return notifsReturn;
    }

    protected void fetchNotifs(boolean Notifications){
        if (!Notifications){
            notifSwitch.setChecked(false);
        }
        else {
            notifSwitch.setChecked(true);
        }
    }

    protected int getSkinType() {
        int skin_type = 0;
        int radioButton = radioGroup.getCheckedRadioButtonId();
        if (radioButton == R.id.radioSkinTypeone)
            skin_type = 1;
        else if (radioButton == R.id.radioSkinTypetwo)
            skin_type = 2;
        else if (radioButton == R.id.radioSkinTypethree)
            skin_type = 3;
        else if (radioButton == R.id.radioSkinTypefour)
            skin_type = 4;
        else if (radioButton == R.id.radioSkinTypefive)
            skin_type = 5;
        else if (radioButton == R.id.radioSkinTypesix)
            skin_type = 6;
        return skin_type;
    }

    protected void fetchSkinType(int skin) {
        if (skin == 1)
            radioSkintype1.setChecked(true);
        else if (skin == 2)
            radioSkintype2.setChecked(true);
        else if (skin == 3)
            radioSkintype3.setChecked(true);
        else if (skin == 4)
            radioSkintype4.setChecked(true);
        else if (skin == 5)
            radioSkintype5.setChecked(true);
        else if (skin == 6)
            radioSkintype6.setChecked(true);
    }

    private void updateLabel() {
        String myFormat = "MM/dd/yyyy"; //In which you need put here
        SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.US);

        editTextAge.setText(sdf.format(myCalendar.getTime()));
    }

    public static int getAge(int year, int month, int day){
        LocalDate today = LocalDate.now();
        LocalDate birthday = LocalDate.of(year, month, day);

        long years = ChronoUnit.YEARS.between(birthday, today);
        int ageCalc = Math.toIntExact(years);

        return ageCalc;
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
