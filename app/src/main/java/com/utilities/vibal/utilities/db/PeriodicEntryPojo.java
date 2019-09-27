package com.utilities.vibal.utilities.db;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Embedded;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;
import androidx.work.Constraints;
import androidx.work.PeriodicWorkRequest;

import com.utilities.vibal.utilities.backgroundTasks.RxPeriodicEntryWorker;
import com.utilities.vibal.utilities.util.DiffDbUsable;

import java.util.Locale;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static androidx.room.ForeignKey.CASCADE;

public class PeriodicEntryPojo implements DiffDbUsable<PeriodicEntryPojo> {
    public static final TimeUnit TIME_UNIT = TimeUnit.SECONDS;//todo

    @Embedded
    private final PeriodicEntryWorkInfo workInfo;
    private String cashBoxName;
    private double cashBoxAmount;

    public PeriodicEntryPojo(PeriodicEntryWorkInfo workInfo, String cashBoxName, double cashBoxAmount) {
        this.workInfo = workInfo;
        this.cashBoxName = cashBoxName;
        this.cashBoxAmount = cashBoxAmount;
    }

    public PeriodicEntryWorkInfo getWorkInfo() {
        return workInfo;
    }

    public String getCashBoxName() {
        return cashBoxName;
    }

    public double getCashBoxAmount() {
        return cashBoxAmount;
    }

    @Override
    public boolean areItemsTheSame(PeriodicEntryPojo newItem) {
        return this.getWorkInfo().id.equals(newItem.getWorkInfo().id);
    }

    @Override
    public boolean areContentsTheSame(PeriodicEntryPojo newItem) {
        return this.workInfo.amount==newItem.workInfo.amount &&
                this.workInfo.info.equals(newItem.workInfo.info) &&
                this.workInfo.repeatInterval==newItem.workInfo.repeatInterval &&
                this.cashBoxName.equals(newItem.cashBoxName);
    }

    @Entity(tableName = "periodicWork_table",
            foreignKeys = @ForeignKey(entity = CashBoxInfo.class, parentColumns = "id",
                    childColumns = "cashBoxId", onDelete = CASCADE, onUpdate = CASCADE),
            indices = {@Index(value = "cashBoxId")})
    public static class PeriodicEntryWorkInfo {
        @NonNull
        @PrimaryKey
        private final UUID id;
        private long cashBoxId;
        private String info;
        private double amount;
        @ColumnInfo(name = "period")
        private long repeatInterval;

        public PeriodicEntryWorkInfo(@NonNull UUID id, long cashBoxId, double amount, String info, long repeatInterval) {
            this.id = id;
            this.cashBoxId = cashBoxId;
            this.info = info;
            this.amount = amount;
            this.repeatInterval = repeatInterval;
        }

        @NonNull
        public UUID getId() {
            return id;
        }

        public long getCashBoxId() {
            return cashBoxId;
        }

        public String getInfo() {
            return info;
        }

        public double getAmount() {
            return amount;
        }

        public long getRepeatInterval() {
            return repeatInterval;
        }

        public void setCashBoxId(long cashBoxId) {
            this.cashBoxId = cashBoxId;
        }

        public void setInfo(String info) {
            this.info = info;
        }

        public void setAmount(double amount) {
            this.amount = amount;
        }

        public void setRepeatInterval(long repeatInterval) {
            this.repeatInterval = repeatInterval;
        }
    }

    public static class PeriodicEntryWorkRequest {
        private PeriodicEntryWorkInfo workInfo;
        private PeriodicWorkRequest workRequest;

        public PeriodicEntryWorkRequest(long cashBoxId, double amount, String info, long repeatInterval) {
            //Create the periodic task
            Constraints constraints = new Constraints.Builder()
//                    .setRequiresBatteryNotLow(true) todo
//                    .setRequiresDeviceIdle(true)
                    .build();
            workRequest = new PeriodicWorkRequest.Builder(RxPeriodicEntryWorker.class,
                    repeatInterval, TIME_UNIT)
                    .setConstraints(constraints)
                    .addTag(RxPeriodicEntryWorker.TAG_PERIODIC)
                    .addTag(String.format(Locale.US, RxPeriodicEntryWorker.TAG_CASHBOX_ID, cashBoxId))
                    .build();
            //Create the PeriodicEntryWorkInfo
            workInfo = new PeriodicEntryWorkInfo(workRequest.getId(),cashBoxId, amount, info, repeatInterval);
        }

        public PeriodicEntryWorkInfo getWorkInfo() {
            return workInfo;
        }

        public PeriodicWorkRequest getWorkRequest() {
            return workRequest;
        }
    }
}
