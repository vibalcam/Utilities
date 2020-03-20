package com.vibal.utilities.db;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.vibal.utilities.modelsNew.Entry;

import java.util.Calendar;
import java.util.Collection;
import java.util.List;

import io.reactivex.Completable;
import io.reactivex.Single;

@Dao
public interface CashBoxEntryLocalDao {
    @Query("SELECT * FROM entries_table WHERE cashBoxId=:cashBoxId ORDER BY date DESC")
    LiveData<List<Entry>> getEntriesByCashBoxId(long cashBoxId);

    @Query("SELECT * FROM entries_table WHERE groupId=:groupId")
    Single<List<Entry>> getGroupEntries(long groupId);

    @Insert
    Completable insert(Entry entry);

    @Insert
    Completable insertAll(Collection<Entry> entries);

    @Delete
    Completable delete(Entry entry);

    @Update
    Completable update(Entry entry);

    @Query("UPDATE entries_table " +
            "SET amount=:amount,info=:info,date=:date " +
            "WHERE id=:id")
    Completable modify(long id, double amount, String info, Calendar date);

    @Query("UPDATE entries_table " +
            "SET amount=:amount,info=:info,date=:date " +
            "WHERE groupId!=0 AND groupId=:groupId")
    Completable modifyGroup(long groupId, double amount, String info, Calendar date);

    @Query("DELETE FROM entries_table " +
            "WHERE groupId!=0 AND groupId=:groupId")
    Single<Integer> deleteGroup(long groupId);

    @Query("DELETE FROM entries_table WHERE cashBoxId=:cashBoxId")
    Single<Integer> deleteAll(long cashBoxId);
}
