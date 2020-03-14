package com.example.coenelec390_uv_exposure.Database;

import android.content.Context;
import android.content.SharedPreferences;

public class SharedPreferenceHelper {

    /** SharedPreferenceHelper class for storing/accessing username, password, skintone, and age */

    private SharedPreferences sharedPreferences;
    public SharedPreferences.Editor mEditor;

    public SharedPreferenceHelper(Context context){
        sharedPreferences = context.getSharedPreferences("Profile",Context.MODE_PRIVATE);
    }

    public void setUserData(User user) {        /** Set user data for all attributes */
        mEditor = sharedPreferences.edit();
        mEditor.putString("Username", user.Username);
        mEditor.putString("Password", user.Password);
        mEditor.putString("Skintone", user.Skintone);
        mEditor.putInt("Age", user.Age);
        mEditor.commit();
    }

    public User getUserData(User user){         /** Return current User attributes */
        String Username = sharedPreferences.getString("Username", "");
        String Password = sharedPreferences.getString("Password", "");
        String Skintone = sharedPreferences.getString("Skintone", "");
        int Age = sharedPreferences.getInt("Age", -1);

        User currentUser = new User(Username, Password, Skintone, Age);
        return currentUser;
    }

    public void deleteUser(){                   /** Delete all of the current user's attributes */
        mEditor = sharedPreferences.edit();
        mEditor.clear();
        mEditor.commit();
    }

    public void setUsername(String username){   /** Set user username */
        mEditor = sharedPreferences.edit();
        mEditor.putString("Username", username);
        mEditor.commit();
    }
    public String getUsername(){                /** Return user username */
        return sharedPreferences.getString("Username", null);
    }

    public void setPassword(String password){   /** Set user password */
        mEditor = sharedPreferences.edit();
        mEditor.putString("Password", password);
        mEditor.commit();
    }
    public String getPassword(){                /** Return user password */
        return sharedPreferences.getString("Password", null);
    }

    public void setSkintone(String skintone){   /** Set user skintone */
        mEditor = sharedPreferences.edit();
        mEditor.putString("Skintone", skintone);
        mEditor.commit();
    }
    public String getSkintone(){                /** Return user skintone */
        return sharedPreferences.getString("Skintone", null);
    }

    public void setAge(int age){                /** Set user age */
        mEditor = sharedPreferences.edit();
        mEditor.putInt("Age", age);
        mEditor.commit();
    }
    public int getAge(){                        /** Return user age */
        return sharedPreferences.getInt("Age", -1);
    }

}
