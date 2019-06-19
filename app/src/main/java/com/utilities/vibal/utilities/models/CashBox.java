package com.utilities.vibal.utilities.models;

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

import com.utilities.vibal.utilities.util.Converters;

import java.text.DateFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Objects;

import static androidx.room.ForeignKey.CASCADE;

public class CashBox implements Parcelable {
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
    @Relation(parentColumn = "id", entityColumn = "cashBoxId")
    private List<Entry> entries;

    @Ignore
    public CashBox(String name) throws IllegalArgumentException {
        this(new CashBoxInfo(name,0),new ArrayList<Entry>());
    }

    @Ignore
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

    @Ignore
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

    public List<Entry> getEntries() {
        return entries;
    }

    public Entry getEntry(int index) {
        return entries.get(index);
    }

    void setName(String name) throws IllegalArgumentException {
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
        return add(0, amount, cause, date);
    }

    public double add(int index, double amount, String cause, Calendar date) {
        cashBoxInfo.cash += amount;
        entries.add(index, new Entry(cashBoxInfo.getId(),amount,cause,date));
        return getCash();
    }

    public double add(int index, Entry entry) {
        return add(index, entry.amount, entry.info, entry.date);
    }

    public double addAll(List<Entry> entries) {
        int size = entries.size()-1;
        for(Entry entry: entries)
            add(size++, entry);

        return getCash();
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
        entries = new ArrayList<>();
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
        Entry modifiedEntry = entries.set(index, new Entry(cashBoxInfo.getId(), amount, cause, date));
        cashBoxInfo.cash += amount - modifiedEntry.getAmount();
        return modifiedEntry;
    }

    public Entry modify(int index, Entry entry) throws IllegalArgumentException {
        return modify(index, entry.amount, entry.info, entry.date);
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
    public CashBox clone() {
        List<Entry> entryList = new ArrayList<>();
        for(Entry entry:entries)
            entryList.add(entry.clone());
        return new CashBox(cashBoxInfo.clone(),entryList);
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

    @Entity(tableName = "cashBoxesInfo_table", indices = {@Index(value = "name", unique = true)})
    public static class CashBoxInfo implements Parcelable {
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

        @PrimaryKey(autoGenerate = true)
        private int id;
        @NonNull
        private String name;
        private double cash; //sum of amounts

        public CashBoxInfo(String name, double cash) throws IllegalArgumentException {
            setName(name);
            this.cash = cash;
        }

        @Ignore
        public CashBoxInfo(String name) throws IllegalArgumentException {
            this(name, 0);
        }

        @Ignore
        private CashBoxInfo(Parcel parcel) {
            id = parcel.readInt();
            name = Objects.requireNonNull(parcel.readString());
            cash = parcel.readDouble();
        }

        public int getId() {
            return id;
        }

        @NonNull
        public String getName() {
            return name;
        }

        public double getCash() {
            return cash;
        }

        public void setId(int id) {
            this.id = id;
        }

        /**
         * Sets the name of the CashBox
         *
         * @param name The name of the CashBox
         * @throws IllegalArgumentException if the name is empty or its length exceeds the MAX_LENGTH_NAME
         */
        void setName(@NonNull String name) throws IllegalArgumentException {
            name = name.trim();
            if (name.isEmpty())
                throw new IllegalArgumentException("Name cannot be empty");
            else if (name.length() > MAX_LENGTH_NAME)
                throw new IllegalArgumentException("Name cannot exceed " + MAX_LENGTH_NAME + " characters");

            this.name = name;
        }

        @Override
        public boolean equals(Object obj) {
            if(obj instanceof CashBoxInfo)
                return ((CashBoxInfo)obj).getName().equalsIgnoreCase(this.getName());
            return false;
        }

        @Override
        @SuppressWarnings("CloneDoesntCallSuperClone")
        public CashBoxInfo clone() {
            CashBoxInfo cashBoxInfo = new CashBoxInfo(name,cash);
            cashBoxInfo.setId(id);
            return cashBoxInfo;
        }

        // Implementation of Parcelable
        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeInt(id);
            dest.writeString(name);
            dest.writeDouble(cash);
        }
    }

    // Immutable object in orderPos for clone to be easier
    // When modifying directly, watch out, since an entry can be in cloned cashBoxes (no set methods)
    @Entity(tableName = "entries_table")
    public static class Entry implements Parcelable {
        @Ignore
        public static final int NO_CASHBOX = -1;
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
                parentColumns = "id", childColumns = "cashBoxId",
                onDelete = CASCADE, onUpdate = CASCADE)
        private int cashBoxId;
        private final String info;
        private final Calendar date;
        private final double amount;

        /**
         * Constructor for Room
         */
        public Entry(int cashBoxId, double amount, @NonNull String info, Calendar date) {
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
            id = parcel.readInt();
            cashBoxId = parcel.readInt();
            info = parcel.readString();
            date = Converters.fromTimestamp(parcel.readLong());
            amount = parcel.readDouble();
        }

        public int getId() {
            return id;
        }

        public int getCashBoxId() {
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

        public void setId(int id) {
            this.id = id;
        }

        public Entry getEntryWithCashBoxId(int cashBoxId) {
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
        @SuppressWarnings("CloneDoesntCallSuperClone")
        public Entry clone() {
            return new Entry(amount,info,date);
        }

        // Parcelable implementation
        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeInt(id);
            dest.writeInt(cashBoxId);
            dest.writeString(info);
            dest.writeLong(Converters.calendarToTimestamp(date));
            dest.writeDouble(amount);
        }
    }
}