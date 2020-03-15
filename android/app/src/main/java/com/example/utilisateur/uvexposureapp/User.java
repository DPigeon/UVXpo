package com.example.utilisateur.uvexposureapp;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import java.time.*;

public class User {


    private String username;
    private String password;
    private int age;
    public double uv;
    private int skin;
    private String notifications;
    

    public User (String username, String password,int age,double uv,int skin, String notifications){

      
        this.username=username;
        this.password=password;
        this.age=age;

        this.uv=uv;
        this.skin=skin;
        this.notifications=notifications;
       

    }


    /* set Username*/
    public void setUsername(String username) {
        this.username = username;
    }
    /* Get Username*/
    public String getUsername() {
        return username;
    }
    /* Set Password*/
    public void setPassword(String password) {
        this.password = password;
    }
    /* Get Password*/
    public String getPassword() {
        return password;
    }
    /* Set age*/
    public void setAge(int age) {
        this.age = age;
    }
    /* Get age*/
    public int getAge() {
        return age;
    }
    /* Get uv value*/
    public void setUv(double uv) {
        this.uv = uv;
    }
    /* set uv value*/
    public double getUv() {
        return uv;
    }
    /* set Skin type*/
    public void setSkin(int skin) {
        this.skin = skin;
    }
    /* get Skin type*/
    public int getSkin() {
        return skin;
    }
    /* Set notifications*/
    public void setNotifications(String notifications) {
        this.notifications = notifications;
    }
    /* Get notifications*/
    public String getNotifications() {
        return notifications;
    }
 
}
