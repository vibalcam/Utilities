package com.vibal.utilities.persistence.db;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.room.Dao;

import com.vibal.utilities.models.CashBoxBalances;
import com.vibal.utilities.models.EntryBase;
import com.vibal.utilities.models.EntryInfo;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;

import io.reactivex.Completable;
import io.reactivex.Single;

@Dao
public interface CashBoxEntryBaseDao {
    LiveData<? extends List<? extends EntryBase<?>>> getEntriesByCashBox(long cashBoxId);

    Single<? extends List<? extends EntryBase<?>>> getSingleEntriesByCashBox(long cashBoxId);

    Single<? extends List<? extends EntryBase<?>>> getGroupEntries(long groupId);

    Single<Long> insertEntry(EntryInfo entry);

    Completable insertAllEntries(Collection<EntryInfo> entries);

    Completable updateEntry(EntryInfo entry);

    Completable deleteEntry(EntryInfo entry);

    /**
     * Foreign key takes care of deleting participants
     */
    default Completable delete(EntryBase<?> entry) {
        return deleteEntry(entry.getEntryInfo());
    }

    /**
     * Inserts the entry with its participants after applying the given function to the entry
     */
    default Completable insert(@NonNull EntryBase<?> entry,
                               @NonNull Function<EntryBase<?>, EntryBase<?>> function) {
        EntryBase<?> entryFunc = function.apply(entry);
        return insertEntry(entryFunc.getEntryInfo())
                .flatMapCompletable(entryId -> insertParticipantsEntry(entryId, entryFunc));
    }

    /**
     * Insert the entries with the given cashBoxId
     */
    default Completable insert(long cashBoxId, @NonNull EntryBase<?> entry) {
        return insert(entry, entryBase -> entryBase.getEntryWithCashBoxId(cashBoxId));
    }

    /**
     * Insert the entries with the given cashBoxId
     */
    default Completable insert(long cashBoxId, @NonNull Collection<? extends EntryBase<?>> entries) {
        Completable completable = Completable.complete();
        for (EntryBase<?> e : entries)
            completable = completable.andThen(insert(cashBoxId, e));
        return completable;

//        return insertRaw(entries, entryBase -> entryBase.getEntryWithCashBoxId(cashBoxId));
//        ArrayList<EntryBase<?>> entryArrayList = new ArrayList<>();
//        for (EntryBase<?> entry : entries) {
//            entryArrayList.add(entry.getEntryWithCashBoxId(cashBoxId));
//        }
//        return insertRaw(entryArrayList);
    }

    /**
     * Inserts the entry with its participants as given
     */
    default Completable insertRaw(@NonNull EntryBase<?> entry) {
        return insert(entry, entryBase -> entryBase);
    }

    /**
     * Inserts the entries and their participants as given
     */
    default Completable insertRaw(@NonNull Collection<? extends EntryBase<?>> entries) {
        ArrayList<EntryInfo> entryInfos = new ArrayList<>();
        Completable pCompletable = Completable.complete();
        for (EntryBase<?> e : entries) {
            entryInfos.add(e.getEntryInfo());
            pCompletable = pCompletable.andThen(insertParticipantsEntry(e.getEntryInfo().getId(), e));
        }

        return insertAllEntries(entryInfos)
                .andThen(pCompletable);
    }

    Completable modify(long id, double amount, String info, Calendar date);

    Completable modifyGroup(long groupId, double amount, String info, Calendar date);

    Single<Integer> deleteGroup(long groupId);

    Single<Integer> deleteAll(long cashBoxId);

    LiveData<List<CashBoxBalances.Entry>> getBalances(long cashBoxId);

    LiveData<Double> getCashBalance(long cashBoxId, String name);

    // Participants Methods

    /**
     * Insert participant with the given entryId
     */
    default Completable insertParticipant(long entryId, @NonNull EntryBase.Participant participant) {
        return insertParticipantRaw(participant.getParticipantWithEntryId(entryId));
    }

    /**
     * Insert participants with the given entryId
     */
    default Completable insertParticipant(long entryId, @NonNull Collection<EntryBase.Participant> participants) {
        List<EntryBase.Participant> list = new ArrayList<>();
        for (EntryBase.Participant p : participants)
            list.add(p.getParticipantWithEntryId(entryId));
        return insertParticipantRaw(list);
    }

    /**
     * Insert participant with the given entryId
     */
    default Completable insertParticipantsEntry(long entryId, @NonNull EntryBase<?> entry) {
        if (entry.getFromParticipants().isEmpty() || entry.getToParticipants().isEmpty())
            throw new IllegalArgumentException("From and To lists cannot be empty");
        return insertParticipant(entryId, entry.getFromParticipants())
                .andThen(insertParticipant(entryId, entry.getToParticipants()));
    }

    /**
     * Insert participant as given
     */
    Completable insertParticipantRaw(EntryBase.Participant participant);

    /**
     * Insert participants as given
     */
    Completable insertParticipantRaw(Collection<EntryBase.Participant> participantList);

    Single<Integer> updateParticipant(EntryBase.Participant participant);

    default Completable deleteParticipant(EntryBase.Participant participant) {
        return unSafeDeleteParticipant(participant.getOnlineId())
                .andThen(countParticipants(participant.getEntryId(), participant.isFrom())
                        .flatMapCompletable(integer -> {
                            if (integer > 0)
                                return Completable.complete();
                            return insertParticipantRaw(EntryBase.Participant.createDefaultParticipant(
                                    participant.getEntryId(), participant.isFrom()));
                        }));
    }

    default Completable deleteParticipant(long id) {
        return getParticipantById(id)
                .flatMapCompletable(this::deleteParticipant);
    }

    Completable unSafeDeleteParticipant(long id);

    Single<EntryBase.Participant> getParticipantById(long id);

    Single<Integer> countParticipants(long entryId, boolean isFrom);
}
