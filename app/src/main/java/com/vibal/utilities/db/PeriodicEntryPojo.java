package com.vibal.utilities.db;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Embedded;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;
import androidx.work.Constraints;
import androidx.work.OneTimeWorkRequest;

import com.vibal.utilities.backgroundTasks.RxPeriodicEntryWorker;
import com.vibal.utilities.util.DiffDbUsable;

import java.util.Locale;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static androidx.room.ForeignKey.CASCADE;

public class PeriodicEntryPojo implements DiffDbUsable<PeriodicEntryPojo> {
    public static final TimeUnit TIME_UNIT = TimeUnit.DAYS;

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
    public boolean areItemsTheSame(@NonNull PeriodicEntryPojo newItem) {
        return this.workInfo.workId.equals(newItem.workInfo.workId);
    }

    @Override
    public boolean areContentsTheSame(@NonNull PeriodicEntryPojo newItem) {
        return this.workInfo.amount == newItem.workInfo.amount &&
                this.workInfo.info.equals(newItem.workInfo.info) &&
                this.workInfo.repeatInterval == newItem.workInfo.repeatInterval &&
                this.cashBoxName.equals(newItem.cashBoxName) &&
                this.workInfo.repetitions == newItem.workInfo.repetitions;
    }

    @Entity(tableName = "periodicWork_table",
            foreignKeys = @ForeignKey(entity = CashBoxInfo.class, parentColumns = "id",
                    childColumns = "cashBoxId", onDelete = CASCADE, onUpdate = CASCADE),
            indices = {@Index(value = "cashBoxId")})
    public static class PeriodicEntryWorkInfo {
        @PrimaryKey(autoGenerate = true)
        private long id;
        @NonNull
        private UUID workId;
        private long cashBoxId;
        private String info;
        private double amount;
        @ColumnInfo(name = "period")
        private long repeatInterval;
        private int repetitions;

        public PeriodicEntryWorkInfo(@NonNull UUID workId, long cashBoxId, double amount, String info,
                                     long repeatInterval, int repetitions) {
            this.workId = workId;
            this.cashBoxId = cashBoxId;
            this.info = info;
            this.amount = amount;
            this.repeatInterval = repeatInterval;
            this.repetitions = repetitions;
        }

        public long getId() {
            return id;
        }

        public void setId(long id) {
            this.id = id;
        }

        @NonNull
        public UUID getWorkId() {
            return workId;
        }

        public void setWorkId(@NonNull UUID workId) {
            this.workId = workId;
        }

        public long getCashBoxId() {
            return cashBoxId;
        }

        public void setCashBoxId(long cashBoxId) {
            this.cashBoxId = cashBoxId;
        }

        public String getInfo() {
            return info;
        }

        public void setInfo(String info) {
            this.info = info;
        }

        public double getAmount() {
            return amount;
        }

        public void setAmount(double amount) {
            this.amount = amount;
        }

        public long getRepeatInterval() {
            return repeatInterval;
        }

        public void setRepeatInterval(long repeatInterval) {
            this.repeatInterval = repeatInterval;
        }

        public int getRepetitions() {
            return repetitions;
        }

        public void setRepetitions(int repetitions) {
            this.repetitions = repetitions;
        }
    }

    public static class PeriodicEntryWorkRequest {
        private PeriodicEntryWorkInfo workInfo;
        private OneTimeWorkRequest workRequest;

        public PeriodicEntryWorkRequest(long cashBoxId, double amount, String info,
                                        long repeatInterval, int repetitions) {
            //Create the periodic task
            Constraints constraints = new Constraints.Builder()
                    .setRequiresBatteryNotLow(true)
                    .build();
            workRequest = new OneTimeWorkRequest.Builder(RxPeriodicEntryWorker.class)
                    .setConstraints(constraints)
//                    .setInitialDelay(repeatInterval, TIME_UNIT)
                    .addTag(RxPeriodicEntryWorker.TAG_PERIODIC)
                    .addTag(String.format(Locale.US, RxPeriodicEntryWorker.TAG_CASHBOX_ID, cashBoxId))
                    .build();
            //Create the PeriodicEntryWorkInfo
            workInfo = new PeriodicEntryWorkInfo(workRequest.getId(), cashBoxId, amount, info,
                    repeatInterval+1, repetitions); // repeat interval + starting


//        private PeriodicEntryWorkInfo workInfo;
//        private PeriodicWorkRequest workRequest;
//
//        public PeriodicEntryWorkRequest(long cashBoxId, double amount, String info,
//                                        long repeatInterval, int repetitions) {
//            //Create the periodic task
//            Constraints constraints = new Constraints.Builder()
////                    .setRequiresBatteryNotLow(true)
////                    .setRequiresDeviceIdle(true)
//                    .build();
//            workRequest = new PeriodicWorkRequest.Builder(RxPeriodicEntryWorker.class,
//                    repeatInterval, TIME_UNIT)
//                    .setConstraints(constraints)
//                    .addTag(RxPeriodicEntryWorker.TAG_PERIODIC)
//                    .addTag(String.format(Locale.US, RxPeriodicEntryWorker.TAG_CASHBOX_ID, cashBoxId))
//                    .build();
//            //Create the PeriodicEntryWorkInfo
//            workInfo = new PeriodicEntryWorkInfo(workRequest.getWorkId(),cashBoxId, amount, info, repeatInterval, repetitions);
        }

        public PeriodicEntryWorkRequest(@NonNull PeriodicEntryWorkInfo workInfo) {
            this(workInfo.getCashBoxId(), workInfo.getAmount(), workInfo.getInfo(),
                    workInfo.getRepeatInterval(), workInfo.getRepetitions());
            this.workInfo.setId(workInfo.getId());
        }

        public PeriodicEntryWorkInfo getWorkInfo() {
            return workInfo;
        }

        public OneTimeWorkRequest getWorkRequest() {
            return workRequest;
        }

        //        public PeriodicWorkRequest getWorkRequest() {
//            return workRequest;
//        }
    }
}
