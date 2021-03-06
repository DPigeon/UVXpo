package com.example.utilisateur.uvexposureapp;

import android.app.Application;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.os.Build;

import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

public class Notifications extends Application {
    public static final String CHANNELID_1 = "channel1";
    public static final String CHANNELID_2 = "channel2";
    private NotificationManagerCompat notificationManagerCompat;
    @Override
    public void onCreate() {
        super.onCreate();
        createChannels();
    }

    public void createChannels () {
        if (Build.VERSION.SDK_INT>=Build.VERSION_CODES.O) {
            NotificationChannel channel1 = new NotificationChannel(CHANNELID_1,"channel 1", NotificationManager.IMPORTANCE_DEFAULT);
            channel1.setDescription("HIGH UV radiation!!");
            NotificationChannel channel2 = new NotificationChannel(CHANNELID_2,"channel 2", NotificationManager.IMPORTANCE_DEFAULT);
            channel2.setDescription("Apply Sunscreen");

            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel1);

            manager.createNotificationChannel(channel2);
        }
    }
}
