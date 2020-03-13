package com.example.utilisateur.uvexposureapp;

import java.time.*;

/*
* A UV Model to work with our MVC architecture
*/

public class UV {
    private double uv;
    private LocalDateTime date;

    public UV (double uv, LocalDateTime date){
        this.uv = uv;
        this.date = date;
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
