package com.example.utilisateur.uvexposureapp;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

/*
 * Created by David from the assignment 1 page
 * The SharedPreferenceHelper Class
 * A controller in the MVC structure
 */

public class SharedPreferencesHelper {
    private SharedPreferences sharedPreferences;

    public SharedPreferencesHelper(Context context) { // Constructor
        sharedPreferences = context.getSharedPreferences("ProfilePreference", Context.MODE_PRIVATE);
    }

    // For the MainActivity
    public void saveProfile(User profile)  { // Save the profile (Setter)
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("ProfileName", profile.getUsername());
        editor.putString("ProfilePass", profile.getPassword());
        editor.putInt("ProfileAge", profile.getAge());
        editor.putInt("ProfileSkin", profile.getSkin());
        editor.putBoolean("ProfileNotif", profile.getNotifications());
        editor.putBoolean("ProfileNewUser", profile.getNewUser());
        editor.putInt("ProfileId", profile.getUserId());

        editor.apply(); // Using apply instead of commit now
    }

    public User getProfile() { // Getter for profile with different keys
        String name = sharedPreferences.getString("ProfileName", null);
        String password = sharedPreferences.getString("ProfilePass", null);
        int age = sharedPreferences.getInt("ProfileAge", 0);
        int skin = sharedPreferences.getInt("ProfileSkin", 0);
        boolean notifications = sharedPreferences.getBoolean("ProfileNotif", true);
        boolean newUser = sharedPreferences.getBoolean("ProfileNewUser", true);
        int id = sharedPreferences.getInt("ProfileId", 0);
        return new User(name, password, age, skin, notifications, newUser);
    }
}
