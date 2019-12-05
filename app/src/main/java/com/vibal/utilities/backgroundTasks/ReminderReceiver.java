package com.vibal.utilities.backgroundTasks;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.vibal.utilities.App;
import com.vibal.utilities.R;
import com.vibal.utilities.db.CashBoxInfo;
import com.vibal.utilities.db.UtilitiesDatabase;
import com.vibal.utilities.ui.cashBoxManager.CashBoxItemFragment;
import com.vibal.utilities.ui.cashBoxManager.CashBoxManagerActivity;
import com.vibal.utilities.util.LogUtil;

import java.util.Map;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

import static com.vibal.utilities.ui.cashBoxManager.CashBoxManagerActivity.EXTRA_CASHBOX_ID;

public class ReminderReceiver extends BroadcastReceiver {
    public static final String REMINDER_PREFERENCE = "com.vibal.utilities.NOTIFICATION_PREFERENCE";
    private static final String TAG = "PruebaReminderReceiver";

    public static void setAlarm(@NonNull AlarmManager alarmManager, Context context, long cashBoxId, long timeInMillis) {
        Intent intentAlarm = new Intent(context, ReminderReceiver.class);
        intentAlarm.putExtra(EXTRA_CASHBOX_ID, cashBoxId);
        alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, timeInMillis,
                PendingIntent.getBroadcast(context, CashBoxItemFragment.REMINDER_ID,
                        intentAlarm, 0));
    }

    public static void setBootReceiverEnabled(@NonNull Context context, boolean enable) {
        ComponentName receiver = new ComponentName(context, ReminderReceiver.class);
        PackageManager pm = context.getPackageManager();
        if (enable)
            pm.setComponentEnabledSetting(receiver, PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                    PackageManager.DONT_KILL_APP);
        else
            pm.setComponentEnabledSetting(receiver, PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                    PackageManager.DONT_KILL_APP);
    }

    @Override
    public void onReceive(@NonNull Context context, @NonNull Intent intent) {
        String action = intent.getAction();
        SharedPreferences sharedPref = context.getSharedPreferences(REMINDER_PREFERENCE,
                Context.MODE_PRIVATE);
        //If reboot completed, reset the alarms
        if (action != null && action.equals(Intent.ACTION_BOOT_COMPLETED)) {
            LogUtil.debug(TAG, "Rescheduling alarms");
            AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            //Intent intentAlarm;
            for (Map.Entry<String, ?> entry : sharedPref.getAll().entrySet()) {
                if (!(entry.getValue() instanceof Long)) {
                    sharedPref.edit().remove(entry.getKey()).apply();
                    break;
                }
                setAlarm(alarmManager, context, Long.parseLong(entry.getKey()), (Long) entry.getValue());

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
                        intentNotif.putExtra(CashBoxManagerActivity.EXTRA_ACTION, CashBoxManagerActivity.ACTION_DETAILS);
                        intentNotif.putExtra(CashBoxManagerActivity.EXTRA_CASHBOX_ID, cashBoxId);

                        Notification notification = new NotificationCompat.Builder(context,
                                App.CHANNEL_REMINDER_ID)
                                .setSmallIcon(R.drawable.logo)
                                .setContentTitle("Reminder CashBox " + cashBox.getName())
                                .setContentText("Total cash: " + cashBox.getCash())
                                .setPriority(NotificationCompat.PRIORITY_HIGH)
                                .setCategory(NotificationCompat.CATEGORY_REMINDER)
                                .setContentIntent(PendingIntent.getActivity(context, 0, intentNotif, 0))
                                .setAutoCancel(true)
                                .setOnlyAlertOnce(true)
                                .setGroup(CashBoxManagerActivity.NOTIFICATION_GROUP_KEY_CASHBOX)
                                .build();

                        //Show notification
                        NotificationManagerCompat.from(context).notify(CashBoxItemFragment.REMINDER_ID, notification);
                    });

            //Remove reminder from SharedPreferences
            sharedPref.edit().remove(Long.toString(cashBoxId)).apply();
        }
    }
}
