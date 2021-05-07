package com.vibal.utilities.models;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.preference.PreferenceManager;
import androidx.room.ColumnInfo;
import androidx.room.Embedded;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Ignore;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import com.vibal.utilities.ui.cashBoxManager.CashBoxManagerActivity;
import com.vibal.utilities.ui.settings.SettingsActivity;
import com.vibal.utilities.util.DiffDbUsable;
import com.vibal.utilities.util.LogUtil;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

import static androidx.room.ColumnInfo.NOCASE;
import static androidx.room.ForeignKey.CASCADE;

public abstract class EntryBase<E extends EntryInfo> implements DiffDbUsable<EntryBase<?>>, Cloneable {
    public static final String DEFAULT_PARTICIPANT = "me";
    protected static String SELF_NAME;

    @NonNull
    @Embedded
    private final E entryInfo;

    @NonNull
    private static EntryBase<EntryInfo> getInstance(EntryInfo entryInfo, List<Participant> fromParticipants,
                                                    List<Participant> toParticipants) {
        return new EntryLocal(entryInfo, fromParticipants, toParticipants);
    }

    @NonNull
    public static EntryBase<EntryInfo> getInstance(EntryInfo entryInfo, Participant participant1,
                                                   Participant participant2) {
        ArrayList<Participant> fromParticipants = new ArrayList<>();
        ArrayList<Participant> toParticipants = new ArrayList<>();
        if (participant1.isFrom)
            fromParticipants.add(participant1);
        else
            toParticipants.add(participant1);
        if (participant2.isFrom)
            fromParticipants.add(participant2);
        else
            toParticipants.add(participant2);
        return getInstance(entryInfo, fromParticipants, toParticipants);
    }

    @NonNull
    public static EntryBase<EntryInfo> getInstance(EntryInfo entryInfo) {
        return new EntryLocal(entryInfo);
    }

    public static void setSelfName(@NonNull Context context) {
        String name = PreferenceManager.getDefaultSharedPreferences(context)
                .getString(SettingsActivity.KEY_SELF_PARTICIPANT, DEFAULT_PARTICIPANT);
        if (name.equals(DEFAULT_PARTICIPANT)) {
            SharedPreferences sharedPreferences = context.getSharedPreferences(
                    CashBoxManagerActivity.CASHBOX_MANAGER_PREFERENCE, Context.MODE_PRIVATE);
            name = sharedPreferences.getString(CashBoxManagerActivity.USERNAME_KEY, null);
            if (name == null)
                name = DEFAULT_PARTICIPANT;
        }

        SELF_NAME = name;
    }

    public static String getSelfName() {
        return SELF_NAME;
    }

    public EntryBase(@NonNull E entryInfo, @NonNull List<Participant> fromParticipants,
                     @NonNull List<Participant> toParticipants) {
        this.entryInfo = entryInfo;
        checkParticipantsEmpty(fromParticipants, toParticipants);
    }

    private void checkParticipantsEmpty(@NonNull List<Participant> fromParticipants,
                                        @NonNull List<Participant> toParticipants) {
        if (fromParticipants.isEmpty())
            fromParticipants.add(Participant.newFrom(EntryBase.SELF_NAME));
        if (toParticipants.isEmpty())
            toParticipants.add(Participant.newTo(EntryBase.SELF_NAME));
    }

    @NonNull
    public static String formatParticipants(@NonNull Collection<Participant> participants) {
        if (participants.isEmpty())
            return "";

        StringBuilder builder = new StringBuilder();
        for (Participant p : participants)
            builder = builder.append(p.toString())
                    .append(',');

        return builder.deleteCharAt(builder.length() - 1).toString();

    }

    @NonNull
    public E getEntryInfo() {
        return entryInfo;
    }

    @NonNull
    public abstract List<Participant> getFromParticipants();

    @NonNull
    public abstract List<Participant> getToParticipants();

    // Implement DiffDbUssable

    @Override
    public boolean areItemsTheSame(@NonNull EntryBase<?> newItem) {
        return getEntryInfo().areItemsTheSame(newItem.getEntryInfo());
    }

    @Override
    public boolean areContentsTheSame(@NonNull EntryBase<?> newItem) {
        return getEntryInfo().areContentsTheSame(newItem.getEntryInfo()) &&
                Objects.equals(getFromParticipants(), newItem.getFromParticipants());
    }

    // Implement Cloneable

