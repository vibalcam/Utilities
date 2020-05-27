package com.vibal.utilities.persistence.db;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.vibal.utilities.modelsNew.CashBox;
import com.vibal.utilities.modelsNew.CashBoxInfo;
import com.vibal.utilities.modelsNew.CashBoxInfoOnline;
import com.vibal.utilities.modelsNew.Entry;

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
            "CASE WHEN C.accepted==0 THEN " + CashBox.InfoWithCash.CHANGE_NEW +
            " ELSE COUNT(changeDate) END as changes " +
            "FROM cashBoxesOnline_table AS C LEFT JOIN entriesOnline_table AS E ON C.id=E.cashBoxId " +
            "GROUP BY C.id,C.name,C.orderId, C.currency " +
            "ORDER BY C.orderId ASC")
    public abstract LiveData<List<CashBox.InfoWithCash>> getAllCashBoxesInfo();

    @Query("SELECT C.id,C.name,C.orderId,SUM(CASE WHEN E.id<0 THEN 0 ELSE amount END) AS cash,C.currency, " +
            "CASE WHEN C.accepted==0 THEN " + CashBox.InfoWithCash.CHANGE_NEW +
            " ELSE COUNT(changeDate) END as changes " +
            "FROM cashBoxesOnline_table AS C LEFT JOIN entriesOnline_table AS E ON C.id=E.cashBoxId " +
            "WHERE C.id=:id " +
            "GROUP BY C.id,C.name,C.orderId, C.currency")
    public abstract LiveData<CashBox.InfoWithCash> getCashBoxInfoWithCashById(long id);

//    @Deprecated
//    @Transaction
//    @Query("SELECT C.id,C.name,C.orderId,SUM(CASE WHEN E.id<0 THEN 0 ELSE amount END) AS cash,C.currency, " +
//            "CASE WHEN C.accepted==0 THEN " + CashBox.InfoWithCash.CHANGE_NEW +
//            " ELSE COUNT(changeDate) END as changes " +
//            "FROM cashBoxesOnline_table AS C LEFT JOIN entriesOnline_table AS E ON C.id=E.cashBoxId " +
//            "WHERE C.id=:id " +
//            "GROUP BY C.id,C.name,C.orderId,C.currency")
//    abstract Single<CashBox.Online> getCashBoxOnlineById(long id);

    @Query("SELECT C.id,C.name,C.orderId,SUM(CASE WHEN E.id<0 THEN 0 ELSE amount END) AS cash,C.currency, " +
            "CASE WHEN C.accepted==0 THEN " + CashBox.InfoWithCash.CHANGE_NEW +
            " ELSE COUNT(changeDate) END as changes " +
            "FROM cashBoxesOnline_table AS C LEFT JOIN entriesOnline_table AS E ON C.id=E.cashBoxId " +
            "WHERE C.id=:id " +
            "GROUP BY C.id,C.name,C.orderId, C.currency")
    abstract Single<CashBox.InfoWithCash> getSingleCashBoxInfoWithCashById(long id);

    @Query("SELECT id,cashBoxId,amount,date,info,groupId " +
            "FROM entriesOnline_table " +
            "WHERE id>0 AND cashBoxId=:cashBoxId ORDER BY date DESC")
    abstract Single<List<Entry>> getEntriesByCashBox(long cashBoxId);

    @Override
    public Single<CashBox> getCashBoxById(long id) {
//        return getCashBoxOnlineById(id).cast(CashBox.class);
        return getSingleCashBoxInfoWithCashById(id)
                .flatMap(infoWithCash -> getEntriesByCashBox(id)
                        .map(entries -> new CashBox.Online(infoWithCash, entries)));

    }

    //todo mejorar coger from directamente
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

    @Query("SELECT id FROM cashBoxesOnline_table WHERE name=:name")
    abstract Maybe<Integer> getCashBoxIdByName(String name);

    public Single<Boolean> checkNameAvailable(String name) {
        return getCashBoxIdByName(name)
                .isEmpty();
    }

    @Query("UPDATE cashBoxesOnline_table SET accepted=:accepted WHERE id=:cashBoxId")
    public abstract Completable setCashBoxAccepted(long cashBoxId, boolean accepted);
}
