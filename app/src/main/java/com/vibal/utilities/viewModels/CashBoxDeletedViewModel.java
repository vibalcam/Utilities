package com.vibal.utilities.viewModels;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.vibal.utilities.models.InfoWithCash;
import com.vibal.utilities.persistence.repositories.CashBoxLocalRepository;

import java.util.List;

import io.reactivex.Completable;
import io.reactivex.Single;

public class CashBoxDeletedViewModel extends AndroidViewModel {
    private static final String TAG = "PruebaCashBoxDeletedViewModel";

    private final CashBoxLocalRepository repository;
    private final LiveData<List<InfoWithCash>> cashBoxesInfo;
//    private CompositeDisposable compositeDisposable = new CompositeDisposable();

    public CashBoxDeletedViewModel(@NonNull Application application) {
        super(application);
//        repository = new CashBoxLocalRepository(application);
        repository = CashBoxLocalRepository.getInstance(application);
        cashBoxesInfo = repository.getAllDeletedCashBoxesInfo();
    }

    public LiveData<List<InfoWithCash>> getCashBoxesInfo() {
        return cashBoxesInfo;
    }

    public Completable restore(InfoWithCash infoWithCash) {
        return repository.restore(infoWithCash.getCashBoxInfo());
    }

    public Completable delete(InfoWithCash infoWithCash) {
        return repository.permanentDeleteCashBox(infoWithCash.getCashBoxInfo());
    }

    public Single<Integer> restoreAll() {
        return repository.restoreAll();
    }

    public Single<Integer> clearRecycleBin() {
        return repository.clearRecycleBin();
    }

//    public void addDisposable(Disposable disposable) {
//        compositeDisposable.add(disposable);
//    }
//
//    @Override
//    protected void onCleared() {
//        super.onCleared();
//        compositeDisposable.clear();
//        LogUtil.debug(TAG, "onCleared: clearing disposable");
//    }
}
