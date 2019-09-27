package com.utilities.vibal.utilities.backgroundTasks;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.preference.PreferenceManager;
import androidx.work.RxWorker;
import androidx.work.WorkerParameters;

import com.utilities.vibal.utilities.App;
import com.utilities.vibal.utilities.R;
import com.utilities.vibal.utilities.db.PeriodicEntryPojo;
import com.utilities.vibal.utilities.db.UtilitiesDatabase;
import com.utilities.vibal.utilities.models.CashBox;
import com.utilities.vibal.utilities.ui.cashBoxManager.CashBoxItemFragment;
import com.utilities.vibal.utilities.ui.cashBoxManager.CashBoxManagerActivity;
import com.utilities.vibal.utilities.util.LogUtil;

import java.util.Calendar;

import io.reactivex.Single;

public class RxPeriodicEntryWorker extends RxWorker {
    private static final String TAG = "PruebaRxPeriodic";

    public static final String TAG_PERIODIC = "com.utilities.vibal.utilities.RxPeriodicEntryWorker.TAG_PERIODIC";
    public static final String TAG_CASHBOX_ID = "com.utilities.vibal.utilities.RxPeriodicEntryWorker.TAG_CASHBOX%d";

    public RxPeriodicEntryWorker(@NonNull Context appContext, @NonNull WorkerParameters workerParams) {
        super(appContext, workerParams);
    }

    @NonNull
    @Override
    public Single<Result> createWork() {
        LogUtil.debug(TAG,"Do job");
        UtilitiesDatabase database = UtilitiesDatabase.getInstance(getApplicationContext());
        return database.periodicEntryWorkDao().getWorkPojoByUUID(getId())
                .flatMap(periodicEntryPojo -> {
                    PeriodicEntryPojo.PeriodicEntryWorkInfo workInfo = periodicEntryPojo.getWorkInfo();
                    CashBox.Entry entry = new CashBox.Entry(workInfo.getCashBoxId(),workInfo.getAmount(),
                            "Periodic: " + workInfo.getInfo(), Calendar.getInstance());
                    return database.cashBoxEntryDao().insert(entry)
                            .toSingle(() -> {
                                LogUtil.debug(TAG,"Success");
                                showNotification(periodicEntryPojo);
                                return Result.success();
                            }).onErrorReturn(throwable -> {
                                LogUtil.error(TAG,"Error en periodic", throwable);
                                return Result.failure();
                            });
                });



//        Data entryData = getInputData();
//        long cashBoxId = entryData.getLong(CashBoxManagerActivity.EXTRA_CASHBOX_ID, CashBoxInfo.NO_CASHBOX);
//
//        LogUtil.debug("PruebaPeriodico: ", "Contains amount: " +
//                entryData.containsKey(KEY_AMOUNT,double.class));
//        //Should not happen, but just in case
//        if(cashBoxId==CashBoxInfo.NO_CASHBOX || entryData.containsKey(KEY_AMOUNT,double.class)) {
//            WorkManager.getInstance(getApplicationContext()).cancelWorkById(getId());
//            return Single.just(Result.failure());
//        }
//
//        String info = entryData.getString(KEY_INFO);
//        if(info==null)
//            info = "";
//        double amount = entryData.getDouble(KEY_AMOUNT,0);
//        UtilitiesDatabase database = UtilitiesDatabase.getInstance(getApplicationContext());
////        return database.cashBoxEntryDao().insert(
////                new CashBox.Entry(cashBoxId, amount, "Periodic: " + info, Calendar.getInstance()))
////                .andThen(database.cashBoxDao().getCashBoxById(cashBoxId))
////                .map(cashBox -> {
////                    showNotification(cashBox, amount);
////                    return Result.success();
////                });
//        CashBox.Entry entry = new CashBox.Entry(cashBoxId, amount, "Periodic: " + info,
//                Calendar.getInstance());
//        return database.cashBoxEntryDao()
//                .insert(entry)
//                .andThen(database.cashBoxDao().getCashBoxById(cashBoxId))
//                .map(cashBox -> {
//                    if(cashBox==null) { //todo cashboxid not found then error EmptyResultSetException
//                        WorkManager.getInstance(getApplicationContext()).cancelAllWorkByTag(
//                                String.format(Locale.US, TAG_CASHBOX_ID, cashBoxId));
//                        database.cashBoxEntryDao().delete(entry); //Delete the entry added earlier
//                    } else
//                        showNotification(cashBox, amount);
//                    return Result.success();
//                });
    }

    private void showNotification(PeriodicEntryPojo periodicEntryPojo) {
        //Check if the notifications are enabled
        if(!PreferenceManager.getDefaultSharedPreferences(getApplicationContext())
                .getBoolean("notifyPeriodic",false))
            return;

        //Set up notification
        Intent intentNotif = new Intent(getApplicationContext(), CashBoxManagerActivity.class);
        intentNotif.putExtra(CashBoxManagerActivity.EXTRA_ACTION,CashBoxManagerActivity.ACTION_DETAILS);
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
                .setContentIntent(PendingIntent.getActivity(getApplicationContext(),0,
                        intentNotif,0))
                .setAutoCancel(true)
                .setGroup(CashBoxManagerActivity.GROUP_KEY_CASHBOX)
                .build();

        //Show notification
        NotificationManagerCompat.from(getApplicationContext())
                .notify(CashBoxItemFragment.REMINDER_ID,notification);
    }

    //todo eliminar cuando se complete
}
