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

import com.utilities.vibal.utilities.util.Converters;

import java.text.DateFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import static androidx.room.ForeignKey.CASCADE;

public class CashBox implements Parcelable,Cloneable {
    public static final int MAX_LENGTH_NAME = 15;
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
    private final CashBoxInfo cashBoxInfo;
    @Relation(parentColumn = "cashBoxInfo.name", entityColumn = "nameCashBox")
    private List<Entry> entries;

    public CashBox(String name) throws IllegalArgumentException {
        this(new CashBoxInfo(name,0),new ArrayList<Entry>());
    }

    public CashBox(String name, List<Entry> entries) throws  IllegalArgumentException {
        this.entries = entries;
        cashBoxInfo = new CashBoxInfo(name,calculateCash(entries));
    }

    /**
     * You must ensure that cash is the sum of all the amounts.
     * Should not be used directly.
     */
    public CashBox(CashBoxInfo cashBoxInfo, List<Entry> entries) throws IllegalArgumentException {
        this.cashBoxInfo = cashBoxInfo;
        this.entries = entries;
    }

    public CashBox(Parcel parcel) {
        cashBoxInfo = CashBoxInfo.CREATOR.createFromParcel(parcel);
        entries = parcel.createTypedArrayList(Entry.CREATOR);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        cashBoxInfo.writeToParcel(dest,flags);
        dest.writeTypedList(entries);
    }

    public CashBoxInfo getCashBoxInfo() {
        return cashBoxInfo;
    }

    public String getName() {
        return cashBoxInfo.getName();
    }

    public double getCash() {
        return cashBoxInfo.getCash();
    }

    public Entry getEntry(int index) {
        return entries.get(index);
    }

    public void setName(String name) throws IllegalArgumentException {
        cashBoxInfo.setName(name);
    }

    /**
     * Adds a new entry to the CashBox. It adds it to the top of the list.
     *
     * @param amount Amount to be added
     * @param cause  Explanation of the addition (can be empty)
     * @param date   Date in which it was added
     * @return Total cash after the addition
     */
    public double add(double amount, String cause, Calendar date) {
        return add(0, new Entry(amount, cause, date));
    }

    public double add(int index, double amount, String cause, Calendar date) {
        return add(index, new Entry(amount, cause, date));
    }

    /**
     * Used for undo operation
     *
     * @param entry entry to add back to the CashBox
     * @return cash after adding
     */
    public double add(int index, Entry entry) {
        cashBoxInfo.cash += entry.getAmount();
        entries.add(index,entry.getEntryOfCashBox(getName()));
        return cashBoxInfo.cash;
    }

    public double addAll(List<Entry> entries) {
        this.entries.addAll(entries);
        cashBoxInfo.cash += calculateCash(entries);
        return cashBoxInfo.cash;
    }

    private int calculateCash(@NonNull List<Entry> entries) {
        int sum = 0;
        for (Entry entry : entries)
            sum += entry.getAmount();
        return sum;
    }

    /**
     * Removes an entry of the CashBox
     *
     * @param index Index to be removed
     * @return Total cash after removing
     */
    public Entry remove(int index) {
        Entry removedEntry = entries.remove(index);
        cashBoxInfo.cash -= removedEntry.getAmount();
        return removedEntry;
    }

    /**
     * Clears all entries in the CashBox
     */
    public List<Entry> clear() {
        cashBoxInfo.cash = 0;
        List<Entry> entriesRemoved = entries;
        entries = new ArrayList<Entry>();
        return entriesRemoved;
    }

    /**
     * Modifies an entry of the CashBox
     *
     * @param amount Amount to be added
     * @param cause  Explanation of the addition (can be empty)
     * @param date   Date in which it was added
     * @param index  Index to be modified
     * @return Total cash after the modification
     */
    public Entry modify(int index, double amount, String cause, Calendar date) {
        return modify(index, new Entry(amount, cause, date));
    }

    public Entry modify(int index, Entry entry) {
        Entry modifiedEntry = entries.set(index, entry);
        cashBoxInfo.cash += entry.getAmount() - modifiedEntry.getAmount();
        return modifiedEntry;
    }

    public int sizeEntries() {
        return entries.size();
    }

