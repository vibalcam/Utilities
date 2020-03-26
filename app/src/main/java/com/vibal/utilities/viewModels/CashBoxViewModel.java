package com.vibal.utilities.viewModels;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.vibal.utilities.db.CashBoxRepository;
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

import static com.vibal.utilities.modelsNew.CashBoxInfo.NO_ID;

public abstract class CashBoxViewModel extends AndroidViewModel {
    private static final String TAG = "PruebaCashBoxViewModel";

//    private WorkManager workManager;
    private CashBoxRepository repository;
    private LiveData<List<CashBox.InfoWithCash>> cashBoxesInfo;
    private LiveData<CashBox> cashBox;
//    private long currentCashBoxId = NO_CASHBOX;
//    @NonNull
//    private CompositeDisposable compositeDisposable = new CompositeDisposable();

    public CashBoxViewModel(@NonNull Application application, @NonNull CashBoxRepository repository) {
        super(application);
//        workManager = WorkManager.getInstance(application);
//        repository = new CashBoxLocalRepository(application);
        this.repository = repository;
        cashBoxesInfo = repository.getCashBoxesInfo();
    }

    public LiveData<List<CashBox.InfoWithCash>> getCashBoxesInfo() {
        return cashBoxesInfo;
    }

    @NonNull
    public LiveData<CashBox> getCurrentCashBox() {
        LogUtil.debug(TAG, "Id cashBox: " + getCurrentCashBoxId());
//        return repository.getOrderedCashBox(currentCashBoxId);
        return cashBox;
    }

    public long getCurrentCashBoxId() {
//        return currentCashBoxId;
        return cashBox == null || cashBox.getValue() == null ? NO_ID :
                cashBox.getValue().getInfoWithCash().getId();
    }

    public void setCurrentCashBoxId(long currentCashBoxId) {
        // If different, get the current cashbox
        if (this.getCurrentCashBoxId() != currentCashBoxId)
            cashBox = repository.getOrderedCashBox(currentCashBoxId);
        // Change current cashbox id
//        this.currentCashBoxId = currentCashBoxId;
    }

    public Single<CashBox> getCashBox(long id) {
        return repository.getCashBox(id);
    }

    public Completable addCashBoxInfo(@NonNull CashBox.InfoWithCash cashBoxInfo) {
        return repository.insertCashBoxInfo(cashBoxInfo).ignoreElement();
    }

    /**
     * CAREFUL: ADDING CASHBOX BEFORE ANOTHER ONE, DOES NOT ASSURE ORDER ID
     */
    public Completable addCashBox(@NonNull CashBox cashBox) {
        return repository.insertCashBox(cashBox);
    }

    public Completable changeCashBoxName(@NonNull CashBox.InfoWithCash cashBoxInfo, @NonNull String newName)
            throws IllegalArgumentException {
        CashBoxInfo changedCashBoxInfo = cashBoxInfo.getCashBoxInfo().clone();
        changedCashBoxInfo.setName(newName);
        return repository.updateCashBoxInfo(changedCashBoxInfo);
    }

    public Completable setCurrency(long cashBoxId, @NonNull Currency currency) {
        return repository.setCashBoxCurrency(cashBoxId, currency);
    }

    public Completable setCurrentCashBoxCurrency(@NonNull Currency currency) {
        return setCurrency(getCurrentCashBoxId(), currency);
    }

    public Completable recycleCashBoxInfo(@NonNull CashBox.InfoWithCash infoWithCash) {
//        CashBoxInfo cashBoxInfo = infoWithCash.getCashBoxInfo();
//        //Cancel works associated with the CashBox
//        workManager.cancelAllWorkByTag(String.format(Locale.US,
//                RxPeriodicEntryWorker.TAG_CASHBOX_ID, cashBoxInfo.getId()));
//
//        //Move to recycle bin
//        cashBoxInfo.setDeleted(true);
//        return repository.updateCashBoxInfo(cashBoxInfo);
        return repository.deleteCashBox(infoWithCash.getCashBoxInfo());
    }

