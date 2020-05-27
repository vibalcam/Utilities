package com.vibal.utilities.persistence.repositories;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;

import com.vibal.utilities.modelsNew.CashBox;
import com.vibal.utilities.modelsNew.CashBoxInfo;
import com.vibal.utilities.modelsNew.Entry;
import com.vibal.utilities.modelsNew.PeriodicEntryPojo;
import com.vibal.utilities.persistence.db.CashBoxBaseDao;
import com.vibal.utilities.persistence.db.CashBoxEntryBaseDao;
import com.vibal.utilities.util.LogUtil;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Currency;
import java.util.List;

import io.reactivex.Completable;
import io.reactivex.Single;

public abstract class CashBoxRepository {
    // PeriodicWork
    private PeriodicEntryWorkRepository workRepository;
    // CashBox Manager
    private LiveData<List<CashBox.InfoWithCash>> cashBoxesInfo = null;

    protected CashBoxRepository(Context context) {
        // PeriodicWork
        workRepository = PeriodicEntryWorkRepository.getInstance(context.getApplicationContext());
    }

    public LiveData<List<CashBox.InfoWithCash>> getCashBoxesInfo() {
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
        liveDataMerger.addSource(getCashBoxEntryDao().getEntriesByCashBoxId(id),
                entries -> {
                    if (entries == null)
                        return;
                    LogUtil.debug("Prueba", "Change in entries: " + entries.toString());
                    CashBox cashBox = liveDataMerger.getValue();
                    cashBox.setEntries(entries);
                    liveDataMerger.setValue(cashBox);
                });

        return liveDataMerger;
    }

    public Single<CashBox> getCashBox(long id) {
        return getCashBoxDao().getCashBoxById(id);
    }

    public Completable insertCashBox(@NonNull CashBox cashBox) {
        if (cashBox.getEntries().isEmpty())
            return insertCashBoxInfo(cashBox.getInfoWithCash().getCashBoxInfo()).ignoreElement();
        else {
            return insertCashBoxInfo(cashBox.getInfoWithCash().getCashBoxInfo())
                    .flatMapCompletable(id -> {
                        LogUtil.debug("Prueba", "Id: " + id);
                        ArrayList<Entry> entryArrayList = new ArrayList<>();
                        for (Entry entry : cashBox.getEntries())
                            entryArrayList.add(entry.getEntryWithCashBoxId(id));
                        return insertEntries(entryArrayList);
                    });
        }
    }

    public Completable setCashBoxCurrency(long cashBoxId, @NonNull Currency currency) {
        return getCashBoxDao().setCashBoxCurrency(cashBoxId, currency);
    }

    public Completable moveCashBoxInfo(@NonNull CashBox.InfoWithCash infoWithCash, long toOrderPos) {
        return getCashBoxDao().moveCashBoxToOrderPos(infoWithCash.getCashBoxInfo().getId(),
                infoWithCash.getCashBoxInfo().getOrderId(), toOrderPos);
    }

    // Entries

    // Main functionality to override

    public Completable insertEntry(Entry entry) {
        return getCashBoxEntryDao().insert(entry);
    }

    public Completable insertEntries(Collection<Entry> entries) {
        return getCashBoxEntryDao().insertAll(entries);
    }

    public Completable updateEntry(Entry entry) {
        return getCashBoxEntryDao().update(entry);
    }

    public Completable modifyEntry(long id, double amount, String info, Calendar date) {
        return getCashBoxEntryDao().modify(id, amount, info, date);
    }

    public Completable deleteEntry(Entry entry) {
        return getCashBoxEntryDao().delete(entry);
    }

    public Single<Integer> deleteAllEntries(long cashBoxId) {
        return getCashBoxEntryDao().deleteAll(cashBoxId);
    }

    // Group Entries

    // Main functionality to override

    public Single<List<Entry>> getGroupEntries(long groupId) {
        return getCashBoxEntryDao().getGroupEntries(groupId);
    }

    public Completable modifyGroupEntry(long groupId, double amount, String info, Calendar date) {
        if (groupId == Entry.NO_GROUP)
            throw new IllegalArgumentException("Default group id cannot be deleted");
        return getCashBoxEntryDao().modifyGroup(groupId, amount, info, date);
    }

    public Single<Integer> deleteGroupEntries(long groupId) {
        if (groupId == Entry.NO_GROUP)
            throw new IllegalArgumentException("Default group id cannot be deleted");
        return getCashBoxEntryDao().deleteGroup(groupId);
    }

    // Periodic Entries

    public Completable addPeriodicEntryWorkRequest(@NonNull PeriodicEntryPojo.PeriodicEntryWorkRequest workRequest) {
        return workRepository.addPeriodicEntryWorkRequest(workRequest);
    }
}
