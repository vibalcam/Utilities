package com.vibal.utilities.modelsNew;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.vibal.utilities.db.PeriodicEntryPojo;
import com.vibal.utilities.db.PeriodicEntryWorkRepository;
import com.vibal.utilities.util.LogUtil;

import java.util.List;
import java.util.UUID;

import io.reactivex.Completable;
import io.reactivex.Single;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;

public class PeriodicEntryWorkViewModel extends AndroidViewModel {
    private static final String TAG = "PruebaPeriodicViewModel";

    private PeriodicEntryWorkRepository repository;
    private LiveData<List<PeriodicEntryPojo>> periodicEntries;
    @NonNull
    private CompositeDisposable compositeDisposable = new CompositeDisposable();


    public PeriodicEntryWorkViewModel(@NonNull Application application) {
        super(application);
        repository = new PeriodicEntryWorkRepository(application);
        periodicEntries = repository.getPeriodicEntries();
    }

    public LiveData<List<PeriodicEntryPojo>> getPeriodicEntries() {
        return periodicEntries;
    }

    public Single<PeriodicEntryPojo> getPeriodicEntryPojo(UUID uuid) {
        return repository.getPeriodicEntryPojo(uuid);
    }

    public Completable addPeriodicEntryWorkRequest(@NonNull PeriodicEntryPojo.PeriodicEntryWorkRequest workRequest) {
        return repository.addPeriodicEntryWorkRequest(workRequest);
    }

    public Single<Integer> updatePeriodicEntryWorkInfo(PeriodicEntryPojo.PeriodicEntryWorkInfo workInfo) {
        return repository.updatePeriodicEntryWorkInfo(workInfo);
    }

//    public Completable replacePeriodicEntryWorkInfo(PeriodicEntryPojo.PeriodicEntryWorkInfo workInfo) {
//        return repository.replacePeriodicEntryWorkInfo(workInfo);
//    }

    public Completable deletePeriodicEntryWorkInfo(@NonNull PeriodicEntryPojo.PeriodicEntryWorkInfo workInfo) {
        return repository.deletePeriodicEntryWorkInfo(workInfo)
                .ignoreElement();
    }

    public Single<Integer> deleteAllPeriodicEntryWorks() {
        return repository.deleteAllPeriodicEntryWorks();
    }

    public void addDisposable(@NonNull Disposable disposable) {
        compositeDisposable.add(disposable);
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        compositeDisposable.clear();
        LogUtil.debug(TAG, "onCleared: clearing disposable");
    }
}
