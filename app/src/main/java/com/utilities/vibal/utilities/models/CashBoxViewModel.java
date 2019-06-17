package com.utilities.vibal.utilities.models;

import android.app.Application;
import android.database.sqlite.SQLiteConstraintException;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.utilities.vibal.utilities.db.CashBoxRepository;
import com.utilities.vibal.utilities.util.LogUtil;

import java.util.List;

import io.reactivex.Completable;

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
            completable = completable.andThen(repository.insertEntry(entry));
        return completable;
    }

    public boolean changeCashBoxName(CashBox cashBox, String newName) {
        return true;
    }

    public void deleteCashBox(CashBox cashBox) {
        repository.deleteCashBox(cashBox.getCashBoxInfo());
    }

    public void deleteAllCashBoxes() {
        repository.deleteAllCashBoxes();
    }

    public boolean duplicateCashBox(CashBox cashBox, String newName) {
        try {
            return changeCashBoxName(cashBox.clone(), newName);
        } catch (CloneNotSupportedException e) {
            // Should never occur
            LogUtil.error("Prueba", "CashBox clone not supported", e);
            return false;
        }
    }

    public void moveCashBox(CashBox cashBox, int index) {

    }

    public void addEntry(CashBox cashBox, CashBox.Entry entry) {

    }

    public void updateEntry(CashBox.Entry entry) {
        repository.updateEntry(entry);
    }

    public  void deleteEntry(CashBox.Entry entry) {
        repository.deleteEntry(entry);
    }

    public void deleteAllEntries(CashBox cashBox) {
        repository.deleteAllEntries(cashBox.getCashBoxInfo().getId());
    }
}
