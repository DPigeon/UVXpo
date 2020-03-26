package com.example.utilisateur.uvexposureapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
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
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.List;

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

    Boolean newuserregcheck = false;
    String usernameIntent;
    String passwordfornewuser;
    DatabaseHelper dbhelper;
    FirebaseFirestore fireStore;
    Cursor userInfoAllData;
    List<User> userInfo;


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
        userInfoAllData = dbhelper.getData();
        userInfo = dbhelper.getAllUserData();

        try {
            Bundle bndset = getIntent().getExtras();
            newuserregcheck = bndset.getBoolean("checknewuser"); /**INTENT RETRIEVAL*/
            usernameIntent = bndset.getString("username");
        } catch (Exception exception) {
            Log.d("Error: ", exception.toString());
        }

        if (newuserregcheck == false) /**IF USER IS NOT NEW DATA WILL BE ADDED TO OBJECTS*/
        {

            //setAllValues();


            for (int i = 0; i < userInfo.size(); i++)
            {
                if (userInfo.get(i).getUsername().equals(usernameIntent))
                {
                    int numintcheck = userInfo.get(i).getAge();
                    String numintToString = Integer.toString(numintcheck);
                    editTextAge.setText(numintToString);
                    switch (userInfo.get(i).getSkin()) {
                        case 1:
                            radioSkintype1.setChecked(true);
                        case 2:
                            radioSkintype2.setChecked(true);
                        case 3:
                            radioSkintype3.setChecked(true);
                        case 4:
                            radioSkintype4.setChecked(true);
                        case 5:
                            radioSkintype5.setChecked(true);
                        case 6:
                            radioSkintype6.setChecked(true);
                    }
                    Toast.makeText(UserActivity.this, "Accessed User Account " + usernameIntent + " Age " + numintcheck, Toast.LENGTH_SHORT).show();
                }
            }
            setAllObjectsFalse();

        }

        saveButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {

                List<User> userAgeChange = dbhelper.getAllUserData();
                boolean catcherCheck = true;

                /**THIS IS WHERE THE NEW VALUES SHOULD BE ENTERED IF CHANGES ARE MADE, (DATABASE)*/

                String ifTextisNum  = editTextAge.getText().toString();
                try
                {
                    int num = Integer.parseInt(ifTextisNum);
                }
                catch (NumberFormatException e)
                {
                    editTextAge.setText(null);
                    catcherCheck = false;
                    Toast.makeText(UserActivity.this, "Invalid Age", Toast.LENGTH_SHORT).show();
                }
                if (catcherCheck == true) {
                    if (parseInt(editTextAge.getText().toString()) <= 0) {
                        Toast.makeText(UserActivity.this, "INVALID AGE", Toast.LENGTH_SHORT).show();
                        editTextAge.setText(null);
                        /**SET ALL CHANGES BACK TO ORIGINAL SINCE AGE INPUT IS INVALID*/

                    } else if (!radioSkintype1.isChecked() && !radioSkintype2.isChecked() && !radioSkintype3.isChecked() &&
                            !radioSkintype4.isChecked() && !radioSkintype5.isChecked() && !radioSkintype6.isChecked()) {
                        Toast.makeText(UserActivity.this, "Please select a skin tone.", Toast.LENGTH_SHORT).show();
                    } else {
                        if (!haveNetworkConnection()) { // Locally, no internet
                            for (int i = 0; i < userAgeChange.size(); i++) {
                                if (userAgeChange.get(i).getUsername().equals(usernameIntent)) {

                                    int userID = userAgeChange.get(i).getUserId();
                                    int oldSkinType = userAgeChange.get(i).getSkin();
                                    int oldAge = userAgeChange.get(i).getAge();

                                    int ageInteger = parseInt(editTextAge.getText().toString());
                                    int skin_type = getSkinType();

                                    dbhelper.updateSkin(userID, skin_type, oldSkinType); /** UPDATE SKIN*/
                                    dbhelper.updateAge(userID, ageInteger, oldAge); /**UPDATE AGE FUNCTION*/

                                    setAllObjectsFalse();
                                    if (newuserregcheck == true) {
                                        newuserregcheck = true;
                                    } else if (newuserregcheck == false) {
                                        newuserregcheck = false;
                                    }

                                    Intent intent = new Intent(UserActivity.this, MainActivity.class);
                                    intent.removeExtra("username");
                                    intent.removeExtra("checknewuser");
                                    intent.putExtra("username", usernameIntent);
                                    intent.putExtra("checknewuser", newuserregcheck);
                                    startActivity(intent);
                                    finish();
                                }
                            }
                        } else { // Online
                            final CollectionReference users = fireStore.collection(DatabaseConfig.USER_TABLE_NAME);
                            users.whereEqualTo(DatabaseConfig.COLUMN_USERNAME, usernameIntent).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                    if (task.isSuccessful()) {
                                        for (QueryDocumentSnapshot document_user : task.getResult()) {
                                            users.document(document_user.getId()).update("age", parseInt(editTextAge.getText().toString()), "skin", getSkinType());
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

                            Intent intent = new Intent(UserActivity.this, MainActivity.class);
                            intent.removeExtra("username");
                            intent.removeExtra("checknewuser");
                            intent.putExtra("username", usernameIntent);
                            intent.putExtra("checknewuser", newuserregcheck);
                            startActivity(intent);
                            finish();
                        }
                    }
                }


                /**THE FUNCTION BELOW IS TO CHECK FOR NOTIFICATION ON OFF CHANGES*/
                notifSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        if (isChecked == true)
                        {
                            /**WE NEED TO CREATE NOTIFICATIONS MANAGER FIRST, TO ENABLE OR DISABLE IT*/
                        }
                        else
                        {
                            /**WE NEED TO CREATE NOTIFICATIONS MANAGER FIRST, TO ENABLE OR DISABLE IT*/
                        }
                    }
                });

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
            newuserregcheck = true;
            Intent intent2 = new Intent(this, MainActivity.class);
            intent2.removeExtra("checknewuser");
            intent2.removeExtra("username");
            intent2.putExtra("username", usernameIntent);
            intent2.putExtra("checknewuser", newuserregcheck);
            startActivity(intent2);
            finish();
        }
        else if (menuId == R.id.EditUserPassword) {
            ChangePasswordFragment dialog = new ChangePasswordFragment();
            Bundle bundle = new Bundle();
            bundle.putString("username", usernameIntent);
            dialog.setArguments(bundle);
            dialog.show(getSupportFragmentManager(), "ChangePasswordFragment");
        }
        else if (menuId == R.id.userLogOut) {
            Intent intent = new Intent(this, LoginActivity.class);
            intent.removeExtra("username");
            intent.removeExtra("checknewuser");
            startActivity(intent);
        }

        return super.onOptionsItemSelected(item);
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
