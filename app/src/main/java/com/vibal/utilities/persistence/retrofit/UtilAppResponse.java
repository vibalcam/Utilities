package com.vibal.utilities.persistence.retrofit;

import androidx.annotation.NonNull;

import com.google.gson.annotations.SerializedName;
import com.vibal.utilities.exceptions.UtilAppException;
import com.vibal.utilities.models.EntryInfo;
import com.vibal.utilities.models.EntryOnline;
import com.vibal.utilities.models.EntryOnlineInfo;
import com.vibal.utilities.models.Participant;
import com.vibal.utilities.util.Converters;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

public class UtilAppResponse {
    private static final String SUCCESS = "success";
    private static final String MESSAGE = "message";
    private static final String VALUES = "val";
    private static final String ENTRIES = "entries";
    private static final String PARTICIPANTS = "part";

    @SerializedName(SUCCESS)
    private final int success;
    @SerializedName(MESSAGE)
    private final String message;

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

    public static class ListResponse<T> extends UtilAppResponse {
        @SerializedName(VALUES)
        private final List<Map<String, T>> list;
        private List<T> values = null;

        public ListResponse(int success, String message, List<Map<String, T>> list) {
            super(success, message);
            this.list = list;
        }

        public List<T> getValues() {
            if (values == null) {
                values = new ArrayList<>();
                for (Map<String, T> map : list)
                    values.addAll(map.values());
            }
            return values;
        }
    }

    public static class ModificationResponse extends UtilAppResponse {
        @SerializedName(VALUES)
        private final Map<Long, Long> values;

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

//        public boolean isWarningNonExistent(long key) {
//            return getValue(key) == UtilAppAPI.NON_EXISTENT_WARNING;
//        }
    }

    public static class EntriesResponse extends UtilAppResponse {
        @SerializedName(VALUES)
        private final Map<Long, Map<String, List<EntryJSON>>> response;

        public EntriesResponse(int success, String message, Map<Long, Map<String, List<EntryJSON>>> response) {
            super(success, message);
            this.response = response;
        }

        public List<EntryJSON> getEntries(long key) throws UtilAppException {
            Map<String, List<EntryJSON>> map = response.get(key);
            if (map == null)
                throw new IllegalArgumentException("Key does not exist");
            List<EntryJSON> list = map.get(ENTRIES);
            if (list == null)
                throw new UtilAppException("Unknown error: Entries not available");
            return list;
        }

        public List<EntryJSON> getParticipants(long key) throws UtilAppException {
            Map<String, List<EntryJSON>> map = response.get(key);
            if (map == null)
                throw new IllegalArgumentException("Key does not exist");
            List<EntryJSON> list = map.get(PARTICIPANTS);
            if (list == null)
                throw new UtilAppException("Unknown error: Participants not available");
            return list;
        }
    }

    public static class ChangesResponse extends UtilAppResponse {
        @SerializedName(VALUES)
        private final TreeSet<ChangesNotification> changes;

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
        private final long notificationId;
        @SerializedName(UtilAppAPI.OP_CODE)
        private final int operationCode;

        public ChangesNotification(long notificationId, int operationCode, long id, long cashBoxId,
                                   double amount, long date, String info) {
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
        public int compareTo(@NonNull ChangesNotification o) {
            return Long.compare(o.notificationId, notificationId);
        }
    }

    public static class EntryJSON {
        @SerializedName(UtilAppAPI.ID)
        private final long id;
        @SerializedName(UtilAppAPI.CASHBOX_ID)
        private final long cashBoxId;
        @SerializedName(UtilAppAPI.AMOUNT)
        private final double amount;
        @SerializedName(UtilAppAPI.DATE)
        private final long date;
        @SerializedName(UtilAppAPI.INFO)
        private final String info;

        public EntryJSON(long id, long cashBoxId, double amount, long date, String info) {
            this.id = id;
            this.cashBoxId = cashBoxId;
            this.amount = amount;
            this.date = date;
            this.info = info;
        }

        @NonNull
        public EntryOnline<EntryOnlineInfo> changeNotificationToEntry() {
            return new EntryOnline<>(new EntryOnlineInfo(getId(), getCashBoxId(),
                    getAmount(), getInfo(), getDateAsCalendar(),
                    EntryInfo.NO_GROUP, Calendar.getInstance()));
        }

        @NonNull
        public Participant changeNotificationToParticipant() {
            return new Participant(getParticipantName(), getParticipantEntryId(),
                    getParticipantIsFrom(), getAmount(), getParticipantOnlineId());
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

        // Methods for cashBox
        public String getCashBoxUsername() {
            return getInfo();
        }

        // Methods for participant
        public long getParticipantEntryId() {
            return getCashBoxId();
        }

        public String getParticipantName() {
            return getInfo();
        }

        public boolean getParticipantIsFrom() {
            return getDate() != 0;
        }

        public long getParticipantOnlineId() {
            return getId();
        }
    }
}
