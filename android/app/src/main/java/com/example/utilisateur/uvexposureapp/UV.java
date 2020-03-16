package com.example.utilisateur.uvexposureapp;

import java.time.*;

/*
* A UV Model to work with our MVC architecture
*/

public class UV {
    private int uvId;
    private int userId;
    private double uv;
    private LocalDateTime date;

    public UV (double uv, LocalDateTime date) {
        this.uv = uv;
        this.date = date;
    }

    /* set UVId*/
    public void setUvId(int uvid) {
        this.uvId = uvid;
    }

    /* Get UVId*/
    public int getUvId() {
        return uvId;
    }

    /* set UserId*/
    public void setUserId(int userid) {
        this.userId = userid;
    }

    /* Get UserId*/
    public int getUserId() {
        return userId;
    }

    /*set UV value */
    public void setUv(double uv) {
        this.uv = uv;
    }

    /*get UV value*/
    public double getUv() {
        return uv;
    }

    /*set current time and date */
    public void setDate(LocalDateTime date) {
        this.date = date;
    }

    /*get current time and date */
    public LocalDateTime getDate() {
        return date;
    }
}
