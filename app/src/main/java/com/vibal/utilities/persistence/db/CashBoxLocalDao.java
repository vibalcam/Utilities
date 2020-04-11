package com.vibal.utilities.persistence.db;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Transaction;
import androidx.room.Update;

import com.vibal.utilities.modelsNew.CashBox;
import com.vibal.utilities.modelsNew.CashBoxInfo;
import com.vibal.utilities.modelsNew.CashBoxInfoLocal;

import java.util.Collection;
import java.util.Currency;
import java.util.List;

import io.reactivex.Completable;
import io.reactivex.Single;

@Dao
public abstract class CashBoxLocalDao extends CashBoxBaseDao {
    @Override
    public LiveData<List<CashBox.InfoWithCash>> getAllCashBoxesInfo() {
        return getAllCashBoxesInfo(false);
    }

    @Insert(entity = CashBoxInfoLocal.class)
    abstract Single<Long> insertWithoutOrderId(CashBoxInfo cashBoxInfo);

    @Update(entity = CashBoxInfoLocal.class)
    public abstract Completable update(CashBoxInfo cashBoxInfo);

    @Update(entity = CashBoxInfoLocal.class)
    abstract Completable updateAll(Collection<CashBoxInfo> cashBoxInfoCollection);

    @Override
    public Completable delete(CashBoxInfo cashBoxInfo) {
        return setDeleted(cashBoxInfo.getId(),true);
    }

    @Override
    public Single<Integer> deleteAll() {
        return setDeletedAll(true);
    }

    // Non deleted CashBoxes
    @Query("SELECT C.id,C.name,C.orderId,SUM(amount) AS cash, 0 AS hasChanges, currency " +
            "FROM cashBoxesInfo_table AS C LEFT JOIN entries_table AS E ON C.id=E.cashBoxId " +
            "WHERE deleted=:deleted " +
            "GROUP BY C.id,C.name,C.orderId, C.deleted, hasChanges, C.currency " +
            "ORDER BY C.orderId ASC")
    public abstract LiveData<List<CashBox.InfoWithCash>> getAllCashBoxesInfo(boolean deleted);

    @Query("SELECT C.id,C.name,C.orderId,SUM(amount) AS cash,0 AS hasChanges,C.currency " +
            "FROM cashBoxesInfo_table AS C LEFT JOIN entries_table AS E ON C.id=E.cashBoxId " +
            "WHERE C.id=:id " +
            "GROUP BY C.id,C.name,C.orderId,hasChanges, C.currency")
    public abstract LiveData<CashBox.InfoWithCash> getCashBoxInfoWithCashById(long id);

    @Transaction
    @Query("SELECT C.id,C.name,C.orderId,SUM(amount) AS cash,0 AS hasChanges,C.currency " +
            "FROM cashBoxesInfo_table AS C LEFT JOIN entries_table AS E ON C.id=E.cashBoxId " +
            "WHERE C.id=:id " +
            "GROUP BY C.id,C.name,C.orderId,hasChanges,C.currency")
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
    public abstract Completable moveCashBoxToOrderPos(long cashBoxId, long fromOrderPos, long toOrderPos);

    // Get all CashBoxInfo to supply the widget
    @Query("SELECT C.id,C.name,C.orderId,SUM(amount) AS cash,0 AS hasChanges,C.currency " +
            "FROM cashBoxesInfo_table AS C LEFT JOIN entries_table AS E ON C.id=E.cashBoxId " +
            "WHERE C.deleted=0 " +
            "GROUP BY C.id,C.name,C.orderId,C.deleted,hasChanges,C.currency " +
            "ORDER BY C.orderId DESC")
    public abstract List<CashBox.InfoWithCash> getAllCashBoxInfoForWidget();

    @Query("UPDATE cashBoxesInfo_table " +
            "SET orderId=:cashBoxId " +
            "WHERE id=:cashBoxId AND orderId=" + CashBoxInfo.NO_ORDER_ID)
    abstract Completable configureOrderId(long cashBoxId);

    @Query("UPDATE cashBoxesInfo_table SET currency=:currency WHERE id=:cashBoxId")
    public abstract Completable setCashBoxCurrency(long cashBoxId, Currency currency);

    @Query("UPDATE cashBoxesInfo_table SET deleted=:deleted WHERE id=:cashBoxId")
    public abstract Completable setDeleted(long cashBoxId, boolean deleted);

    @Query("UPDATE cashBoxesInfo_table SET deleted=:deleted WHERE deleted!=:deleted")
    public abstract Single<Integer> setDeletedAll(boolean deleted);

    @Delete(entity = CashBoxInfoLocal.class)
    public abstract Completable permanentDelete(CashBoxInfo cashBoxInfo);

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
