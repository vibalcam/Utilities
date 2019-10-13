package com.vibal.utilities.db;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.vibal.utilities.modelsNew.CashBox;

import java.util.Collection;
import java.util.List;

import io.reactivex.Completable;
import io.reactivex.Single;

@Dao
public interface CashBoxEntryDao {
    @Query("SELECT * FROM entries_table WHERE cashBoxId=:cashBoxId ORDER BY date DESC")
    LiveData<List<CashBox.Entry>> getEntriesByCashBoxId(long cashBoxId);

    @Insert
    Completable insert(CashBox.Entry entry);

    @Insert
    Completable insertAll(Collection<CashBox.Entry> entries);

    @Delete
    Completable delete(CashBox.Entry entry);

    @Update
    Completable update(CashBox.Entry entry);

    @Query("UPDATE entries_table " +
            "SET amount=:amount,info=:info " +
            "WHERE id=:id")
    Completable modify(long id, double amount, String info);

    @Query("DELETE FROM entries_table WHERE cashBoxId=:cashBoxId")
    Single<Integer> deleteAll(long cashBoxId);
}
