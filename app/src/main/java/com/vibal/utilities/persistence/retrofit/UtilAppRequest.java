package com.vibal.utilities.persistence.retrofit;

import androidx.annotation.NonNull;

import com.google.gson.annotations.SerializedName;
import com.vibal.utilities.models.EntryBase.Participant;
import com.vibal.utilities.models.EntryInfo;
import com.vibal.utilities.persistence.retrofit.UtilAppAPI.OperationCode;
import com.vibal.utilities.util.Converters;

public class UtilAppRequest {
    @SerializedName(UtilAppAPI.OP_CODE)
    private @OperationCode
    int opCode;
    @SerializedName(UtilAppAPI.ID)
    private long id;

    public UtilAppRequest(@OperationCode int opCode, long id) throws IllegalArgumentException {
        this.opCode = opCode;
        setId(id);
    }

    public @OperationCode
    int getOpCode() {
        return opCode;
    }

    public void setOpCode(@OperationCode int opCode) {
        this.opCode = opCode;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
//        if(id == 0)
//            throw new IllegalArgumentException("Id cannot be 0");
        this.id = id;
    }

    public static class InvitationRequest extends UtilAppRequest {
        @SerializedName(UtilAppAPI.INVITATION)
        private String invitation;

        public InvitationRequest(int opCode, long id, String invitation) {
            super(opCode, id);
            this.invitation = invitation;
        }

        public String getInvitation() {
            return invitation;
        }

        public void setInvitation(String invitation) {
            this.invitation = invitation;
        }
    }

    public static class EntryInfoRequest extends UtilAppRequest {
        @SerializedName(UtilAppAPI.CASHBOX_ID)
        private long cashBoxId;
        @SerializedName(UtilAppAPI.AMOUNT)
        private double amount;
        @SerializedName(UtilAppAPI.DATE)
        private long date;
        @SerializedName(UtilAppAPI.INFO)
        private String info;

        public EntryInfoRequest(@OperationCode int opCode, long id, long cashBoxId, double amount,
                                long date, String info) throws IllegalArgumentException {
            super(opCode, id);
            this.cashBoxId = cashBoxId;
            this.amount = amount;
            this.date = date;
            this.info = info;
        }

        public EntryInfoRequest(@OperationCode int opCode, long id, @NonNull EntryInfo entry)
                throws IllegalArgumentException {
            super(opCode, id);
            cashBoxId = entry.getCashBoxId();
            amount = entry.getAmount();
            date = Converters.calendarToTimestamp(entry.getDate());
            info = entry.getInfo();
        }

        public long getCashBoxId() {
            return cashBoxId;
        }

        public void setCashBoxId(long cashBoxId) {
            this.cashBoxId = cashBoxId;
        }

        public double getAmount() {
            return amount;
        }

        public void setAmount(double amount) {
            this.amount = amount;
        }

        public long getDate() {
            return date;
        }

        public void setDate(long date) {
            this.date = date;
        }

        public String getInfo() {
            return info;
        }

        public void setInfo(String info) {
            this.info = info;
        }
    }

    public static class ParticipantRequest extends UtilAppRequest {
        @SerializedName(UtilAppAPI.NAME)
        private String name;
        @SerializedName(UtilAppAPI.ENTRY_ID)
        private long entryId;
        @SerializedName(UtilAppAPI.IS_FROM)
        private boolean isFrom;
        @SerializedName(UtilAppAPI.AMOUNT)
        private double amount;

        public ParticipantRequest(@OperationCode int opCode, long id, @NonNull Participant participant)
                throws IllegalArgumentException {
            super(opCode, id);
            name = participant.getName();
            entryId = participant.getEntryId();
            isFrom = participant.isFrom();
            amount = participant.getAmount();
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public long getEntryId() {
            return entryId;
        }

        public void setEntryId(long entryId) {
            this.entryId = entryId;
        }

        public boolean isFrom() {
            return isFrom;
        }

        public void setFrom(boolean from) {
            isFrom = from;
        }

        public double getAmount() {
            return amount;
        }

        public void setAmount(double amount) {
            this.amount = amount;
        }
    }
}
