package com.vibal.utilities.backgroundTasks;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.preference.PreferenceManager;
import androidx.work.RxWorker;
import androidx.work.WorkManager;
import androidx.work.WorkerParameters;

import com.vibal.utilities.App;
import com.vibal.utilities.R;
import com.vibal.utilities.db.PeriodicEntryPojo;
import com.vibal.utilities.db.UtilitiesDatabase;
import com.vibal.utilities.modelsNew.CashBox;
import com.vibal.utilities.ui.cashBoxManager.CashBoxItemFragment;
import com.vibal.utilities.ui.cashBoxManager.CashBoxManagerActivity;
import com.vibal.utilities.util.LogUtil;

import java.util.Calendar;

import io.reactivex.Single;

public class RxPeriodicEntryWorker extends RxWorker {
    public static final String TAG_PERIODIC = "com.vibal.utilities.RxPeriodicEntryWorker.TAG_PERIODIC";
    public static final String TAG_CASHBOX_ID = "com.vibal.utilities.RxPeriodicEntryWorker.TAG_CASHBOX%d";
    private static final String TAG = "PruebaRxPeriodic";

    public RxPeriodicEntryWorker(@NonNull Context appContext, @NonNull WorkerParameters workerParams) {
        super(appContext, workerParams);
    }

    @NonNull
    @Override
    public Single<Result> createWork() {
        LogUtil.debug(TAG, "Do job");
        UtilitiesDatabase database = UtilitiesDatabase.getInstance(getApplicationContext());
        return database.periodicEntryWorkDao().getWorkPojoByUUID(getId())
                .flatMap(periodicEntryPojo -> {
                    //Create entry
                    PeriodicEntryPojo.PeriodicEntryWorkInfo workInfo = periodicEntryPojo.getWorkInfo();
                    CashBox.Entry entry = new CashBox.Entry(workInfo.getCashBoxId(),
                            workInfo.getAmount(), workInfo.getInfo(), Calendar.getInstance());
                    entry.setInfo("Periodic: " + entry.getInfo());
                    Single<Result> result = database.cashBoxEntryDao().insert(entry)
                            .toSingle(() -> {
                                LogUtil.debug(TAG, "Success");
                                showNotification(periodicEntryPojo);
                                return Result.success();
                            }).onErrorReturn(throwable -> {
                                LogUtil.error(TAG, "Error en periodic", throwable);
                                return Result.failure();
                            });

                    //Reschedule work
                    int repetitions = workInfo.getRepetitions() - 1;
                    if (repetitions > 0) {
                        LogUtil.debug(TAG, "Rescheduling work " + repetitions);
                        PeriodicEntryPojo.PeriodicEntryWorkRequest workRequest =
                                new PeriodicEntryPojo.PeriodicEntryWorkRequest(workInfo);
                        workRequest.getWorkInfo().setRepetitions(repetitions);
                        return database.periodicEntryWorkDao().update(workRequest.getWorkInfo())
                                .flatMap(integer -> {
                                    if (integer > 0)
                                        WorkManager.getInstance(getApplicationContext()).enqueue(workRequest.getWorkRequest());
                                    return result;
                                });
                    } else
                        return database.periodicEntryWorkDao().delete(workInfo)
                                .flatMap(integer -> result);
                }).onErrorReturn(throwable -> {
                    LogUtil.debug(TAG, "No found the corresponding work info");
                    return Result.success();
                });


//        LogUtil.debug(TAG,"Do job");
//        UtilitiesDatabase database = UtilitiesDatabase.getInstance(getApplicationContext());
//        return database.periodicEntryWorkDao().getWorkPojoByUUID(getId())
//                .flatMap(periodicEntryPojo -> {
//                    PeriodicEntryPojo.PeriodicEntryWorkInfo workInfo = periodicEntryPojo.getWorkInfo();
//                    CashBox.Entry entry = new CashBox.Entry(workInfo.getCashBoxId(),workInfo.getAmount(),
//                            "Periodic: " + workInfo.getInfo(), Calendar.getInstance());
//                    return database.cashBoxEntryDao().insert(entry)
//                            .toSingle(() -> {
//                                LogUtil.debug(TAG,"Success");
//                                showNotification(periodicEntryPojo);
//                                return Result.success();
//                            }).onErrorReturn(throwable -> {
//                                LogUtil.error(TAG,"Error en periodic", throwable);
//                                return Result.failure();
//                            });
//                });
    }

    private void showNotification(@NonNull PeriodicEntryPojo periodicEntryPojo) {
        //Check if the notifications are enabled
        if (!PreferenceManager.getDefaultSharedPreferences(getApplicationContext())
                .getBoolean("notifyPeriodic", false))
            return;

        //Set up notification
        Intent intentNotif = new Intent(getApplicationContext(), CashBoxManagerActivity.class);
        intentNotif.putExtra(CashBoxManagerActivity.EXTRA_ACTION, CashBoxManagerActivity.ACTION_DETAILS);
        intentNotif.putExtra(CashBoxManagerActivity.EXTRA_CASHBOX_ID,
                periodicEntryPojo.getWorkInfo().getCashBoxId());

        Notification notification = new NotificationCompat.Builder(getApplicationContext(),
                App.CHANNEL_PERIODIC_ID)
                .setSmallIcon(R.drawable.logo)
                .setContentTitle("Periodic entry added to " + periodicEntryPojo.getCashBoxName())
                .setContentText("Added amount: " + periodicEntryPojo.getWorkInfo().getAmount() +
                        "\nTotal cash: " + periodicEntryPojo.getCashBoxAmount())
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setCategory(NotificationCompat.CATEGORY_SERVICE)
                .setContentIntent(PendingIntent.getActivity(getApplicationContext(), 0,
                        intentNotif, 0))
                .setAutoCancel(true)
                .setGroup(CashBoxManagerActivity.NOTIFICATION_GROUP_KEY_CASHBOX)
                .build();

        //Show notification
        NotificationManagerCompat.from(getApplicationContext())
                .notify(CashBoxItemFragment.REMINDER_ID, notification);
    }
}
