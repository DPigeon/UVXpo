package com.example.utilisateur.uvexposureapp;

import java.util.ArrayList;

public class User {
    private int userId;
    private String username;
    private String password;
    private int age;
    private ArrayList<UV> uvData; // A user will have stored uv values tracked
    private int skin;
    private boolean notifications;

    public User (String username, String password, int age, ArrayList<UV> uv, int skin, boolean notifications) {
        this.username = username;
        this.password = password;
        this.age = age;
        this.uvData = uv;
        this.skin = skin;
        this.notifications = notifications;
    }

    public User (String username, String password, int age, int skin, boolean notifications) {
        this.username = username;
        this.password = password;
        this.age = age;
        this.skin = skin;
        this.notifications = notifications;
    }

    public User (String username, String password)
        {
        this.username = username;
        this.password = password;
    }

    /* set UserId*/
    public void setUserId(int userid) {
        this.userId = userid;
    }

    /* Get UserId*/
    public int getUserId() {
        return userId;
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

    /* set uv value*/
    public void setUv(UV uv) {
        this.uvData.add(uv);
    }

    /* get uv values*/
    public ArrayList<UV> getUvData() {
        return uvData;
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
    public void setNotifications(boolean notifications) {
        this.notifications = notifications;
    }

    /* Get notifications*/
    public boolean getNotifications() {
        return notifications;
    }
}
