package com.vibal.utilities.modelsNew;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import com.vibal.utilities.util.LogUtil;

import java.util.Currency;
import java.util.Locale;

import static androidx.room.ColumnInfo.NOCASE;

@Entity(tableName = "cashBoxesInfo_table", indices = {@Index(value = "name", unique = true),
        @Index(value = "deleted")})
public class CashBoxInfo implements Cloneable {
    @Ignore
    public static final int NO_CASHBOX = 0;
    @Ignore
    public static final long NO_ORDER_ID = 0;
    @Ignore
    public static final int MAX_LENGTH_NAME = 15;
//    @Ignore
//    public static final long NO_ONLINE_ID = 0;

    @PrimaryKey(autoGenerate = true)
    protected long id;

    @NonNull
    @ColumnInfo(collate = NOCASE)
    private String name;

    private long orderId = NO_ORDER_ID;
    @ColumnInfo(defaultValue = "0")
    private boolean deleted = false;

    @ColumnInfo(defaultValue = "")
    private Currency currency = Currency.getInstance(Locale.getDefault());

//    @ColumnInfo(defaultValue = ""+NO_ONLINE_ID)
//    private long onlineId = NO_ONLINE_ID;

//    public CashBoxInfo(long id, @NonNull String name, long orderId, boolean deleted,
//                       Currency currency,long onlineId) {
public CashBoxInfo(long id, @NonNull String name, long orderId, boolean deleted, Currency currency) {
        this.id = id;
        this.name = name;
        this.orderId = orderId;
        this.deleted = deleted;
        this.currency = currency;
//        this.onlineId = onlineId;
    }

    @Ignore
    public CashBoxInfo(@NonNull String name) throws IllegalArgumentException {
        setName(name);
    }

    public long getId() {
        return id;
    }

//    public long getOnlineId() {
//        return onlineId;
//    }

    @NonNull
    public String getName() {
        return name;
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

    public long getOrderId() {
        return orderId;
    }

    public boolean isDeleted() {
        return deleted;
    }

    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
    }

    public Currency getCurrency() {
        return currency;
    }

    public void setCurrency(Currency currency) {
        this.currency = currency;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof CashBoxInfo)
            return ((CashBoxInfo) obj).getName().equalsIgnoreCase(this.getName());
        return false;
    }

    public boolean areContentsTheSame(@NonNull CashBoxInfo other) {
        return this.equals(other) && this.currency.equals(other.currency);
    }

    @Override
    public int hashCode() {
//        return name.toLowerCase().hashCode();
        return (int) id;
    }

    @Override
    public String toString() {
        return "CashBoxInfo{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", orderId=" + orderId +
                ", deleted=" + deleted +
                ", currency=" + currency +
//                ", onlineId=" + onlineId +
                '}';
    }

    /**
     * Clones the object without conserving the id, the orderId and the onlineId
     *
     * @return the new object, product of the cloning
     */
    @NonNull
    public CashBoxInfo cloneContents() {
        CashBoxInfo cashBoxInfo = clone();
        cashBoxInfo.id = NO_CASHBOX;
        cashBoxInfo.orderId = NO_ORDER_ID;
//        cashBoxInfo.onlineId = NO_ONLINE_ID;
        // Currency and deleted are maintained
        return cashBoxInfo;
    }

    /**
     * Clones the object
     *
     * @return the new object, product of the cloning
     */
    @NonNull
    @Override
    public CashBoxInfo clone() {
        try {
            return (CashBoxInfo) super.clone();
        } catch (CloneNotSupportedException e) { // won't happen
            LogUtil.error("PruebaCashBoxInfo", "Cloning error", e);
            return null;
        }
    }
}