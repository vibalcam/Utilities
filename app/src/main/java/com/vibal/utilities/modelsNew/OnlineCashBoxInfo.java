package com.vibal.utilities.modelsNew;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.Ignore;

import java.util.Currency;

@Entity(tableName = "onlineCashBoxesInfo_table", inheritSuperIndices = true)
public class OnlineCashBoxInfo extends CashBoxInfo {
    @Ignore
    public static final long NO_ONLINE_ID = 0;

    private long onlineId;

    public OnlineCashBoxInfo(long id, @NonNull String name, long orderId, boolean deleted,
                             long onlineId, Currency currency) {
        super(id, name, orderId, deleted, currency);
    }

    public OnlineCashBoxInfo(@NonNull String name) throws IllegalArgumentException {
        super(name);
    }

    public long getOnlineId() {
        return onlineId;
    }

    @NonNull
    @Override
    public OnlineCashBoxInfo cloneContents() { //todo check if it works
        OnlineCashBoxInfo onlineCashBoxInfo = (OnlineCashBoxInfo) super.cloneContents();
        onlineCashBoxInfo.onlineId = NO_ONLINE_ID;
        return onlineCashBoxInfo;
    }

    @NonNull
    @Override
    public OnlineCashBoxInfo clone() { //todo check if it works
        return (OnlineCashBoxInfo) super.clone();
    }
}
