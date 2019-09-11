package com.utilities.vibal.utilities.db;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Transaction;
import androidx.room.Update;

import com.utilities.vibal.utilities.models.CashBox;
import com.utilities.vibal.utilities.util.LogUtil;

import java.util.ArrayList;
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
    @Query("SELECT C.id,C.name,C.orderId,SUM(amount) AS cash FROM cashBoxesInfo_table AS C " +
            "LEFT JOIN entries_table AS E ON C.id=E.cashBoxId " +
            "GROUP BY C.id,C.name,C.orderId " +
            "ORDER BY C.orderId DESC")
    abstract LiveData<List<CashBox.InfoWithCash>> getAllCashBoxesInfo();

    @Query("SELECT C.id,C.name,C.orderId,SUM(amount) AS cash FROM cashBoxesInfo_table AS C " +
            "LEFT JOIN entries_table AS E ON C.id=E.cashBoxId " +
            "WHERE C.id=:id " +
            "GROUP BY C.id,C.name,C.orderId")
    abstract LiveData<CashBox.InfoWithCash> getCashBoxInfoWithCashById(long id);

    @Transaction
    @Query("SELECT C.id,C.name,C.orderId,SUM(amount) AS cash FROM cashBoxesInfo_table AS C " +
            "LEFT JOIN entries_table AS E ON C.id=E.cashBoxId " +
            "WHERE C.id=:id " +
            "GROUP BY C.id,C.name,C.orderId")
    public abstract Single<CashBox> getCashBoxById(long id);

//    @Query("UPDATE cashBoxesInfo_table " +
//            "SET orderId=CASE WHEN EXISTS(" +
//            "SELECT * FROM cashBoxesInfo_table WHERE orderId=:minOrderPos) " +
//            "THEN orderId+1 ELSE orderId END")
//    @Query("UPDATE cashBoxesInfo_table " +
//            "SET orderId=CASE " +
//            "WHEN EXISTS(" +
//            "SELECT * FROM cashBoxesInfo_table WHERE orderId=:minOrderPos) " +
//            "THEN orderId+1 ELSE orderId END")
//    Completable incrementOrderPos(long minOrderPos);

//    @Query("UPDATE cashBoxesInfo_table " +
//            "SET orderId=CASE " +
//            "WHEN EXISTS(SELECT * FROM cashBoxesInfo_table WHERE orderId=:fromOrderPos) " +
//            "THEN orderId ELSE (CASE " +
//            "WHEN id=:cashBoxId THEN :toOrderPos " +
//            "WHEN orderId BETWEEN :fromOrderPos AND :toOrderPos THEN )")
    @Query("UPDATE cashBoxesInfo_table " +
            "SET orderId=CASE " +
            "WHEN id=:cashBoxId THEN :toOrderPos " +
            "WHEN orderId BETWEEN :fromOrderPos AND :toOrderPos THEN orderId-1 " +
            "WHEN orderId BETWEEN :toOrderPos AND :fromOrderPos THEN orderId+1 " +
            "ELSE orderId END " +
            "WHERE orderId BETWEEN :fromOrderPos AND :toOrderPos " +
            "OR orderId BETWEEN :toOrderPos AND :fromOrderPos")
    abstract Completable moveCashBoxToOrderPos(long cashBoxId, long fromOrderPos, long toOrderPos);

//    @Transaction
//    MediatorLiveData<CashBox> getCashBoxById(int id) {
//        LiveData<CashBox.InfoWithCash> cashBoxInfoWithCash = getCashBoxInfoWithCashById(id);
//        LiveData<List<CashBox.Entry>> entries = getEntriesByCashBoxId(id);
//
//        MediatorLiveData<CashBox> liveDataMerger = new MediatorLiveData<>();
//        liveDataMerger.addSource(cashBoxInfoWithCash,
//                infoWithCash -> new CashBox(cashBoxInfoWithCash.getValue(),entries.getValue()));
//        liveDataMerger.addSource(entries,
//                infoWithCash -> new CashBox(cashBoxInfoWithCash.getValue(),entries.getValue()));
//
//        return liveDataMerger;
//    }

    // Get all CashBoxInfo to supply the widget
//    @Query("SELECT * FROM cashBoxesInfo_table ORDER BY id DESC")
    @Query("SELECT C.id,C.name,C.orderId,SUM(amount) AS cash FROM cashBoxesInfo_table AS C " +
            "LEFT JOIN entries_table AS E ON C.id=E.cashBoxId " +
            "GROUP BY C.id,C.name,C.orderId " +
            "ORDER BY C.orderId DESC")
    public abstract List<CashBox.InfoWithCash> getAllCashBoxInfoForWidget();

    Completable insert(CashBox cashBox, CashBoxEntryDao cashBoxEntryDao){
        if(cashBox.getEntries().isEmpty())
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

    @Insert
    abstract Single<Long> insert(CashBoxInfo cashBoxInfo);

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
