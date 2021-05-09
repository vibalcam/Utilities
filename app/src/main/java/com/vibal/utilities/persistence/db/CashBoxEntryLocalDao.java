package com.vibal.utilities.persistence.db;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Transaction;
import androidx.room.Update;

import com.vibal.utilities.models.CashBoxBalances;
import com.vibal.utilities.models.EntryBase;
import com.vibal.utilities.models.EntryInfo;
import com.vibal.utilities.models.EntryLocal;

import java.util.Calendar;
import java.util.Collection;
import java.util.List;

import io.reactivex.Completable;
import io.reactivex.Single;

@Dao
public interface CashBoxEntryLocalDao extends CashBoxEntryBaseDao {
    @Insert(entity = EntryInfo.class)
    Single<Long> insertEntry(EntryInfo entry);

    @Insert(entity = EntryInfo.class)
    Completable insertAllEntries(Collection<EntryInfo> entries);

    @Update(entity = EntryInfo.class)
    Completable updateEntry(EntryInfo entry);

    @Delete(entity = EntryInfo.class)
    Completable deleteEntry(EntryInfo entry);

    @Transaction
    @Query("SELECT * FROM entries_table " +
            "WHERE id>0 AND cashBoxId=:cashBoxId ORDER BY date DESC")
    LiveData<List<EntryLocal>> getEntriesByCashBox(long cashBoxId);

    @Transaction
    @Query("SELECT * FROM entries_table " +
            "WHERE id>0 AND cashBoxId=:cashBoxId ORDER BY date DESC")
    Single<List<EntryLocal>> getSingleEntriesByCashBox(long cashBoxId);

    @Transaction
    @Query("SELECT * FROM entries_table WHERE groupId=:groupId")
    Single<List<EntryLocal>> getGroupEntries(long groupId);

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

    @Query("SELECT P.name, SUM(P.amount * E.amount /" +
            "ABS((SELECT SUM(O.amount) FROM entriesParticipants_table as O " +
            "WHERE O.entryId=P.entryId AND O.isFrom=P.isFrom GROUP BY O.entryId))" +
            ") AS amount " +
            "FROM entriesParticipants_table AS P LEFT JOIN entries_table AS E ON P.entryId=E.id " +
            "WHERE E.cashBoxId=:cashBoxId " +
            "GROUP BY P.name " +
            "ORDER BY amount DESC")
    LiveData<List<CashBoxBalances.Entry>> getBalances(long cashBoxId);

    @Query("SELECT SUM(P.amount * E.amount /" +
            "ABS((SELECT SUM(O.amount) FROM entriesParticipants_table as O " +
            "WHERE O.entryId=P.entryId AND O.isFrom=P.isFrom GROUP BY O.entryId))" +
            ") " +
            "FROM entriesParticipants_table AS P LEFT JOIN entries_table AS E ON P.entryId=E.id " +
            "WHERE E.cashBoxId=:cashBoxId AND P.name=:name " +
            "GROUP BY P.name")
    @Override
    LiveData<Double> getCashBalance(long cashBoxId, String name);

    // Participants Methods

    @Insert(entity = EntryBase.Participant.class)
    Completable insertParticipantRaw(EntryBase.Participant participant);

    @Insert(entity = EntryBase.Participant.class)
    Completable insertParticipantRaw(Collection<EntryBase.Participant> participantList);

    @Update(entity = EntryBase.Participant.class)
    Single<Integer> updateParticipant(EntryBase.Participant participant);

    @Query("DELETE FROM entriesParticipants_table WHERE onlineId=:id")
    Completable unSafeDeleteParticipant(long id);

    @Override
    @Query("SELECT * FROM entriesParticipants_table WHERE onlineId=:id")
    Single<EntryBase.Participant> getParticipantById(long id);

    @Query("SELECT COUNT(*) FROM entriesParticipants_table " +
            "WHERE entryId=:entryId AND isFrom=:isFrom")
    Single<Integer> countParticipants(long entryId, boolean isFrom);
}
