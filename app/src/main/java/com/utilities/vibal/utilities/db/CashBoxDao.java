package com.utilities.vibal.utilities.db;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.utilities.vibal.utilities.models.CashBox;

import java.util.List;

import io.reactivex.Completable;
import io.reactivex.Single;

@Dao
public interface CashBoxDao {
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
            "GROUP BY C.id,C.name " +
            "ORDER BY C.id DESC")
    LiveData<List<CashBox.InfoWithCash>> getAllCashBoxesInfo();

//    @Transaction
////    @Query("SELECT * FROM cashBoxesInfo_table WHERE id=:id")
//    @Query("SELECT C.id,C.name,SUM(amount) AS cash FROM cashBoxesInfo_table AS C " +
//            "LEFT JOIN entries_table AS E ON C.id=E.cashBoxId " +
//            "WHERE C.id=:id " +
//            "GROUP BY C.id,C.name")
//    abstract LiveData<CashBox> getCashBoxById(int id);

    @Query("SELECT C.id,C.name,SUM(amount) AS cash FROM cashBoxesInfo_table AS C " +
            "LEFT JOIN entries_table AS E ON C.id=E.cashBoxId " +
            "WHERE C.id=:id " +
            "GROUP BY C.id,C.name")
    LiveData<CashBox.InfoWithCash> getCashBoxInfoWithCashById(long id);

    @Query("SELECT * FROM entries_table WHERE cashBoxId=:cashBoxId ORDER BY date DESC")
    LiveData<List<CashBox.Entry>> getEntriesByCashBoxId(long cashBoxId);



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
    @Query("SELECT C.id,C.name,SUM(amount) AS cash FROM cashBoxesInfo_table AS C " +
            "LEFT JOIN entries_table AS E ON C.id=E.cashBoxId " +
            "GROUP BY C.id,C.name " +
            "ORDER BY C.id DESC")
    List<CashBox.InfoWithCash> getAllCashBoxInfoForWidget();

    @Insert
    Single<Long> insert(CashBoxInfo cashBoxInfo);

    @Update
    Completable update(CashBoxInfo cashBoxInfo);

    @Delete
    Completable delete(CashBoxInfo cashBoxInfo);

    @Query("DELETE FROM cashBoxesInfo_table")
    Single<Integer> deleteAll();

//    @Query("UPDATE cashBoxesInfo_table SET orderPos=:newPos WHERE name=:name")
//    abstract void updateOrder(String name, int newPos);
//
//    @Transaction
//    void updateOrderAll(Collection<CashBox.CashBoxInfo> cashBoxInfos) {
//        for(CashBox.CashBoxInfo cashBoxInfo:cashBoxInfos)
//            updateOrder(cashBoxInfo.getName(),cashBoxInfo.getOrderPos());
//    }
}
