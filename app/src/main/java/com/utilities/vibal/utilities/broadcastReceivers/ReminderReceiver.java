package com.utilities.vibal.utilities.broadcastReceivers;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.utilities.vibal.utilities.App;
import com.utilities.vibal.utilities.R;
import com.utilities.vibal.utilities.db.CashBoxInfo;
import com.utilities.vibal.utilities.db.UtilitiesDatabase;
import com.utilities.vibal.utilities.models.CashBox;
import com.utilities.vibal.utilities.ui.cashBoxManager.CashBoxItemFragment;
import com.utilities.vibal.utilities.ui.cashBoxManager.CashBoxManagerActivity;

import java.util.Map;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

import static com.utilities.vibal.utilities.ui.cashBoxManager.CashBoxManagerActivity.EXTRA_CASHBOX_ID;

public class ReminderReceiver extends BroadcastReceiver {
    public static final String PREFERENCE_KEY = "com.utilities.vibal.utilities.NOTIFICATION_PREFERENCE";

    public static void setAlarm(AlarmManager alarmManager, Context context, long cashBoxId, long timeInMillis) {
        Intent intentAlarm = new Intent(context, ReminderReceiver.class);
        intentAlarm.putExtra(EXTRA_CASHBOX_ID, cashBoxId);
        alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, timeInMillis,
                PendingIntent.getBroadcast(context, CashBoxItemFragment.REMINDER_ID,
                        intentAlarm, 0));
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        //If reboot completed, reset the alarms
        if(action!=null && action.equals(Intent.ACTION_BOOT_COMPLETED)) {
            SharedPreferences sharedPref = context.getSharedPreferences(PREFERENCE_KEY,
                    Context.MODE_PRIVATE);
            AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            //Intent intentAlarm;
            for (Map.Entry<String, ?> entry : sharedPref.getAll().entrySet()) {
                if(!(entry.getValue() instanceof Long)) {
                    sharedPref.edit().remove(entry.getKey()).apply();
                    break;
                }
                setAlarm(alarmManager,context,Long.parseLong(entry.getKey()),(Long) entry.getValue());

//                intentAlarm = new Intent(context, ReminderReceiver.class);
//                intentAlarm.putExtra(EXTRA_CASHBOX_ID, Long.parseLong(entry.getKey()));
//                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, (Long) entry.getValue(),
//                        PendingIntent.getBroadcast(context, CashBoxItemFragment.REMINDER_ID,
//                                intentAlarm, 0));
            }
        } else { //Show notification reminder
            long cashBoxId = intent.getLongExtra(EXTRA_CASHBOX_ID, CashBoxInfo.NO_CASHBOX);

            //Get info of the CashBox
            Disposable disposable = UtilitiesDatabase.getInstance(context).cashBoxDao()
                    .getCashBoxById(cashBoxId)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(cashBox -> {
                        //Set up notification
                        Intent intentNotif = new Intent(context, CashBoxManagerActivity.class);
                        intentNotif.putExtra(CashBoxManagerActivity.EXTRA_ACTION,CashBoxManagerActivity.ACTION_DETAILS);
                        intentNotif.putExtra(CashBoxManagerActivity.EXTRA_CASHBOX_ID,cashBoxId);

                        Notification notification = new NotificationCompat.Builder(context,
                                App.CHANNEL_REMINDER_ID)
                                .setSmallIcon(R.drawable.logo)
                                .setContentTitle("Reminder CashBox " + cashBox.getName())
                                .setContentText("Total cash: " + cashBox.getCash())
                                .setPriority(NotificationCompat.PRIORITY_HIGH)
                                .setCategory(NotificationCompat.CATEGORY_REMINDER)
                                .setContentIntent(PendingIntent.getActivity(context,0,intentNotif,0))
                                .setAutoCancel(true)
                                .setOnlyAlertOnce(true)
                                .setGroup(CashBoxManagerActivity.GROUP_KEY_CASHBOX)
                                .build();

                        //Show notification
                        NotificationManagerCompat.from(context).notify(CashBoxItemFragment.REMINDER_ID,notification);
                    });
        }
    }
}
