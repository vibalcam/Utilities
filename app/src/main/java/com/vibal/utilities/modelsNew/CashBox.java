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

public abstract class CashBox {
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
//    @NonNull
//    @Relation(parentColumn = "id", entityColumn = "cashBoxId", entity = EntryOnline.class)
//    private List<Entry> entries;

    @Ignore
    public CashBox(InfoWithCash infoWithCash) {
        this.infoWithCash = infoWithCash;
    }

    /**
     * Used to create a puppet CashBox
     *
     * @param name Name for the puppet CashBox
     */
    @Ignore
    public static CashBox create(String name) {
        return new CashBox.Local(new InfoWithCash(name), new ArrayList<>());
    }

    @Ignore
    public static CashBox createLocal(String name) throws IllegalArgumentException {
        return new CashBox.Local(InfoWithCash.createLocal(name), new ArrayList<>());
    }

//    /**
//     * You must ensure that cash is the sum of all the amounts.
//     * Should not be used directly.
//     */
//    public CashBox(InfoWithCash infoWithCash, @NonNull List<Entry> entries) throws IllegalArgumentException {
//        this.infoWithCash = infoWithCash;
//        this.entries = entries;
//    }

    @Ignore
    public static CashBox createOnline(String name) throws IllegalArgumentException {
        return new CashBox.Online(InfoWithCash.createOnline(name), new ArrayList<>());
    }

    @NonNull
    public InfoWithCash getInfoWithCash() {
        return infoWithCash;
    }

    public void setInfoWithCash(InfoWithCash infoWithCash) {
        this.infoWithCash = infoWithCash;
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
    abstract public List<Entry> getEntries();

    abstract public void setEntries(List<Entry> entries);

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
        for (Entry entry : getEntries())
            entryList.add(entry.cloneContents());
        if (this instanceof CashBox.Online)
            return new CashBox.Online(infoWithCash.cloneContents(), entryList);
        else if (this instanceof CashBox.Local)
            return new CashBox.Local(infoWithCash.cloneContents(), entryList);
        else // should never happen
            throw new RuntimeException("CashBox is not online nor local: cannot clone");
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
        for (Entry entry : getEntries())
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

    public static class Local extends CashBox {
        @NonNull
        @Relation(parentColumn = "id", entityColumn = "cashBoxId", entity = Entry.class)
        private List<Entry> entries;

        /**
         * You must ensure that cash is the sum of all the amounts.
         * Should not be used directly.
         */
        public Local(InfoWithCash infoWithCash, @NonNull List<Entry> entries) {
            super(infoWithCash);
            this.entries = entries;
        }

        @Override
        @NonNull
        public List<Entry> getEntries() {
            return entries;
        }

        @Override
        public void setEntries(@NonNull List<Entry> entries) {
            this.entries = entries;
        }
    }

    public static class Online extends CashBox {
        @NonNull
        @Relation(parentColumn = "id", entityColumn = "cashBoxId", entity = EntryOnline.class)
        private List<Entry> entries;

        /**
         * You must ensure that cash is the sum of all the amounts.
         * Should not be used directly.
         */
        public Online(InfoWithCash infoWithCash, @NonNull List<Entry> entries) {
            super(infoWithCash);
            this.entries = entries;
        }

        @Override
        @NonNull
        public List<Entry> getEntries() {
            return entries;
        }

        @Override
        public void setEntries(@NonNull List<Entry> entries) {
            this.entries = entries;
        }
    }

    public static class InfoWithCash implements Cloneable, DiffDbUsable<InfoWithCash> {
        public static final int CHANGE_NEW = -1;
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
        private int changes; // if the CashBox has any unviewed changes


        @Ignore
        public InfoWithCash(String name) {
            this(new CashBoxInfo(name), 0, 0);
        }

        public InfoWithCash(@NonNull CashBoxInfo cashBoxInfo, double cash, int changes) {
            this.cashBoxInfo = cashBoxInfo;
            this.cash = cash;
            this.changes = changes;
        }

        @Ignore
        public static InfoWithCash createLocal(String name) {
            return new InfoWithCash(new CashBoxInfoLocal(name), 0, 0);
        }

        @Ignore
        public static InfoWithCash createOnline(String name) {
            return new InfoWithCash(new CashBoxInfoOnline(name), 0, 0);
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
            return changes != 0;
        }

        public boolean isNew() {
            return changes == CHANGE_NEW;
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
                    ", changes=" + changes +
                    '}';
        }

        @NonNull
        public InfoWithCash cloneContents() {
            return new InfoWithCash(cashBoxInfo.cloneContents(), cash, changes);
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
                    this.changes == newItem.changes &&
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