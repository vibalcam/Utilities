package com.utilities.vibal.utilities.ui.cashBoxManager;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.utilities.vibal.utilities.db.CashBoxRepository;
import com.utilities.vibal.utilities.models.CashBox;

import java.util.List;

public class CashBoxViewModel extends AndroidViewModel {
    private CashBoxRepository repository;
    private LiveData<List<CashBox>> cashBoxes;


    public CashBoxViewModel(@NonNull Application application) {
        super(application);
        repository = new CashBoxRepository(application);
        cashBoxes = repository.getCashBoxes();
    }

    public void insertCashBox(CashBox cashBox) {
        repository.insertCashBox(cashBox);
    }

    public void updateCashBox(CashBox cashBox) {
        repository.updateCashBox(cashBox);
    }

    public void deleteCashBox(CashBox cashBox) {
        repository.deleteCashBox(cashBox);
    }

    public LiveData<List<CashBox>> getCashBoxes() {
        return cashBoxes;
    }
}
