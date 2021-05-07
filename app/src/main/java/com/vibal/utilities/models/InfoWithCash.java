package com.vibal.utilities.models;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.Embedded;
import androidx.room.Ignore;

import com.vibal.utilities.util.DiffDbUsable;

public class InfoWithCash implements Cloneable, DiffDbUsable<InfoWithCash> {
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
    private final double cash; //sum of amounts
    private final int changes; // if the CashBox has any unviewed changes


    @Ignore
    public InfoWithCash(String name) {
        this(new CashBoxInfoLocal(name), 0, 0);
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
