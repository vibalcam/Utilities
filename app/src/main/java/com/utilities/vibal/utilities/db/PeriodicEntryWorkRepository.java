package com.utilities.vibal.utilities.db;

import android.app.Application;

import androidx.lifecycle.LiveData;
import androidx.work.WorkManager;

import com.utilities.vibal.utilities.backgroundTasks.RxPeriodicEntryWorker;

import java.util.List;
import java.util.UUID;

import io.reactivex.Completable;
import io.reactivex.Single;

public class PeriodicEntryWorkRepository {
    private WorkManager workManager;
    private PeriodicEntryWorkDao periodicEntryWorkDao;
    private LiveData<List<PeriodicEntryPojo>> periodicEntries;

    public PeriodicEntryWorkRepository(Application application) {
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

    public Completable addPeriodicEntryWorkRequest (PeriodicEntryPojo.PeriodicEntryWorkRequest workRequest) {
//        //Create the periodic task
//        Constraints constraints = new Constraints.Builder()
//                .setRequiresBatteryNotLow(true)
//                .setRequiresDeviceIdle(true)
//                .build();
////        Data entryData = new Data.Builder()
////                .putLong(EXTRA_CASHBOX_ID, cashBoxId)
////                .putString(RxPeriodicEntryWorker.KEY_INFO, info)
////                .putDouble(RxPeriodicEntryWorker.KEY_AMOUNT, amount)
////                .build();
//        PeriodicWorkRequest saveRequest = new PeriodicWorkRequest.Builder(RxPeriodicEntryWorker.class,
//                workInfo.getRepeatInterval(), PeriodicEntryPojo.TIME_UNIT)
//                .setConstraints(constraints)
////                .setInputData(entryData)
//                .addTag(RxPeriodicEntryWorker.TAG_PERIODIC)
//                .addTag(String.format(Locale.US, RxPeriodicEntryWorker.TAG_CASHBOX_ID,
//                        workInfo.getCashBoxId()))
//                .build();
        workManager.enqueue(workRequest.getWorkRequest()); //todo observe, calculate difference between lists

        //Add the data to database
        return periodicEntryWorkDao.insert(workRequest.getWorkInfo());
    }

    public Completable replacePeriodicEntryWorkInfo(PeriodicEntryPojo.PeriodicEntryWorkInfo workInfo) {
        return deletePeriodicEntryWorkInfo(workInfo)
                .flatMapCompletable(integer -> integer==0 ?
                        Completable.error(new IllegalArgumentException("No entry delted")) :
                        addPeriodicEntryWorkRequest(new PeriodicEntryPojo.PeriodicEntryWorkRequest(
                                workInfo.getCashBoxId(),workInfo.getAmount(),workInfo.getInfo(),
                                workInfo.getRepeatInterval())));
    }

    public Single<Integer> deletePeriodicEntryWorkInfo (PeriodicEntryPojo.PeriodicEntryWorkInfo workInfo) {
        workManager.cancelWorkById(workInfo.getId()); //Cancel work
        return periodicEntryWorkDao.delete(workInfo); //Delete from database
    }

    public Single<Integer> deleteAllPeriodicEntryWorks() {
        workManager.cancelAllWorkByTag(RxPeriodicEntryWorker.TAG_PERIODIC);
        return periodicEntryWorkDao.deleteAll();
    }
}
