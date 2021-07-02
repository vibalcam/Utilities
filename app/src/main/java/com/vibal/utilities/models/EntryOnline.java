package com.vibal.utilities.models;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.Ignore;
import androidx.room.Relation;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Objects;

// imp check if entryOnlineInfo is ever used

public class EntryOnline<E extends EntryInfo> extends EntryBase<E> implements Comparable<EntryOnline<?>> {
    @NonNull
    @Relation(parentColumn = "id", entityColumn = "entryId",
            entity = EntryOnlineInfo.ParticipantFromView.class)
    private final List<Participant> fromParticipants;
    @NonNull
    @Relation(parentColumn = "id", entityColumn = "entryId",
            entity = EntryOnlineInfo.ParticipantToView.class)
    private final List<Participant> toParticipants;

    /**
     * Constructor for Room and for cloning in EntryBase using reflection (do not change)
     */
    public EntryOnline(@NonNull E entryInfo, @NonNull List<Participant> fromParticipants,
                       @NonNull List<Participant> toParticipants) {
        super(entryInfo, fromParticipants, toParticipants);
        this.fromParticipants = fromParticipants;
        this.toParticipants = toParticipants;
    }

    @Ignore
    public EntryOnline(@NonNull E entry) {
        this(entry, new ArrayList<>(), new ArrayList<>());
    }

    @NonNull
    @Override
    public List<Participant> getFromParticipants() {
        return fromParticipants;
    }

    @NonNull
    @Override
    public List<Participant> getToParticipants() {
        return toParticipants;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        EntryOnline<?> that = (EntryOnline<?>) o;
        return Objects.equals(fromParticipants, that.fromParticipants) &&
                Objects.equals(toParticipants, that.toParticipants);
    }

    @Override
    public int compareTo(@NonNull EntryOnline<?> entryOnline) {
        int res = getEntryInfo().compareTo(entryOnline.getEntryInfo());
        if (res != 0)
            return res;
        return equals(entryOnline) ? 0 : Integer.compare(hashCode(), entryOnline.hashCode());
    }

    // Classes without generics for Room
    public static class Complete extends EntryOnline<EntryOnlineInfo> {
        public Complete(@NonNull EntryOnlineInfo entryInfo, List<Participant> fromParticipants, List<Participant> toParticipants) {
            super(entryInfo, fromParticipants, toParticipants);
        }
    }

    public static class Simple extends EntryOnline<EntryInfo> {
        public Simple(@NonNull EntryInfo entryInfo, List<Participant> fromParticipants, List<Participant> toParticipants) {
            super(entryInfo, fromParticipants, toParticipants);
        }
    }

    // imp change entry changes to take into account participants
    public static class EntryChanges implements Comparable<EntryChanges> {
        @Nullable
        private final EntryOnline<EntryOnlineInfo> newEntry;
        @Nullable
        private final EntryOnline<EntryOnlineInfo> oldEntry;

        public EntryChanges(@NonNull EntryOnline<EntryOnlineInfo> entry1, @Nullable EntryOnline<EntryOnlineInfo> entry2) {
            if (entry2 != null && entry1.getEntryInfo().getId() != (-entry2.getEntryInfo().getId()))
                throw new IllegalArgumentException("Entries entered are not old and new versions");

            if (entry1.getEntryInfo().isOld()) {
                oldEntry = entry1;
                newEntry = entry2;
            } else {
                newEntry = entry1;
                oldEntry = entry2;
            }
        }

        public EntryChanges(EntryOnline<EntryOnlineInfo> entryOnline) {
            this(entryOnline, null);
        }

        @Nullable
        public EntryOnline<EntryOnlineInfo> getNewEntry() {
            return newEntry;
        }

        @Nullable
        public EntryOnline<EntryOnlineInfo> getOldEntry() {
            return oldEntry;
        }

        @Nullable
        public Double getDiffAmount() {
            return newEntry.getEntryInfo().getAmount() == oldEntry.getEntryInfo().getAmount() ? null :
                    oldEntry.getEntryInfo().getAmount();
        }

        @Nullable
        public String getDiffInfo() {
            return newEntry.getEntryInfo().getInfo().equals(oldEntry.getEntryInfo().getInfo()) ? null :
                    oldEntry.getEntryInfo().getInfo();
        }

        @Nullable
        public Calendar getDiffDate() {
            return newEntry.getEntryInfo().getDate().equals(oldEntry.getEntryInfo().getDate()) ? null :
                    oldEntry.getEntryInfo().getDate();
        }

//        public boolean isDiffParticipants() {
//            return newEntry.getFromParticipants().equals(oldEntry.getFromParticipants()) &&
//                    newEntry.getToParticipants().equals(oldEntry.getToParticipants());
//        }

        @Override
        public int compareTo(@NonNull EntryChanges entryChanges) {
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
}
