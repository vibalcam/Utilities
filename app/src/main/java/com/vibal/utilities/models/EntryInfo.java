package com.vibal.utilities.models;

import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.ColumnInfo;
import androidx.room.DatabaseView;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Ignore;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import com.vibal.utilities.util.Converters;
import com.vibal.utilities.util.DiffDbUsable;
import com.vibal.utilities.util.LogUtil;
import com.vibal.utilities.util.Util;

import java.text.DateFormat;
import java.text.NumberFormat;
import java.util.Calendar;
import java.util.Objects;

import static androidx.room.ForeignKey.CASCADE;

/**
 * Immutable object in orderPos for cloneContents to be easier.
 * When modifying directly, watch out, since an entry can be in cloned cashBoxes (no set methods)
 */
@DatabaseView(viewName = "entriesOnlineAsEntries_view",
        value = "SELECT id,cashBoxId,amount,date,info,groupId " +
                "FROM entriesOnline_table")
@Entity(tableName = "entries_table",
        foreignKeys = @ForeignKey(entity = CashBoxInfoLocal.class, parentColumns = "id",
                childColumns = "cashBoxId", onDelete = CASCADE, onUpdate = CASCADE),
        indices = {@Index(value = "cashBoxId")})
public class EntryInfo implements Cloneable, DiffDbUsable<EntryInfo> {
    @Ignore
    public static final long NO_GROUP = 0;
    @Ignore
    public static final String NO_INFO = "(No info)";

    @Ignore
    public static final String DIFF_AMOUNT = "amount";
    @Ignore
    public static final String DIFF_DATE = "date";
    @Ignore
    public static final String DIFF_INFO = "info";
    @Ignore
    public static final String DIFF_FROM_NAMES = "fromNames";

    @PrimaryKey(autoGenerate = true)
    protected long id;
    private long cashBoxId;
    private double amount;
    private final Calendar date;
    private String info;
    @ColumnInfo(defaultValue = "0")
    private final long groupId;

    /**
     * Constructor for Room
     */
    public EntryInfo(long id, long cashBoxId, double amount, @Nullable String info, Calendar date,
                     long groupId) {
        this.id = id;
        setInfo(info);
        this.groupId = groupId;
        this.date = date;
        setAmount(amount);
        this.cashBoxId = cashBoxId;
    }

    /**
     * Constructor for JSON
     */
    @Ignore
    public EntryInfo(long id, long cashBoxId, double amount, long date, String info) {
        this(id, cashBoxId, amount, info, Converters.calendarFromTimestamp(date), NO_GROUP);
    }

    /**
     * Constructor with just the id
     *
     * @param id entry id
     */
    @Ignore
    public EntryInfo(long id) {
        this(id, CashBoxInfo.NO_ID, 0, "", Calendar.getInstance(), NO_GROUP);
    }

    @Ignore
    public EntryInfo(long id, long cashBoxId, double amount, @Nullable String info, Calendar date) {
        this(id, cashBoxId, amount, info, date, NO_GROUP);
    }

    @Ignore
    public EntryInfo(long cashBoxId, double amount, @Nullable String info, Calendar date) {
        this(CashBoxInfo.NO_ID, cashBoxId, amount, info, date, NO_GROUP);
    }

    @Ignore
    public EntryInfo(double amount, @NonNull String info, Calendar date, long groupId) {
        this(CashBoxInfo.NO_ID, CashBoxInfo.NO_ID, amount, info, date, groupId);
    }

    @Ignore
    public EntryInfo(double amount, @NonNull String info, Calendar date) {
        this(CashBoxInfo.NO_ID, amount, info, date);
    }

    public long getId() {
        return id;
    }

    public long getCashBoxId() {
        return cashBoxId;
    }

    @NonNull
    public String getInfo() {
        return info;
    }

    public void setInfo(@Nullable String info) {
        if (!TextUtils.isEmpty(info))
            this.info = info.trim();
        else
            this.info = "";
    }

    @NonNull
    public String printInfo() {
        String string = info.isEmpty() ? NO_INFO : info;

        if (groupId == NO_GROUP)
            return string;
        else
            return "Group Add: " + string;
    }

