package com.vibal.utilities.persistence.db;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.vibal.utilities.models.CashBoxInfo;
import com.vibal.utilities.models.CashBoxInfoLocal;
import com.vibal.utilities.models.InfoWithCash;

import java.util.Collection;
import java.util.Currency;
import java.util.List;

import io.reactivex.Completable;
import io.reactivex.Single;

@Dao
public abstract class CashBoxLocalDao extends CashBoxBaseDao {
    @Override
    public LiveData<List<InfoWithCash>> getAllCashBoxesInfo() {
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
        return setDeleted(cashBoxInfo.getId(), true);
    }

    @Override
    public Single<Integer> deleteAll() {
        return setDeletedAll(true);
    }

    // Non deleted CashBoxes
    @Query("SELECT C.id,C.name,C.orderId,SUM(amount) AS cash, 0 AS changes, currency " +
            "FROM cashBoxesInfo_table AS C LEFT JOIN entries_table AS E ON C.id=E.cashBoxId " +
            "WHERE deleted=:deleted " +
            "GROUP BY C.id,C.name,C.orderId, C.deleted, changes, C.currency " +
            "ORDER BY C.orderId ASC")
    public abstract LiveData<List<InfoWithCash>> getAllCashBoxesInfo(boolean deleted);

    @Query("SELECT C.id,C.name,C.orderId,SUM(amount) AS cash,0 AS changes,C.currency " +
            "FROM cashBoxesInfo_table AS C LEFT JOIN entries_table AS E ON C.id=E.cashBoxId " +
            "WHERE C.id=:id " +
            "GROUP BY C.id,C.name,C.orderId,changes, C.currency")
    public abstract LiveData<InfoWithCash> getCashBoxInfoWithCashById(long id);

//    @Transaction
//    @Query("SELECT C.id,C.name,C.orderId,SUM(amount) AS cash,0 AS changes,C.currency " +
//            "FROM cashBoxesInfo_table AS C LEFT JOIN entries_table AS E ON C.id=E.cashBoxId " +
//            "WHERE C.id=:id " +
//            "GROUP BY C.id,C.name,C.orderId,changes,C.currency")
//    abstract Single<CashBox.Simple> getCashBoxLocalById(long id);

    @Query("SELECT C.id,C.name,C.orderId,SUM(amount) AS cash,0 AS changes,C.currency " +
            "FROM cashBoxesInfo_table AS C LEFT JOIN entries_table AS E ON C.id=E.cashBoxId " +
            "WHERE C.id=:id " +
            "GROUP BY C.id,C.name,C.orderId,changes, C.currency")
    public abstract Single<InfoWithCash> getSingleCashBoxInfoWithCashById(long id);

    @Query("SELECT DISTINCT lower(P.name) " +
            "FROM entriesParticipants_table AS P LEFT JOIN entries_table AS E ON P.entryId=E.id " +
            "WHERE E.cashBoxId=:cashBoxId " +
            "ORDER BY P.name DESC")
    public abstract LiveData<List<String>> getNamesByCashBox(long cashBoxId);

    @Query("SELECT DISTINCT lower(P.name) " +
            "FROM entriesParticipants_table AS P LEFT JOIN entries_table AS E ON P.entryId=E.id " +
            "WHERE E.cashBoxId=:cashBoxId " +
            "ORDER BY P.name DESC")
    abstract Single<List<String>> getSingleNamesByCashBox(long cashBoxId);

//    @Override
//    public Single<CashBox> getCashBoxById(long id) {
////        return getCashBoxLocalById(id)
////                .map(local -> {
////                    local.getEntries().sort((o1, o2) -> o1.getDate().compareTo(o2.getDate()));
////                    return local;
////                });
//
//        return getSingleCashBoxInfoWithCashById(id)
//                .flatMap(infoWithCash -> getEntriesByCashBox(id).flatMap(
//                        entries -> getNamesByCashBox(id).map(
//                                names -> new CashBox.Local(infoWithCash, names, entries))));
//    }

    @Query("UPDATE cashBoxesInfo_table " +
            "SET orderId=CASE " +
            "WHEN id=:cashBoxId THEN :toOrderPos " +
            "WHEN orderId BETWEEN :fromOrderPos AND :toOrderPos THEN orderId-1 " +
            "WHEN orderId BETWEEN :toOrderPos AND :fromOrderPos THEN orderId+1 " +
            "ELSE orderId END " +
            "WHERE orderId BETWEEN :fromOrderPos AND :toOrderPos " +
            "OR orderId BETWEEN :toOrderPos AND :fromOrderPos")
    public abstract Completable moveCashBoxToOrderPos(long cashBoxId, long fromOrderPos, long toOrderPos);

    /**
     * Get all CashBoxInfo to supply the widget
     */
    @Query("SELECT C.id,C.name,C.orderId,SUM(amount) AS cash,0 AS changes,C.currency " +
            "FROM cashBoxesInfo_table AS C LEFT JOIN entries_table AS E ON C.id=E.cashBoxId " +
            "WHERE C.deleted=0 " +
            "GROUP BY C.id,C.name,C.orderId,C.deleted,changes,C.currency " +
            "ORDER BY C.orderId DESC")
    public abstract List<InfoWithCash> getAllCashBoxInfoForWidget();

    @Query("UPDATE cashBoxesInfo_table " +
            "SET orderId=:cashBoxId " +
            "WHERE id=:cashBoxId AND orderId=" + CashBoxInfo.NO_ORDER_ID)
    abstract Completable configureOrderId(long cashBoxId);

    @Query("UPDATE cashBoxesInfo_table SET currency=:currency WHERE id=:cashBoxId")
    public abstract Completable setCashBoxCurrency(long cashBoxId, Currency currency);

    @Query("SELECT currency FROM cashBoxesInfo_table WHERE id=:cashBoxId")
    public abstract Single<Currency> getCashBoxCurrency(long cashBoxId);

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