    public Single<Integer> recycleAllCashBoxes() {
//        //Cancel all works associated with CashBoxes
//        workManager.cancelAllWorkByTag(RxPeriodicEntryWorker.TAG_PERIODIC);
        //Move all to recycle bin
        return repository.deleteAllCashBoxes();
    }

    public Completable duplicateCashBox(@NonNull CashBox cashBox, @NonNull String newName) {
        CashBox cashBoxClone = cashBox.cloneContents();
        cashBoxClone.setName(newName);
        return addCashBox(cashBoxClone);
    }

    public Completable duplicateCashBox(long cashBoxId, @NonNull String newName) {
        return repository.getCashBox(cashBoxId)
                .flatMapCompletable(cashBox -> duplicateCashBox(cashBox, newName));

//        CashBox cashBoxClone = cashBox.cloneContents();
//        cashBoxClone.setName(newName);
//        return addCashBox(cashBoxClone);
    }

    public Completable moveCashBox(@NonNull CashBox.InfoWithCash infoWithCash, int toIndex) {
        List<CashBox.InfoWithCash> cashBoxInfoList = cashBoxesInfo.getValue();
        if (cashBoxInfoList == null)
            return Completable.error(new IllegalArgumentException("Null list of CashBoxes"));
        else if (toIndex < 0 || toIndex >= cashBoxInfoList.size())
            return Completable.error(new IndexOutOfBoundsException("Cannot move to index in list"));

        return repository.moveCashBoxInfo(infoWithCash,
                cashBoxInfoList.get(toIndex).getCashBoxInfo().getOrderId());
    }

    public Completable addEntryToCurrentCashBox(@NonNull Entry entry) {
        return addEntry(getCurrentCashBoxId(), entry);
    }

    public Completable addEntry(long cashBoxId, @NonNull Entry entry) {
        return repository.insertEntry(entry.getEntryWithCashBoxId(cashBoxId));
    }

    public Completable addAllEntriesToCurrentCashBox(@NonNull List<Entry> entries) {
        return addAllEntries(getCurrentCashBoxId(), entries);
    }

    private Completable addAllEntries(long cashBoxId, @NonNull Collection<Entry> entries) {
        LogUtil.debug(TAG, entries.toString());
        ArrayList<Entry> entryArrayList = new ArrayList<>();
        for (Entry entry : entries)
            entryArrayList.add(entry.getEntryWithCashBoxId(cashBoxId));
        return repository.insertAllEntries(entryArrayList);
    }

    public Completable addAllEntries(@NonNull Collection<Entry> entries) {
        return repository.insertAllEntries(entries);
    }

    public Completable updateEntry(Entry entry) {
        return repository.updateEntry(entry);
    }

    public Completable modifyEntry(@NonNull Entry entry, double amount, String info, Calendar date) {
        return repository.modifyEntry(entry.getId(), amount, info, date);
    }

    public Completable deleteEntry(Entry entry) {
        return repository.deleteEntry(entry);
    }

    public Single<Integer> deleteAllEntriesFromCurrentCashBox() {
        return repository.deleteAllEntries(getCurrentCashBoxId());
    }

    public Completable addPeriodicEntryWorkRequest(@NonNull PeriodicEntryPojo.PeriodicEntryWorkRequest workRequest) {
        return repository.addPeriodicEntryWorkRequest(workRequest);
    }

    public Single<List<Entry>> getGroupEntries(Entry entry) {
        return repository.getGroupEntries(entry.getGroupId());
    }

    public Completable modifyGroupEntry(Entry entry, double amount, String info, Calendar date) {
        return repository.modifyGroupEntry(entry.getGroupId(), amount, info, date);
    }

    public Single<Integer> deleteGroupEntries(Entry entry) {
        return repository.deleteGroupEntries(entry.getGroupId());
    }

    // Periodic Entries
//    public Completable deletePeriodicInactive() {
//        return repository.deletePeriodicInactive();
//    }

//    public void addDisposable(@NonNull Disposable disposable) {
//        compositeDisposable.add(disposable);
//    }
//
//    @Override
//    protected void onCleared() {
//        super.onCleared();
////        compositeDisposable.clear();
//        LogUtil.debug(TAG, "onCleared: clearing disposable");
//    }
}
