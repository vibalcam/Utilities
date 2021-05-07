package com.vibal.utilities.models;


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.DatabaseView;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Ignore;

import java.util.Calendar;

import static androidx.room.ForeignKey.CASCADE;

@Entity(tableName = "entriesOnline_table", inheritSuperIndices = true,
        foreignKeys = @ForeignKey(entity = CashBoxInfoOnline.class, parentColumns = "id",
                childColumns = "cashBoxId", onDelete = CASCADE, onUpdate = CASCADE))
public class EntryOnlineInfo extends EntryInfo implements Comparable<EntryOnlineInfo> {
    @Nullable
//    @ColumnInfo(defaultValue = "NULL")
    private Calendar changeDate = null;

    public EntryOnlineInfo(long id, long cashBoxId, double amount, @Nullable String info, Calendar date,
                           long groupId, Calendar changeDate) {
        super(id, cashBoxId, amount, info, date, groupId);
        setChangeDate(changeDate);
    }

    @Ignore
    public EntryOnlineInfo(@NonNull EntryInfo entry) {
        this(entry.getId(), entry.getCashBoxId(), entry.getAmount(), entry.getInfo(), entry.getDate(),
                entry.getGroupId(), null);
    }

    @Ignore
    public EntryOnlineInfo(long cashBoxId, double amount, @Nullable String info, Calendar date) {
        super(cashBoxId, amount, info, date);
    }

    @Ignore
    public EntryOnlineInfo(double amount, @NonNull String info, Calendar date, long groupId) {
        super(amount, info, date, groupId);
    }

    @Ignore
    public EntryOnlineInfo(double amount, @NonNull String info, Calendar date) {
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
    public EntryInfo getEntryWithCashBoxId(long cashBoxId) {
        return new EntryOnlineInfo(super.getEntryWithCashBoxId(cashBoxId));
    }

    @Override
    public int compareTo(@NonNull EntryOnlineInfo entry) {
        if (equals(entry))
            return 0;

        long change = changeDate == null ? 0 : changeDate.getTimeInMillis();
        long otherChange = entry.changeDate == null ? 0 : entry.changeDate.getTimeInMillis();

        int result = Long.compare(change, otherChange);
        if (result == 0)
            result = super.compareTo(entry);
        return result;
    }

    @Entity(tableName = "entriesOnlineParticipants_table", inheritSuperIndices = true,
            foreignKeys = @ForeignKey(entity = EntryOnlineInfo.class, parentColumns = "id",
                    childColumns = "entryId", onDelete = CASCADE, onUpdate = CASCADE))
    public static class Participant extends EntryBase.Participant {
        public Participant(@NonNull String name, long entryId, boolean isFrom, double amount) {
            super(name, entryId, isFrom, amount);
        }
    }

    @DatabaseView(viewName = "fromEntriesOnlineParticipants_view",
            value = "SELECT * FROM entriesOnlineParticipants_table WHERE isFrom==1")
    public abstract static class ParticipantFromView extends Participant {
        public ParticipantFromView(@NonNull String name, long entryId, boolean isFrom, double amount) {
            super(name, entryId, isFrom, amount);
        }
    }

    @DatabaseView(viewName = "toEntriesOnlineParticipants_view",
            value = "SELECT * FROM entriesOnlineParticipants_table WHERE isFrom==0")
    public abstract static class ParticipantToView extends Participant {
        public ParticipantToView(@NonNull String name, long entryId, boolean isFrom, double amount) {
            super(name, entryId, isFrom, amount);
        }
    }

//    public static class EntryChanges implements Comparable<EntryChanges> {
//        private EntryOnlineInfo newEntry;
//        private Bundle changesPayload;
//
//        public EntryChanges(EntryOnlineInfo entry1, EntryOnlineInfo entry2) {
//            if (entry1.isOld()) {
//                newEntry = entry2;
//                changesPayload = entry2.getChangePayload(entry1);
//            } else {
//                newEntry = entry1;
//                changesPayload = entry1.getChangePayload(entry2);
//            }
//        }
//
//        public EntryChanges(EntryOnlineInfo entryOnline) {
//            this.newEntry = entryOnline;
//            changesPayload = null;
//        }
//
//        public EntryOnlineInfo getNewEntry() {
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
//            if(obj instanceof EntryOnlineInfo)
//                return newEntry.equals(obj);
//            else
//                return false;
//        }
//    }
}
