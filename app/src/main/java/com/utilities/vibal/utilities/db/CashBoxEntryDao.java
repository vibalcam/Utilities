package com.utilities.vibal.utilities.db;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.utilities.vibal.utilities.models.CashBox;

@Dao
public interface CashBoxEntryDao {
    @Insert
    void insert(CashBox.Entry entry);

    @Delete
    void delete(CashBox.Entry entry);

    @Update
    void update(CashBox.Entry entry);

    @Query("DELETE FROM entries_table WHERE cashBoxId=:id")
    void deleteAll(int id);
}
