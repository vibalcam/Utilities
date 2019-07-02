package com.utilities.vibal.utilities.models;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;
import androidx.room.Embedded;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;
import androidx.room.Relation;

import com.utilities.vibal.utilities.db.CashBoxInfo;
import com.utilities.vibal.utilities.util.Converters;
import com.utilities.vibal.utilities.util.LogUtil;

import java.text.DateFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import static androidx.room.ForeignKey.CASCADE;

public class CashBox implements Parcelable {
    public static final Parcelable.Creator<CashBox> CREATOR = new Parcelable.Creator<CashBox>() {
        @Override
        public CashBox createFromParcel(Parcel source) {
            return new CashBox(source);
        }

        @Override
        public CashBox[] newArray(int size) {
            return new CashBox[size];
        }
    };

    @Embedded
    private InfoWithCash infoWithCash;
    @Relation(parentColumn = "id", entityColumn = "cashBoxId")
    private List<Entry> entries;

    @Ignore
    public CashBox(String name) throws IllegalArgumentException {
        this(new InfoWithCash(name,0),new ArrayList<Entry>());
    }

    @Ignore
    public CashBox(String name, List<Entry> entries) throws  IllegalArgumentException {
        this.entries = entries;
        infoWithCash = new InfoWithCash(name,calculateCash(entries));
    }

    /**
     * You must ensure that cash is the sum of all the amounts.
     * Should not be used directly.
     */
    public CashBox(InfoWithCash infoWithCash, List<Entry> entries) throws IllegalArgumentException {
        this.infoWithCash = infoWithCash;
        this.entries = entries;
    }

    @Ignore
    public CashBox(Parcel parcel) {
        infoWithCash = InfoWithCash.CREATOR.createFromParcel(parcel);
        entries = parcel.createTypedArrayList(Entry.CREATOR);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        infoWithCash.writeToParcel(dest,flags);
        dest.writeTypedList(entries);
    }

    public InfoWithCash getInfoWithCash() {
        return infoWithCash;
    }

    public String getName() {
        return infoWithCash.getCashBoxInfo().getName();
    }

    public double getCash() {
        return infoWithCash.getCash();
    }

    public List<Entry> getEntries() {
        return entries;
    }

    public Entry getEntry(int index) {
        return entries.get(index);
    }

    public void setInfoWithCash(InfoWithCash infoWithCash) {
        this.infoWithCash = infoWithCash;
    }

    void setName(String name) throws IllegalArgumentException {
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
     * Deep clone of CashBox
     */
    @Override
    @SuppressWarnings("CloneDoesntCallSuperClone")
    public CashBox clone() {
        List<Entry> entryList = new ArrayList<>();
        for(Entry entry:entries)
            entryList.add(entry.clone());
        return new CashBox(infoWithCash.clone(),entryList);
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

    public static class InfoWithCash implements Parcelable, Cloneable {
        public static final Parcelable.Creator<InfoWithCash> CREATOR = new Parcelable.Creator<InfoWithCash>() {
            @Override
            public InfoWithCash createFromParcel(Parcel source) {
                return new InfoWithCash(source);
            }

            @Override
            public InfoWithCash[] newArray(int size) {
                return new InfoWithCash[size];
            }
        };

        @Embedded
        private final CashBoxInfo cashBoxInfo;
        private double cash; //sum of amounts

        @Ignore
        public InfoWithCash(String name, double cash) throws IllegalArgumentException {
            this(new CashBoxInfo(name),cash);
        }

        @Ignore
        public InfoWithCash(String name) throws IllegalArgumentException {
            this(name, 0);
        }

        public InfoWithCash(CashBoxInfo cashBoxInfo, double cash) {
            this.cashBoxInfo = cashBoxInfo;
            this.cash = cash;
        }

        @Ignore
        private InfoWithCash(Parcel parcel) {
            cashBoxInfo = new CashBoxInfo(parcel.readString());
            cashBoxInfo.setId(parcel.readLong());
            cash = parcel.readDouble();
        }

        public double getCash() {
            return cash;
        }

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

        @Override
        //@SuppressWarnings("CloneDoesntCallSuperClone")
        public InfoWithCash clone() {
            return new InfoWithCash(cashBoxInfo.clone(),cash);
        }

        // Implementation of Parcelable
        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeString(cashBoxInfo.getName());
            dest.writeLong(cashBoxInfo.getId());
            dest.writeDouble(cash);
        }
    }

    // Immutable object in orderPos for clone to be easier
    // When modifying directly, watch out, since an entry can be in cloned cashBoxes (no set methods)
    @Entity(tableName = "entries_table")
    public static class Entry implements Parcelable, Cloneable {
        @Ignore
        public static final int NO_CASHBOX = 0;
        @Ignore
        public static final Parcelable.Creator<Entry> CREATOR = new Parcelable.Creator<Entry>() {
            @Override
            public Entry createFromParcel(Parcel source) {
                return new Entry(source);
            }

            @Override
            public Entry[] newArray(int size) {
                return new Entry[size];
            }
        };

        @PrimaryKey(autoGenerate = true)
        private long id;
        @ForeignKey(entity = InfoWithCash.class,
                parentColumns = "id", childColumns = "cashBoxId",
                onDelete = CASCADE, onUpdate = CASCADE)
        private long cashBoxId;
        private final String info;
        private final Calendar date;
        private final double amount;

        /**
         * Constructor for Room
         */
        public Entry(long cashBoxId, double amount, @NonNull String info, Calendar date) {
            this.info = info.trim();
            this.date = date;
            this.amount = amount;
            this.cashBoxId = cashBoxId;
        }

        @Ignore
        public Entry(double amount, @NonNull String info, Calendar date) {
            this(NO_CASHBOX,amount,info,date);
        }

        @Ignore
        private Entry(@NonNull Parcel parcel) {
            id = parcel.readLong();
            cashBoxId = parcel.readLong();
            info = parcel.readString();
            date = Converters.fromTimestamp(parcel.readLong());
            amount = parcel.readDouble();
        }

        public long getId() {
            return id;
        }

        public long getCashBoxId() {
            return cashBoxId;
        }

        public String getInfo() {
            return info;
        }

        public Calendar getDate() {
            return date;
        }

        public double getAmount() {
            return amount;
        }

        public void setId(long id) {
            this.id = id;
        }

        public Entry getEntryWithCashBoxId(long cashBoxId) {
            if(this.cashBoxId==cashBoxId)
                return this;

            Entry entry = this.cashBoxId!=NO_CASHBOX ? this.clone() : this;
            entry.cashBoxId = cashBoxId;
            return entry;
        }

        @Override
        @NonNull
        public String toString() {
            return toString(NumberFormat.getCurrencyInstance(),DateFormat.getDateInstance(DateFormat.SHORT));
        }

        @NonNull
        private String toString(NumberFormat currencyFormat, DateFormat dateFormat) {
            return dateFormat.format(date.getTime()) +
                    "\t\t" +
                    currencyFormat.format(amount) +
                    "\n" +
                    info;
        }

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
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeLong(id);
            dest.writeLong(cashBoxId);
            dest.writeString(info);
            dest.writeLong(Converters.calendarToTimestamp(date));
            dest.writeDouble(amount);
        }
    }
}