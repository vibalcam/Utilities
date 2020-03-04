package com.vibal.utilities.viewModels;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.vibal.utilities.db.CashBoxRepository;
import com.vibal.utilities.modelsNew.CashBox;
import com.vibal.utilities.modelsNew.CashBoxInfo;

import java.util.List;

import io.reactivex.Completable;
import io.reactivex.Single;

public class CashBoxDeletedViewModel extends AndroidViewModel {
    private static final String TAG = "PruebaCashBoxDeletedViewModel";

    private CashBoxRepository repository;
    private LiveData<List<CashBox.InfoWithCash>> cashBoxesInfo;
//    private CompositeDisposable compositeDisposable = new CompositeDisposable();

    public CashBoxDeletedViewModel(@NonNull Application application) {
        super(application);
        repository = new CashBoxRepository(application);
        cashBoxesInfo = repository.getAllDeletedCashBoxesInfo();
    }

    public LiveData<List<CashBox.InfoWithCash>> getCashBoxesInfo() {
        return cashBoxesInfo;
    }

    public Completable restore(CashBox.InfoWithCash infoWithCash) {
        CashBoxInfo cashBoxInfo = infoWithCash.getCashBoxInfo();
        cashBoxInfo.setDeleted(false);
        return repository.updateCashBoxInfo(cashBoxInfo);
    }

    public Completable delete(CashBox.InfoWithCash infoWithCash) {
        return repository.deleteCashBox(infoWithCash.getCashBoxInfo());
    }

    public Single<Integer> restoreAll() {
        return repository.setDeletedAll(false);
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
