package com.utilities.vibal.utilities.db;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import com.utilities.vibal.utilities.models.CashBox;
import com.utilities.vibal.utilities.util.LogUtil;

import java.util.Objects;

@Entity(tableName = "cashBoxesInfo_table", indices = {@Index(value = "name", unique = true)})
public class CashBoxInfo {
    @Ignore
    public static final int MAX_LENGTH_NAME = 15;

    @PrimaryKey(autoGenerate = true)
    private int id;
    @NonNull
    private String name;

    public CashBoxInfo(String name) throws IllegalArgumentException {
        setName(name);
    }

    public int getId() {
        return id;
    }

    @NonNull
    public String getName() {
        return name;
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
    public void setName(@NonNull String name) throws IllegalArgumentException {
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
    public int hashCode() {
        return name.toLowerCase().hashCode();
    }

    @Override
    public CashBoxInfo clone() {
        try {
            return (CashBoxInfo) super.clone();
        } catch (CloneNotSupportedException e) { // won't happen
            LogUtil.error("PruebaCashBoxInfo","Cloning error",e);
            return null;
        }
    }
}