package com.vibal.utilities.persistence.retrofit;

import com.google.gson.annotations.SerializedName;
import com.vibal.utilities.util.Converters;

import java.util.Calendar;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

public class UtilAppResponse {
    private static final String SUCCESS = "success";
    private static final String MESSAGE = "message";
    private static final String VALUES = "val";

    @SerializedName(SUCCESS)
    private int success;
    @SerializedName(MESSAGE)
    private String message;

    public UtilAppResponse(int success, String message) {
        this.success = success;
        this.message = message;
    }

    public int getSuccess() {
        return success;
    }

    public boolean isSuccessful() {
        return success == 1;
    }

    public String getMessage() {
        return message;
    }

    public static class ModificationResponse extends UtilAppResponse {
        @SerializedName(VALUES)
        private Map<Long, Long> values;

        public ModificationResponse(int success, String message, Map<Long, Long> values) {
            super(success, message);
            this.values = values;
        }

        public Map<Long, Long> getValues() {
            return values;
        }

        public long getValue(long key) {
            Long value = values.get(key);
            return value == null ? -1 : value;
        }

        public boolean isOperationSuccessful(long key) {
            return getValue(key) >= 0;
        }

        public boolean isWarningNonExistent(long key) {
            return getValue(key) == UtilAppAPI.NON_EXISTENT_WARNING;
        }
    }

    public static class EntriesResponse extends UtilAppResponse {
        @SerializedName(VALUES)
        private Map<Long, List<EntryJSON>> entries;

        public EntriesResponse(int success, String message, Map<Long, List<EntryJSON>> entries) {
            super(success, message);
            this.entries = entries;
        }

        public List<EntryJSON> getEntries(long key) {
            List<EntryJSON> list = entries.get(key);
            if (list == null)
                throw new IllegalArgumentException("Key does not exist");
            return list;
        }
    }

    public static class ChangesResponse extends UtilAppResponse {
        @SerializedName(VALUES)
        private TreeSet<ChangesNotification> changes;

        public ChangesResponse(int success, String message, TreeSet<ChangesNotification> changes) {
            super(success, message);
            this.changes = changes;
        }

        public TreeSet<ChangesNotification> getChanges() {
            return changes;
        }
    }

    public static class ChangesNotification extends EntryJSON implements Comparable<ChangesNotification> {
        @SerializedName(UtilAppAPI.NOTIFICATION_ID)
        private long notificationId;
        @SerializedName(UtilAppAPI.OP_CODE)
        private int operationCode;

        public ChangesNotification(long notificationId, int operationCode, long id, long cashBoxId, double amount, long date, String info) {
            super(id, cashBoxId, amount, date, info);
            this.notificationId = notificationId;
            this.operationCode = operationCode;
        }

        public long getNotificationId() {
            return notificationId;
        }

        public int getOperationCode() {
            return operationCode;
        }

        // Reverse order so the biggest returns first
        @Override
        public int compareTo(ChangesNotification o) {
            return Long.compare(o.notificationId, notificationId);
        }
    }

    public static class EntryJSON {
        @SerializedName(UtilAppAPI.ID)
        private long id;
        @SerializedName(UtilAppAPI.CASHBOX_ID)
        private long cashBoxId;
        @SerializedName(UtilAppAPI.AMOUNT)
        private double amount;
        @SerializedName(UtilAppAPI.DATE)
        private long date;
        @SerializedName(UtilAppAPI.INFO)
        private String info;

        public EntryJSON(long id, long cashBoxId, double amount, long date, String info) {
            this.id = id;
            this.cashBoxId = cashBoxId;
            this.amount = amount;
            this.date = date;
            this.info = info;
        }

        public long getId() {
            return id;
        }

        public long getCashBoxId() {
            return cashBoxId;
        }

        public double getAmount() {
            return amount;
        }

        public long getDate() {
            return date;
        }

        public Calendar getDateAsCalendar() {
            return Converters.calendarFromTimestamp(date);
        }

        public String getInfo() {
            return info;
        }
    }
}
