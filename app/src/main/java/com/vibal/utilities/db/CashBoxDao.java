package com.vibal.utilities.db;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Transaction;
import androidx.room.Update;

import com.vibal.utilities.modelsNew.CashBox;
import com.vibal.utilities.modelsNew.CashBoxInfo;
import com.vibal.utilities.util.LogUtil;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Currency;
import java.util.List;

import io.reactivex.Completable;
import io.reactivex.Single;

@Dao
public abstract class CashBoxDao {
    // Non deleted CashBoxes
    @Query("SELECT C.id,C.name,C.orderId,SUM(amount) AS cash, deleted, currency " +
            "FROM cashBoxesInfo_table AS C LEFT JOIN entries_table AS E ON C.id=E.cashBoxId " +
            "WHERE deleted=:deleted " +
            "GROUP BY C.id,C.name,C.orderId, C.deleted, C.currency " +
            "ORDER BY C.orderId ASC")
    abstract LiveData<List<CashBox.InfoWithCash>> getAllCashBoxesInfo(boolean deleted);

    @Query("SELECT C.id,C.name,C.orderId,SUM(amount) AS cash,C.deleted,C.currency " +
            "FROM cashBoxesInfo_table AS C LEFT JOIN entries_table AS E ON C.id=E.cashBoxId " +
            "WHERE C.id=:id " +
            "GROUP BY C.id,C.name,C.orderId,C.deleted, C.currency")
    abstract LiveData<CashBox.InfoWithCash> getCashBoxInfoWithCashById(long id);

    @Transaction
    @Query("SELECT C.id,C.name,C.orderId,SUM(amount) AS cash,C.deleted,C.currency " +
            "FROM cashBoxesInfo_table AS C LEFT JOIN entries_table AS E ON C.id=E.cashBoxId " +
            "WHERE C.id=:id " +
            "GROUP BY C.id,C.name,C.orderId,C.deleted,C.currency")
    public abstract Single<CashBox> getCashBoxById(long id);

    //todo mejorar coger from directamente
    @Query("UPDATE cashBoxesInfo_table " +
            "SET orderId=CASE " +
            "WHEN id=:cashBoxId THEN :toOrderPos " +
            "WHEN orderId BETWEEN :fromOrderPos AND :toOrderPos THEN orderId-1 " +
            "WHEN orderId BETWEEN :toOrderPos AND :fromOrderPos THEN orderId+1 " +
            "ELSE orderId END " +
            "WHERE orderId BETWEEN :fromOrderPos AND :toOrderPos " +
            "OR orderId BETWEEN :toOrderPos AND :fromOrderPos")
    abstract Completable moveCashBoxToOrderPos(long cashBoxId, long fromOrderPos, long toOrderPos);

    // Get all CashBoxInfo to supply the widget
    @Query("SELECT C.id,C.name,C.orderId,SUM(amount) AS cash,C.deleted,C.currency " +
            "FROM cashBoxesInfo_table AS C LEFT JOIN entries_table AS E ON C.id=E.cashBoxId " +
            "WHERE C.deleted=0 " +
            "GROUP BY C.id,C.name,C.orderId,C.deleted,C.currency " +
            "ORDER BY C.orderId DESC")
    public abstract List<CashBox.InfoWithCash> getAllCashBoxInfoForWidget();

    Completable insert(@NonNull CashBox cashBox, @NonNull CashBoxEntryDao cashBoxEntryDao) {
        if (cashBox.getEntries().isEmpty())
            return insert(cashBox.getInfoWithCash().getCashBoxInfo()).ignoreElement();
        else {
            return insert(cashBox.getInfoWithCash().getCashBoxInfo()).flatMapCompletable(id -> {
                LogUtil.debug("Prueba", "Id: " + id);
                ArrayList<CashBox.Entry> entryArrayList = new ArrayList<>();
                for (CashBox.Entry entry : cashBox.getEntries())
                    entryArrayList.add(entry.getEntryWithCashBoxId(id));
                return cashBoxEntryDao.insertAll(entryArrayList);
            });
        }
    }

    Single<Long> insert(CashBoxInfo cashBoxInfo) {
        return insertWithoutOrderId(cashBoxInfo)
                .flatMap(id -> configureOrderId(id)
                        .toSingle(() -> id));
    }

    @Query("UPDATE cashBoxesInfo_table " +
            "SET orderId=:cashBoxId " +
            "WHERE id=:cashBoxId AND orderId=" + CashBoxInfo.NO_ORDER_ID)
    abstract Completable configureOrderId(long cashBoxId);

    @Insert
    abstract Single<Long> insertWithoutOrderId(CashBoxInfo cashBoxInfo);

    @Update
    abstract Completable update(CashBoxInfo cashBoxInfo);

    @Update
    abstract Completable updateAll(Collection<CashBoxInfo> cashBoxInfoCollection);

    @Query("UPDATE cashBoxesInfo_table SET currency=:currency WHERE id=:cashBoxId")
    abstract Completable setCashBoxCurrency(long cashBoxId, Currency currency);

    @Query("UPDATE cashBoxesInfo_table SET deleted=:deleted WHERE deleted!=:deleted")
    abstract Single<Integer> setDeletedAll(boolean deleted);

    @Delete
    abstract Completable delete(CashBoxInfo cashBoxInfo);

    @Query("DELETE FROM cashBoxesInfo_table WHERE deleted=1")
    abstract Single<Integer> clearRecycleBin();

//    @Query("UPDATE cashBoxesInfo_table SET orderPos=:newPos WHERE name=:name")
//    abstract void updateOrder(String name, int newPos);
//
//    @Transaction
//    void updateOrderAll(Collection<CashBox.CashBoxInfo> cashBoxInfos) {
//        for(CashBox.CashBoxInfo cashBoxInfo:cashBoxInfos)
//            updateOrder(cashBoxInfo.getName(),cashBoxInfo.getOrderPos());
//    }
}
