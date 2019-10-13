package com.vibal.utilities.models;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class CashBoxManager implements Serializable, Parcelable {
    public static final Parcelable.Creator<CashBoxManager> CREATOR = new Parcelable.Creator<CashBoxManager>() {
        @NonNull
        @Override
        public CashBoxManager createFromParcel(@NonNull Parcel source) {
            return new CashBoxManager(source);
        }

        @NonNull
        @Override
        public CashBoxManager[] newArray(int size) {
            return new CashBoxManager[size];
        }
    };

    private static final long serialVersionUID = 1L;

    private static final String TAG = "PruebaCashBoxItem";
    @Nullable
    private final List<CashBox> cashBoxes;

    public CashBoxManager() {
        cashBoxes = new ArrayList<>();
    }

    public CashBoxManager(@NonNull Parcel parcel) {
        cashBoxes = parcel.createTypedArrayList(CashBox.CREATOR);
    }

    public CashBox get(int pos) {
        return cashBoxes.get(pos);
    }

    public int size() {
        return cashBoxes.size();
    }

    public boolean isEmpty() {
        return cashBoxes.isEmpty();
    }

    /**
     * Adds a cashBox
     *
     * @param cashBox cashBox to be added
     * @return true if this list doesn't contain the specified element
     */
    public boolean add(CashBox cashBox) {
        if (!cashBoxes.contains(cashBox))
            return cashBoxes.add(cashBox);
        return false;
    }

    public boolean add(int index, CashBox cashBox) {
        if (!cashBoxes.contains(cashBox)) {
            cashBoxes.add(index, cashBox);
            return true;
        }
        return false;
    }

    /**
     * Changes the name of an element
     *
     * @param pos     the position of the CashBox to be renamed
     * @param newName the new name of the CashBox
     * @return true if this list doesn't contain another CashBox with the same name
     * @throws IllegalArgumentException if the name is empty or its length exceeds CashBox.MAX_LENGTH_NAME
     */
    public boolean changeName(int pos, String newName) throws IllegalArgumentException {
        int index = cashBoxes.indexOf(new CashBox(newName));
        if (index == -1) {
            cashBoxes.get(pos).setName(newName);
            return true;
        } else
            return index == pos;
    }

    public CashBox remove(int pos) {
        return cashBoxes.remove(pos);
    }

    public void clear() {
        cashBoxes.clear();
    }

    public boolean set(int pos, CashBox cashBox) {
        if (!cashBoxes.contains(cashBox)) {
            cashBoxes.set(pos, cashBox);
            return true;
        }
        return false;
    }

    public void move(int oldPos, int newPos) throws IndexOutOfBoundsException {
        if (oldPos < 0 || newPos >= cashBoxes.size())
            throw new IndexOutOfBoundsException("Trying to move from " + oldPos + " to " + newPos);
        else if (oldPos != newPos) {   // if both are the same position, no need to move
            CashBox cashBox = cashBoxes.remove(oldPos);
            cashBoxes.add(newPos, cashBox);
        }
    }

    public boolean duplicate(int index, String newName) throws IllegalArgumentException {
        CashBox cashBox = (CashBox) cashBoxes.get(index).clone();
        cashBox.setName(newName);
        return this.add(index + 1, cashBox);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        dest.writeTypedList(cashBoxes);
    }
}
