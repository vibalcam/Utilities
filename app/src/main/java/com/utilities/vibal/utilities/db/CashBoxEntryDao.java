package com.utilities.vibal.utilities.db;

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
public interface CashBoxEntryDao {
    @Insert
    Completable insert(CashBox.Entry entry);

    @Insert
    Completable insertAll(List<CashBox.Entry> entries);

    @Delete
    Completable delete(CashBox.Entry entry);

    @Update
    Completable update(CashBox.Entry entry);

    @Query("DELETE FROM entries_table WHERE cashBoxId=:id")
    Single<Integer> deleteAll(int id);
}
