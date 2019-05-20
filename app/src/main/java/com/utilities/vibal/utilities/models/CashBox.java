package com.utilities.vibal.utilities.models;

import androidx.annotation.NonNull;

import java.io.Serializable;
import java.text.DateFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class CashBox implements Serializable {
    private static final long serialVersionUID = 2L;

    public static final int MAX_LENGTH_NAME = 15;

    private String name;
    private double cash; //sum of amounts
    private List<Entry> entries;

    public CashBox(String name) throws IllegalArgumentException {
        this(name,0,new ArrayList<Entry>());
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

    // Immutable object in order for clone to be easier
    public class Entry implements Serializable {
        private static final long serialVersionUID = 3L;

        private String cause;
        private Calendar date;
        private double amount;

        private Entry(double amount,String cause,Calendar date) {
            setCause(cause);
            this.date = date;
            this.amount = amount;
        }

        public String getCause() {
            return cause;
        }

        public Calendar getDate() {
            return date;
        }

        public double getAmount() {
            return amount;
        }

        // When modifying directly, watch out, since an entry can be in cloned cashBoxes
        private void setCause(String cause) {
            this.cause = cause.trim();
        }

        @Override
        @NonNull
        public String toString() {
            NumberFormat currencyFormat = NumberFormat.getCurrencyInstance();
            return DateFormat.getDateInstance().format(date.getTime()) + "\t\t" +
                    currencyFormat.format(amount) +
                    "\n" +
                    cause;
        }
    }

    public String getName() {
        return name;
    }

    public double getCash() {
        return cash;
    }

    public CashBox.Entry getEntry(int index) {
        return entries.get(index);
    }

    public String getInfo(int position) {
        return entries.get(position).getCause();
    }

    public Calendar getDate(int position) {
        return entries.get(position).getDate();
    }

    public double getAmount(int position) {
        return entries.get(position).getAmount();
    }

    /**
     * Sets the name of the CashBox
     * @param name The name of the CashBox
     * @throws IllegalArgumentException if the name is empty or its length exceeds the MAX_LENGTH_NAME
     */
    void setName(String name) throws IllegalArgumentException{
        name = name.trim();
        if(name.isEmpty())
            throw new IllegalArgumentException("Name cannot be empty");
        else if(name.length()>MAX_LENGTH_NAME)
            throw new IllegalArgumentException("Name cannot exceed " + MAX_LENGTH_NAME + " characters");
        else
            this.name=name;
    }

    /**
     * Adds a new entry to the CashBox. It adds it to the top of the list.
     * @param amount Amount to be added
     * @param cause Explanation of the addition (can be empty)
     * @param date Date in which it was added
     * @return Total cash after the addition
     */
    public double add(double amount,String cause,Calendar date) {
        return add(0,new Entry(amount,cause,date));
    }

    public double add(int index,double amount,String cause,Calendar date) {
        return add(index,new Entry(amount,cause,date));
    }

    /**
     * Used for undo operation
     * @param entry entry to add back to the CashBox
     * @return cash after adding
     */
    public double add(int index,CashBox.Entry entry) {
        cash += entry.getAmount();
        entries.add(index,entry);
        return cash;
    }

    public double addAll(List<Entry> entries) {
        this.entries.addAll(entries);
        updateCash();
        return cash;
    }

    private void updateCash() {
        cash = 0;
        for(Entry entry:entries)
            cash+=entry.getAmount();
    }

    /**
     * Removes an entry of the CashBox
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
    public List<Entry> clear(){
        cash=0;
        List<Entry> entriesRemoved = entries;
        entries = new ArrayList<Entry>();

        return entriesRemoved;
    }

    /**
     * Modifies an entry of the CashBox
     * @param amount Amount to be added
     * @param cause Explanation of the addition (can be empty)
     * @param date Date in which it was added
     * @param index Index to be modified
     * @return Total cash after the modification
     */
    public CashBox.Entry modify(int index,double amount,String cause,Calendar date){
        return modify(index, new Entry(amount,cause,date));
    }

    public CashBox.Entry modify(int index, CashBox.Entry modifiedEntry) {
        CashBox.Entry entry = entries.set(index, modifiedEntry);
        cash += modifiedEntry.getAmount() - entry.getAmount();
        return entry;
    }

    public int sizeEntries(){
        return entries.size();
    }

    public boolean isEmpty() {
        return entries.isEmpty();
    }

    @Override
    public boolean equals(Object obj){
        if(obj instanceof CashBox)
            return ((CashBox) obj).getName().equalsIgnoreCase(this.getName());
        return false;
    }

    @Override
    @SuppressWarnings("CloneDoesntCallSuperClone")
    public Object clone(){
        return new CashBox(name,cash,new ArrayList<Entry>(entries));
    }

    @Override
    @NonNull
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("*")
                .append(name)
                .append("*");
        for(Entry k : entries)
            builder.append("\n")
            .append(k.toString());
        builder.append("\n*TotalCash: ")
                .append(cash)
                .append("*");
        return builder.toString();
    }
}