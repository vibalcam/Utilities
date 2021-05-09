package com.vibal.utilities.persistence.repositories;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;

import com.vibal.utilities.models.CashBox;
import com.vibal.utilities.models.CashBoxBalances;
import com.vibal.utilities.models.CashBoxInfo;
import com.vibal.utilities.models.EntryBase;
import com.vibal.utilities.models.EntryInfo;
import com.vibal.utilities.models.InfoWithCash;
import com.vibal.utilities.models.Participant;
import com.vibal.utilities.models.PeriodicEntryPojo;
import com.vibal.utilities.persistence.db.CashBoxBaseDao;
import com.vibal.utilities.persistence.db.CashBoxEntryBaseDao;
import com.vibal.utilities.util.LogUtil;

import java.util.Calendar;
import java.util.Collection;
import java.util.Currency;
import java.util.HashSet;
import java.util.List;

import io.reactivex.Completable;
import io.reactivex.Single;

public abstract class CashBoxRepository {
    // PeriodicWork
    private final PeriodicEntryWorkRepository workRepository;
    // CashBox Manager
    private LiveData<List<InfoWithCash>> cashBoxesInfo = null;

    protected CashBoxRepository(Context context) {
        // PeriodicWork
        workRepository = PeriodicEntryWorkRepository.getInstance(context.getApplicationContext());
    }

    public LiveData<List<InfoWithCash>> getCashBoxesInfo() {
        if (cashBoxesInfo == null)
            cashBoxesInfo = getCashBoxDao().getAllCashBoxesInfo();
        return cashBoxesInfo;
    }

    protected abstract CashBoxBaseDao getCashBoxDao();

    protected abstract CashBoxEntryBaseDao getCashBoxEntryDao();

    // CashBox Manager

    // Main functionality to Override

    public Single<Long> insertCashBoxInfo(@NonNull CashBoxInfo cashBoxInfo) {
        return getCashBoxDao().insert(cashBoxInfo);
    }

    public Completable updateCashBoxInfo(CashBoxInfo cashBoxInfo) {
        return getCashBoxDao().update(cashBoxInfo);
    }

    public Completable deleteCashBox(@NonNull CashBoxInfo cashBoxInfo) {
        workRepository.cancelAllCashBoxWork(cashBoxInfo.getId());
        return getCashBoxDao().delete(cashBoxInfo);
    }

    public Single<Integer> deleteAllCashBoxes() {
        return workRepository.deleteAllPeriodicEntryWorks()
                .flatMap(integer -> getCashBoxDao().deleteAll());
    }

    // Rest of functionality

    @NonNull
    public LiveData<CashBox> getOrderedCashBox(long id) {
        MediatorLiveData<CashBox> liveDataMerger = new MediatorLiveData<>();
        liveDataMerger.setValue(CashBox.create("Loading...")); // Puppet CashBox to be changed

        liveDataMerger.addSource(getCashBoxDao().getCashBoxInfoWithCashById(id),
                infoWithCash -> {
                    if (infoWithCash == null)
                        return;
                    LogUtil.debug("Prueba", "Change in info with cash: " + infoWithCash.toString());
                    CashBox cashBox = liveDataMerger.getValue();
                    cashBox.setInfoWithCash(infoWithCash);
                    liveDataMerger.setValue(cashBox);
                });
        liveDataMerger.addSource(getCashBoxEntryDao().getEntriesByCashBox(id),
                entries -> {
                    if (entries == null)
                        return;
                    LogUtil.debug("Prueba", "Change in entries: " + entries.toString());
                    CashBox cashBox = liveDataMerger.getValue();
                    cashBox.setEntries(entries);
                    liveDataMerger.setValue(cashBox);
                });
        liveDataMerger.addSource(getCashBoxDao().getNamesByCashBox(id),
                strings -> {
                    HashSet<String> stringSet = new HashSet<>(strings);
                    stringSet.add(Participant.getSelfName());
                    CashBox cashBox = liveDataMerger.getValue();
                    cashBox.setCacheNames(stringSet);
                    liveDataMerger.setValue(cashBox);
                });

        return liveDataMerger;
    }

    public Single<CashBox> getCashBox(long id) {
        return getCashBoxDao().getCashBoxById(id, getCashBoxEntryDao());
    }

