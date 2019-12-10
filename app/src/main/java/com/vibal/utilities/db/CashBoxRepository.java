package com.vibal.utilities.db;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.work.WorkManager;

import com.vibal.utilities.backgroundTasks.RxPeriodicEntryWorker;
import com.vibal.utilities.modelsNew.CashBox;
import com.vibal.utilities.modelsNew.CashBoxInfo;
import com.vibal.utilities.modelsNew.PeriodicEntryPojo;
import com.vibal.utilities.util.LogUtil;

import java.util.Calendar;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

import io.reactivex.Completable;
import io.reactivex.Single;

public class CashBoxRepository {
    private WorkManager workManager;
    private CashBoxDao cashBoxDao;
    private CashBoxEntryDao cashBoxEntryDao;
    private PeriodicEntryWorkDao periodicEntryWorkDao;
    private LiveData<List<CashBox.InfoWithCash>> cashBoxesInfo;

    public CashBoxRepository(Application application) {
        UtilitiesDatabase database = UtilitiesDatabase.getInstance(application);

        // CashBox Manager
        cashBoxDao = database.cashBoxDao();
        cashBoxEntryDao = database.cashBoxEntryDao();
        cashBoxesInfo = cashBoxDao.getAllCashBoxesInfo(false);

        // WorkManager
        workManager = WorkManager.getInstance(application);
        periodicEntryWorkDao = database.periodicEntryWorkDao();
    }

    // CashBox Manager

    public LiveData<List<CashBox.InfoWithCash>> getCashBoxesInfo() {
        return cashBoxesInfo;
    }

    @NonNull
    public LiveData<CashBox> getOrderedCashBox(long id) {
        MediatorLiveData<CashBox> liveDataMerger = new MediatorLiveData<>();
        liveDataMerger.setValue(new CashBox("Loading..."));

        liveDataMerger.addSource(cashBoxDao.getCashBoxInfoWithCashById(id),
                infoWithCash -> {
                    if (infoWithCash == null)
                        return;
                    LogUtil.debug("Prueba", "Change in info with cash: " + infoWithCash.toString());
                    CashBox cashBox = liveDataMerger.getValue();
                    cashBox.setInfoWithCash(infoWithCash);
                    liveDataMerger.setValue(cashBox);
                });
        liveDataMerger.addSource(cashBoxEntryDao.getEntriesByCashBoxId(id),
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

    public LiveData<List<CashBox.InfoWithCash>> getAllDeletedCashBoxesInfo() {
        return cashBoxDao.getAllCashBoxesInfo(true);
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

    public Single<Integer> setDeletedAll(boolean deleted) {
        return cashBoxDao.setDeletedAll(deleted);
    }

    public Completable deleteCashBox(@NonNull CashBoxInfo cashBoxInfo) {
        return cashBoxDao.delete(cashBoxInfo);
    }

    public Single<Integer> clearRecycleBin() {
        return cashBoxDao.clearRecycleBin();
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

    public Completable modifyEntry(long id, double amount, String info, Calendar date) {
        return cashBoxEntryDao.modify(id, amount, info, date);
    }

    public Completable deleteEntry(CashBox.Entry entry) {
        return cashBoxEntryDao.delete(entry);
    }

    public Single<Integer> deleteAllEntries(long cashBoxId) {
        return cashBoxEntryDao.deleteAll(cashBoxId);
    }

    // Group Entries

    public Single<List<CashBox.Entry>> getGroupEntries(long groupId) {
        return cashBoxEntryDao.getGroupEntries(groupId);
    }

    public Completable modifyGroupEntry(long groupId, double amount, String info, Calendar date) {
        return cashBoxEntryDao.modifyGroup(groupId, amount, info, date);
    }

    public Single<Integer> deleteGroupEntries(long groupId) {
        return cashBoxEntryDao.deleteGroup(groupId);
    }

    // WorkManager

    public LiveData<List<PeriodicEntryPojo>> getPeriodicEntries() {
        return periodicEntryWorkDao.getAllWorkPojos();
    }

    public Single<PeriodicEntryPojo> getPeriodicEntryPojo(UUID uuid) {
        return periodicEntryWorkDao.getWorkPojoByUUID(uuid);
    }

    public Completable addPeriodicEntryWorkRequest(@NonNull PeriodicEntryPojo.PeriodicEntryWorkRequest workRequest) {
        LogUtil.debug("PruebaPeriodicViewModel", "Add new periodic work");
        workManager.enqueue(workRequest.getWorkRequest()); //todo observe, calculate difference between lists
        //Add the data to database
        return periodicEntryWorkDao.insert(workRequest.getWorkInfo());
    }

    public Single<Integer> updatePeriodicEntryWorkInfo(PeriodicEntryPojo.PeriodicEntryWorkInfo workInfo) {
        return periodicEntryWorkDao.update(workInfo);
    }

//    public Completable replacePeriodicEntryWorkInfo(PeriodicEntryPojo.PeriodicEntryWorkInfo workInfo) {
//        return deletePeriodicEntryWorkInfo(workInfo)
//                .flatMapCompletable(integer -> integer==0 ?
//                        Completable.error(new IllegalArgumentException("No entry delted")) :
//                        addPeriodicEntryWorkRequest(new PeriodicEntryPojo.PeriodicEntryWorkRequest(
//                                workInfo.getCashBoxId(),workInfo.getAmount(),workInfo.getInfo(),
//                                workInfo.getRepeatInterval())));
//    }

    public Single<Integer> deletePeriodicEntryWorkInfo(@NonNull PeriodicEntryPojo.PeriodicEntryWorkInfo workInfo) {
        workManager.cancelWorkById(workInfo.getWorkId()); //Cancel work
        return periodicEntryWorkDao.delete(workInfo); //Delete from database
    }

    public Single<Integer> deleteAllPeriodicEntryWorks() {
        workManager.cancelAllWorkByTag(RxPeriodicEntryWorker.TAG_PERIODIC);
        return periodicEntryWorkDao.deleteAll();
    }

    public Completable deletePeriodicInactive() {
        return periodicEntryWorkDao.deleteInactive();
    }
}
