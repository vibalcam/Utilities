package com.utilities.vibal.utilities.models;

import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;
import androidx.room.Embedded;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Ignore;
import androidx.room.Index;
import androidx.room.PrimaryKey;
import androidx.room.Relation;

import com.utilities.vibal.utilities.db.CashBoxInfo;
import com.utilities.vibal.utilities.util.Converters;
import com.utilities.vibal.utilities.util.DiffDbUsable;
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

    public void setInfoWithCash(InfoWithCash infoWithCash) {
        this.infoWithCash = infoWithCash;
    }

    public void setName(String name) throws IllegalArgumentException {
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
        public String toString() {
            return "InfoWithCash{" +
                    "cashBoxInfo=" + cashBoxInfo.toString() +
                    ", cash=" + cash +
                    '}';
        }

        public InfoWithCash cloneContents() {
            return new InfoWithCash(cashBoxInfo.cloneContents(),cash);
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

        //Implement DiffDbUsable
        @Override
        public long getId() {
            return cashBoxInfo.getId();
        }

        @Override
        public boolean areContentsTheSame(InfoWithCash newItem) {
            return this.cash==newItem.cash && this.equals(newItem);
        }

        @Override
        public Bundle getChangePayload(InfoWithCash newItem) {
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
    @Entity(tableName = "entries_table", foreignKeys = @ForeignKey(entity = CashBoxInfo.class,
            parentColumns = "id", childColumns = "cashBoxId",onDelete = CASCADE, onUpdate = CASCADE),
            indices = {@Index(value = "cashBoxId")})
    public static class Entry implements Parcelable, Cloneable, DiffDbUsable<Entry> {
        @Ignore
        private static final String DIFF_AMOUNT = "amount";
        @Ignore
        private static final String DIFF_DATE = "date";
        @Ignore
        private static final String DIFF_INFO = "info";
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
            this(CashBoxInfo.NO_CASHBOX,amount,info,date);
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
        private String toString(NumberFormat currencyFormat, DateFormat dateFormat) {
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
        public Entry cloneContents() {
                Entry entry = clone();
                entry.id = 0;
                entry.cashBoxId = CashBoxInfo.NO_CASHBOX;
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
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeLong(id);
            dest.writeLong(cashBoxId);
            dest.writeString(info);
            dest.writeLong(Converters.calendarToTimestamp(date));
            dest.writeDouble(amount);
        }

        //Implements DiffDbUsable
        @Override
        public boolean areContentsTheSame(Entry newItem) {
            return this.amount==newItem.amount && this.date.equals(newItem.date)
                    && this.info.equals(newItem.info);
        }

        @Override
        public Bundle getChangePayload(Entry newItem) {
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