    @NonNull
    public EntryBase<?> cloneContents(long entryId, long cashBoxId) {
        List<Participant> cloneFromParticipants = new ArrayList<>();
        for (Participant p : getFromParticipants())
            cloneFromParticipants.add(p.cloneContents(entryId));
        List<Participant> cloneToParticipants = new ArrayList<>();
        for (Participant p : getToParticipants())
            cloneToParticipants.add(p.cloneContents(entryId));

        // Hack for clone in abstract using reflection
        try {
            EntryInfo entryInfo = getEntryInfo().cloneContents(entryId, cashBoxId);
            return getClass()
                    .getDeclaredConstructor(entryInfo.getClass(), List.class, List.class)
                    .newInstance(entryInfo, cloneFromParticipants, cloneToParticipants);
        } catch (NoSuchMethodException | IllegalAccessException | InstantiationException | InvocationTargetException e) {
            e.printStackTrace();
            throw new RuntimeException("Error while cloning", e);
        }
    }

    /**
     * Clones the object without conserving the id and the cashBoxId
     *
     * @return the new object, product of the cloning
     */
    @NonNull
    public EntryBase<?> cloneContents() {
        return cloneContents(CashBoxInfo.NO_ID, CashBoxInfo.NO_ID);
    }

    /**
     * Clones the object conserving the id
     *
     * @return the new object, product of the cloning
     */
    @NonNull
    @Override
    public EntryBase<?> clone() {
        List<Participant> cloneFromParticipants = new ArrayList<>();
        for (Participant p : getFromParticipants())
            cloneFromParticipants.add(p.clone());
        List<Participant> cloneToParticipants = new ArrayList<>();
        for (Participant p : getToParticipants())
            cloneToParticipants.add(p.clone());

        // Hack for clone in abstract using reflection
        try {
            EntryInfo entryInfo = getEntryInfo().clone();
            return getClass()
                    .getDeclaredConstructor(entryInfo.getClass(), List.class, List.class)
                    .newInstance(entryInfo, cloneFromParticipants, cloneToParticipants);
        } catch (NoSuchMethodException | IllegalAccessException | InstantiationException | InvocationTargetException e) {
            e.printStackTrace();
            throw new RuntimeException("Error while cloning", e);
        }
    }

    @NonNull
    public EntryBase<?> getEntryWithCashBoxId(long cashBoxId) {
        if (this.entryInfo.getCashBoxId() == cashBoxId)
            return this;

        List<Participant> cloneFromParticipants, cloneToParticipants;
        EntryInfo entryInfo = getEntryInfo().getEntryWithCashBoxId(cashBoxId);
        if (this.entryInfo.getCashBoxId() != CashBoxInfo.NO_ID) {    // get a clone with this cashbox id
            cloneFromParticipants = new ArrayList<>();
            for (Participant p : getFromParticipants())
                cloneFromParticipants.add(p.getParticipantWithEntryId(entryInfo.getId()));
            cloneToParticipants = new ArrayList<>();
            for (Participant p : getToParticipants())
                cloneToParticipants.add(p.getParticipantWithEntryId(entryInfo.getId()));
        } else {
            cloneFromParticipants = getFromParticipants();
            cloneToParticipants = getToParticipants();
        }

        try {
            return getClass()
                    .getDeclaredConstructor(entryInfo.getClass(), List.class, List.class)
                    .newInstance(entryInfo, cloneFromParticipants, cloneToParticipants);
        } catch (NoSuchMethodException | IllegalAccessException | InstantiationException | InvocationTargetException e) {
            e.printStackTrace();
            throw new RuntimeException("Error while cloning", e);
        }
    }

    /**
     * Immutable class representing a participant of an CashBox EntryInfo
     */
    @Entity(tableName = "entriesParticipants_table",
//            primaryKeys = {"name", "entryId", "isFrom"},
            foreignKeys = @ForeignKey(entity = EntryInfo.class, parentColumns = "id",
                    childColumns = "entryId", onDelete = CASCADE, onUpdate = CASCADE),
            indices = {@Index(value = "entryId"),
                    @Index(value = {"name", "entryId", "isFrom"}, unique = true)})
    public static class Participant implements DiffDbUsable<Participant>, Cloneable {
        @NonNull
        @ColumnInfo(collate = NOCASE)
        private final String name;
        private long entryId;
        private final boolean isFrom;
        @ColumnInfo(defaultValue = "1")
        private double amount;    // If from it amount positive, negative if to

