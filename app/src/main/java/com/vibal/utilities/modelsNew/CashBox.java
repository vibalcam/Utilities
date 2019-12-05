package com.vibal.utilities.modelsNew;

import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.ColumnInfo;
import androidx.room.Embedded;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Ignore;
import androidx.room.Index;
import androidx.room.PrimaryKey;
import androidx.room.Relation;

import com.vibal.utilities.db.CashBoxInfo;
import com.vibal.utilities.util.Converters;
import com.vibal.utilities.util.DiffDbUsable;
import com.vibal.utilities.util.LogUtil;
import com.vibal.utilities.util.Util;

import java.text.DateFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import static androidx.room.ForeignKey.CASCADE;

public class CashBox implements Parcelable {
    public static final Parcelable.Creator<CashBox> CREATOR = new Parcelable.Creator<CashBox>() {
        @NonNull
        @Override
        public CashBox createFromParcel(@NonNull Parcel source) {
            return new CashBox(source);
        }

        @NonNull
        @Override
        public CashBox[] newArray(int size) {
            return new CashBox[size];
        }
    };

    @Embedded
    private InfoWithCash infoWithCash;
    @NonNull
    @Relation(parentColumn = "id", entityColumn = "cashBoxId")
    private List<Entry> entries;

    @Ignore
    public CashBox(@NonNull String name) throws IllegalArgumentException {
        this(new InfoWithCash(name,0), new ArrayList<>());
    }

    @Ignore
    public CashBox(@NonNull String name, @NonNull List<Entry> entries) throws  IllegalArgumentException {
        this.entries = entries;
        infoWithCash = new InfoWithCash(name,calculateCash(entries));
    }

    /**
     * You must ensure that cash is the sum of all the amounts.
     * Should not be used directly.
     */
    public CashBox(InfoWithCash infoWithCash, @NonNull List<Entry> entries) throws IllegalArgumentException {
        this.infoWithCash = infoWithCash;
        this.entries = entries;
    }

    @Ignore
    public CashBox(@NonNull Parcel parcel) {
        infoWithCash = InfoWithCash.CREATOR.createFromParcel(parcel);
        entries = parcel.createTypedArrayList(Entry.CREATOR);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        infoWithCash.writeToParcel(dest,flags);
        dest.writeTypedList(entries);
    }

    @NonNull
    public InfoWithCash getInfoWithCash() {
        return infoWithCash;
    }

    @NonNull
    public String getName() {
        return infoWithCash.getCashBoxInfo().getName();
    }

    public double getCash() {
        return infoWithCash.getCash();
    }

    @NonNull
    public List<Entry> getEntries() {
        return entries;
    }

    public void setInfoWithCash(InfoWithCash infoWithCash) {
        this.infoWithCash = infoWithCash;
    }

    public void setName(@NonNull String name) throws IllegalArgumentException {
        infoWithCash.getCashBoxInfo().setName(name);
    }

    public void setEntries(List<Entry> entries) {
        this.entries = entries;
    }

