package com.vibal.utilities.modelsNew;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.Embedded;
import androidx.room.Ignore;
import androidx.room.Relation;

import com.vibal.utilities.util.DiffDbUsable;

import java.text.DateFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;

public class CashBox {
//    public static final Parcelable.Creator<CashBox> CREATOR = new Parcelable.Creator<CashBox>() {
//        @NonNull
//        @Override
//        public CashBox createFromParcel(@NonNull Parcel source) {
//            return new CashBox(source);
//        }
//
//        @NonNull
//        @Override
//        public CashBox[] newArray(int size) {
//            return new CashBox[size];
//        }
//    };

    @Embedded
    private InfoWithCash infoWithCash;
    @NonNull
    @Relation(parentColumn = "id", entityColumn = "cashBoxId")
    private List<Entry> entries;

    @Ignore
    public static CashBox createLocal(String name) throws IllegalArgumentException {
        return new CashBox(InfoWithCash.createLocal(name), new ArrayList<>());
    }

    @Ignore
    public static CashBox createOnline(String name) throws IllegalArgumentException {
        return new CashBox(InfoWithCash.createOnline(name), new ArrayList<>());
    }

    /**
     * Used to create a puppet CashBox
     * @param name Name for the puppet CashBox
     */
    @Ignore
    public CashBox(String name) {
        this(new InfoWithCash(name), new ArrayList<>());
    }

//    @Ignore
//    public CashBox(@NonNull String name) throws IllegalArgumentException {
//        this(new InfoWithCash(name, 0), new ArrayList<>());
//    }

//    @Ignore
//    public CashBox(@NonNull String name, @NonNull List<Entry> entries) throws IllegalArgumentException {
//        this.entries = entries;
//        infoWithCash = new InfoWithCash(name, calculateCash(entries));
//    }

    /**
     * You must ensure that cash is the sum of all the amounts.
     * Should not be used directly.
     */
    public CashBox(InfoWithCash infoWithCash, @NonNull List<Entry> entries) throws IllegalArgumentException {
        this.infoWithCash = infoWithCash;
        this.entries = entries;
    }

//    @Ignore
//    public CashBox(@NonNull Parcel parcel) {
//        infoWithCash = InfoWithCash.CREATOR.createFromParcel(parcel);
//        entries = parcel.createTypedArrayList(Entry.CREATOR);
//    }

//    @Override
//    public int describeContents() {
//        return 0;
//    }
//
//    @Override
//    public void writeToParcel(@NonNull Parcel dest, int flags) {
//        infoWithCash.writeToParcel(dest,flags);
//        dest.writeTypedList(entries);
//    }

    @NonNull
    public InfoWithCash getInfoWithCash() {
        return infoWithCash;
    }

    public void setInfoWithCash(InfoWithCash infoWithCash) {
        this.infoWithCash = infoWithCash;
    }

    @NonNull
    public String getName() {
        return infoWithCash.getCashBoxInfo().getName();
    }

    public void setName(@NonNull String name) throws IllegalArgumentException {
        infoWithCash.getCashBoxInfo().setName(name);
    }

    public double getCash() {
        return infoWithCash.getCash();
    }

    @NonNull
    public List<Entry> getEntries() {
        return entries;
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
    public CashBox cloneContents() {
        List<Entry> entryList = new ArrayList<>();
        for (Entry entry : entries)
            entryList.add(entry.cloneContents());
        return new CashBox(infoWithCash.cloneContents(), entryList);
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
                    .append(entry.toString(currencyFormat, dateFormat));
        builder.append("\n*TotalCash: ")
                .append(currencyFormat.format(infoWithCash.cash))
                .append("*");
        return builder.toString();
    }

    @Override
    public int hashCode() {
        return infoWithCash.hashCode();
    }


    public static class InfoWithCash implements Cloneable, DiffDbUsable<InfoWithCash> {
        private static final String DIFF_CASH = "cash";
        private static final String DIFF_NAME = "name";
//        public static final Parcelable.Creator<InfoWithCash> CREATOR = new Parcelable.Creator<InfoWithCash>() {
//            @NonNull
//            @Override
//            public InfoWithCash createFromParcel(@NonNull Parcel source) {
//                return new InfoWithCash(source);
//            }
//
//            @NonNull
//            @Override
//            public InfoWithCash[] newArray(int size) {
//                return new InfoWithCash[size];
//            }
//        };

        @NonNull
        @Embedded
        private final CashBoxInfo cashBoxInfo;
        private double cash; //sum of amounts
        private boolean hasChanges; // if the CashBox has any unviewed changes


        @Ignore
        public static InfoWithCash createLocal(String name) {
            return new InfoWithCash(new CashBoxInfoLocal(name),0, false);
        }

        @Ignore
        public static InfoWithCash createOnline(String name) {
            return new InfoWithCash(new CashBoxInfoOnline(name),0, false);
        }

        @Ignore
        public InfoWithCash (String name) {
            this(new CashBoxInfo(name), 0, false);
        }

//        @Ignore
//        public InfoWithCash(@NonNull String name, double cash) throws IllegalArgumentException {
//            this(new CashBoxInfo(name), cash);
//        }
//
//        @Ignore
//        public InfoWithCash(@NonNull String name) throws IllegalArgumentException {
//            this(name, 0);
//        }

        public InfoWithCash(@NonNull CashBoxInfo cashBoxInfo, double cash, boolean hasChanges) {
            this.cashBoxInfo = cashBoxInfo;
            this.cash = cash;
            this.hasChanges = hasChanges;
        }

//        @Ignore
//        private InfoWithCash(@NonNull Parcel parcel) {
//            cashBoxInfo = new CashBoxInfo(parcel.readLong(),parcel.readString(),
//                    parcel.readLong(),parcel.readBoolean());
//            cash = parcel.readDouble();
//        }

        public long getId() {
            return cashBoxInfo.getId();
        }

        public double getCash() {
            return cash;
        }

        public boolean hasChanges() {
            return hasChanges;
        }

        @NonNull
        public CashBoxInfo getCashBoxInfo() {
            return cashBoxInfo;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj instanceof InfoWithCash)
                return ((InfoWithCash) obj).getCashBoxInfo().equals(cashBoxInfo);
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
            return new InfoWithCash(cashBoxInfo.cloneContents(), cash, hasChanges);
        }

        // Implementation of Parcelable
//        @Override
//        public int describeContents() {
//            return 0;
//        }
//
//        @Override
//        public void writeToParcel(@NonNull Parcel dest, int flags) {
//            dest.writeLong(cashBoxInfo.getId());
//            dest.writeString(cashBoxInfo.getName());
//            dest.writeLong(cashBoxInfo.getOrderId());
//            dest.writeBoolean(cashBoxInfo.isDeleted());
//            dest.writeDouble(cash);
//        }

        //Implements DiffDbUsable
        @Override
        public boolean areItemsTheSame(@NonNull InfoWithCash newItem) {
            return this.getId() == newItem.getId();
        }

        @Override
        public boolean areContentsTheSame(@NonNull InfoWithCash newItem) {
            return this.cash == newItem.cash &&
                    this.cashBoxInfo.areContentsTheSame(newItem.cashBoxInfo);
        }

        @Nullable
        @Override
        public Bundle getChangePayload(@NonNull InfoWithCash newItem) {
            Bundle diff = new Bundle();
            if (this.cash != newItem.cash)
                diff.putDouble(DIFF_CASH, newItem.cash);
            if (!this.equals(newItem))
                diff.putString(DIFF_NAME, newItem.cashBoxInfo.getName());

            if (diff.size() == 0)
                return null;
            else
                return diff;
        }
    }
}