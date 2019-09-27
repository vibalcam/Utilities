package com.utilities.vibal.utilities.models;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.utilities.vibal.utilities.db.PeriodicEntryPojo;
import com.utilities.vibal.utilities.db.PeriodicEntryWorkRepository;
import com.utilities.vibal.utilities.util.LogUtil;

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

    public Completable addPeriodicEntryWorkRequest(PeriodicEntryPojo.PeriodicEntryWorkRequest workRequest) {
        return repository.addPeriodicEntryWorkRequest(workRequest);
    }

    public Completable replacePeriodicEntryWorkInfo(PeriodicEntryPojo.PeriodicEntryWorkInfo workInfo) {
        return repository.replacePeriodicEntryWorkInfo(workInfo);
    }

    public Completable deletePeriodicEntryWorkInfo(PeriodicEntryPojo.PeriodicEntryWorkInfo workInfo) {
        return repository.deletePeriodicEntryWorkInfo(workInfo)
                .ignoreElement();
    }

    public Single<Integer> deleteAllPeriodicEntryWorks() {
        return repository.deleteAllPeriodicEntryWorks();
    }

    public void addDisposable(Disposable disposable) {
        compositeDisposable.add(disposable);
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        compositeDisposable.clear();
        LogUtil.debug(TAG, "onCleared: clearing disposable");
    }
}
