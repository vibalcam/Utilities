package com.utilities.vibal.utilities.models;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.utilities.vibal.utilities.db.CashBoxRepository;
import com.utilities.vibal.utilities.util.LogUtil;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import io.reactivex.Completable;
import io.reactivex.Single;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;

import static com.utilities.vibal.utilities.models.CashBox.Entry.NO_CASHBOX;

public class CashBoxViewModel extends AndroidViewModel {
    private CashBoxRepository repository;
    private LiveData<List<CashBox.InfoWithCash>> cashBoxesInfo;
    private long currentCashBoxId = NO_CASHBOX;
    private CompositeDisposable compositeDisposable = new CompositeDisposable();

    public CashBoxViewModel(@NonNull Application application) {
        super(application);
        repository = new CashBoxRepository(application);
        cashBoxesInfo = repository.getCashBoxesInfo();
    }

    public LiveData<List<CashBox.InfoWithCash>> getCashBoxesInfo() {
        return cashBoxesInfo;
    }

    public LiveData<CashBox> getCurrentCashBox() {
        return repository.getOrderedCashBox(currentCashBoxId);
    }

    public void setCurrentCashBoxId(long currentCashBoxId) {
        this.currentCashBoxId = currentCashBoxId;
    }

    public Single<CashBox> getCashBox(long id) {
        return repository.getCashBox(id);
    }

    public Completable addCashBoxInfo(CashBox.InfoWithCash cashBoxInfo) {
        return repository.insertCashBoxInfo(cashBoxInfo).ignoreElement();
    }

    public Completable addCashBox(CashBox cashBox) { // TODO: does not work right
        return repository.insertCashBoxInfo(cashBox.getInfoWithCash())
                .flatMapCompletable(id -> addAllEntries(id,cashBox.getEntries()));


//        CashBox.InfoWithCash cashBoxInfo = cashBox.getInfoWithCash();
//        Completable completable = addCashBoxInfo(cashBoxInfo);
//        for(CashBox.Entry entry:cashBox.getEntries())
//            completable = completable.andThen(addEntry(cashBoxInfo.getCashBoxInfo().getId(),entry));
//        return completable;
    }

    public Completable changeCashBoxName(CashBox.InfoWithCash cashBoxInfo, String newName)
            throws IllegalArgumentException {
        CashBox.InfoWithCash changedCashBoxInfo = cashBoxInfo.clone();
        changedCashBoxInfo.getCashBoxInfo().setName(newName);
        return repository.updateCashBoxInfo(changedCashBoxInfo);
    }

    public Completable deleteCashBoxInfo(CashBox.InfoWithCash cashBoxInfo) {
        return repository.deleteCashBox(cashBoxInfo);
    }

    public Single<Integer> deleteAllCashBoxes() {
        return repository.deleteAllCashBoxes();
    }

    public Completable duplicateCashBox(CashBox cashBox, String newName) {
        CashBox cashBoxClone = cashBox.clone();
        cashBoxClone.setName(newName);
        return addCashBox(cashBoxClone);
    }

    public Completable moveCashBox(CashBox.InfoWithCash infoWithCash, int toIndex) {
        //TODO move infoWithCash

        List<CashBox.InfoWithCash> cashBoxInfoList = cashBoxesInfo.getValue();
        if(cashBoxInfoList==null)
            return Completable.error(new IllegalArgumentException("Null list of CashBoxes"));
        else if (toIndex<0 || toIndex>=cashBoxInfoList.size())
            return Completable.error(new IndexOutOfBoundsException("Cannot move to index in list"));

        return repository.moveCashBoxInfo(infoWithCash,
                cashBoxInfoList.get(toIndex).getCashBoxInfo().getOrderId());
    }

    public Completable addEntryToCurrentCashBox(CashBox.Entry entry) {
        return addEntry(currentCashBoxId,entry);
    }

    private Completable addEntry(long cashBoxId, CashBox.Entry entry) {
        return repository.insertEntry(entry.getEntryWithCashBoxId(cashBoxId));
    }

    public Completable addAllEntriesToCurrentCashBox(List<CashBox.Entry> entries) {
        return addAllEntries(currentCashBoxId,entries);
    }

    private Completable addAllEntries(long cashBoxId, Collection<CashBox.Entry> entries) { // TODO
        ArrayList<CashBox.Entry> entryArrayList = new ArrayList<>();
        for(CashBox.Entry entry:entries)
            entryArrayList.add(entry.getEntryWithCashBoxId(cashBoxId));
        return repository.insertAllEntries(entryArrayList);
    }

    public Completable updateEntry(CashBox.Entry entry) {
        return repository.updateEntry(entry);
    }

    public  Completable deleteEntry(CashBox.Entry entry) {
        return repository.deleteEntry(entry);
    }

    public Single<Integer> deleteAllEntriesFromCurrentCashBox() {
        return repository.deleteAllEntries(currentCashBoxId);
    }

    public void addDisposable(Disposable disposable) {
        compositeDisposable.add(disposable);
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        compositeDisposable.clear();
        LogUtil.debug("PruebaViewModel", "onCleared: clearing disposable");
    }
}
