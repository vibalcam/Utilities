package com.vibal.utilities.persistence.repositories;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.work.WorkManager;

import com.vibal.utilities.backgroundTasks.RxPeriodicEntryWorker;
import com.vibal.utilities.modelsNew.PeriodicEntryPojo;
import com.vibal.utilities.persistence.db.PeriodicEntryWorkDao;
import com.vibal.utilities.persistence.db.UtilitiesDatabase;
import com.vibal.utilities.util.LogUtil;

import java.util.List;
import java.util.Locale;
import java.util.UUID;

import io.reactivex.Completable;
import io.reactivex.Single;

public class PeriodicEntryWorkRepository {
    private static PeriodicEntryWorkRepository INSTANCE = null;

    // Work Manager
    private WorkManager workManager;
    private PeriodicEntryWorkDao periodicEntryWorkDao;
    private LiveData<List<PeriodicEntryPojo>> periodicEntries = null;

    public static PeriodicEntryWorkRepository getInstance(Application application) {
        if(INSTANCE == null)
            INSTANCE = new PeriodicEntryWorkRepository(application);
        return INSTANCE;
    }

    private PeriodicEntryWorkRepository(Application application) {
        // WorkManager
        workManager = WorkManager.getInstance(application);
        periodicEntryWorkDao = UtilitiesDatabase.getInstance(application).periodicEntryWorkDao();
    }

    // WorkManager

    public LiveData<List<PeriodicEntryPojo>> getPeriodicEntries() {
        if(periodicEntries == null)
            periodicEntries = periodicEntryWorkDao.getAllWorkPojos();
        return periodicEntries;
    }

    public Single<PeriodicEntryPojo> getPeriodicEntryPojo(UUID uuid) {
        return periodicEntryWorkDao.getWorkPojoByUUID(uuid);
    }

    public Completable addPeriodicEntryWorkRequest(@NonNull PeriodicEntryPojo.PeriodicEntryWorkRequest workRequest) {
        LogUtil.debug("PruebaPeriodicViewModel", "Add new periodic work");
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

    public void cancelAllCashBoxWork(long cashBoxId) {
        //Cancel works associated with the CashBox
        workManager.cancelAllWorkByTag(String.format(Locale.US,
                RxPeriodicEntryWorker.TAG_CASHBOX_ID, cashBoxId));
    }

    public Single<Integer> deleteAllPeriodicEntryWorks() {
        workManager.cancelAllWorkByTag(RxPeriodicEntryWorker.TAG_PERIODIC);
        return periodicEntryWorkDao.deleteAll();
    }

//    public Completable deletePeriodicInactive() {
//        return periodicEntryWorkDao.deleteInactive();
//    }
}
