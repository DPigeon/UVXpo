package com.example.utilisateur.uvexposureapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
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
import android.widget.Toast;

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

        radioGroup.setEnabled(false); /**SET TO UNEDITABLE UNTIL EDIT MODE IS SET ON*/
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


        /**STILL NEED TO ADD CALLS THAT BRING RADIOBUTTON VALUE IN, AND AGE, AND NOTIF*/

        saveButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                radioGroup.setEnabled(false); /**SET TO UNEDITABLE WHEN SAVE IS CLICKED*/
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
                /**THIS IS WHERE THE NEW VALUES SHOULD BE ENTERED IF CHANGES ARE MADE, (DATABASE)*/
                if (editTextAge.getText() == null || Integer.parseInt(editTextAge.getText().toString()) <= 0)
                {
                    /**PUSH NEW AGE TO DATABASE*/
                }
                else
                {
                    Toast.makeText(UserActivity.this, "INVALID AGE", Toast.LENGTH_SHORT).show();
                    /**SET ALL CHANGES BACK TO ORIGINAL SINCE AGE INPUT IS INVALID*/
                }
                switch (radioButton.getId())
                {
                    case R.id.radioSkinTypeone:
                        /**PUSH NEW SKIN COLOR TO DATABASE*/
                    case R.id.radioSkinTypetwo:
                        /**PUSH NEW SKIN COLOR TO DATABASE*/
                    case R.id.radioSkinTypethree:
                        /**PUSH NEW SKIN COLOR TO DATABASE*/
                    case R.id.radioSkinTypefour:
                        /**PUSH NEW SKIN COLOR TO DATABASE*/
                    case R.id.radioSkinTypefive:
                        /**PUSH NEW SKIN COLOR TO DATABASE*/
                    case R.id.radioSkinTypesix:
                        /**PUSH NEW SKIN COLOR TO DATABASE*/
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
        MenuInflater inflater = getMenuInflater(); /**SHOWS 'BACK' AND 'EDIT MODE'*/
        inflater.inflate(R.menu.user_options, menu);
        return true;

    }
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) /**FOR OPENING THE ACTION BAR*/
    {
        switch(item.getItemId())
        {
            case R.id.EditUserProfileItem: /**IF 'EDIT MODE' IS PRESSED, LET USER EDIT INPUTS*/
                Toast.makeText(this, "Edit Mode Enabled", Toast.LENGTH_SHORT).show();
                radioGroup.setEnabled(true); /**SET ALL BUTTONS AND INPUTS TO ENABLED SO USER CAN CHANGE*/
                radioGroup.setClickable(true);
                notifSwitch.setClickable(true);
                editTextAge.setEnabled(true);
                notifSwitch.setEnabled(true);
                saveButton.setEnabled(false); // Not available for now (but later yes)
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
                return true;
            case R.id.EditUserPassword:
                ChangePasswordFragment dialog = new ChangePasswordFragment();
                dialog.show(getSupportFragmentManager(), "ChangePasswordFragment");

        }
        return super.onOptionsItemSelected(item); /**RETURNS ITEM CLICKED*/
    }
    public void checkButton(View v) /**CHECKS WHICH SKIN COLOR HAS BEEN PRESSED*/
    {
        int radioId = radioGroup.getCheckedRadioButtonId();

        radioButton = findViewById(radioId); /**FINDS WHAT RADIO BUTTON (SKIN COLOR) HAS BEEN SELECTED*/
        radioButton.setId(radioGroup.getCheckedRadioButtonId()); /**GETS THIS VALUE AS ID SO IT CAN BE CHECKED AFTER SAVE*/

    }

    void goToActivity(Class page) { // Function that goes from the main activity to another one
        Intent intent = new Intent(UserActivity.this, page); // from the main activity to the profile class
        startActivity(intent);
    }
}
