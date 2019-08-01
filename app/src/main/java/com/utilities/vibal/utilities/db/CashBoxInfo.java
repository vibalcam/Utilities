package com.utilities.vibal.utilities.db;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import com.utilities.vibal.utilities.util.LogUtil;

import static androidx.room.ColumnInfo.NOCASE;

@Entity(tableName = "cashBoxesInfo_table", indices = {@Index(value = "name", unique = true)})
public class CashBoxInfo implements Cloneable {
    @Ignore
    public static final int NO_CASHBOX = 0;
    @Ignore
    public static final long NO_ORDER_ID = 0;
    @Ignore
    public static final int MAX_LENGTH_NAME = 15;

    @PrimaryKey(autoGenerate = true)
    private long id;
    @NonNull
    @ColumnInfo(collate = NOCASE)
    private String name;
    private long orderId;

    public CashBoxInfo(@NonNull String name) throws IllegalArgumentException {
        setName(name);
    }

    public long getId() {
        return id;
    }

    @NonNull
    public String getName() {
        return name;
    }

    public long getOrderId() {
        return orderId;
    }

    public void setOrderId(long orderId) {
        this.orderId = orderId;
    }

    public void setId(long id) {
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
    public String toString() {
        return "CashBoxInfo{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", orderId=" + orderId +
                '}';
    }

    /**
     * Clones the object without conserving the id
     * @return the new object, product of the cloning
     */
    @Override
    public CashBoxInfo clone() {
        try {
            CashBoxInfo cashBoxInfo = (CashBoxInfo) super.clone();
            cashBoxInfo.id = 0;
            cashBoxInfo.orderId = NO_ORDER_ID;
            return cashBoxInfo;
        } catch (CloneNotSupportedException e) { // won't happen
            LogUtil.error("PruebaCashBoxInfo","Cloning error",e);
            return null;
        }
    }
}