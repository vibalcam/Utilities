package com.vibal.utilities.persistence.db;

import androidx.lifecycle.LiveData;

import com.vibal.utilities.models.Entry;

import java.util.Calendar;
import java.util.Collection;
import java.util.List;

import io.reactivex.Completable;
import io.reactivex.Single;

public interface CashBoxEntryBaseDao {
    LiveData<List<Entry>> getEntriesByCashBoxId(long cashBoxId);

    Single<List<Entry>> getGroupEntries(long groupId);

    //    @Insert
    Completable insert(Entry entry);

    //    @Insert
    Completable insertAll(Collection<Entry> entries);

    //    @Delete
    Completable delete(Entry entry);

    //    @Update
    Completable update(Entry entry);

    Completable modify(long id, double amount, String info, Calendar date);

    Completable modifyGroup(long groupId, double amount, String info, Calendar date);

    Single<Integer> deleteGroup(long groupId);

    Single<Integer> deleteAll(long cashBoxId);
}
