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

import io.reactivex.Completable;
import io.reactivex.Single;

@Dao
public abstract class CashBoxDao {
//    @Query("SELECT COUNT(*) FROM cashBoxesInfo_table WHERE name=:name")
//    abstract int countCashBoxByName(String name);

//    @Transaction
//    @Query("SELECT * FROM cashBoxesInfo_table WHERE name=:name")
//    abstract LiveData<CashBox> getCashBoxByName(String name);

//    @Transaction
//    @Query("SELECT * FROM cashBoxesInfo_table ORDER BY id DESC")
//    abstract LiveData<List<CashBox>> getAllCashBoxes();

    @Query("SELECT * FROM cashBoxesInfo_table ORDER BY id DESC")
    abstract LiveData<List<CashBox.CashBoxInfo>> getAllCashBoxesInfo();

    @Transaction
    @Query("SELECT * FROM cashBoxesInfo_table WHERE id=:id")
    abstract LiveData<CashBox> getCashBoxById(int id);

    // Get all CashBoxInfo to supply the widget
    @Query("SELECT * FROM cashBoxesInfo_table ORDER BY id DESC")
    public abstract List<CashBox.CashBoxInfo> getAllCashBoxInfoForWidget();

    @Insert
    abstract Completable insert(CashBox.CashBoxInfo cashBoxInfo);

    @Update
    abstract Completable update(CashBox.CashBoxInfo cashBoxInfo);

    @Delete
    abstract Completable delete(CashBox.CashBoxInfo cashBoxInfo);

    @Query("DELETE FROM cashBoxesInfo_table")
    abstract Single<Integer> deleteAll();

//    @Query("UPDATE cashBoxesInfo_table SET orderPos=:newPos WHERE name=:name")
//    abstract void updateOrder(String name, int newPos);
//
//    @Transaction
//    void updateOrderAll(Collection<CashBox.CashBoxInfo> cashBoxInfos) {
//        for(CashBox.CashBoxInfo cashBoxInfo:cashBoxInfos)
//            updateOrder(cashBoxInfo.getName(),cashBoxInfo.getOrderPos());
//    }
}
