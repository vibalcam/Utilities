package com.vibal.utilities.db;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;

import com.vibal.utilities.modelsNew.CashBox;
import com.vibal.utilities.util.LogUtil;

import java.util.Collection;
import java.util.List;

import io.reactivex.Completable;
import io.reactivex.Single;

public class CashBoxRepository {
    private CashBoxDao cashBoxDao;
    private CashBoxEntryDao cashBoxEntryDao;
    private LiveData<List<CashBox.InfoWithCash>> cashBoxesInfo;

    public CashBoxRepository(Application application) {
        UtilitiesDatabase database = UtilitiesDatabase.getInstance(application);
        cashBoxDao = database.cashBoxDao();
        cashBoxEntryDao = database.cashBoxEntryDao();
        cashBoxesInfo = cashBoxDao.getAllCashBoxesInfo();
    }

    public LiveData<List<CashBox.InfoWithCash>> getCashBoxesInfo() {
        return cashBoxesInfo;
    }

    @NonNull
    public LiveData<CashBox> getOrderedCashBox(long id) {
        MediatorLiveData<CashBox> liveDataMerger = new MediatorLiveData<>();
        liveDataMerger.setValue(new CashBox("Loading..."));

        liveDataMerger.addSource(cashBoxDao.getCashBoxInfoWithCashById(id),
                infoWithCash -> {
                    if(infoWithCash == null)
                        return;
                    LogUtil.debug("Prueba", "Change in info with cash: " + infoWithCash.toString());
                    CashBox cashBox = liveDataMerger.getValue();
                    cashBox.setInfoWithCash(infoWithCash);
                    liveDataMerger.setValue(cashBox);
                });
        liveDataMerger.addSource(cashBoxEntryDao.getEntriesByCashBoxId(id),
                entries -> {
                    if(entries == null)
                        return;
                    LogUtil.debug("Prueba", "Change in entries: " + entries.toString());
                    CashBox cashBox = liveDataMerger.getValue();
                    cashBox.setEntries(entries);
                    liveDataMerger.setValue(cashBox);
                });

        return liveDataMerger;
    }

    public Single<CashBox> getCashBox(long id) {
        return cashBoxDao.getCashBoxById(id);
    }

    public Completable insertCashBox(@NonNull CashBox cashBox) {
        return cashBoxDao.insert(cashBox, cashBoxEntryDao);
    }

    public Single<Long> insertCashBoxInfo(@NonNull CashBox.InfoWithCash infoWithCash) {
        return cashBoxDao.insert(infoWithCash.getCashBoxInfo());
    }

    public Completable updateCashBoxInfo(CashBoxInfo cashBoxInfo) {
        return cashBoxDao.update(cashBoxInfo);
    }

    public Completable moveCashBoxInfo(@NonNull CashBox.InfoWithCash infoWithCash, long toOrderPos) {
        return cashBoxDao.moveCashBoxToOrderPos(infoWithCash.getCashBoxInfo().getId(),
                infoWithCash.getCashBoxInfo().getOrderId(), toOrderPos);
    }

    public Completable deleteCashBox(@NonNull CashBox.InfoWithCash cashBoxInfo) {
        return cashBoxDao.delete(cashBoxInfo.getCashBoxInfo());
    }

    public Single<Integer> deleteAllCashBoxes() {
        return cashBoxDao.deleteAll();
    }

    public Completable insertEntry(CashBox.Entry entry) {
        return cashBoxEntryDao.insert(entry);
    }

    public Completable insertAllEntries(Collection<CashBox.Entry> entries) {
        return cashBoxEntryDao.insertAll(entries);
    }

    public Completable updateEntry(CashBox.Entry entry) {
        return cashBoxEntryDao.update(entry);
    }

    public Completable modifyEntry(long id, double amount, String info) {
        return cashBoxEntryDao.modify(id, amount, info);
    }

    public Single<List<CashBox.Entry>> getGroupEntries(long groupId) {
        return cashBoxEntryDao.getGroupEntries(groupId);
    }

    public Completable modifyGroupEntry(long groupId, double amount, String info) {
        return cashBoxEntryDao.modifyGroup(groupId, amount, info);
    }

    public Single<Integer> deleteGroupEntries(long groupId) {
        return cashBoxEntryDao.deleteGroup(groupId);
    }

    public Completable deleteEntry(CashBox.Entry entry) {
        return cashBoxEntryDao.delete(entry);
    }

    public Single<Integer> deleteAllEntries(long cashBoxId) {
        return cashBoxEntryDao.deleteAll(cashBoxId);
    }
}
