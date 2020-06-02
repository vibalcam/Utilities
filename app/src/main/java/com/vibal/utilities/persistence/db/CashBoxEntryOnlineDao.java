package com.vibal.utilities.persistence.db;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.vibal.utilities.models.Entry;
import com.vibal.utilities.models.EntryOnline;
import com.vibal.utilities.persistence.retrofit.UtilAppResponse;

import java.util.Calendar;
import java.util.Collection;
import java.util.List;

import io.reactivex.Completable;
import io.reactivex.Single;

@Dao
public interface CashBoxEntryOnlineDao extends CashBoxEntryBaseDao {
    @Insert(entity = EntryOnline.class)
    Completable insert(Entry entry);

    @Insert(entity = EntryOnline.class)
    Completable insert(EntryOnline entry);

    @Insert(entity = EntryOnline.class)
    Completable insertAll(Collection<Entry> entries);

    @Insert(entity = EntryOnline.class)
    Completable insertAllJSON(Collection<UtilAppResponse.EntryJSON> entries);

    @Update(entity = EntryOnline.class)
    Completable update(Entry entry);

    @Delete(entity = EntryOnline.class)
    Completable delete(Entry entry);

    @Query("DELETE FROM entriesOnline_table WHERE id=:id")
    Completable delete(long id);

    @Query("SELECT id,cashBoxId,amount,date,info,groupId " +
            "FROM entriesOnline_table " +
            "WHERE id>0 AND cashBoxId=:cashBoxId ORDER BY date DESC")
    LiveData<List<Entry>> getEntriesByCashBoxId(long cashBoxId);

    @Query("SELECT * FROM entriesOnline_table " +
            "WHERE cashBoxId=:cashBoxId AND changeDate IS NOT NULL ORDER BY changeDate DESC")
    Single<List<EntryOnline>> getNonViewedEntriesByCashBoxId(long cashBoxId);

    @Query("SELECT id,cashBoxId,amount,date,info,groupId " +
            "FROM entriesOnline_table " +
            "WHERE id>0 AND groupId=:groupId")
    Single<List<Entry>> getGroupEntries(long groupId);

    @Query("SELECT id FROM entriesOnline_table WHERE id>0 AND groupId=:groupId")
    Single<List<Integer>> getGroupIds(long groupId);

    @Query("UPDATE entriesOnline_table " +
            "SET amount=:amount,info=:info,date=:date " +
            "WHERE id=:id")
    Completable modify(long id, double amount, String info, Calendar date);

    @Query("UPDATE entriesOnline_table " +
            "SET amount=:amount,info=:info,date=:date " +
            "WHERE id>0 AND groupId!=0 AND groupId=:groupId")
    Completable modifyGroup(long groupId, double amount, String info, Calendar date);

    @Query("DELETE FROM entriesOnline_table " +
            "WHERE id>0 AND groupId!=0 AND groupId=:groupId")
    Single<Integer> deleteGroup(long groupId);

    @Query("DELETE FROM entriesOnline_table WHERE id>0 AND cashBoxId=:cashBoxId")
    Single<Integer> deleteAll(long cashBoxId);

    @Query("SELECT id FROM entriesOnline_table WHERE id>0 AND cashBoxId=:cashBoxId")
    Single<List<Integer>> getCashBoxEntriesIds(long cashBoxId);

    // Viewed logic

    // If -id already exists it does nothing
    @Query("INSERT INTO entriesOnline_table (id,cashBoxId,changeDate,amount,info,date,groupId)" +
            "SELECT -:id AS id,cashBoxId,changeDate,amount,info,date,groupId " +
            "FROM entriesOnline_table WHERE id=:id AND NOT EXISTS(" +
            "SELECT 1 FROM entriesOnline_table WHERE id=-:id)")
    Completable copyAsNonViewedOld(long id);

    // If -id already exists it does nothing
    @Query("INSERT INTO entriesOnline_table (id,cashBoxId,changeDate,amount,info,date,groupId) " +
            "SELECT -:id AS id,cashBoxId,:changeDate AS changeDate,amount,info,date,groupId " +
            "FROM entriesOnline_table WHERE id=:id AND NOT EXISTS(" +
            "SELECT 1 FROM entriesOnline_table WHERE id=-:id)")
    Completable copyAsNonViewedOld(long id, Calendar changeDate);

    @Query("DELETE FROM entriesOnline_table WHERE cashBoxId=:cashBoxId AND id<0")
    Completable deleteOldEntries(long cashBoxId);

    @Query("UPDATE entriesOnline_table SET changeDate=:changeDate WHERE id=:id")
    Single<Integer> setChangeDate(long id, Calendar changeDate);

    @Query("UPDATE entriesOnline_table SET changeDate=NULL WHERE cashBoxId=:cashBoxId AND changeDate IS NOT NULL")
    Completable setViewedAll(long cashBoxId);
}
