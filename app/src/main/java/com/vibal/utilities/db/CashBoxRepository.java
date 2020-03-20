package com.vibal.utilities.db;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.work.WorkManager;

import com.vibal.utilities.backgroundTasks.RxPeriodicEntryWorker;
import com.vibal.utilities.backgroundTasks.UtilAppAPI;
import com.vibal.utilities.modelsNew.CashBox;
import com.vibal.utilities.modelsNew.CashBoxInfo;
import com.vibal.utilities.modelsNew.Entry;
import com.vibal.utilities.modelsNew.PeriodicEntryPojo;
import com.vibal.utilities.util.LogUtil;

import java.util.Calendar;
import java.util.Collection;
import java.util.Currency;
import java.util.List;
import java.util.UUID;

import io.reactivex.Completable;
import io.reactivex.Single;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

public class CashBoxRepository {
    // Work Manager
    private WorkManager workManager;
    private PeriodicEntryWorkDao periodicEntryWorkDao;

    // CashBox Manager
    private LiveData<List<CashBox.InfoWithCash>> cashBoxesInfo;
    private final long onlineId;

    // CashBox Manager Local
    private CashBoxLocalDao cashBoxLocalDao;
    private CashBoxEntryLocalDao cashBoxEntryLocalDao;

    // CashBox Manager Online
    private CashBoxOnlineDao cashBoxOnlineDao;
    private CashBoxEntryOnlineDao cashBoxEntryOnlineDao;
    private static UtilAppAPI utilAppAPI = null;
//    private static final String BASE_URL = "https://utilserver.ddns.net:25575/utilApp";
    private static final String BASE_URL = "https://192.168.0.42/util";

    //todo onlineMode
    public CashBoxRepository(Application application, long onlineId) {
        UtilitiesDatabase database = UtilitiesDatabase.getInstance(application);
        this.onlineId = onlineId;

        // CashBox Manager of selected mode
        if(isOnline()) {
            // CashBox Manager Online
            cashBoxOnlineDao = database.cashBoxOnlineDao();
            cashBoxEntryOnlineDao = database.cashBoxEntryOnlineDao();
            cashBoxesInfo = cashBoxOnlineDao.getAllCashBoxesInfo();
            if(utilAppAPI == null) {
                Retrofit retrofit = new Retrofit.Builder()
                        .baseUrl(BASE_URL)
                        .client(getOkHttpClient())
                        .addConverterFactory(GsonConverterFactory.create())
                        .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                        .build();
                utilAppAPI = retrofit.create(UtilAppAPI.class);//todo comprobar
            }
        } else {
            // CashBox Manager Local
            cashBoxLocalDao = database.cashBoxLocalDao();
            cashBoxEntryLocalDao = database.cashBoxEntryLocalDao();
            cashBoxesInfo = cashBoxLocalDao.getAllCashBoxesInfo(false);
        }

        // WorkManager
        workManager = WorkManager.getInstance(application);
        periodicEntryWorkDao = database.periodicEntryWorkDao();
    }

    private boolean isOnline() {
        return onlineId != -1;
    }

    @NonNull
    private OkHttpClient getOkHttpClient() {
        return new OkHttpClient.Builder()
                .addInterceptor(chain -> {
                    Request originalRequest = chain.request();
                    Request newRequest = originalRequest.newBuilder()
                            .header(UtilAppAPI.CLIENT_ID, String.valueOf(onlineId))
                            .header(UtilAppAPI.PASSWORD_HEADER,UtilAppAPI.PASSWORD)
                            .build();
                    return chain.proceed(newRequest);
                }).build();
    }

    // CashBox Manager

    public LiveData<List<CashBox.InfoWithCash>> getCashBoxesInfo() {
        return cashBoxesInfo;
    }

