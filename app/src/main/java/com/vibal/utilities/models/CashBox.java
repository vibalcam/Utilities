package com.vibal.utilities.models;

import androidx.annotation.NonNull;
import androidx.room.Embedded;
import androidx.room.Ignore;

import java.text.DateFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
    private Set<String> cacheNames;
    @NonNull
    private List<? extends EntryBase<?>> entries;

    @Ignore
    public CashBox(InfoWithCash infoWithCash, @NonNull Collection<String> cacheNames,
                   @NonNull List<? extends EntryBase<?>> entries) {
        this.infoWithCash = infoWithCash;
        this.cacheNames = new HashSet<>(cacheNames);
        this.entries = entries;
    }

    /**
     * Used to create a puppet CashBox
     *
     * @param name Name for the puppet CashBox
     */
    @Ignore
    public static CashBox create(String name) {
        return new CashBox(new InfoWithCash(name), new HashSet<>(), new ArrayList<>());
    }

//    @Ignore
//    public static CashBox createLocal(String name) throws IllegalArgumentException {
//        return new CashBox.Local(InfoWithCash.createLocal(name), new HashSet<>(), new ArrayList<>());
//    }
//
//    @Ignore
//    public static CashBox createOnline(String name) throws IllegalArgumentException {
//        return new CashBox.Online(InfoWithCash.createOnline(name), new HashSet<>(), new ArrayList<>());
//    }

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
//        entries = parcel.createTypedArrayList(EntryBase.CREATOR);
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
    public List<? extends EntryBase<?>> getEntries() {
        return entries;
    }

    public void setEntries(@NonNull List<? extends EntryBase<?>> entries) {
        this.entries = entries;
    }

    @NonNull
    public Set<String> getCacheNames() {
        return cacheNames;
    }

    public void setCacheNames(@NonNull Set<String> cacheNames) {
        this.cacheNames = cacheNames;
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
        List<EntryBase<?>> entryList = new ArrayList<>();
        for (EntryBase<?> entry : getEntries())
            entryList.add(entry.cloneContents());
        return new CashBox(infoWithCash.cloneContents(), cacheNames, entryList);
//        if (this instanceof CashBox.Online)
//            return new CashBox.Online(infoWithCash.cloneContents(), entryList, cacheNames);
//        else if (this instanceof CashBox.Local)
//            return new CashBox.Local(infoWithCash.cloneContents(), entryList, cacheNames);
//        else // should never happen
//            throw new RuntimeException("CashBox is not online nor local: cannot clone. Should never happen");
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
        for (EntryBase<?> entry : getEntries())
            builder.append("\n\n")
                    .append(entry.getEntryInfo().toString(currencyFormat, dateFormat));
        builder.append("\n*TotalCash: ")
                .append(currencyFormat.format(infoWithCash.getCash()))
                .append("*");
        return builder.toString();
    }

    @Override
    public int hashCode() {
        return infoWithCash.hashCode();
    }
}