package com.vibal.utilities.viewModels;

import android.app.Application;

import androidx.annotation.NonNull;

import com.vibal.utilities.db.CashBoxLocalRepository;

public class CashBoxLocalViewModel extends CashBoxViewModel {
    public CashBoxLocalViewModel(@NonNull Application application) {
        super(application, new CashBoxLocalRepository(application));
    }
}