    @NonNull
    public Calendar getDate() {
        return date;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = Util.roundTwoDecimals(amount);
    }

    public long getGroupId() {
        return groupId;
    }

    @NonNull
    public EntryInfo getEntryWithCashBoxId(long cashBoxId) {
        if (this.cashBoxId == cashBoxId)
            return this;

        EntryInfo entry = this.cashBoxId != CashBoxInfo.NO_ID ? this.cloneContents() : this;
        entry.cashBoxId = cashBoxId;
        return entry;
    }

    @Override
    @NonNull
    public String toString() {
        return toString(NumberFormat.getCurrencyInstance(), DateFormat.getDateInstance(DateFormat.SHORT));
    }

    @NonNull
    public String toString(@NonNull NumberFormat currencyFormat, @NonNull DateFormat dateFormat) {
        return dateFormat.format(date.getTime()) +
                "\t\t" +
                currencyFormat.format(amount) +
                "\n" +
                info;
    }

    @NonNull
    public EntryInfo cloneContents(long id, long cashBoxId) {
        EntryInfo entry = clone();
        entry.id = id;
        entry.cashBoxId = cashBoxId;
//        entry.groupId = NO_GROUP;
        return entry;
    }

    /**
     * Clones the object without conserving the id and the cashBoxId
     *
     * @return the new object, product of the cloning
     */
    @NonNull
    public EntryInfo cloneContents() {
        return cloneContents(CashBoxInfo.NO_ID, CashBoxInfo.NO_ID);
    }

    /**
     * Clones the object conserving the id
     *
     * @return the new object, product of the cloning
     */
    @NonNull
    @Override
    public EntryInfo clone() {
        try {
            return (EntryInfo) super.clone();
        } catch (CloneNotSupportedException e) { // won't happen
            LogUtil.error("PruebaCashBoxInfo", "Cloning error", e);
            return null;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        EntryInfo entry = (EntryInfo) o;
        return id == entry.id &&
                cashBoxId == entry.cashBoxId &&
                Double.compare(entry.amount, amount) == 0 &&
                Objects.equals(date, entry.date) &&
                Objects.equals(info, entry.info);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    //Implements DiffDbUsable
    @Override
    public boolean areItemsTheSame(@NonNull EntryInfo newItem) {
        return this.id == newItem.id;
    }

    @Override
    public boolean areContentsTheSame(@NonNull EntryInfo newItem) {
        return this.amount == newItem.amount &&
                this.date.equals(newItem.date) &&
                this.info.equals(newItem.info);
    }

//    @Override
//    public Bundle getChangePayload(@NonNull EntryInfo newItem) {
//        Bundle diff = new Bundle();
//        if (this.amount != newItem.amount)
//            diff.putDouble(DIFF_AMOUNT, newItem.amount);
//        if (!this.date.equals(newItem.date))
//            diff.putLong(DIFF_DATE, Converters.calendarToTimestamp(newItem.date));
//        if (!this.info.equals(newItem.info))
//            diff.putString(DIFF_INFO, newItem.info);
//        if (!Objects.equals(fromNames, newItem.fromNames))
//            diff.putString(DIFF_FROM_NAMES, newItem.fromNames);
//
//        return diff;
//    }

    public int compareTo(@NonNull EntryInfo entry) {
        if (equals(entry))
            return 0;

        int result = Long.compare(getId(), entry.getId());
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

        return result;
    }

    @DatabaseView(viewName = "fromEntriesParticipants_view",
            value = "SELECT * FROM entriesParticipants_table WHERE isFrom==1")
    public abstract static class ParticipantFromView extends Participant {
        public ParticipantFromView(@NonNull String name, long entryId, boolean isFrom, double amount) {
            super(name, entryId, isFrom, amount);
        }
    }

    @DatabaseView(viewName = "toEntriesParticipants_view",
            value = "SELECT * FROM entriesParticipants_table WHERE isFrom==0")
    public abstract static class ParticipantToView extends Participant {
        public ParticipantToView(@NonNull String name, long entryId, boolean isFrom, double amount) {
            super(name, entryId, isFrom, amount);
        }
    }
}