    public Completable insertCashBox(@NonNull CashBox cashBox) {
        if (cashBox.getEntries().isEmpty())
            return insertCashBoxInfo(cashBox.getInfoWithCash().getCashBoxInfo()).ignoreElement();
        else {
            return insertCashBoxInfo(cashBox.getInfoWithCash().getCashBoxInfo())
                    .flatMapCompletable(id -> {
                        LogUtil.debug("Prueba", "Id: " + id);
                        return insertEntries(id, cashBox.getEntries());
                    });
        }
    }

    public Completable setCashBoxCurrency(long cashBoxId, @NonNull Currency currency) {
        return getCashBoxDao().setCashBoxCurrency(cashBoxId, currency);
    }

    @NonNull
    public Single<Currency> getCashBoxCurrency(long cashBoxId) {
        return getCashBoxDao().getCashBoxCurrency(cashBoxId);
    }

    public Completable moveCashBoxInfo(@NonNull InfoWithCash infoWithCash, long toOrderPos) {
        return getCashBoxDao().moveCashBoxToOrderPos(infoWithCash.getCashBoxInfo().getId(),
                infoWithCash.getCashBoxInfo().getOrderId(), toOrderPos);
    }

    // Entries

    // Main functionality to override

    public Completable insertEntry(long cashBoxId, @NonNull EntryBase<?> entry) {
        return getCashBoxEntryDao().insert(cashBoxId, entry);
    }

    public Completable insertEntries(long cashBoxId, @NonNull Collection<? extends EntryBase<?>> entries) {
        return getCashBoxEntryDao().insert(cashBoxId, entries);
    }

    public Completable insertEntriesRaw(Collection<? extends EntryBase<?>> entries) {
        return getCashBoxEntryDao().insertRaw(entries);
    }

    public Completable updateEntryInfo(EntryInfo entry) {
        return getCashBoxEntryDao().updateEntry(entry);
    }

    public Completable deleteEntry(EntryBase<?> entry) {
        return getCashBoxEntryDao().delete(entry);
    }

    public Completable modifyEntry(long id, double amount, String info, Calendar date) {
        return getCashBoxEntryDao().modify(id, amount, info, date);
    }

    public Single<Integer> deleteAllEntries(long cashBoxId) {
        return getCashBoxEntryDao().deleteAll(cashBoxId);
    }

    // Rest of functionality

    public Completable insertEntryRaw(@NonNull EntryBase<?> entry) {
        return getCashBoxEntryDao().insertRaw(entry);
    }

    public LiveData<List<CashBoxBalances.Entry>> getBalances(long cashBoxId) {
        return getCashBoxEntryDao().getBalances(cashBoxId);
    }

    public LiveData<Double> getCashBalance(long cashBoxId, String name) {
        return getCashBoxEntryDao().getCashBalance(cashBoxId, name);
    }

    // Group Entries

    // Main functionality to override

    public Single<? extends List<? extends EntryBase<?>>> getGroupEntries(long groupId) {
        return getCashBoxEntryDao().getGroupEntries(groupId);
    }

    public Completable modifyGroupEntry(long groupId, double amount, String info, Calendar date) {
        if (groupId == EntryInfo.NO_GROUP)
            throw new IllegalArgumentException("Default group id cannot be deleted");
        return getCashBoxEntryDao().modifyGroup(groupId, amount, info, date);
    }

    public Single<Integer> deleteGroupEntries(long groupId) {
        if (groupId == EntryInfo.NO_GROUP)
            throw new IllegalArgumentException("Default group id cannot be deleted");
        return getCashBoxEntryDao().deleteGroup(groupId);
    }

    // Periodic Entries

    public Completable addPeriodicEntryWorkRequest(@NonNull PeriodicEntryPojo.PeriodicEntryWorkRequest workRequest) {
        return workRepository.addPeriodicEntryWorkRequest(workRequest);
    }

    // Participants

    // Main functionality to override

    public Completable insertParticipant(long entryId, @NonNull Participant participant) {
        return getCashBoxEntryDao().insertParticipant(entryId, participant);
    }

    public Completable updateParticipant(@NonNull Participant participant) {
        return getCashBoxEntryDao().updateParticipant(participant).ignoreElement();
    }

    public Completable deleteParticipant(@NonNull Participant participant) {
        return getCashBoxEntryDao().deleteParticipant(participant);
    }
}
