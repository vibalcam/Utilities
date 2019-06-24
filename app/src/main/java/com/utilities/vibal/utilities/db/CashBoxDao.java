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

//    @Query("SELECT * FROM cashBoxesInfo_table ORDER BY id DESC")
    @Query("SELECT C.id,C.name,SUM(amount) AS cash FROM cashBoxesInfo_table AS C " +
            "LEFT JOIN entries_table AS E ON C.id=E.cashBoxId " +
            "GROUP BY C.id,C.name")
    abstract LiveData<List<CashBox.InfoWithCash>> getAllCashBoxesInfo();

    @Transaction
//    @Query("SELECT * FROM cashBoxesInfo_table WHERE id=:id")
    @Query("SELECT C.id,C.name,SUM(amount) AS cash FROM cashBoxesInfo_table AS C " +
            "LEFT JOIN entries_table AS E ON C.id=E.cashBoxId " +
            "WHERE C.id=:id " +
            "GROUP BY C.id,C.name")
    abstract LiveData<CashBox> getCashBoxById(int id);

    // Get all CashBoxInfo to supply the widget
//    @Query("SELECT * FROM cashBoxesInfo_table ORDER BY id DESC")
    @Query("SELECT C.id,C.name,SUM(amount) AS cash FROM cashBoxesInfo_table AS C " +
            "LEFT JOIN entries_table AS E ON C.id=E.cashBoxId " +
            "GROUP BY C.id,C.name")
    public abstract List<CashBox.InfoWithCash> getAllCashBoxInfoForWidget();

    @Insert
    abstract Completable insert(CashBoxInfo cashBoxInfo);

    @Update
    abstract Completable update(CashBoxInfo cashBoxInfo);

    @Delete
    abstract Completable delete(CashBoxInfo cashBoxInfo);

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
