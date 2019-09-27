package com.utilities.vibal.utilities;

import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.os.Build;

public class App extends Application {
    public static final String CHANNEL_REMINDER_ID = "com.utilities.vibal.utilities.App.CHANNEL_REMINDER";
    public static final String CHANNEL_PERIODIC_ID = "com.utilities.vibal.utilities.App.CHANNEL_PERIODIC";

    @Override
    public void onCreate() {
        super.onCreate();
        createNotificationChannels();
    }

    private void createNotificationChannels(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            //Reminder Channel
            NotificationChannel channelReminder = new NotificationChannel(CHANNEL_REMINDER_ID,
                    "Reminder Channel", NotificationManager.IMPORTANCE_HIGH);
            channelReminder.setDescription("Channel for user set reminders");

            //Periodic Channel
            NotificationChannel channelPeriodic = new NotificationChannel(CHANNEL_PERIODIC_ID,
                    "Periodic Channel", NotificationManager.IMPORTANCE_LOW);
            channelPeriodic.setDescription("Channel to notify user of the execution of a periodic task");

            //Create channels
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channelReminder);
            notificationManager.createNotificationChannel(channelPeriodic);
        }
    }
}
