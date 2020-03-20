package com.vibal.utilities.db;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Query;
import androidx.room.Transaction;

import com.vibal.utilities.modelsNew.CashBox;
import com.vibal.utilities.modelsNew.CashBoxInfo;

import java.util.Currency;
import java.util.List;

import io.reactivex.Completable;
import io.reactivex.Single;

@Dao
public abstract class CashBoxOnlineDao extends CashBoxBaseDao  {
    @Query("SELECT C.id,C.name,C.orderId,SUM(amount) AS cash, currency " +
            "FROM cashBoxesOnline_table AS C LEFT JOIN entriesOnline_table AS E ON C.id=E.cashBoxId " +
            "GROUP BY C.id,C.name,C.orderId, C.currency " +
            "ORDER BY C.orderId ASC")
    abstract LiveData<List<CashBox.InfoWithCash>> getAllCashBoxesInfo();

    @Query("SELECT C.id,C.name,C.orderId,SUM(amount) AS cash,C.currency " +
            "FROM cashBoxesOnline_table AS C LEFT JOIN entriesOnline_table AS E ON C.id=E.cashBoxId " +
            "WHERE C.id=:id " +
            "GROUP BY C.id,C.name,C.orderId, C.currency")
    abstract LiveData<CashBox.InfoWithCash> getCashBoxInfoWithCashById(long id);

    @Transaction
    @Query("SELECT C.id,C.name,C.orderId,SUM(amount) AS cash,C.currency " +
            "FROM cashBoxesOnline_table AS C LEFT JOIN entriesOnline_table AS E ON C.id=E.cashBoxId " +
            "WHERE C.id=:id " +
            "GROUP BY C.id,C.name,C.orderId,C.currency")
    public abstract Single<CashBox> getCashBoxById(long id);

    //todo mejorar coger from directamente
    @Query("UPDATE cashBoxesOnline_table " +
            "SET orderId=CASE " +
            "WHEN id=:cashBoxId THEN :toOrderPos " +
            "WHEN orderId BETWEEN :fromOrderPos AND :toOrderPos THEN orderId-1 " +
            "WHEN orderId BETWEEN :toOrderPos AND :fromOrderPos THEN orderId+1 " +
            "ELSE orderId END " +
            "WHERE orderId BETWEEN :fromOrderPos AND :toOrderPos " +
            "OR orderId BETWEEN :toOrderPos AND :fromOrderPos")
    abstract Completable moveCashBoxToOrderPos(long cashBoxId, long fromOrderPos, long toOrderPos);

    @Query("UPDATE cashBoxesOnline_table " +
            "SET orderId=:cashBoxId " +
            "WHERE id=:cashBoxId AND orderId=" + CashBoxInfo.NO_ORDER_ID)
    abstract Completable configureOrderId(long cashBoxId);

    @Query("UPDATE cashBoxesOnline_table SET currency=:currency WHERE id=:cashBoxId")
    abstract Completable setCashBoxCurrency(long cashBoxId, Currency currency);


}
