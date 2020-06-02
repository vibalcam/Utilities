package com.vibal.utilities.models;

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

    /**
     * Periodic Entry Work only for local purposes, not available for online
     */
    @Entity(tableName = "periodicWork_table",
            foreignKeys = @ForeignKey(entity = CashBoxInfoLocal.class, parentColumns = "id",
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

        /**
         * Create PeriodicEntryWorkRequest
         *
         * @param cashBoxId      id of the cashBox where the entries will be added
         * @param amount         amount of the entries
         * @param info           info of the entries
         * @param repeatInterval interval between each entry
         * @param repetitions    number of entries to be added
         * @param delay          true if there should be a delay before the first entry,
         *                       false otherwise
         */
        public PeriodicEntryWorkRequest(long cashBoxId, double amount, String info,
                                        long repeatInterval, int repetitions, long delay) {
            //Create the periodic task
            Constraints constraints = new Constraints.Builder()
                    .setRequiresBatteryNotLow(true)
                    .build();
            OneTimeWorkRequest.Builder builder = new OneTimeWorkRequest.Builder(RxPeriodicEntryWorker.class)
                    .setConstraints(constraints)
                    .addTag(RxPeriodicEntryWorker.TAG_PERIODIC)
                    .addTag(String.format(Locale.US, RxPeriodicEntryWorker.TAG_CASHBOX_ID, cashBoxId));
            if (delay >= 0)
                builder.setInitialDelay(delay, TIME_UNIT);
            workRequest = builder.build();

            //Create the PeriodicEntryWorkInfo
            workInfo = new PeriodicEntryWorkInfo(workRequest.getId(), cashBoxId, amount, info,
                    repeatInterval, repetitions + 1); // repetitions + starting
        }

        /**
         * Create PeriodicEntryWorkRequest without initial delay
         *
         * @param cashBoxId      id of the cashBox where the entries will be added
         * @param amount         amount of the entries
         * @param info           info of the entries
         * @param repeatInterval interval between each entry
         * @param repetitions    number of entries to be added
         */
        public PeriodicEntryWorkRequest(long cashBoxId, double amount, String info,
                                        long repeatInterval, int repetitions) {
            this(cashBoxId, amount, info, repeatInterval, repetitions, 0);
        }

        /**
         * Create PeriodicEntryWorkRequest from a work info with an initial delay equal to the
         * repeat interval
         *
         * @param workInfo workInfo from which the workRequest will be built
         */
        public PeriodicEntryWorkRequest(@NonNull PeriodicEntryWorkInfo workInfo) {
            this(workInfo.getCashBoxId(), workInfo.getAmount(), workInfo.getInfo(),
                    workInfo.getRepeatInterval(), workInfo.getRepetitions(), workInfo.getRepeatInterval());
            this.workInfo.setId(workInfo.getId());
        }

        public PeriodicEntryWorkInfo getWorkInfo() {
            return workInfo;
        }

        public OneTimeWorkRequest getWorkRequest() {
            return workRequest;
        }
    }
}
