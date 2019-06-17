package com.utilities.vibal.utilities.models;

import android.app.Application;
import android.database.sqlite.SQLiteConstraintException;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.utilities.vibal.utilities.db.CashBoxRepository;
import com.utilities.vibal.utilities.util.LogUtil;

import java.util.List;

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

    public boolean addCashBox(CashBox cashBox) {
        try {
            repository.insertCashBoxInfo(cashBox.getCashBoxInfo());
            return true;
        } catch (SQLiteConstraintException e) {
            return false;
        }
    }

    public boolean changeCashBoxName(CashBox cashBox, String newName) {
        cashBox.setName(newName);
        return addCashBox(cashBox);
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
