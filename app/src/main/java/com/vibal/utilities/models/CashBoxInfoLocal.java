package com.vibal.utilities.models;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.Index;

import java.util.Currency;

@Entity(tableName = "cashBoxesInfo_table", indices = {@Index(value = "name", unique = true),
        @Index(value = "deleted")})
public class CashBoxInfoLocal extends CashBoxInfo {
    @ColumnInfo(defaultValue = "0")
    private boolean deleted = false;

    public CashBoxInfoLocal(long id, @NonNull String name, long orderId, boolean deleted, Currency currency) {
        super(id, name, orderId, currency);
        this.deleted = deleted;
    }

    @Ignore
    public CashBoxInfoLocal(@NonNull String name) throws IllegalArgumentException {
        super(name);
    }

    public boolean isDeleted() {
        return deleted;
    }

    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
    }

    @NonNull
    @Override
    public CashBoxInfoLocal cloneContents() {
        // Currency and deleted are maintained
        return (CashBoxInfoLocal) super.cloneContents();
    }

    @NonNull
    @Override
    public CashBoxInfoLocal clone() {
        return (CashBoxInfoLocal) super.clone();
    }
}