    @NonNull
    public LiveData<CashBox> getOrderedCashBox(long id) {
        MediatorLiveData<CashBox> liveDataMerger = new MediatorLiveData<>();
        liveDataMerger.setValue(new CashBox("Loading..."));

        liveDataMerger.addSource(cashBoxLocalDao.getCashBoxInfoWithCashById(id),
                infoWithCash -> {
                    if (infoWithCash == null)
                        return;
                    LogUtil.debug("Prueba", "Change in info with cash: " + infoWithCash.toString());
                    CashBox cashBox = liveDataMerger.getValue();
                    cashBox.setInfoWithCash(infoWithCash);
                    liveDataMerger.setValue(cashBox);
                });
        liveDataMerger.addSource(cashBoxEntryLocalDao.getEntriesByCashBoxId(id),
                entries -> {
                    if (entries == null)
                        return;
                    LogUtil.debug("Prueba", "Change in entries: " + entries.toString());
                    CashBox cashBox = liveDataMerger.getValue();
                    cashBox.setEntries(entries);
                    liveDataMerger.setValue(cashBox);
                });

        return liveDataMerger;
    }

    public LiveData<List<CashBox.InfoWithCash>> getAllDeletedCashBoxesInfo() {
        return cashBoxLocalDao.getAllCashBoxesInfo(true);
    }

    public Single<CashBox> getCashBox(long id) {
        return cashBoxLocalDao.getCashBoxById(id);
    }

    public Completable insertCashBox(@NonNull CashBox cashBox) {
        return cashBoxLocalDao.insert(cashBox, cashBoxEntryLocalDao);
    }

    public Single<Long> insertCashBoxInfo(@NonNull CashBox.InfoWithCash infoWithCash) {
        return cashBoxLocalDao.insert(infoWithCash.getCashBoxInfo());
    }

    public Completable updateCashBoxInfo(CashBoxInfo cashBoxInfo) {
        return cashBoxLocalDao.update(cashBoxInfo);
    }

    public Completable setCashBoxCurrency(long cashBoxId, @NonNull Currency currency) {
        return cashBoxLocalDao.setCashBoxCurrency(cashBoxId, currency);
    }

    public Completable moveCashBoxInfo(@NonNull CashBox.InfoWithCash infoWithCash, long toOrderPos) {
        return cashBoxLocalDao.moveCashBoxToOrderPos(infoWithCash.getCashBoxInfo().getId(),
                infoWithCash.getCashBoxInfo().getOrderId(), toOrderPos);
    }

    public Single<Integer> setDeletedAll(boolean deleted) {
        return cashBoxLocalDao.setDeletedAll(deleted);
    }

    public Completable deleteCashBox(@NonNull CashBoxInfo cashBoxInfo) {
        return cashBoxLocalDao.delete(cashBoxInfo);
    }

    public Single<Integer> clearRecycleBin() {
        return cashBoxLocalDao.clearRecycleBin();
    }

    // Entries

    public Completable insertEntry(Entry entry) {
        return cashBoxEntryLocalDao.insert(entry);
    }

    public Completable insertAllEntries(Collection<Entry> entries) {
        return cashBoxEntryLocalDao.insertAll(entries);
    }

    public Completable updateEntry(Entry entry) {
        return cashBoxEntryLocalDao.update(entry);
    }

    public Completable modifyEntry(long id, double amount, String info, Calendar date) {
        return cashBoxEntryLocalDao.modify(id, amount, info, date);
    }

    public Completable deleteEntry(Entry entry) {
        return cashBoxEntryLocalDao.delete(entry);
    }

    public Single<Integer> deleteAllEntries(long cashBoxId) {
        return cashBoxEntryLocalDao.deleteAll(cashBoxId);
    }

    // Group Entries

    public Single<List<Entry>> getGroupEntries(long groupId) {
        return cashBoxEntryLocalDao.getGroupEntries(groupId);
    }

    public Completable modifyGroupEntry(long groupId, double amount, String info, Calendar date) {
        return cashBoxEntryLocalDao.modifyGroup(groupId, amount, info, date);
    }

    public Single<Integer> deleteGroupEntries(long groupId) {
        return cashBoxEntryLocalDao.deleteGroup(groupId);
    }

    // WorkManager

    public LiveData<List<PeriodicEntryPojo>> getPeriodicEntries() {
        return periodicEntryWorkDao.getAllWorkPojos();
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

    public Single<Integer> deleteAllPeriodicEntryWorks() {
        workManager.cancelAllWorkByTag(RxPeriodicEntryWorker.TAG_PERIODIC);
        return periodicEntryWorkDao.deleteAll();
    }

    public Completable deletePeriodicInactive() {
        return periodicEntryWorkDao.deleteInactive();
    }
}
