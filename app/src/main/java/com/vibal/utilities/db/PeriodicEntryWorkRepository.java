package com.vibal.utilities.db;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.work.WorkManager;

import com.vibal.utilities.backgroundTasks.RxPeriodicEntryWorker;

import java.util.List;
import java.util.UUID;

import io.reactivex.Completable;
import io.reactivex.Single;

public class PeriodicEntryWorkRepository {
    private WorkManager workManager;
    private PeriodicEntryWorkDao periodicEntryWorkDao;
    private LiveData<List<PeriodicEntryPojo>> periodicEntries;

    public PeriodicEntryWorkRepository(@NonNull Application application) {
        workManager = WorkManager.getInstance(application);
        periodicEntryWorkDao = UtilitiesDatabase.getInstance(application).periodicEntryWorkDao();
        periodicEntries = periodicEntryWorkDao.getAllWorkPojos();
    }

    public LiveData<List<PeriodicEntryPojo>> getPeriodicEntries() {
        return periodicEntries;
    }

    public Single<PeriodicEntryPojo> getPeriodicEntryPojo(UUID uuid) {
        return periodicEntryWorkDao.getWorkPojoByUUID(uuid);
    }

    public Completable addPeriodicEntryWorkRequest(@NonNull PeriodicEntryPojo.PeriodicEntryWorkRequest workRequest) {
        workManager.enqueue(workRequest.getWorkRequest()); //todo observe, calculate difference between lists
        //Add the data to database
        return periodicEntryWorkDao.insert(workRequest.getWorkInfo());
    }

    public Single<Integer> updatePeriodicEntryWorkInfo(PeriodicEntryPojo.PeriodicEntryWorkInfo workInfo) {
        return periodicEntryWorkDao.update(workInfo);
    }

//    public Completable replacePeriodicEntryWorkInfo(PeriodicEntryPojo.PeriodicEntryWorkInfo workInfo) {
//        return deletePeriodicEntryWorkInfo(workInfo)
//                .flatMapCompletable(integer -> integer==0 ?
//                        Completable.error(new IllegalArgumentException("No entry delted")) :
//                        addPeriodicEntryWorkRequest(new PeriodicEntryPojo.PeriodicEntryWorkRequest(
//                                workInfo.getCashBoxId(),workInfo.getAmount(),workInfo.getInfo(),
//                                workInfo.getRepeatInterval())));
//    }

    public Single<Integer> deletePeriodicEntryWorkInfo(@NonNull PeriodicEntryPojo.PeriodicEntryWorkInfo workInfo) {
        workManager.cancelWorkById(workInfo.getWorkId()); //Cancel work
        return periodicEntryWorkDao.delete(workInfo); //Delete from database
    }

    public Single<Integer> deleteAllPeriodicEntryWorks() {
        workManager.cancelAllWorkByTag(RxPeriodicEntryWorker.TAG_PERIODIC);
        return periodicEntryWorkDao.deleteAll();
    }
}
