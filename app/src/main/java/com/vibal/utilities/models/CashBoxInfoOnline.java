package com.vibal.utilities.models;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.Index;

import java.util.Currency;

@Entity(tableName = "cashBoxesOnline_table", indices = {@Index(value = "name", unique = true)})
public class CashBoxInfoOnline extends CashBoxInfo {
    @ColumnInfo(defaultValue = "0")
    private boolean accepted = false;

    public CashBoxInfoOnline(long id, @NonNull String name, long orderId, Currency currency, boolean accepted) {
        super(id, name, orderId, currency);
        this.accepted = accepted;
    }

    @Ignore
    public CashBoxInfoOnline(long id, @NonNull String name, long orderId, Currency currency) {
        super(id, name, orderId, currency);
    }

    @Ignore
    public CashBoxInfoOnline(long id, @NonNull String name) throws IllegalArgumentException {
        super(id, name);
    }

    @Ignore
    public CashBoxInfoOnline(@NonNull String name) throws IllegalArgumentException {
        super(name);
    }

    @NonNull
    @Override
    public CashBoxInfoOnline cloneContents() {
        return (CashBoxInfoOnline) super.cloneContents();
    }

    @NonNull
    @Override
    public CashBoxInfoOnline clone() {
        return (CashBoxInfoOnline) super.clone();
    }

    public boolean isAccepted() {
        return accepted;
    }

    public void setAccepted(boolean accepted) {
        this.accepted = accepted;
    }
}
