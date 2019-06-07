package com.utilities.vibal.utilities.db;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Transaction;
import androidx.room.Update;

import com.utilities.vibal.utilities.models.CashBox;

import java.util.Collection;
import java.util.List;

@Dao
public abstract class CashBoxDao {
    @Transaction
    @Query("SELECT * FROM cashBoxesInfo_table WHERE name=:name")
    abstract LiveData<CashBox> getCashBox(String name);

    @Transaction
    @Query("SELECT * FROM cashBoxesInfo_table ORDER BY orderPos ASC")
    abstract LiveData<List<CashBox>> getAllCashBoxes();

    @Query("SELECT * FROM cashBoxesInfo_table ORDER BY orderPos ASC")
    abstract LiveData<List<CashBox.CashBoxInfo>> getAllCashBoxInfo();

    @Insert
    abstract void insert(CashBox.CashBoxInfo cashBoxInfo);

    @Delete
    abstract void delete(CashBox.CashBoxInfo cashBoxInfo);

    @Query("DELETE FROM cashBoxesInfo_table")
    abstract void deleteAll();

    @Update
    abstract void update(CashBox.CashBoxInfo cashBoxInfo);

    @Query("UPDATE cashBoxesInfo_table SET orderPos=:newPos WHERE name=:name")
    abstract void updateOrder(String name, int newPos);

    @Transaction
    void updateOrderAll(Collection<CashBox.CashBoxInfo> cashBoxInfos) {
        for(CashBox.CashBoxInfo cashBoxInfo:cashBoxInfos)
            updateOrder(cashBoxInfo.getName(),cashBoxInfo.getOrderPos());
    }
}
