package com.vibal.utilities.models;


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
public class EntryOnline extends Entry implements Comparable<EntryOnline> {
    @Nullable
    @ColumnInfo(defaultValue = "NULL")
    private Calendar changeDate = null;

    public EntryOnline(long id, long cashBoxId, double amount, @Nullable String info, Calendar date,
                       long groupId, Calendar changeDate) {
        super(id, cashBoxId, amount, info, date, groupId);
        setChangeDate(changeDate);
    }

    @Ignore
    public EntryOnline(@NonNull Entry entry) {
        this(entry.getId(), entry.getCashBoxId(), entry.getAmount(), entry.getInfo(), entry.getDate(),
                entry.getGroupId(), null);
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

    @Nullable
    public Calendar getChangeDate() {
        return changeDate;
    }

    public void setChangeDate(@Nullable Calendar changeDate) {
        this.changeDate = changeDate;
    }

    public boolean isViewed() {
        return changeDate == null;
    }

    public boolean isOld() {
        return id < 0;
    }

    @NonNull
    @Override
    public Entry getEntryWithCashBoxId(long cashBoxId) {
        return new EntryOnline(super.getEntryWithCashBoxId(cashBoxId));
    }

    @Override
    public int compareTo(@NonNull EntryOnline entry) {
        if (equals(entry))
            return 0;

        long change = changeDate == null ? 0 : changeDate.getTimeInMillis();
        long otherChange = entry.changeDate == null ? 0 : entry.changeDate.getTimeInMillis();

        int result = Long.compare(change, otherChange);
        if (result == 0) {
            result = Long.compare(getId(), entry.getId());
            if (result == 0) {
                result = Long.compare(getCashBoxId(), entry.getCashBoxId());
                if (result == 0) {
                    result = getDate().compareTo(entry.getDate());
                    if (result == 0) {
                        result = Double.compare(getAmount(), entry.getAmount());
                        if (result == 0)
                            result = getInfo().compareTo(entry.getInfo());
                    }
                }
            }
        }
        return result;
    }

    public static class EntryChanges implements Comparable<EntryChanges> {
        @Nullable
        private EntryOnline newEntry;
        @Nullable
        private EntryOnline oldEntry;

        public EntryChanges(@NonNull EntryOnline entry1, @Nullable EntryOnline entry2) {
            if (entry2 != null && entry1.getId() != (-entry2.getId()))
                throw new IllegalArgumentException("Entries entered are not old and new versions");

            if (entry1.isOld()) {
                oldEntry = entry1;
                newEntry = entry2;
            } else {
                newEntry = entry1;
                oldEntry = entry2;
            }
        }

        public EntryChanges(EntryOnline entryOnline) {
            this(entryOnline, null);
        }

        @Nullable
        public EntryOnline getNewEntry() {
            return newEntry;
        }

        @Nullable
        public EntryOnline getOldEntry() {
            return oldEntry;
        }

        @Nullable
        public Double getDiffAmount() {
            return newEntry.getAmount() == oldEntry.getAmount() ? null : oldEntry.getAmount();
        }

        @Nullable
        public String getDiffInfo() {
            return newEntry.getInfo().equals(oldEntry.getInfo()) ? null : oldEntry.getInfo();
        }

        @Nullable
        public Calendar getDiffDate() {
            return newEntry.getDate().equals(oldEntry.getDate()) ? null : oldEntry.getDate();
        }

        @Override
        public int compareTo(EntryChanges entryChanges) {
            if (newEntry != null) { // new entry --> insertion
                return entryChanges.newEntry != null ? newEntry.compareTo(entryChanges.newEntry) :
                        newEntry.compareTo(entryChanges.oldEntry);
            } else { // old entry --> deletion
                return entryChanges.newEntry != null ? oldEntry.compareTo(entryChanges.newEntry) :
                        oldEntry.compareTo(entryChanges.oldEntry);
            }
//            return newEntry != null ? newEntry.compareTo(entryChanges.newEntry) : oldEntry.compareTo(entryChanges.oldEntry);
        }
    }

//    public static class EntryChanges implements Comparable<EntryChanges> {
//        private EntryOnline newEntry;
//        private Bundle changesPayload;
//
//        public EntryChanges(EntryOnline entry1, EntryOnline entry2) {
//            if (entry1.isOld()) {
//                newEntry = entry2;
//                changesPayload = entry2.getChangePayload(entry1);
//            } else {
//                newEntry = entry1;
//                changesPayload = entry1.getChangePayload(entry2);
//            }
//        }
//
//        public EntryChanges(EntryOnline entryOnline) {
//            this.newEntry = entryOnline;
//            changesPayload = null;
//        }
//
//        public EntryOnline getNewEntry() {
//            return newEntry;
//        }
//
//        public Bundle getChangesPayload() {
//            return changesPayload;
//        }
//
//        public boolean isInsert() {
//            return changesPayload == null && !newEntry.isOld();
//        }
//
//        public boolean isDelete() {
//            return changesPayload == null && newEntry.isOld();
//        }
//
//        @Override
//        public int compareTo(EntryChanges entryChanges) {
//            return newEntry.compareTo(entryChanges.newEntry);
//        }
//
//        @Override
//        public boolean equals(@Nullable Object obj) {
//            if(obj instanceof EntryOnline)
//                return newEntry.equals(obj);
//            else
//                return false;
//        }
//    }
}
