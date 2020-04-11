package com.vibal.utilities.modelsNew;


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Ignore;

import java.util.Calendar;

import static androidx.room.ForeignKey.CASCADE;

@Entity(tableName = "entriesOnline_table", inheritSuperIndices = true,
        foreignKeys = @ForeignKey(entity = CashBoxInfoOnline.class, parentColumns = "id",
        childColumns = "cashBoxId", onDelete = CASCADE, onUpdate = CASCADE))
public class EntryOnline extends Entry {
    @ColumnInfo(defaultValue = "0")
    private boolean viewed = false;

    public EntryOnline(long id, long cashBoxId, double amount, @Nullable String info, Calendar date,
                       long groupId, boolean viewed) {
        super(id, cashBoxId, amount, info, date, groupId);
        setViewed(viewed);
    }

    @Ignore
    public EntryOnline(@NonNull Entry entry) {
        this(entry.getId(),entry.getCashBoxId(),entry.getAmount(),entry.getInfo(),entry.getDate(),
                entry.getGroupId(),false);
    }

    @Ignore
    public EntryOnline(long cashBoxId, double amount, @Nullable String info, Calendar date) {
        super(cashBoxId, amount, info, date);
    }

    @Ignore
    public EntryOnline(double amount, @NonNull String info, Calendar date, long groupId) {
        super(amount, info, date, groupId);
    }

    @Ignore
    public EntryOnline(double amount, @NonNull String info, Calendar date) {
        super(amount, info, date);
    }

    public boolean isViewed() {
        return viewed;
    }

    public void setViewed(boolean viewed) {
        this.viewed = viewed;
    }

    public boolean isOld() {
        return id < 0;
    }

    public boolean setToOld() {
        if(id>0) {
            id = -id;
            return true;
        } else
            return false;
    }

    @NonNull
    @Override
    public Entry getEntryWithCashBoxId(long cashBoxId) {
        return new EntryOnline(super.getEntryWithCashBoxId(cashBoxId));
    }
}
