package com.utilities.vibal.utilities.db;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;
import java.util.UUID;

import io.reactivex.Completable;
import io.reactivex.Single;

@Dao
public interface PeriodicEntryWorkDao {
    @Query("SELECT P.id, P.workId,P.cashBoxId,P.amount,P.info,P.period, P.repetitions,C.name AS cashBoxName," +
            "SUM(E.amount) AS cashBoxAmount " +
            "FROM periodicWork_table AS P LEFT JOIN cashBoxesInfo_table AS C ON P.cashBoxId=C.id " +
            "LEFT JOIN entries_table AS E ON P.cashBoxId=E.cashBoxId " +
            "WHERE P.workId=:uuid " +
            "GROUP BY P.id, P.workId,P.cashBoxId,P.amount,P.info,P.period, P.repetitions,cashBoxName")
    Single<PeriodicEntryPojo> getWorkPojoByUUID(UUID uuid);

    @Query("SELECT P.id, P.workId,P.cashBoxId,P.amount,P.info,P.period, P.repetitions,C.name AS cashBoxName," +
            "SUM(E.amount) AS cashBoxAmount " +
            "FROM periodicWork_table AS P LEFT JOIN cashBoxesInfo_table AS C ON P.cashBoxId=C.id " +
            "LEFT JOIN entries_table AS E ON P.cashBoxId=E.cashBoxId " +
            "GROUP BY P.id, P.workId,P.cashBoxId,P.amount,P.info,P.period, P.repetitions,cashBoxName")
    LiveData<List<PeriodicEntryPojo>> getAllWorkPojos();

    @Insert
    Completable insert(PeriodicEntryPojo.PeriodicEntryWorkInfo periodicEntryWorkInfo);

    @Update
    Single<Integer> update(PeriodicEntryPojo.PeriodicEntryWorkInfo periodicEntryWorkInfo);

    @Delete
    Single<Integer> delete(PeriodicEntryPojo.PeriodicEntryWorkInfo periodicEntryWorkInfo);

    @Query("DELETE FROM periodicWork_table")
    Single<Integer> deleteAll();
}
