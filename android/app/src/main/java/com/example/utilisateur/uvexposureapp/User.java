package com.example.utilisateur.uvexposureapp;

public class User {
    private int user_id;
    private String username;
    private String password;
    private int age;
    private int skin_type;
    private boolean notifications;
    private boolean new_user;

    public User(String username, String password, int age, int skin_type, boolean notifications, boolean new_user) {
        this.username = username;
        this.password = password;
        this.age = age;
        this.skin_type = skin_type;
        this.notifications = notifications;
        this.new_user = new_user;
    }

    public User(String username, String password, int age, int skin_type, boolean notifications) {
        this.username = username;
        this.password = password;
        this.age = age;
        this.skin_type = skin_type;
        this.notifications = notifications;
    }

    public User(String username, String password) {
        this.username = username;
        this.password = password;
    }

    /* set UserId*/
    public void setUserId(int user_id) {
        this.user_id = user_id;
    }

    /* Get UserId*/
    public int getUserId() {
        return user_id;
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

    /* set Skin type*/
    public void setSkin(int skin_type) {
        this.skin_type = skin_type;
    }

    /* get Skin type*/
    public int getSkin() {
        return skin_type;
    }

    /* Set notifications*/
    public void setNotifications(boolean notifications) {
        this.notifications = notifications;
    }

    /* Get notifications*/
    public boolean getNotifications() {
        return notifications;
    }

    /* Set new or old user*/
    public void setNewUser(boolean new_user) {
        this.new_user = new_user;
    }

    /* Get new or old user*/
    public boolean getNewUser() { return new_user; }
}
