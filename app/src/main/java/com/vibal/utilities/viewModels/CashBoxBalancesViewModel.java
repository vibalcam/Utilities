package com.vibal.utilities.viewModels;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.vibal.utilities.models.CashBoxBalances;
import com.vibal.utilities.persistence.repositories.CashBoxRepository;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Currency;
import java.util.List;
import java.util.Objects;

import io.reactivex.Single;

public class CashBoxBalancesViewModel extends AndroidViewModel {
    private static final String TAG = "PruebaCashBoxBalancesViewModel";

    private final CashBoxRepository repository;
    private final LiveData<List<CashBoxBalances.Entry>> balancesEntries;
    private final long cashBoxId;

    protected CashBoxBalancesViewModel(@NonNull Application application,
                                       Class<? extends CashBoxRepository> classRepository,
                                       long cashBoxId) throws NoSuchMethodException,
            InvocationTargetException, IllegalAccessException {
        super(application);
        // Get repository
        Method m = classRepository.getDeclaredMethod("getInstance", Application.class);
        repository = Objects.requireNonNull((CashBoxRepository) m.invoke(null, application));
        // Get data
        this.cashBoxId = cashBoxId;
        balancesEntries = repository.getBalances(cashBoxId);
    }

    public LiveData<List<CashBoxBalances.Entry>> getBalancesEntries() {
        return balancesEntries;
    }

    public Single<Currency> getCurrency() {
        return repository.getCashBoxCurrency(cashBoxId);
    }

    public static class Factory implements ViewModelProvider.Factory {
        private static Factory INSTANCE = null;
        private Application application;
        private Class<? extends CashBoxRepository> classRepository;
        private long cashBoxId;

        public static Factory getInstance(Application application, Class<? extends CashBoxRepository> classRepository, long cashBoxId) {
            if (INSTANCE == null)
                INSTANCE = new Factory();
            INSTANCE.application = application;
            INSTANCE.classRepository = classRepository;
            INSTANCE.cashBoxId = cashBoxId;

            return INSTANCE;
        }

        private Factory() {
        }

        @NonNull
        @Override
        public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
            if (!modelClass.equals(CashBoxBalancesViewModel.class))
                throw new RuntimeException("Cannot create instances of class " + modelClass);
            try {
                return (T) new CashBoxBalancesViewModel(application, classRepository, cashBoxId);
            } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
                throw new RuntimeException("Cannot create an instance of " + modelClass, e);
            }
        }
    }
}
