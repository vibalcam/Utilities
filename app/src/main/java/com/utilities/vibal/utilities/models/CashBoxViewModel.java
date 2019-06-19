package com.utilities.vibal.utilities.models;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.utilities.vibal.utilities.db.CashBoxRepository;

import java.util.List;

import io.reactivex.Completable;
import io.reactivex.Single;

public class CashBoxViewModel extends AndroidViewModel {
    private CashBoxRepository repository;
    private LiveData<List<CashBox>> cashBoxes;

    public CashBoxViewModel(@NonNull Application application) {
        super(application);
        repository = new CashBoxRepository(application);
        cashBoxes = repository.getCashBoxes();
    }

    public LiveData<List<CashBox>> getCashBoxes() {
        return cashBoxes;
    }

    public Completable addCashBox(CashBox cashBox) {
        Completable completable = repository.insertCashBoxInfo(cashBox.getCashBoxInfo());
        for(CashBox.Entry entry:cashBox.getEntries())
            completable = completable.andThen(addEntry(cashBox,entry));
        return completable;
    }

    public Completable changeCashBoxName(CashBox cashBox, String newName) throws IllegalArgumentException {
        CashBox.CashBoxInfo cashBoxInfo = cashBox.getCashBoxInfo().clone();
        cashBoxInfo.setName(newName);
        return repository.updateCashBoxInfo(cashBoxInfo);
    }

    public Completable deleteCashBox(CashBox cashBox) {
        return repository.deleteCashBox(cashBox.getCashBoxInfo());
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
        List<CashBox> cashBoxList = cashBoxes.getValue();
//        if(cashBoxList==null)
            return Completable.complete();


    }

    public Completable addEntry(CashBox cashBox, CashBox.Entry entry) {
        return repository.insertEntry(entry.getEntryWithCashBoxId(cashBox.getCashBoxInfo().getId()));
    }

    public Completable updateEntry(CashBox.Entry entry) {
        return repository.updateEntry(entry);
    }

    public  Completable deleteEntry(CashBox.Entry entry) {
        return repository.deleteEntry(entry);
    }

    public Single<Integer> deleteAllEntries(CashBox cashBox) {
        return repository.deleteAllEntries(cashBox.getCashBoxInfo().getId());
    }
}
