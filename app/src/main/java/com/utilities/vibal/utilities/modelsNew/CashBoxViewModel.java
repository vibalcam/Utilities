package com.utilities.vibal.utilities.modelsNew;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.work.WorkManager;

import com.utilities.vibal.utilities.backgroundTasks.RxPeriodicEntryWorker;
import com.utilities.vibal.utilities.db.CashBoxInfo;
import com.utilities.vibal.utilities.db.CashBoxRepository;
import com.utilities.vibal.utilities.db.PeriodicEntryPojo;
import com.utilities.vibal.utilities.db.PeriodicEntryWorkRepository;
import com.utilities.vibal.utilities.util.LogUtil;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;

import io.reactivex.Completable;
import io.reactivex.Single;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;

import static com.utilities.vibal.utilities.db.CashBoxInfo.NO_CASHBOX;

public class CashBoxViewModel extends AndroidViewModel {
    private static final String TAG = "PruebaCashBoxViewModel";

    private WorkManager workManager;
    private CashBoxRepository cashBoxRepository;
    private PeriodicEntryWorkRepository periodicEntryRepository;
    private LiveData<List<CashBox.InfoWithCash>> cashBoxesInfo;
    private long currentCashBoxId = NO_CASHBOX;
    private CompositeDisposable compositeDisposable = new CompositeDisposable();

    public CashBoxViewModel(@NonNull Application application) {
        super(application);
        workManager = WorkManager.getInstance(application);
        cashBoxRepository = new CashBoxRepository(application);
        cashBoxesInfo = cashBoxRepository.getCashBoxesInfo();
        periodicEntryRepository = new PeriodicEntryWorkRepository(application);
    }

    public LiveData<List<CashBox.InfoWithCash>> getCashBoxesInfo() {
        return cashBoxesInfo;
    }

    public LiveData<CashBox> getCurrentCashBox() {
        LogUtil.debug(TAG,"Id cashBox: " + currentCashBoxId);
        return cashBoxRepository.getOrderedCashBox(currentCashBoxId);
    }

    public void setCurrentCashBoxId(long currentCashBoxId) {
        this.currentCashBoxId = currentCashBoxId;
    }

    public long getCurrentCashBoxId() {
        return currentCashBoxId;
    }

    public Single<CashBox> getCashBox(long id) {
        return cashBoxRepository.getCashBox(id);
    }

    public Completable addCashBoxInfo(CashBox.InfoWithCash cashBoxInfo) {
        return cashBoxRepository.insertCashBoxInfo(cashBoxInfo).ignoreElement();
    }

    public Completable addCashBox(CashBox cashBox) {
//        return cashBoxRepository.insertCashBoxInfo(cashBox.getInfoWithCash())
//                .flatMapCompletable(id -> {
//                    LogUtil.debug("Prueba","Id: " + id);
//                    return addAllEntries(id,cashBox.getEntries());
//                });
        return cashBoxRepository.insertCashBox(cashBox);


//        CashBox.InfoWithCash cashBoxInfo = cashBox.getInfoWithCash();
//        Completable completable = addCashBoxInfo(cashBoxInfo);
//        for(CashBox.Entry entry:cashBox.getEntries())
//            completable = completable.andThen(addEntry(cashBoxInfo.getCashBoxInfo().getWorkId(),entry));
//        return completable;
    }

    public Completable changeCashBoxName(CashBox.InfoWithCash cashBoxInfo, String newName)
            throws IllegalArgumentException {
        CashBoxInfo changedCashBoxInfo = cashBoxInfo.getCashBoxInfo().clone();
        changedCashBoxInfo.setName(newName);
        return cashBoxRepository.updateCashBoxInfo(changedCashBoxInfo);
    }

    public Completable deleteCashBoxInfo(CashBox.InfoWithCash cashBoxInfo) {
        //Cancel works associated with the CashBox
        workManager.cancelAllWorkByTag(String.format(Locale.US,
                RxPeriodicEntryWorker.TAG_CASHBOX_ID,cashBoxInfo.getId()));
        return cashBoxRepository.deleteCashBox(cashBoxInfo);
    }

    public Single<Integer> deleteAllCashBoxes() {
        //Cancel all works associated with CashBoxes
        workManager.cancelAllWorkByTag(RxPeriodicEntryWorker.TAG_PERIODIC); //todo observe
        return cashBoxRepository.deleteAllCashBoxes();
    }

    public Completable duplicateCashBox(CashBox cashBox, String newName) {
        CashBox cashBoxClone = cashBox.cloneContents();
        cashBoxClone.setName(newName);
        return addCashBox(cashBoxClone);
    }

    public Completable duplicateCashBox(long cashBoxId, String newName) {
        return cashBoxRepository.getCashBox(cashBoxId)
                .flatMapCompletable(cashBox -> duplicateCashBox(cashBox,newName));

//        CashBox cashBoxClone = cashBox.cloneContents();
//        cashBoxClone.setName(newName);
//        return addCashBox(cashBoxClone);
    }

    public Completable moveCashBox(CashBox.InfoWithCash infoWithCash, int toIndex) {
        List<CashBox.InfoWithCash> cashBoxInfoList = cashBoxesInfo.getValue();
        if(cashBoxInfoList==null)
            return Completable.error(new IllegalArgumentException("Null list of CashBoxes"));
        else if (toIndex<0 || toIndex>=cashBoxInfoList.size())
            return Completable.error(new IndexOutOfBoundsException("Cannot move to index in list"));

        return cashBoxRepository.moveCashBoxInfo(infoWithCash,
                cashBoxInfoList.get(toIndex).getCashBoxInfo().getOrderId());
    }

    public Completable addEntryToCurrentCashBox(CashBox.Entry entry) {
        return addEntry(currentCashBoxId,entry);
    }

    public Completable addEntry(long cashBoxId, CashBox.Entry entry) {
        return cashBoxRepository.insertEntry(entry.getEntryWithCashBoxId(cashBoxId));
    }

    public Completable addAllEntriesToCurrentCashBox(List<CashBox.Entry> entries) {
        return addAllEntries(currentCashBoxId,entries);
    }

    private Completable addAllEntries(long cashBoxId, Collection<CashBox.Entry> entries) {
        ArrayList<CashBox.Entry> entryArrayList = new ArrayList<>();
        for(CashBox.Entry entry:entries)
            entryArrayList.add(entry.getEntryWithCashBoxId(cashBoxId));
        return cashBoxRepository.insertAllEntries(entryArrayList);
    }

    public Completable updateEntry(CashBox.Entry entry) {
        return cashBoxRepository.updateEntry(entry);
    }

    public Completable modifyEntry(CashBox.Entry entry, double amount, String info) {
        return cashBoxRepository.modifyEntry(entry.getId(), amount, info);
    }

    public  Completable deleteEntry(CashBox.Entry entry) {
        return cashBoxRepository.deleteEntry(entry);
    }

    public Single<Integer> deleteAllEntriesFromCurrentCashBox() {
        return cashBoxRepository.deleteAllEntries(currentCashBoxId);
    }

    public Completable addPeriodicEntryWorkRequest(PeriodicEntryPojo.PeriodicEntryWorkRequest workRequest) {
        return periodicEntryRepository.addPeriodicEntryWorkRequest(workRequest);
    }

    public void addDisposable(Disposable disposable) {
        compositeDisposable.add(disposable);
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        compositeDisposable.clear();
        LogUtil.debug(TAG, "onCleared: clearing disposable");
    }
}
