package com.vibal.utilities.models;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.io.Serializable;
import java.text.DateFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class CashBox implements Serializable, Parcelable {
    public static final int MAX_LENGTH_NAME = 15;
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

    private static final long serialVersionUID = 2L;

    @Nullable
    private String name;
    private double cash; //sum of amounts
    @Nullable
    private List<Entry> entries;

    public CashBox(String name) throws IllegalArgumentException {
        this(name, 0, new ArrayList<>());
    }

    /**
     * You must ensure that cash is the sum of all the amounts.
     * Should not be used directly.
     */
    private CashBox(String name, double cash, List<Entry> entries) throws IllegalArgumentException {
        setName(name);
        this.cash = cash;
        this.entries = entries;
    }

    public CashBox(@NonNull Parcel parcel) {
        name = parcel.readString();
        cash = parcel.readDouble();
        entries = parcel.createTypedArrayList(Entry.CREATOR);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        dest.writeString(name);
        dest.writeDouble(cash);
        dest.writeTypedList(entries);
    }

    @Nullable
    public String getName() {
        return name;
    }

    /**
     * Sets the name of the CashBox
     *
     * @param name The name of the CashBox
     * @throws IllegalArgumentException if the name is empty or its length exceeds the MAX_LENGTH_NAME
     */
    void setName(String name) throws IllegalArgumentException {
        name = name.trim();
        if (name.isEmpty())
            throw new IllegalArgumentException("Name cannot be empty");
        else if (name.length() > MAX_LENGTH_NAME)
            throw new IllegalArgumentException("Name cannot exceed " + MAX_LENGTH_NAME + " characters");
        else
            this.name = name;
    }

    public double getCash() {
        return cash;
    }

    public CashBox.Entry getEntry(int index) {
        return entries.get(index);
    }

    @Nullable
    public String getInfo(int position) {
        return entries.get(position).getInfo();
    }

    public Calendar getDate(int position) {
        return entries.get(position).getDate();
    }

    public double getAmount(int position) {
        return entries.get(position).getAmount();
    }

    /**
     * Adds a new entry to the CashBox. It adds it to the top of the list.
     *
     * @param amount Amount to be added
     * @param cause  Explanation of the addition (can be empty)
     * @param date   Date in which it was added
     * @return Total cash after the addition
     */
    public double add(double amount, @NonNull String cause, Calendar date) {
        return add(0, new Entry(amount, cause, date));
    }

    public double add(int index, double amount, @NonNull String cause, Calendar date) {
        return add(index, new Entry(amount, cause, date));
    }

    /**
     * Used for undo operation
     *
     * @param entry entry to add back to the CashBox
     * @return cash after adding
     */
    public double add(int index, @NonNull CashBox.Entry entry) {
        cash += entry.getAmount();
        entries.add(index, entry);
        return cash;
    }

    public double addAll(@NonNull List<Entry> entries) {
        this.entries.addAll(entries);
        cash = updateCash();
        return cash;
    }

    private int updateCash() {
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
    public CashBox.Entry remove(int index) {
        cash -= entries.get(index).getAmount();
        return entries.remove(index);
    }

    /**
     * Clears all entries in the CashBox
     */
    @Nullable
    public List<Entry> clear() {
        cash = 0;
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
    public CashBox.Entry modify(int index, double amount, @NonNull String cause, Calendar date) {
        return modify(index, new Entry(amount, cause, date));
    }

    public CashBox.Entry modify(int index, @NonNull CashBox.Entry modifiedEntry) {
        CashBox.Entry entry = entries.set(index, modifiedEntry);
        cash += modifiedEntry.getAmount() - entry.getAmount();
        return entry;
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
            return ((CashBox) obj).getName().equalsIgnoreCase(this.getName());
        return false;
    }

    @NonNull
    @Override
    @SuppressWarnings("CloneDoesntCallSuperClone")
    public Object clone() {
        return new CashBox(name, cash, new ArrayList<>(entries));
    }

    @Override
    @NonNull
    public String toString() {
        NumberFormat currencyFormat = NumberFormat.getCurrencyInstance();
        DateFormat dateFormat = DateFormat.getDateInstance(DateFormat.SHORT);
        StringBuilder builder = new StringBuilder();

        builder.append("*")
                .append(name)
                .append("*");
        for (Entry entry : entries)
            builder.append("\n\n")
                    .append(entry.toString(currencyFormat,dateFormat));
        builder.append("\n*TotalCash: ")
                .append(currencyFormat.format(cash))
                .append("*");
        return builder.toString();
    }

    // Immutable object in order for clone to be easier
    // When modifying directly, watch out, since an entry can be in cloned cashBoxes (no set methods)
    public static class Entry implements Serializable, Parcelable {
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
        private static final long serialVersionUID = 3L;

        @Nullable
        private final String info;
        private final Calendar date;
        private final double amount;

        private Entry(double amount, @NonNull String info, Calendar date) {
            this.info = info.trim();
            this.date = date;
            this.amount = amount;
        }

        private Entry(@NonNull Parcel parcel) {
            info = parcel.readString();
            date = Calendar.getInstance();
            date.setTimeInMillis(parcel.readLong());
            amount = parcel.readDouble();
        }

        @Nullable
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
        private String toString(@NonNull NumberFormat currencyFormat, @NonNull DateFormat dateFormat) {
            return dateFormat.format(date.getTime()) +
                    "\t\t" +
                    currencyFormat.format(amount) +
                    "\n" +
                    info;
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(@NonNull Parcel dest, int flags) {
            dest.writeString(info);
            dest.writeLong(date.getTimeInMillis());
            dest.writeDouble(amount);
        }
    }
}