    public boolean isEmpty() {
        return entries.isEmpty();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof CashBox)
            return ((CashBox) obj).getCashBoxInfo().equals(this.cashBoxInfo);
        return false;
    }

    /**
     * Shallow clone of CashBox
     */
    @Override
    @SuppressWarnings("CloneDoesntCallSuperClone")
    public CashBox clone() throws CloneNotSupportedException {
        return new CashBox(cashBoxInfo.clone(),new ArrayList<Entry>(entries));
    }

    @Override
    @NonNull
    public String toString() {
        NumberFormat currencyFormat = NumberFormat.getCurrencyInstance();
        DateFormat dateFormat = DateFormat.getDateInstance(DateFormat.SHORT);
        StringBuilder builder = new StringBuilder();

        builder.append("*")
                .append(cashBoxInfo.name)
                .append("*");
        for (Entry entry : entries)
            builder.append("\n\n")
                    .append(entry.toString(currencyFormat,dateFormat));
        builder.append("\n*TotalCash: ")
                .append(currencyFormat.format(cashBoxInfo.cash))
                .append("*");
        return builder.toString();
    }

    @Entity(tableName = "cashBoxesInfo_table")
    public static class CashBoxInfo implements Parcelable,Cloneable {
        @Ignore
        public static final Parcelable.Creator<CashBoxInfo> CREATOR = new Parcelable.Creator<CashBoxInfo>() {
            @Override
            public CashBoxInfo createFromParcel(Parcel source) {
                return new CashBoxInfo(source);
            }

            @Override
            public CashBoxInfo[] newArray(int size) {
                return new CashBoxInfo[size];
            }
        };

        @PrimaryKey
        private String name;
        private double cash; //sum of amounts

        public CashBoxInfo(String name, double cash) {
            setName(name);
            this.cash = cash;
        }

        public CashBoxInfo(String name) {
            this(name, 0);
        }

        private CashBoxInfo(Parcel parcel) {
            name = parcel.readString();
            cash = parcel.readDouble();
        }

        public String getName() {
            return name;
        }

        public double getCash() {
            return cash;
        }

        /**
         * Sets the name of the CashBox
         *
         * @param name The name of the CashBox
         * @throws IllegalArgumentException if the name is empty or its length exceeds the MAX_LENGTH_NAME
         */
        private void setName(String name) throws IllegalArgumentException {
            name = name.trim();
            if (name.isEmpty())
                throw new IllegalArgumentException("Name cannot be empty");
            else if (name.length() > MAX_LENGTH_NAME)
                throw new IllegalArgumentException("Name cannot exceed " + MAX_LENGTH_NAME + " characters");
            else
                this.name = name;
        }

        @Override
        public boolean equals(Object obj) {
            if(obj instanceof CashBoxInfo)
                return ((CashBoxInfo)obj).getName().equalsIgnoreCase(this.getName());
            return false;
        }

        @Override
        protected CashBoxInfo clone() throws CloneNotSupportedException {
            return (CashBoxInfo) super.clone();
        }

        // Implementation of Parcelable
        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeString(name);
            dest.writeDouble(cash);
        }
    }

    // Immutable object in order for clone to be easier
    // When modifying directly, watch out, since an entry can be in cloned cashBoxes (no set methods)
    @Entity(tableName = "entries_table")
    public static class Entry implements Parcelable {
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
        private int id;
        @ForeignKey(entity = CashBoxInfo.class,
                parentColumns = "name", childColumns = "nameCashBox",
                onDelete = CASCADE, onUpdate = CASCADE)
        private String nameCashBox;
        private final String info;
        private final Calendar date;
        private final double amount;

        /**
         * Do NOT use this constructor. Only for Room.
         */
        public Entry(String nameCashBox, double amount, @NonNull String info, Calendar date) {
            this(amount,info,date);
            this.nameCashBox = nameCashBox;
        }

        @Ignore
        public Entry (double amount, @NonNull String info, Calendar date) {
            this.info = info.trim();
            this.date = date;
            this.amount = amount;
        }

        private Entry(@NonNull Parcel parcel) {
            id = parcel.readInt();
            nameCashBox = parcel.readString();
            info = parcel.readString();
            date = Converters.fromTimestamp(parcel.readLong());
            amount = parcel.readDouble();
        }

        public void setId(int id) {
            this.id = id;
        }

        private Entry getEntryOfCashBox(String nameCashBox) {
            if (this.nameCashBox != null) {
                if(new CashBoxInfo(nameCashBox).equals(new CashBoxInfo(this.nameCashBox)))
                    return this;
                else
                    return new Entry(nameCashBox,amount,info,date);
            } else {
                this.nameCashBox = nameCashBox;
                return this;
            }
        }

        public int getId() {
            return id;
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

        // Parcelable implementation
        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeInt(id);
            dest.writeString(nameCashBox);
            dest.writeString(info);
            dest.writeLong(Converters.calendarToTimestamp(date));
            dest.writeDouble(amount);
        }
    }
}