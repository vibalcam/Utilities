package com.utilities.vibal.utilities;

import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.os.Build;

public class App extends Application {
    public static final String CHANNEL_REMINDER_ID = "channelReminder";

    @Override
    public void onCreate() {
        super.onCreate();

        createNotificationChannels();
    }

    private void createNotificationChannels(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channelReminder = new NotificationChannel(CHANNEL_REMINDER_ID,
                    "Reminder Channel", NotificationManager.IMPORTANCE_DEFAULT);
            channelReminder.setDescription("Channel for user set reminders");

            getSystemService(NotificationManager.class).createNotificationChannel(channelReminder);
        }
    }
}
