package com.example.utilisateur.uvexposureapp;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import java.time.*;

public class User {

    private String name;
    private String username;
    private String password;
    private int age;
    private double uv;
    private int skin;
    private String notifications;
    private LocalDateTime date;

    public User (String name,String username, String password,int age,double uv,int skin, String notifications,LocalDateTime date){

        this.name=name;
        this.username=username;
        this.password=password;
        this.age=age;

        this.uv=uv;
        this.skin=skin;
        this.notifications=notifications;
        this.date=date;

    }


    /*  Set name*/
    public void setName(String name) {
        this.name = name;
    }
    /* Get name*/
    public String getName() {
        return name;

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
    /* Set name*/
    public void setNotifications(String notifications) {
        this.notifications = notifications;
    }
    /* Get name*/
    public String getNotifications() {
        return notifications;
    }
    /* Set time and  date*/
    public void setDate(LocalDateTime date) {
        this.date = date;

    }
    /* Get time and date*/
    public LocalDateTime getDate() {
        return date;
    }
}
