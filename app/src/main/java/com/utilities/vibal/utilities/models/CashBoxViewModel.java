package com.utilities.vibal.utilities.models;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.utilities.vibal.utilities.db.CashBoxRepository;
import com.utilities.vibal.utilities.util.LogUtil;

import java.util.List;

import io.reactivex.Completable;
import io.reactivex.Single;

import static com.utilities.vibal.utilities.models.CashBox.Entry.NO_CASHBOX;

public class CashBoxViewModel extends AndroidViewModel {
    private CashBoxRepository repository;
    private LiveData<List<CashBox.InfoWithCash>> cashBoxesInfo;
    private int currentCashBoxId = NO_CASHBOX;

    public CashBoxViewModel(@NonNull Application application) {
        super(application);
        repository = new CashBoxRepository(application);
        cashBoxesInfo = repository.getCashBoxesInfo();
    }

    public LiveData<List<CashBox.InfoWithCash>> getCashBoxesInfo() {
        return cashBoxesInfo;
    }

    public LiveData<CashBox> getCurrentCashBox() {
        return repository.getCashBox(currentCashBoxId);
    }

    public void setCurrentCashBoxId(int currentCashBoxId) {
        this.currentCashBoxId = currentCashBoxId;
    }

    public Completable addCashBoxInfo(CashBox.InfoWithCash cashBoxInfo) {
        LogUtil.debug("Prueba",""+cashBoxInfo.getCashBoxInfo().getId());
        return repository.insertCashBoxInfo(cashBoxInfo);
    }

    public Completable addCashBox(CashBox cashBox) {
        CashBox.InfoWithCash cashBoxInfo = cashBox.getCashBoxInfo();
        Completable completable = addCashBoxInfo(cashBoxInfo);
        for(CashBox.Entry entry:cashBox.getEntries())
            completable = completable.andThen(addEntry(cashBoxInfo.getCashBoxInfo().getId(),entry));
        return completable;
    }

    public Completable changeCashBoxName(CashBox.InfoWithCash cashBoxInfo, String newName) throws IllegalArgumentException {
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
        CashBox cashBoxDup = cashBox.clone();
        cashBoxDup.setName(newName);
        return addCashBox(cashBoxDup);
    }

    public Completable moveCashBox(CashBox cashBox, int index) {
        //TODO
        List<CashBox.InfoWithCash> cashBoxInfoList = cashBoxesInfo.getValue();
//        if(cashBoxList==null)
            return Completable.complete();


    }

    public Completable addEntryToCurrentCashBox(CashBox.Entry entry) {
        return addEntry(currentCashBoxId,entry);
    }

    private Completable addEntry(int cashBoxId, CashBox.Entry entry) {
        return repository.insertEntry(entry.getEntryWithCashBoxId(cashBoxId));
    }

    public Completable addAllEntriesToCurrentCashBox(List<CashBox.Entry> entries) {
        return addAllEntries(currentCashBoxId,entries);
    }

    private Completable addAllEntries(int cashBoxId, List<CashBox.Entry> entries) {
//        return repository.in TODO
        return Completable.complete();
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
}
