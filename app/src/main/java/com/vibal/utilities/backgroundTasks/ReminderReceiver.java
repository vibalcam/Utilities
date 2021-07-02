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
import androidx.annotation.StringDef;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.vibal.utilities.App;
import com.vibal.utilities.R;
import com.vibal.utilities.persistence.db.CashBoxBaseDao;
import com.vibal.utilities.persistence.db.UtilitiesDatabase;
import com.vibal.utilities.ui.cashBoxManager.CashBoxItemFragment;
import com.vibal.utilities.ui.cashBoxManager.CashBoxManagerActivity;
import com.vibal.utilities.ui.cashBoxManager.CashBoxType;
import com.vibal.utilities.util.LogUtil;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.Calendar;
import java.util.Map;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class ReminderReceiver extends BroadcastReceiver {
    private static final String SEPARATOR = ":";
    public static final String REMINDER_PREFERENCE = "com.vibal.utilities.NOTIFICATION_PREFERENCE";
    public static final String ACTION_REMINDER = "com.vibal.utilities.ACTION_REMINDER" + SEPARATOR + "%s";

    // Reminder types
    public static final String ONLINE = "%d" + SEPARATOR + "online";    // to be completed with CashBox id
    public static final String LOCAL = "%d";      // to be completed with CashBox id

    @StringDef({ONLINE, LOCAL})
    @Retention(RetentionPolicy.SOURCE)
    public @interface ReminderType {
    }

    private static final String TAG = "PruebaReminderReceiver";

    private static SharedPreferences getSharedPreferences(@NonNull Context context) {
        return context.getSharedPreferences(REMINDER_PREFERENCE, Context.MODE_PRIVATE);
    }

    public static boolean hasAlarm(@NonNull Context context, long cashBoxId,
                                   @ReminderType String reminderType) {
        return getSharedPreferences(context)
                .contains(String.format(reminderType, cashBoxId));
    }

    public static long getTimeInMillis(@NonNull Context context, long cashBoxId,
                                       @ReminderType String reminderType, long defaultValue) {
        return getSharedPreferences(context)
                .getLong(String.format(reminderType, cashBoxId), defaultValue);
    }

    private static PendingIntent createReminderIntent(Context context, String cashBoxExtra) {
        Intent intentAlarm = new Intent(context, ReminderReceiver.class);
        intentAlarm.setAction(String.format(ACTION_REMINDER, cashBoxExtra));

        return PendingIntent.getBroadcast(context, CashBoxItemFragment.REMINDER_ID,
                intentAlarm, 0);
    }

    private static String getCashBoxExtraFromAction(@NonNull String action) {
        return action.split(SEPARATOR, 2)[1];
    }

    /**
     * Sets a new alarm
     *
     * @return true if the alarm was set, false otherwise (time given has already passed)
     */
    public static boolean setAlarm(@NonNull AlarmManager alarmManager, @NonNull Context context,
                                   long cashBoxId, long timeInMillis, @ReminderType String reminderType) {
        String reminderExtra = String.format(reminderType, cashBoxId);
        if (!setAlarmManager(alarmManager, context, timeInMillis, reminderExtra))
            return false;

        //Enable boot receiver
        ReminderReceiver.setBootReceiverEnabled(context, true);
        //Add reminder to Notification SharedPreferences
        SharedPreferences sharedPref = getSharedPreferences(context);
        sharedPref.edit().putLong(reminderExtra, timeInMillis).apply();
        return true;
    }

    /**
     * Sets the alarm in the alarm manager
     *
     * @return true if the alarm was set, false otherwise (time given has already passed)
     */
    private static boolean setAlarmManager(@NonNull AlarmManager alarmManager, Context context,
                                           long timeInMillis, String reminderExtra) {
        // Do not set alarm if time is after current, instead call notification
        if (Calendar.getInstance().getTimeInMillis() >= timeInMillis) {
            showReminderNotification(context, reminderExtra);
            return false;
        }

        alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC, timeInMillis,
                createReminderIntent(context, reminderExtra));
        return true;
    }

    public static void cancelAlarm(@NonNull AlarmManager alarmManager, @NonNull Context context,
                                   long cashBoxId, @ReminderType String reminderType) {
        // Delete reminder from Notifications SharedPreference
        SharedPreferences sharedPref = getSharedPreferences(context);
        String reminderExtra = String.format(reminderType, cashBoxId);
        sharedPref.edit().remove(reminderExtra).apply();

        alarmManager.cancel(createReminderIntent(context, reminderExtra));

        //Disable boot receiver if there are no other alarms
        if (sharedPref.getAll().isEmpty())
            ReminderReceiver.setBootReceiverEnabled(context, false);
    }

    private static void setBootReceiverEnabled(@NonNull Context context, boolean enable) {
        LogUtil.debug(TAG, "Enable receiver " + enable);
        ComponentName receiver = new ComponentName(context, ReminderReceiver.class);
        PackageManager pm = context.getPackageManager();
        int enabled = pm.getComponentEnabledSetting(receiver);
        if (enable && enabled != PackageManager.COMPONENT_ENABLED_STATE_ENABLED)
            pm.setComponentEnabledSetting(receiver, PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                    PackageManager.DONT_KILL_APP);
        else if (!enable && enabled != PackageManager.COMPONENT_ENABLED_STATE_DISABLED)
            pm.setComponentEnabledSetting(receiver, PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                    PackageManager.DONT_KILL_APP);
    }

    private static void showReminderNotification(@NonNull Context context, String cashBoxExtra) {
        //Remove reminder from SharedPreferences
        SharedPreferences sharedPref = getSharedPreferences(context);
        sharedPref.edit().remove(cashBoxExtra).apply();

        if (sharedPref.getAll().isEmpty())
            setBootReceiverEnabled(context, false);

        // Get cashbox type
        String[] extraSplit = cashBoxExtra.split(SEPARATOR);
        @CashBoxType.Type int cashBoxType = extraSplit.length == 1 ?
                CashBoxType.LOCAL : CashBoxType.ONLINE;

        //Get info of the CashBox
        UtilitiesDatabase database = UtilitiesDatabase.getInstance(context);
        CashBoxBaseDao dao = cashBoxType == CashBoxType.LOCAL
                ? database.cashBoxLocalDao() : database.cashBoxOnlineDao();
        long cashBoxId = Long.parseLong(extraSplit[0]);

        Disposable disposable = dao
                .getCashBoxById(cashBoxId, database.cashBoxEntryLocalDao())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(cashBox -> {
                    //Set up notification
                    Intent intentNotif = new Intent(context, CashBoxManagerActivity.class);
                    intentNotif.putExtra(CashBoxManagerActivity.EXTRA_ACTION, CashBoxManagerActivity.ACTION_DETAILS);
                    intentNotif.putExtra(CashBoxManagerActivity.EXTRA_CASHBOX_ID, cashBoxId);
                    intentNotif.putExtra(CashBoxManagerActivity.EXTRA_CASHBOX_TYPE, cashBoxType);

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
    }

    @Override
    public void onReceive(@NonNull Context context, @NonNull Intent intent) {
        LogUtil.debug(TAG, "On Receive: " + intent.getAction());
        String action = intent.getAction();
        if (action == null) {
            LogUtil.debug(TAG, "Received null action in reminder");
            return;
        }

        //If reboot completed, reset the alarms
        if (action.equals(Intent.ACTION_BOOT_COMPLETED)) {
            SharedPreferences sharedPref = getSharedPreferences(context);
            LogUtil.debug(TAG, "Rescheduling alarms");
            AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            SharedPreferences.Editor editor = sharedPref.edit();
            for (Map.Entry<String, ?> entry : sharedPref.getAll().entrySet()) {
                if (!(entry.getValue() instanceof Long)) {
                    LogUtil.debug(TAG, "removed");
                    editor.remove(entry.getKey());
                    continue;
                }
                LogUtil.debug(TAG, "set alarm");
                setAlarmManager(alarmManager, context, (Long) entry.getValue(), entry.getKey());
            }
            editor.apply();
        } else { //Show notification reminder
            LogUtil.debug(TAG, "Show notification");
            showReminderNotification(context, getCashBoxExtraFromAction(action));
        }
    }
}
