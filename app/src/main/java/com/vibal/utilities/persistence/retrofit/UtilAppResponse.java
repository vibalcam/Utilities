package com.vibal.utilities.persistence.retrofit;

import com.google.gson.annotations.SerializedName;
import com.vibal.utilities.util.Converters;

import java.util.Calendar;
import java.util.List;
import java.util.Map;

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
        return success==1;
    }

    public String getMessage() {
        return message;
    }

    public static class ModificationResponse extends UtilAppResponse {
        @SerializedName(VALUES)
        private Map<Long,Long> values;

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

        public boolean operationSuccessful(long key) {
            return getValue(key) >= 0;
        }
    }

    public static class ChangesResponse extends UtilAppResponse {
        @SerializedName(VALUES)
        private List<ChangesNotification> changes;

        public ChangesResponse(int success, String message, List<ChangesNotification> changes) {
            super(success, message);
            this.changes = changes;
        }

        public List<ChangesNotification> getChanges() {
            return changes;
        }
    }

    public static class ChangesNotification {
        @SerializedName(UtilAppAPI.NOTIFICATION_ID) //todo
        private long notificationId;
        @SerializedName(UtilAppAPI.OP_CODE)
        private int operationCode;
        @SerializedName(UtilAppAPI.ID)
        private long id;
        @SerializedName(UtilAppAPI.CASHBOX_ID)
        private long cashBoxId;
        @SerializedName(UtilAppAPI.AMOUNT)
        private double amount;
        @SerializedName(UtilAppAPI.DATE)
        private Calendar date;
        @SerializedName(UtilAppAPI.INFO)
        private String info;

        public ChangesNotification(long notificationId, int operationCode, long id, long cashBoxId,
                                   double amount, Calendar date, String info) {
            this.notificationId = notificationId;
            this.operationCode = operationCode;
            this.id = id;
            this.cashBoxId = cashBoxId;
            this.amount = amount;
            this.date = date;
            this.info = info;
        }

        public ChangesNotification(long notificationId, int operationCode, long id, long cashBoxId,
                                   double amount, long date, String info) {
            this(notificationId,operationCode,id,cashBoxId,amount,
                    Converters.calendarFromTimestamp(date),info);
        }

        public long getNotificationId() {
            return notificationId;
        }

        public int getOperationCode() {
            return operationCode;
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

        public Calendar getDate() {
            return date;
        }

        public String getInfo() {
            return info;
        }
    }
}
