package com.vibal.utilities.persistence.db;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.vibal.utilities.models.CashBoxInfo;
import com.vibal.utilities.models.CashBoxInfoOnline;
import com.vibal.utilities.models.InfoWithCash;

import java.util.Collection;
import java.util.Currency;
import java.util.List;

import io.reactivex.Completable;
import io.reactivex.Maybe;
import io.reactivex.Single;

@Dao
public abstract class CashBoxOnlineDao extends CashBoxBaseDao {
    @Insert(entity = CashBoxInfoOnline.class)
    abstract Single<Long> insertWithoutOrderId(CashBoxInfo cashBoxInfo);

    @Update(entity = CashBoxInfoOnline.class)
    public abstract Completable update(CashBoxInfo cashBoxInfo);

    @Update(entity = CashBoxInfoOnline.class)
    abstract Completable updateAll(Collection<CashBoxInfo> cashBoxInfoCollection);

    @Delete(entity = CashBoxInfoOnline.class)
    public abstract Completable delete(CashBoxInfo cashBoxInfo);

    @Query("DELETE FROM cashBoxesOnline_table")
    public abstract Single<Integer> deleteAll();

    @Query("SELECT C.id,C.name,C.orderId,SUM(" +
            "CASE WHEN E.id<0 THEN 0 ELSE amount END) AS cash, C.currency, " +
            "CASE WHEN C.accepted==0 THEN " + InfoWithCash.CHANGE_NEW +
            " ELSE COUNT(changeDate) END as changes " +
            "FROM cashBoxesOnline_table AS C LEFT JOIN entriesOnline_table AS E ON C.id=E.cashBoxId " +
            "GROUP BY C.id,C.name,C.orderId, C.currency " +
            "ORDER BY C.orderId ASC")
    public abstract LiveData<List<InfoWithCash>> getAllCashBoxesInfo();

    @Query("SELECT C.id,C.name,C.orderId,SUM(CASE WHEN E.id<0 THEN 0 ELSE amount END) AS cash,C.currency, " +
            "CASE WHEN C.accepted==0 THEN " + InfoWithCash.CHANGE_NEW +
            " ELSE COUNT(changeDate) END as changes " +
            "FROM cashBoxesOnline_table AS C LEFT JOIN entriesOnline_table AS E ON C.id=E.cashBoxId " +
            "WHERE C.id=:id " +
            "GROUP BY C.id,C.name,C.orderId, C.currency")
    public abstract LiveData<InfoWithCash> getCashBoxInfoWithCashById(long id);

//    @Deprecated
//    @Transaction
//    @Query("SELECT C.id,C.name,C.orderId,SUM(CASE WHEN E.id<0 THEN 0 ELSE amount END) AS cash,C.currency, " +
//            "CASE WHEN C.accepted==0 THEN " + CashBox.InfoWithCash.CHANGE_NEW +
//            " ELSE COUNT(changeDate) END as changes " +
//            "FROM cashBoxesOnline_table AS C LEFT JOIN entriesOnline_table AS E ON C.id=E.cashBoxId " +
//            "WHERE C.id=:id " +
//            "GROUP BY C.id,C.name,C.orderId,C.currency")
//    abstract Single<CashBox.Complete> getCashBoxOnlineById(long id);

    @Query("SELECT C.id,C.name,C.orderId,SUM(CASE WHEN E.id<0 THEN 0 ELSE amount END) AS cash,C.currency, " +
            "CASE WHEN C.accepted==0 THEN " + InfoWithCash.CHANGE_NEW +
            " ELSE COUNT(changeDate) END as changes " +
            "FROM cashBoxesOnline_table AS C LEFT JOIN entriesOnline_table AS E ON C.id=E.cashBoxId " +
            "WHERE C.id=:id " +
            "GROUP BY C.id,C.name,C.orderId, C.currency")
    abstract Single<InfoWithCash> getSingleCashBoxInfoWithCashById(long id);

    @Query("SELECT DISTINCT lower(P.name) " +
            "FROM entriesOnlineParticipants_table AS P LEFT JOIN entriesOnline_table AS E ON P.entryId=E.id " +
            "WHERE E.cashBoxId=:cashBoxId " +
            "ORDER BY P.name DESC")
    public abstract LiveData<List<String>> getNamesByCashBox(long cashBoxId);

    @Query("SELECT DISTINCT lower(P.name) " +
            "FROM entriesOnlineParticipants_table AS P LEFT JOIN entriesOnline_table AS E ON P.entryId=E.id " +
            "WHERE E.cashBoxId=:cashBoxId " +
            "ORDER BY P.name DESC")
    abstract Single<List<String>> getSingleNamesByCashBox(long cashBoxId);

//    @Override
//    public Single<CashBox> getCashBoxById(long id) {
//        return getSingleCashBoxInfoWithCashById(id)
//                .flatMap(infoWithCash -> getEntriesByCashBox(id).flatMap(
//                        entries -> getNamesByCashBox(id).map(
//                                names -> new CashBox.Online(infoWithCash, names, entries))));
//
//    }

    @Query("UPDATE cashBoxesOnline_table " +
            "SET orderId=CASE " +
            "WHEN id=:cashBoxId THEN :toOrderPos " +
            "WHEN orderId BETWEEN :fromOrderPos AND :toOrderPos THEN orderId-1 " +
            "WHEN orderId BETWEEN :toOrderPos AND :fromOrderPos THEN orderId+1 " +
            "ELSE orderId END " +
            "WHERE orderId BETWEEN :fromOrderPos AND :toOrderPos " +
            "OR orderId BETWEEN :toOrderPos AND :fromOrderPos")
    public abstract Completable moveCashBoxToOrderPos(long cashBoxId, long fromOrderPos, long toOrderPos);

    @Query("UPDATE cashBoxesOnline_table " +
            "SET orderId=:cashBoxId " +
            "WHERE id=:cashBoxId AND orderId=" + CashBoxInfo.NO_ORDER_ID)
    abstract Completable configureOrderId(long cashBoxId);

    @Query("UPDATE cashBoxesOnline_table SET currency=:currency WHERE id=:cashBoxId")
    public abstract Completable setCashBoxCurrency(long cashBoxId, Currency currency);

    @Query("SELECT currency FROM cashBoxesOnline_table WHERE id=:cashBoxId")
    public abstract Single<Currency> getCashBoxCurrency(long cashBoxId);

    @Query("SELECT id FROM cashBoxesOnline_table WHERE name=:name")
    abstract Maybe<Integer> getCashBoxIdByName(String name);

    public Single<Boolean> checkNameAvailable(String name) {
        return getCashBoxIdByName(name).isEmpty();
    }

    @Query("UPDATE cashBoxesOnline_table SET accepted=:accepted WHERE id=:cashBoxId")
    public abstract Completable setCashBoxAccepted(long cashBoxId, boolean accepted);
}
