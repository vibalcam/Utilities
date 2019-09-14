package com.utilities.vibal.utilities.backgroundTasks;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.work.Data;
import androidx.work.RxWorker;
import androidx.work.WorkManager;
import androidx.work.WorkerParameters;

import com.utilities.vibal.utilities.App;
import com.utilities.vibal.utilities.R;
import com.utilities.vibal.utilities.db.CashBoxEntryDao;
import com.utilities.vibal.utilities.db.CashBoxInfo;
import com.utilities.vibal.utilities.db.UtilitiesDatabase;
import com.utilities.vibal.utilities.models.CashBox;
import com.utilities.vibal.utilities.ui.cashBoxManager.CashBoxItemFragment;
import com.utilities.vibal.utilities.ui.cashBoxManager.CashBoxManagerActivity;
import com.utilities.vibal.utilities.util.LogUtil;

import java.util.Calendar;

import io.reactivex.Single;

public class RxPeriodicAddWorker extends RxWorker {
    public static final String TAG = "com.utilities.vibal.utilities.RxPeriodicAddWorker.TAG";
    public static final String KEY_AMOUNT = "com.utilities.vibal.utilities.RxPeriodicAddWorker.KEY_AMOUNT";
    public static final String KEY_INFO = "com.utilities.vibal.utilities.RxPeriodicAddWorker.KEY_INFO";

    public RxPeriodicAddWorker(@NonNull Context appContext, @NonNull WorkerParameters workerParams) {
        super(appContext, workerParams);
    }

    @NonNull
    @Override
    public Single<Result> createWork() {
        Data entryData = getInputData();
        long cashBoxId = entryData.getLong(CashBoxManagerActivity.EXTRA_CASHBOX_ID, CashBoxInfo.NO_CASHBOX);

        LogUtil.debug("PruebaPeriodico: ", "Contains amount: " +
                entryData.containsKey(KEY_AMOUNT,double.class));
        //Should not happen, but just in case
        if(cashBoxId==CashBoxInfo.NO_CASHBOX || entryData.containsKey(KEY_AMOUNT,double.class)) {
            WorkManager.getInstance(getApplicationContext()).cancelWorkById(getId());
            return Single.just(Result.failure());
        }

        String info = entryData.getString(KEY_INFO);
        if(info==null)
            info = "";
        double amount = entryData.getDouble(KEY_AMOUNT,0);
        UtilitiesDatabase database = UtilitiesDatabase.getInstance(getApplicationContext());
        return database.cashBoxEntryDao().insert(
                new CashBox.Entry(cashBoxId, amount, "Periodic: " + info, Calendar.getInstance()))
                .andThen(database.cashBoxDao().getCashBoxById(cashBoxId))
                .map(cashBox -> {
                    showNotification(cashBox, amount);
                    return Result.success();
                });
        //todo what if cashbox has been deleted
    }

    private void showNotification(CashBox cashBox, double amount) {
        //Set up notification
        Intent intentNotif = new Intent(getApplicationContext(), CashBoxManagerActivity.class);
        intentNotif.putExtra(CashBoxManagerActivity.EXTRA_ACTION,CashBoxManagerActivity.ACTION_DETAILS);
        intentNotif.putExtra(CashBoxManagerActivity.EXTRA_CASHBOX_ID,cashBox.getInfoWithCash().getId());

        Notification notification = new NotificationCompat.Builder(getApplicationContext(),
                App.CHANNEL_PERIODIC_ID)
                .setSmallIcon(R.drawable.logo)
                .setContentTitle("Periodic entry added to " + cashBox.getName())
                .setContentText("Added amount: " + amount + "\nTotal cash: " + cashBox.getCash())
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
}
