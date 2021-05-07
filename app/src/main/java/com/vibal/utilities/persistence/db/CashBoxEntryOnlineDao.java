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
import com.vibal.utilities.models.EntryOnline;
import com.vibal.utilities.models.EntryOnlineInfo;

import java.util.Calendar;
import java.util.Collection;
import java.util.List;

import io.reactivex.Completable;
import io.reactivex.Single;

@Dao
public interface CashBoxEntryOnlineDao extends CashBoxEntryBaseDao {
    @Insert(entity = EntryOnlineInfo.class)
    Single<Long> insertEntry(EntryInfo entry);

    @Insert(entity = EntryOnlineInfo.class)
    Completable insertEntry(EntryOnlineInfo entry);

//    default Completable insert(EntryOnline<?> entry) {
//        return insertEntry(entry.getEntryInfo()).andThen(insertParticipantsInEntry(entry));
//    }

    @Insert(entity = EntryOnlineInfo.class)
    Completable insertAllEntries(Collection<EntryInfo> entries);

    // test change
//    @Insert(entity = EntryOnlineInfo.class)
//    Completable insertAllJSONEntriesInfo(Collection<UtilAppResponse.EntryJSON> entries);

    @Update(entity = EntryOnlineInfo.class)
    Completable updateEntry(EntryInfo entry);

    @Delete(entity = EntryOnlineInfo.class)
    Completable deleteEntry(EntryInfo entry);

    @Query("DELETE FROM entriesOnline_table WHERE id=:id")
    Completable delete(long id);

    //    @Query("SELECT id,cashBoxId,amount,date,info,groupId,toName,fromName " +
//            "FROM entriesOnline_table " +
//            "WHERE id>0 AND cashBoxId=:cashBoxId ORDER BY date DESC")
    @Transaction
    @Query("SELECT * FROM entriesOnlineAsEntries_view " +
            "WHERE id>0 AND cashBoxId=:cashBoxId ORDER BY date DESC")
    LiveData<List<EntryOnline.Simple>> getEntriesByCashBox(long cashBoxId);

    @Transaction
    @Query("SELECT * FROM entriesOnlineAsEntries_view " +
            "WHERE id>0 AND cashBoxId=:cashBoxId ORDER BY date DESC")
    Single<List<EntryOnline.Simple>> getSingleEntriesByCashBox(long cashBoxId);

    @Transaction
    @Query("SELECT * FROM entriesOnline_table " +
            "WHERE cashBoxId=:cashBoxId AND changeDate IS NOT NULL ORDER BY changeDate DESC")
    Single<List<EntryOnline.Complete>> getNonViewedEntriesByCashBoxId(long cashBoxId);

    //    @Query("SELECT id,cashBoxId,amount,date,info,groupId,toName,fromName " +
//            "FROM entriesOnline_table " +
//            "WHERE id>0 AND groupId=:groupId")
    @Transaction
    @Query("SELECT * FROM entriesOnlineAsEntries_view " +
            "WHERE id>0 AND groupId=:groupId")
    Single<List<EntryOnline.Simple>> getGroupEntries(long groupId);

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

    @Query("SELECT P.name, SUM(P.amount * E.amount /" +
            "ABS((SELECT SUM(O.amount) FROM entriesOnlineParticipants_table as O " +
            "WHERE O.entryId=P.entryId AND O.isFrom=P.isFrom GROUP BY O.entryId))" +
            ") AS amount " +
            "FROM entriesOnlineParticipants_table AS P LEFT JOIN entriesOnline_table AS E ON P.entryId=E.id " +
            "WHERE E.cashBoxId=:cashBoxId " +
            "GROUP BY P.name " +
            "ORDER BY amount DESC")
    LiveData<List<CashBoxBalances.Entry>> getBalances(long cashBoxId);


    // Participants Methods

    @Insert(entity = EntryOnlineInfo.Participant.class)
    Completable insertParticipantRaw(EntryBase.Participant participant);

    @Insert(entity = EntryOnlineInfo.Participant.class)
    Completable insertParticipantRaw(Collection<EntryBase.Participant> participantList);

    // test change
//    @Insert(entity = EntryOnlineInfo.Participant.class)
//    Completable insertAllJSONParticipants(Collection<UtilAppResponse.EntryJSON> participants);

    @Update(entity = EntryOnlineInfo.Participant.class)
    Single<Integer> updateParticipant(EntryBase.Participant participant);

    @Query("DELETE FROM entriesOnlineParticipants_table WHERE onlineId=:id")
    Completable unSafeDeleteParticipant(long id);

    @Override
    @Query("SELECT * FROM entriesonlineparticipants_table WHERE onlineId=:id")
    Single<EntryBase.Participant> getParticipantById(long id);

    @Query("SELECT COUNT(*) FROM entriesOnlineParticipants_table " +
            "WHERE entryId=:entryId AND isFrom=:isFrom")
    Single<Integer> countParticipants(long entryId, boolean isFrom);


    // Viewed logic

    @Query("INSERT INTO entriesOnlineParticipants_table (name, entryId, isFrom, amount, onlineId) " +
            "SELECT name, :toId, isFrom, amount, onlineId " +
            "FROM entriesOnlineParticipants_table WHERE entryId=:fromId AND NOT EXISTS(" +
            "SELECT 1 FROM entriesOnline_table WHERE id=:toId)")
    Completable copyEntriesParticipants(long fromId, long toId);

    // If -id already exists it does nothing
    @Query("INSERT INTO entriesOnline_table (id,cashBoxId,changeDate,amount,info,date,groupId) " +
            "SELECT -:id AS id,cashBoxId,changeDate,amount,info,date,groupId " +
            "FROM entriesOnline_table WHERE id=:id AND NOT EXISTS(" +
            "SELECT 1 FROM entriesOnline_table WHERE id=-:id)")
    Completable copyEntryInfoAsNonViewedOld(long id);

    default Completable copyAsNonViewedOld(long id) {
        return copyEntriesParticipants(id, -id).andThen(copyEntryInfoAsNonViewedOld(id));
    }

    // If -id already exists it does nothing
    @Query("INSERT INTO entriesOnline_table (id,cashBoxId,changeDate,amount,info,date,groupId) " +
            "SELECT -:id AS id,cashBoxId,:changeDate AS changeDate,amount,info,date,groupId " +
            "FROM entriesOnline_table WHERE id=:id AND NOT EXISTS(" +
            "SELECT 1 FROM entriesOnline_table WHERE id=-:id)")
    Completable copyEntryInfoAsNonViewedOld(long id, Calendar changeDate);

    default Completable copyAsNonViewedOld(long id, Calendar changeDate) {
        return copyEntriesParticipants(id, -id).andThen(copyEntryInfoAsNonViewedOld(id, changeDate));
    }

    @Query("DELETE FROM entriesOnline_table WHERE cashBoxId=:cashBoxId AND id<0")
    Completable deleteOldEntries(long cashBoxId);

    @Query("UPDATE entriesOnline_table SET changeDate=:changeDate WHERE id=:id")
    Single<Integer> setChangeDate(long id, Calendar changeDate);

    @Query("UPDATE entriesOnline_table SET changeDate=NULL WHERE cashBoxId=:cashBoxId AND changeDate IS NOT NULL")
    Completable setViewedAll(long cashBoxId);
}