        // test make primary key and the other unique and index, for non viewed in the future
//        @ColumnInfo(defaultValue = "0")
        @PrimaryKey(autoGenerate = true)
        private long onlineId;

        public static Participant newFrom(@NonNull String name) {
            return new Participant(name, true, 1);
        }

        public static Participant newTo(@NonNull String name) {
            return new Participant(name, false, 1);
        }

        @NonNull
        public static String printName(@NonNull String name) {
            if (name.equalsIgnoreCase(SELF_NAME) &&
                    !name.equalsIgnoreCase(DEFAULT_PARTICIPANT))
                return DEFAULT_PARTICIPANT + '(' + name + ')';
            return name;
        }

        @NonNull
        public static Participant createDefaultParticipant(long entryId, boolean isFrom) {
            return new Participant(DEFAULT_PARTICIPANT,
                    entryId, isFrom, 1);
        }

        public Participant(@NonNull String name, long entryId, boolean isFrom, double amount,
                           long onlineId) {
            name = name.trim();
            if (TextUtils.isEmpty(name))
                throw new IllegalArgumentException("Name cannot be empty");
            else if (amount == 0)
                throw new IllegalArgumentException("Amount cannot be 0");

            this.name = name.toLowerCase();
            setEntryId(entryId);
            this.isFrom = isFrom;
            setAmount(amount);
            this.onlineId = onlineId;
        }

        @Ignore
        public Participant(@NonNull String name, long entryId, boolean isFrom, double amount) {
            this(name, entryId, isFrom, amount, CashBoxInfo.NO_ID);
        }

        @Ignore
        private Participant(@NonNull String name, boolean isFrom, double amount) {
            this(name, CashBoxInfo.NO_ID, isFrom, amount);
        }

        @NonNull
        public String getName() {
            return name;
        }

        public long getEntryId() {
            return entryId;
        }

        public boolean isFrom() {
            return isFrom;
        }

        /**
         * Represents the importance of the participant in the total entry amount
         *
         * @return amount given (positive) or received (negative)
         */
        public double getAmount() {
            return amount;
        }

        private void setEntryId(long entryId) {
            this.entryId = entryId;
        }

        private void setAmount(double amount) {
            this.amount = isFrom ? Math.abs(amount) : -Math.abs(amount);
        }

        public long getOnlineId() {
            return onlineId;
        }

        public void setOnlineId(long onlineId) {
            this.onlineId = onlineId;
        }

        public String printName() {
            return printName(name);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Participant that = (Participant) o;
            return entryId == that.entryId &&
                    isFrom == that.isFrom &&
                    Double.compare(that.amount, amount) == 0 &&
                    name.equals(that.name);
        }

        @Override
        public int hashCode() {
            return Objects.hash(name, entryId, isFrom);
        }

        // Implement DiffDbUssable

        @Override
        public boolean areItemsTheSame(@NonNull Participant newItem) {
            return this.entryId == newItem.entryId && this.name.equalsIgnoreCase(newItem.name) &&
                    this.isFrom == newItem.isFrom;
        }

        @Override
        public boolean areContentsTheSame(@NonNull Participant newItem) {
            return this.amount == newItem.amount;
        }

        // Implement Cloneable

        @NonNull
        public Participant cloneContents(long entryId) {
            Participant participant = clone();
            participant.setEntryId(entryId);
            return participant;
        }

        @NonNull
        public Participant cloneContents(long entryId, long onlineId) {
            Participant participant = clone();
            participant.setEntryId(entryId);
            participant.setOnlineId(onlineId);
            return participant;
        }

        public Participant clone(double amount) {
            Participant participant = clone();
            participant.setAmount(amount);
            return participant;
        }

        @NonNull
        @Override
        public Participant clone() {
            try {
                return (Participant) super.clone();
            } catch (CloneNotSupportedException e) { // won't happen
                LogUtil.error("Participant", "Cloning error", e);
                return null;
            }
        }

        public Participant getParticipantWithEntryId(long entryId) {
            if (this.entryId == entryId)
                return this;

            Participant participant;
            if (this.entryId == CashBoxInfo.NO_ID) {    // If not already set use this one
                participant = this;
                setEntryId(entryId);
            } else {    // if already set clone it
                participant = cloneContents(entryId);
            }
            return participant;
        }

        @Override
        public String toString() {
            return getName();
        }
    }
}
