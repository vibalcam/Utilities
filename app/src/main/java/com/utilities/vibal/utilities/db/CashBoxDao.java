package com.utilities.vibal.utilities.db;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Transaction;
import androidx.room.Update;

import com.utilities.vibal.utilities.models.CashBox;

import java.util.List;

@Dao
public abstract class CashBoxDao {
    @Query("SELECT COUNT(*) FROM cashBoxesInfo_table WHERE name=:name")
    abstract int existsCashBox(String name);

//    @Transaction
//    @Query("SELECT * FROM cashBoxesInfo_table WHERE name=:name")
//    abstract LiveData<CashBox> getCashBoxByName(String name);

//    @Query("SELECT * FROM cashBoxesInfo_table ORDER BY orderPos ASC")
//    abstract LiveData<List<CashBox.CashBoxInfo>> getAllCashBoxInfo();

    @Transaction
    @Query("SELECT * FROM cashBoxesInfo_table ORDER BY id DESC")
    abstract LiveData<List<CashBox>> getAllCashBoxes();

    @Insert
    abstract void insert(CashBox.CashBoxInfo cashBoxInfo);

    @Update
    abstract void update(CashBox.CashBoxInfo cashBoxInfo);

    @Delete
    abstract void delete(CashBox.CashBoxInfo cashBoxInfo);

    @Query("DELETE FROM cashBoxesInfo_table")
    abstract void deleteAll();

//    @Query("UPDATE cashBoxesInfo_table SET orderPos=:newPos WHERE name=:name")
//    abstract void updateOrder(String name, int newPos);
//
//    @Transaction
//    void updateOrderAll(Collection<CashBox.CashBoxInfo> cashBoxInfos) {
//        for(CashBox.CashBoxInfo cashBoxInfo:cashBoxInfos)
//            updateOrder(cashBoxInfo.getName(),cashBoxInfo.getOrderPos());
//    }
}
