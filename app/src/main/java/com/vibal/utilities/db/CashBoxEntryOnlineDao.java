package com.vibal.utilities.db;

import androidx.lifecycle.LiveData;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.vibal.utilities.modelsNew.EntryOnline;

import java.util.Calendar;
import java.util.Collection;
import java.util.List;

import io.reactivex.Completable;
import io.reactivex.Single;

public interface CashBoxEntryOnlineDao {
    @Query("SELECT * FROM entriesOnline_table WHERE cashBoxId=:cashBoxId ORDER BY date DESC")
    LiveData<List<EntryOnline>> getEntriesByCashBoxId(long cashBoxId);

    @Query("SELECT * FROM entriesOnline_table WHERE groupId=:groupId")
    Single<List<EntryOnline>> getGroupEntries(long groupId);

    @Insert
    Completable insert(EntryOnline entry);

    @Insert
    Completable insertAll(Collection<EntryOnline> entries);

    @Delete
    Completable delete(EntryOnline entry);

    @Update
    Completable update(EntryOnline entry);

    @Query("UPDATE entriesOnline_table " +
            "SET amount=:amount,info=:info,date=:date " +
            "WHERE id=:id")
    Completable modify(long id, double amount, String info, Calendar date);

    @Query("UPDATE entriesOnline_table " +
            "SET amount=:amount,info=:info,date=:date " +
            "WHERE groupId!=0 AND groupId=:groupId")
    Completable modifyGroup(long groupId, double amount, String info, Calendar date);

    @Query("DELETE FROM entriesOnline_table " +
            "WHERE groupId!=0 AND groupId=:groupId")
    Single<Integer> deleteGroup(long groupId);

    @Query("DELETE FROM entriesOnline_table WHERE cashBoxId=:cashBoxId")
    Single<Integer> deleteAll(long cashBoxId);
}