    private int calculateCash(@NonNull List<Entry> entries) {
        int sum = 0;
        for (Entry entry : entries)
            sum += entry.getAmount();
        return sum;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof CashBox)
            return ((CashBox) obj).getInfoWithCash().equals(this.infoWithCash);
        return false;
    }

    /**
     * Deep cloneContents of CashBox
     */
    @NonNull
    @SuppressWarnings("CloneDoesntCallSuperClone")
    public CashBox cloneContents() {
        List<Entry> entryList = new ArrayList<>();
        for(Entry entry:entries)
            entryList.add(entry.cloneContents());
        return new CashBox(infoWithCash.cloneContents(),entryList);
    }

    @Override
    @NonNull
    public String toString() {
        NumberFormat currencyFormat = NumberFormat.getCurrencyInstance();
        DateFormat dateFormat = DateFormat.getDateInstance(DateFormat.SHORT);
        StringBuilder builder = new StringBuilder();

        builder.append("*")
                .append(infoWithCash.getCashBoxInfo().getName())
                .append("*");
        for (Entry entry : entries)
            builder.append("\n\n")
                    .append(entry.toString(currencyFormat,dateFormat));
        builder.append("\n*TotalCash: ")
                .append(currencyFormat.format(infoWithCash.cash))
                .append("*");
        return builder.toString();
    }

    @Override
    public int hashCode() {
        return infoWithCash.hashCode();
    }

    public static class InfoWithCash implements Parcelable, Cloneable, DiffDbUsable<InfoWithCash> {
        private static final String DIFF_CASH = "cash";
        private static final String DIFF_NAME = "name";
        public static final Parcelable.Creator<InfoWithCash> CREATOR = new Parcelable.Creator<InfoWithCash>() {
            @NonNull
            @Override
            public InfoWithCash createFromParcel(@NonNull Parcel source) {
                return new InfoWithCash(source);
            }

            @NonNull
            @Override
            public InfoWithCash[] newArray(int size) {
                return new InfoWithCash[size];
            }
        };

        @NonNull
        @Embedded
        private final CashBoxInfo cashBoxInfo;
        private double cash; //sum of amounts

        @Ignore
        public InfoWithCash(@NonNull String name, double cash) throws IllegalArgumentException {
            this(new CashBoxInfo(name),cash);
        }

        @Ignore
        public InfoWithCash(@NonNull String name) throws IllegalArgumentException {
            this(name, 0);
        }

        public InfoWithCash(CashBoxInfo cashBoxInfo, double cash) {
            this.cashBoxInfo = cashBoxInfo;
            this.cash = cash;
        }

        @Ignore
        private InfoWithCash(@NonNull Parcel parcel) {
            cashBoxInfo = new CashBoxInfo(parcel.readString());
            cashBoxInfo.setId(parcel.readLong());
            cash = parcel.readDouble();
        }

        public long getId() {
            return cashBoxInfo.getId();
        }

        public double getCash() {
            return cash;
        }

        @Nullable
        public CashBoxInfo getCashBoxInfo() {
            return cashBoxInfo;
        }

        @Override
        public boolean equals(Object obj) {
            if(obj instanceof InfoWithCash)
                return ((InfoWithCash)obj).getCashBoxInfo().equals(cashBoxInfo);
            return false;
        }

        @Override
        public int hashCode() {
            return cashBoxInfo.hashCode();
        }

        @NonNull
        @Override
        public String toString() {
            return "InfoWithCash{" +
                    "cashBoxInfo=" + cashBoxInfo.toString() +
                    ", cash=" + cash +
                    '}';
        }

        @NonNull
        public InfoWithCash cloneContents() {
            return new InfoWithCash(cashBoxInfo.cloneContents(),cash);
        }

        // Implementation of Parcelable
        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(@NonNull Parcel dest, int flags) {
            dest.writeString(cashBoxInfo.getName());
            dest.writeLong(cashBoxInfo.getId());
            dest.writeDouble(cash);
        }

        //Implements DiffDbUsable
        @Override
        public boolean areItemsTheSame(@NonNull InfoWithCash newItem) {
            return this.getId()==newItem.getId();
        }

        @Override
        public boolean areContentsTheSame(@NonNull InfoWithCash newItem) {
            return this.cash==newItem.cash && this.equals(newItem);
        }

        @Nullable
        @Override
        public Bundle getChangePayload(@NonNull InfoWithCash newItem) {
            Bundle diff = new Bundle();
            if(this.cash!=newItem.cash)
                diff.putDouble(DIFF_CASH,newItem.cash);
            if(!this.equals(newItem))
                diff.putString(DIFF_NAME,newItem.cashBoxInfo.getName());

            if(diff.size()==0)
                return null;
            else
                return diff;
        }
    }

    // Immutable object in orderPos for cloneContents to be easier
    // When modifying directly, watch out, since an entry can be in cloned cashBoxes (no set methods)
    @Entity(tableName = "entries_table",
            foreignKeys = @ForeignKey(entity = CashBoxInfo.class, parentColumns = "id",
                    childColumns = "cashBoxId",onDelete = CASCADE, onUpdate = CASCADE),
            indices = {@Index(value = "cashBoxId")})
    public static class Entry implements Parcelable, Cloneable, DiffDbUsable<Entry> {
        @Ignore
        public static final long NO_GROUP = 0;
        @Ignore
        public static final String NO_INFO = "(No info)";
        @Ignore
        private static final String DIFF_AMOUNT = "amount";
        @Ignore
        private static final String DIFF_DATE = "date";
        @Ignore
        private static final String DIFF_INFO = "info";
        @Ignore
        public static final Parcelable.Creator<Entry> CREATOR = new Parcelable.Creator<Entry>() {
            @NonNull
            @Override
            public Entry createFromParcel(@NonNull Parcel source) {
                return new Entry(source);
            }

            @NonNull
            @Override
            public Entry[] newArray(int size) {
                return new Entry[size];
            }
        };

        @PrimaryKey(autoGenerate = true)
        private long id;
        private long cashBoxId;
        private double amount;
        private Calendar date;
        private String info;
        @ColumnInfo(defaultValue = "0")
        private long groupId;

        /**
         * Constructor for Room
         */
        public Entry(long cashBoxId, double amount, @Nullable String info, Calendar date, long groupId) {
            setInfo(info);
            this.groupId = groupId;
            this.date = date;
            setAmount(amount);
            this.cashBoxId = cashBoxId;
        }

        @Ignore
        public Entry(long cashBoxId, double amount, @Nullable String info, Calendar date) {
            this(cashBoxId,amount,info,date,NO_GROUP);
        }

        @Ignore
        public Entry(double amount, @NonNull String info, Calendar date, long groupId) {
            this(CashBoxInfo.NO_CASHBOX,amount,info,date,groupId);
        }

        @Ignore
        public Entry(double amount, @NonNull String info, Calendar date) {
            this(CashBoxInfo.NO_CASHBOX,amount,info,date);
        }

        @Ignore
        private Entry(@NonNull Parcel parcel) {
            setId(parcel.readLong());
            cashBoxId = parcel.readLong();
            setInfo(parcel.readString());
            date = Converters.fromTimestamp(parcel.readLong());
            setAmount(parcel.readDouble());
            this.groupId = parcel.readLong();
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

        @NonNull
        public String printInfo() {
            String string = info.isEmpty() ? NO_INFO : info;

            if(groupId==NO_GROUP)
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

        public long getGroupId() {
            return groupId;
        }

        public void setId(long id) {
            this.id = id;
        }

        public void setAmount(double amount) {
            this.amount = Util.roundTwoDecimals(amount);
        }

        public void setInfo(@Nullable String info) {
            if(!TextUtils.isEmpty(info))
                this.info = info.trim();
            else
                this.info = "";
        }

        @NonNull
        public Entry getEntryWithCashBoxId(long cashBoxId) {
            if(this.cashBoxId==cashBoxId)
                return this;

            Entry entry = this.cashBoxId!=CashBoxInfo.NO_CASHBOX ? this.cloneContents() : this;
            entry.cashBoxId = cashBoxId;
            return entry;
        }

        @Override
        @NonNull
        public String toString() {
            return toString(NumberFormat.getCurrencyInstance(),DateFormat.getDateInstance(DateFormat.SHORT));
        }

        @NonNull
        private String toString(@NonNull NumberFormat currencyFormat, @NonNull DateFormat dateFormat) {
            return dateFormat.format(date.getTime()) +
                    "\t\t" +
                    currencyFormat.format(amount) +
                    "\n" +
                    info;
        }

        /**
         * Clones the object without conserving the id and the cashBoxId
         * @return the new object, product of the cloning
         */
        @NonNull
        public Entry cloneContents() {
                Entry entry = clone();
                entry.id = 0;
                entry.cashBoxId = CashBoxInfo.NO_CASHBOX;
                entry.groupId = NO_GROUP;
                return entry;
        }

        /**
         * Clones the object conserving the id
         * @return the new object, product of the cloning
         */
        @NonNull
        @Override
        public Entry clone() {
            try {
                return (Entry) super.clone();
            } catch (CloneNotSupportedException e) { // won't happen
                LogUtil.error("PruebaCashBoxInfo","Cloning error",e);
                return null;
            }
        }

        // Parcelable implementation
        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(@NonNull Parcel dest, int flags) {
            dest.writeLong(id);
            dest.writeLong(cashBoxId);
            dest.writeString(info);
            dest.writeLong(Converters.calendarToTimestamp(date));
            dest.writeDouble(amount);
            dest.writeLong(groupId);
        }

        //Implements DiffDbUsable
        @Override
        public boolean areItemsTheSame(@NonNull Entry newItem) {
            return this.id==newItem.id;
        }

        @Override
        public boolean areContentsTheSame(@NonNull Entry newItem) {
            return this.amount==newItem.amount && this.date.equals(newItem.date)
                    && this.info.equals(newItem.info);
        }

        @Nullable
        @Override
        public Bundle getChangePayload(@NonNull Entry newItem) {
            Bundle diff = new Bundle();
            if(this.amount!=newItem.amount)
                diff.putDouble(DIFF_AMOUNT,newItem.amount);
            if(!this.date.equals(newItem.date))
                diff.putLong(DIFF_DATE, Converters.calendarToTimestamp(newItem.date));
            if(!this.info.equals(newItem.info))
                diff.putString(DIFF_INFO,newItem.info);

            if(diff.size()==0)
                return null;
            else
                return diff;
        }
    }
}