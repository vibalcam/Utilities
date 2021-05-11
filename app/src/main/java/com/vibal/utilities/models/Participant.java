package com.vibal.utilities.models;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.preference.PreferenceManager;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Ignore;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import com.vibal.utilities.ui.cashBoxManager.CashBoxManagerActivity;
import com.vibal.utilities.ui.settings.SettingsActivity;
import com.vibal.utilities.util.DiffDbUsable;
import com.vibal.utilities.util.LogUtil;

import java.util.Objects;

import static androidx.room.ColumnInfo.NOCASE;
import static androidx.room.ForeignKey.CASCADE;

/**
 * Immutable class representing a participant of an CashBox EntryInfo
 */
@Entity(tableName = "entriesParticipants_table",
//            primaryKeys = {"name", "entryId", "isFrom"},
        foreignKeys = @ForeignKey(entity = EntryInfo.class, parentColumns = "id",
                childColumns = "entryId", onDelete = CASCADE, onUpdate = CASCADE),
        indices = {@Index(value = "entryId"),
                @Index(value = {"name", "entryId", "isFrom"}, unique = true)})
public class Participant implements DiffDbUsable<com.vibal.utilities.models.Participant>, Cloneable {
    public static final String DEFAULT_PARTICIPANT = "me";
    private static String SELF_NAME;

    @NonNull
    @ColumnInfo(collate = NOCASE)
    private final String name;
    private long entryId;
    private final boolean isFrom;
//    @ColumnInfo(defaultValue = "1")
    private double amount;    // If from it amount positive, negative if to

    // test make primary key and the other unique and index, for non viewed in the future
//        @ColumnInfo(defaultValue = "0")
    @PrimaryKey(autoGenerate = true)
    private long onlineId;

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

    public static Participant newFrom(@NonNull String name) {
        return new com.vibal.utilities.models.Participant(name, true, 1);
    }

    public static Participant newTo(@NonNull String name) {
        return new com.vibal.utilities.models.Participant(name, false, 1);
    }

    @NonNull
    public static String printName(@NonNull String name) {
        if (name.equalsIgnoreCase(getSelfName()) &&
                !name.equalsIgnoreCase(DEFAULT_PARTICIPANT))
            return DEFAULT_PARTICIPANT + '(' + name + ')';
        return name;
    }

    @NonNull
    public static Participant createDefaultParticipant(long entryId, boolean isFrom) {
        return new com.vibal.utilities.models.Participant(getSelfName(),
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
        com.vibal.utilities.models.Participant that = (com.vibal.utilities.models.Participant) o;
        // imp only take into account name
        return onlineId == that.onlineId &&
                entryId == that.entryId &&
                isFrom == that.isFrom &&
                Double.compare(that.amount, amount) == 0 &&
                name.equals(that.name);
    }

    public boolean isSameParticipant(@NonNull com.vibal.utilities.models.Participant p) {
        return p.getName().equals(this.getName());
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, entryId, isFrom);
    }

    // Implement DiffDbUssable

    @Override
    public boolean areItemsTheSame(@NonNull com.vibal.utilities.models.Participant newItem) {
        return this.onlineId == newItem.onlineId;
//                    this.entryId == newItem.entryId && this.name.equalsIgnoreCase(newItem.name) &&
//                    this.isFrom == newItem.isFrom;
    }

    @Override
    public boolean areContentsTheSame(@NonNull com.vibal.utilities.models.Participant newItem) {
        return this.name.equalsIgnoreCase(newItem.name) &&
                this.amount == newItem.amount;
//                    this.entryId == newItem.entryId  &&
//                    this.isFrom == newItem.isFrom;
    }

    // Implement Cloneable

    @NonNull
    public com.vibal.utilities.models.Participant cloneContents(long entryId) {
        com.vibal.utilities.models.Participant participant = clone();
        participant.setEntryId(entryId);
        return participant;
    }

    @NonNull
    public com.vibal.utilities.models.Participant cloneContents(long entryId, long onlineId) {
        com.vibal.utilities.models.Participant participant = clone();
        participant.setEntryId(entryId);
        participant.setOnlineId(onlineId);
        return participant;
    }

    public com.vibal.utilities.models.Participant clone(double amount) {
        com.vibal.utilities.models.Participant participant = clone();
        participant.setAmount(amount);
        return participant;
    }

    @NonNull
    @Override
    public com.vibal.utilities.models.Participant clone() {
        try {
            return (com.vibal.utilities.models.Participant) super.clone();
        } catch (CloneNotSupportedException e) { // won't happen
            LogUtil.error("Participant", "Cloning error", e);
            return null;
        }
    }

    public com.vibal.utilities.models.Participant getParticipantWithEntryId(long entryId) {
        if (this.entryId == entryId)
            return this;

        com.vibal.utilities.models.Participant participant;
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
