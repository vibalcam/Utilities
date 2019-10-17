package com.vibal.utilities.db;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;

import com.vibal.utilities.modelsNew.CashBox;
import com.vibal.utilities.util.LogUtil;

import java.util.Collection;
import java.util.List;

import io.reactivex.Completable;
import io.reactivex.Single;

import static com.vibal.utilities.db.CashBoxInfo.NO_ORDER_ID;

public class CashBoxRepository {
    private CashBoxDao cashBoxDao;
    private CashBoxEntryDao cashBoxEntryDao;
    private LiveData<List<CashBox.InfoWithCash>> cashBoxesInfo;

    public CashBoxRepository(Application application) {
        UtilitiesDatabase database = UtilitiesDatabase.getInstance(application);
        cashBoxDao = database.cashBoxDao();
        cashBoxEntryDao = database.cashBoxEntryDao();
        cashBoxesInfo = cashBoxDao.getAllCashBoxesInfo();
    }

    public LiveData<List<CashBox.InfoWithCash>> getCashBoxesInfo() {
        return cashBoxesInfo;
    }

    @NonNull
    public LiveData<CashBox> getOrderedCashBox(long id) {
        MediatorLiveData<CashBox> liveDataMerger = new MediatorLiveData<>();
        liveDataMerger.setValue(new CashBox("Loading..."));

        liveDataMerger.addSource(cashBoxDao.getCashBoxInfoWithCashById(id),
                infoWithCash -> {
                    if(infoWithCash == null)
                        return;
                    LogUtil.debug("Prueba", "Change in info with cash: " + infoWithCash.toString());
                    CashBox cashBox = liveDataMerger.getValue();
                    cashBox.setInfoWithCash(infoWithCash);
                    liveDataMerger.setValue(cashBox);
                });
        liveDataMerger.addSource(cashBoxEntryDao.getEntriesByCashBoxId(id),
                entries -> {
                    if(entries == null)
                        return;
                    LogUtil.debug("Prueba", "Change in entries: " + entries.toString());
                    CashBox cashBox = liveDataMerger.getValue();
                    cashBox.setEntries(entries);
                    liveDataMerger.setValue(cashBox);
                });

        return liveDataMerger;
    }

    public Single<CashBox> getCashBox(long id) {
        return cashBoxDao.getCashBoxById(id);
    }

    public Completable insertCashBox(@NonNull CashBox cashBox) {
        configureOrderId(cashBox.getInfoWithCash().getCashBoxInfo());
        return cashBoxDao.insert(cashBox, cashBoxEntryDao);
    }

    public Single<Long> insertCashBoxInfo(@NonNull CashBox.InfoWithCash infoWithCash) {
        configureOrderId(infoWithCash.getCashBoxInfo());
        return cashBoxDao.insert(infoWithCash.getCashBoxInfo());
    }

    /**
     * Configures the orderId by getting CashBox orderId and incrementing it by one
     *
     * @param cashBoxInfo the cashBoxInfo which orderId is going to be configured
     */
    private void configureOrderId(@NonNull CashBoxInfo cashBoxInfo) {
//        if (cashBoxInfo.getOrderId() == NO_ORDER_ID) {
//            List<CashBox.InfoWithCash> temp = cashBoxesInfo.getValue();
//            long orderId = temp == null || temp.isEmpty() ? NO_ORDER_ID + 1 :
//                    temp.get(0).getCashBoxInfo().getOrderId() + 1;
//            cashBoxInfo.setOrderId(orderId);
//        }
        if(cashBoxInfo.getOrderId() == NO_ORDER_ID)
            cashBoxInfo.setOrderId(cashBoxInfo.getId());
    }

    public Completable updateCashBoxInfo(CashBoxInfo cashBoxInfo) {
        return cashBoxDao.update(cashBoxInfo);
    }

    public Completable moveCashBoxInfo(@NonNull CashBox.InfoWithCash infoWithCash, long toOrderPos) {
        return cashBoxDao.moveCashBoxToOrderPos(infoWithCash.getCashBoxInfo().getId(),
                infoWithCash.getCashBoxInfo().getOrderId(), toOrderPos);
    }

    public Completable deleteCashBox(@NonNull CashBox.InfoWithCash cashBoxInfo) {
        return cashBoxDao.delete(cashBoxInfo.getCashBoxInfo());
    }

    public Single<Integer> deleteAllCashBoxes() {
        return cashBoxDao.deleteAll();
    }

    public Completable insertEntry(CashBox.Entry entry) {
        return cashBoxEntryDao.insert(entry);
    }

    public Completable insertAllEntries(Collection<CashBox.Entry> entries) {
        return cashBoxEntryDao.insertAll(entries);
    }

    public Completable updateEntry(CashBox.Entry entry) {
        return cashBoxEntryDao.update(entry);
    }

    public Completable modifyEntry(long id, double amount, String info) {
        return cashBoxEntryDao.modify(id, amount, info);
    }

    public Completable deleteEntry(CashBox.Entry entry) {
        return cashBoxEntryDao.delete(entry);
    }

    public Single<Integer> deleteAllEntries(long cashBoxId) {
        return cashBoxEntryDao.deleteAll(cashBoxId);
    }
}
