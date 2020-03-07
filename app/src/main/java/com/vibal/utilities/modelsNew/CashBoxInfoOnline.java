package com.vibal.utilities.modelsNew;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import java.util.Currency;

@Entity(tableName = "cashBoxesOnline_table", indices = {@Index(value = "name", unique = true)},
        ignoredColumns = "deleted")
public class CashBoxInfoOnline extends CashBoxInfo {
    public CashBoxInfoOnline(long id, @NonNull String name, long orderId, Currency currency) {
        super(id, name, orderId, false, currency);
    }

    @Ignore
    public CashBoxInfoOnline(@NonNull String name) throws IllegalArgumentException {
        super(name);
    }

    @NonNull
    @Override
    public CashBoxInfoOnline cloneContents() {
        return  (CashBoxInfoOnline) super.cloneContents();
    }

    @NonNull
    @Override
    public CashBoxInfoOnline clone() {
        return (CashBoxInfoOnline) super.clone();
    }
}
