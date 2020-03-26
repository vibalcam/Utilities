package com.vibal.utilities.db;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;

import com.vibal.utilities.modelsNew.CashBox;
import com.vibal.utilities.modelsNew.CashBoxInfo;
import com.vibal.utilities.modelsNew.Entry;
import com.vibal.utilities.modelsNew.PeriodicEntryPojo;
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

    protected CashBoxRepository(Application application) {
        // PeriodicWork
        UtilitiesDatabase database = UtilitiesDatabase.getInstance(application);
        workRepository = new PeriodicEntryWorkRepository(application);
    }

//    protected void setCashBoxesInfo(LiveData<List<CashBox.InfoWithCash>> cashBoxesInfo) {
//        this.cashBoxesInfo = cashBoxesInfo;
//    }

    public LiveData<List<CashBox.InfoWithCash>> getCashBoxesInfo() {
        if(cashBoxesInfo == null)
            cashBoxesInfo = getCashBoxDao().getAllCashBoxesInfo();
        return cashBoxesInfo;
    }

    protected abstract CashBoxBaseDao getCashBoxDao();
    protected abstract CashBoxEntryBaseDao getCashBoxEntryDao();

    // CashBox Manager

    @NonNull
    public LiveData<CashBox> getOrderedCashBox(long id) {
        MediatorLiveData<CashBox> liveDataMerger = new MediatorLiveData<>();
        liveDataMerger.setValue(new CashBox("Loading...")); // Puppet CashBox to be changed

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
//        return getCashBoxDao().insert(cashBox, getCashBoxEntryDao());
        if (cashBox.getEntries().isEmpty())
            return insertCashBoxInfo(cashBox.getInfoWithCash()).ignoreElement();
        else {
            return insertCashBoxInfo(cashBox.getInfoWithCash())
                    .flatMapCompletable(id -> {
                        LogUtil.debug("Prueba", "Id: " + id);
                        ArrayList<Entry> entryArrayList = new ArrayList<>();
                        for (Entry entry : cashBox.getEntries())
                            entryArrayList.add(entry.getEntryWithCashBoxId(id));
                        return insertAllEntries(entryArrayList);
                    });
        }
    }

    public Single<Long> insertCashBoxInfo(@NonNull CashBox.InfoWithCash infoWithCash) {
        return getCashBoxDao().insert(infoWithCash.getCashBoxInfo());
    }

    public Completable updateCashBoxInfo(CashBoxInfo cashBoxInfo) {
        return getCashBoxDao().update(cashBoxInfo);
    }

    public Completable setCashBoxCurrency(long cashBoxId, @NonNull Currency currency) {
        return getCashBoxDao().setCashBoxCurrency(cashBoxId, currency);
    }

    public Completable moveCashBoxInfo(@NonNull CashBox.InfoWithCash infoWithCash, long toOrderPos) {
        return getCashBoxDao().moveCashBoxToOrderPos(infoWithCash.getCashBoxInfo().getId(),
                infoWithCash.getCashBoxInfo().getOrderId(), toOrderPos);
    }

//    public Completable setDeleted(CashBoxInfo cashBoxInfo, boolean deleted) {
//        if(deleted) //Cancel works associated with the CashBox
//            workRepository.cancelAllCashBoxWork(cashBoxInfo.getId());
//        cashBoxInfo.setDeleted(deleted);
//        return updateCashBoxInfo(cashBoxInfo);
//    }

//    public Single<Integer> setDeletedAll(boolean deleted) {
//        if(deleted) // cancel all periodic works and set deleted
//            return workRepository.deleteAllPeriodicEntryWorks()
//                    .flatMap(integer -> getCashBoxDao().setDeletedAll(true));
//        else
//            return getCashBoxDao().setDeletedAll(false);
//    }

    public Completable deleteCashBox(@NonNull CashBoxInfo cashBoxInfo) {
        workRepository.cancelAllCashBoxWork(cashBoxInfo.getId());
        return getCashBoxDao().delete(cashBoxInfo);
    }

    public Single<Integer> deleteAllCashBoxes() {
        return workRepository.deleteAllPeriodicEntryWorks()
                .flatMap(integer -> getCashBoxDao().deleteAll());
    }

    // Entries

    public Completable insertEntry(Entry entry) {
        return getCashBoxEntryDao().insert(entry);
    }

    public Completable insertAllEntries(Collection<Entry> entries) {
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

    public Single<List<Entry>> getGroupEntries(long groupId) {
        return getCashBoxEntryDao().getGroupEntries(groupId);
    }

    public Completable modifyGroupEntry(long groupId, double amount, String info, Calendar date) {
        return getCashBoxEntryDao().modifyGroup(groupId, amount, info, date);
    }

    public Single<Integer> deleteGroupEntries(long groupId) {
        return getCashBoxEntryDao().deleteGroup(groupId);
    }

    // Periodic Entries
    public Completable addPeriodicEntryWorkRequest(@NonNull PeriodicEntryPojo.PeriodicEntryWorkRequest workRequest) {
        return workRepository.addPeriodicEntryWorkRequest(workRequest);
    }
}
