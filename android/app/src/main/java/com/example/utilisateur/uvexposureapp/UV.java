package com.example.utilisateur.uvexposureapp;

/*
* A UV Model to work with our MVC architecture
*/

public class UV {
    private int uvId;
    private String uv_user_id;
    private double uv_time;
    private double uv_value;
    private String date;

    public UV (String uv_user_id, double uv_time, double uv_value, String date) {
        this.uv_user_id = uv_user_id;
        this.uv_time = uv_time;
        this.uv_value = uv_value;
        this.date = date;
    }

    /* set UVId*/
    public void setUvId(int uvId) {
        this.uvId = uvId;
    }

    /* Get UVId*/
    public int getUvId() {
        return uvId;
    }

    /* set UserId*/
    public void setUserId(String userId) {
        this.uv_user_id = userId;
    }

    /* Get UserId*/
    public String getUserId() {
        return uv_user_id;
    }

    /*set UV time value */
    public void setUvTime(double time) {
        this.uv_time = time;
    }

    /*get UV time value*/
    public double getUvTime() {
        return uv_time;
    }

    /*set UV value */
    public void setUv(double uv) {
        this.uv_value = uv;
    }

    /*get UV value*/
    public double getUv() {
        return uv_value;
    }

    /*set current time and date */
    public void setDate(String date) {
        this.date = date;
    }

    /*get current time and date */
    public String getDate() {
        return date;
    }
}
