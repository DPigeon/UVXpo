package com.example.coenelec390_uv_exposure.Database;

public class User {       /** User config class */

    public String Username;
    public String Password;
    public String Skintone;
    public int Age;

    public User(String Username, String Password, String Skintone, int Age){    /** Creating a user when all details are known */
        this.Username = Username;
        this.Password = Password;
        this.Skintone = Skintone;
        this.Age = Age;
    }

    public User(String Username, String Password){      /** Creating a user when only username and password are known */
        this.Username = Username;
        this.Password = Password;
        this.Skintone = "";
        this.Age = -1;
    }
}
