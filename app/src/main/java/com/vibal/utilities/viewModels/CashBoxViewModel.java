package com.vibal.utilities.viewModels;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.work.WorkManager;

import com.vibal.utilities.backgroundTasks.RxPeriodicEntryWorker;
import com.vibal.utilities.db.CashBoxRepository;
import com.vibal.utilities.modelsNew.CashBox;
import com.vibal.utilities.modelsNew.CashBoxInfo;
import com.vibal.utilities.modelsNew.PeriodicEntryPojo;
import com.vibal.utilities.util.LogUtil;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.List;
import java.util.Locale;

import io.reactivex.Completable;
import io.reactivex.Single;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;

import static com.vibal.utilities.modelsNew.CashBoxInfo.NO_CASHBOX;

public class CashBoxViewModel extends AndroidViewModel {
    private static final String TAG = "PruebaCashBoxViewModel";

    private WorkManager workManager;
    private CashBoxRepository repository;
    private LiveData<List<CashBox.InfoWithCash>> cashBoxesInfo;
    private long currentCashBoxId = NO_CASHBOX;
    @NonNull
    private CompositeDisposable compositeDisposable = new CompositeDisposable();

    public CashBoxViewModel(@NonNull Application application) {
        super(application);
        workManager = WorkManager.getInstance(application);
        repository = new CashBoxRepository(application);
        cashBoxesInfo = repository.getCashBoxesInfo();
    }

    public LiveData<List<CashBox.InfoWithCash>> getCashBoxesInfo() {
        return cashBoxesInfo;
    }

    @NonNull
    public LiveData<CashBox> getCurrentCashBox() {
        LogUtil.debug(TAG, "Id cashBox: " + currentCashBoxId);
        return repository.getOrderedCashBox(currentCashBoxId);
    }

    public long getCurrentCashBoxId() {
        return currentCashBoxId;
    }

    public void setCurrentCashBoxId(long currentCashBoxId) {
        this.currentCashBoxId = currentCashBoxId;
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

    public Completable recycleCashBoxInfo(@NonNull CashBox.InfoWithCash infoWithCash) {
        CashBoxInfo cashBoxInfo = infoWithCash.getCashBoxInfo();
        //Cancel works associated with the CashBox
        workManager.cancelAllWorkByTag(String.format(Locale.US,
                RxPeriodicEntryWorker.TAG_CASHBOX_ID, cashBoxInfo.getId()));

        //Move to recycle bin
        cashBoxInfo.setDeleted(true);
        return repository.updateCashBoxInfo(cashBoxInfo);
    }

    public Single<Integer> recycleAllCashBoxes() {
        //Cancel all works associated with CashBoxes
        workManager.cancelAllWorkByTag(RxPeriodicEntryWorker.TAG_PERIODIC);
        //Move all to recycle bin
        return repository.setDeletedAll(true);
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

    public Completable addEntryToCurrentCashBox(@NonNull CashBox.Entry entry) {
        return addEntry(currentCashBoxId, entry);
    }

    public Completable addEntry(long cashBoxId, @NonNull CashBox.Entry entry) {
        return repository.insertEntry(entry.getEntryWithCashBoxId(cashBoxId));
    }

    public Completable addAllEntriesToCurrentCashBox(@NonNull List<CashBox.Entry> entries) {
        return addAllEntries(currentCashBoxId, entries);
    }

    private Completable addAllEntries(long cashBoxId, @NonNull Collection<CashBox.Entry> entries) {
        ArrayList<CashBox.Entry> entryArrayList = new ArrayList<>();
        for (CashBox.Entry entry : entries)
            entryArrayList.add(entry.getEntryWithCashBoxId(cashBoxId));
        return repository.insertAllEntries(entryArrayList);
    }

    public Completable addAllEntries(@NonNull Collection<CashBox.Entry> entries) {
        return repository.insertAllEntries(entries);
    }

    public Completable updateEntry(CashBox.Entry entry) {
        return repository.updateEntry(entry);
    }

    public Completable modifyEntry(@NonNull CashBox.Entry entry, double amount, String info, Calendar date) {
        return repository.modifyEntry(entry.getId(), amount, info, date);
    }

    public Completable deleteEntry(CashBox.Entry entry) {
        return repository.deleteEntry(entry);
    }

    public Single<Integer> deleteAllEntriesFromCurrentCashBox() {
        return repository.deleteAllEntries(currentCashBoxId);
    }

    public Completable addPeriodicEntryWorkRequest(@NonNull PeriodicEntryPojo.PeriodicEntryWorkRequest workRequest) {
        return repository.addPeriodicEntryWorkRequest(workRequest);
    }

    public Single<List<CashBox.Entry>> getGroupEntries(CashBox.Entry entry) {
        return repository.getGroupEntries(entry.getGroupId());
    }

    public Completable modifyGroupEntry(CashBox.Entry entry, double amount, String info, Calendar date) {
        return repository.modifyGroupEntry(entry.getGroupId(), amount, info, date);
    }

    public Single<Integer> deleteGroupEntries(CashBox.Entry entry) {
        return repository.deleteGroupEntries(entry.getGroupId());
    }

    // Periodic Entries
    public Completable deletePeriodicInactive() {
        return repository.deletePeriodicInactive();
    }

    public void addDisposable(@NonNull Disposable disposable) {
        compositeDisposable.add(disposable);
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        compositeDisposable.clear();
        LogUtil.debug(TAG, "onCleared: clearing disposable");
    }
}
