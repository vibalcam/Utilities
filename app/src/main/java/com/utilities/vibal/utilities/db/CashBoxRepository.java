package com.utilities.vibal.utilities.db;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;

import com.utilities.vibal.utilities.models.CashBox;
import com.utilities.vibal.utilities.util.LogUtil;

import java.util.Collection;
import java.util.List;

import io.reactivex.Completable;
import io.reactivex.Single;

import static com.utilities.vibal.utilities.db.CashBoxInfo.NO_ORDER_ID;

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

    public LiveData<CashBox> getOrderedCashBox(long id) {
        MediatorLiveData<CashBox> liveDataMerger = new MediatorLiveData<>();
        liveDataMerger.setValue(new CashBox("Loading..."));

        liveDataMerger.addSource(cashBoxDao.getCashBoxInfoWithCashById(id),
                infoWithCash -> {
                    LogUtil.debug("Prueba","Change in info with cash: " + infoWithCash.toString());
                    CashBox cashBox = liveDataMerger.getValue();
                    cashBox.setInfoWithCash(infoWithCash);
                    liveDataMerger.setValue(cashBox);
                });
        liveDataMerger.addSource(cashBoxEntryDao.getEntriesByCashBoxId(id),
                entries -> {
                    LogUtil.debug("Prueba","Change in entries: " + entries.toString());
                    CashBox cashBox = liveDataMerger.getValue();
                    cashBox.setEntries(entries);
                    liveDataMerger.setValue(cashBox);
                });

//        CashBox cashBox = liveDataMerger.getValue();
//        cashBox.setInfoWithCash(new CashBox.InfoWithCash("probando",10));
//        liveDataMerger.postValue(cashBox);
//        cashBoxInfoWithCashLiveData.postValue(new CashBox.InfoWithCash("probando",10));

        return liveDataMerger;
    }

    public Single<CashBox> getCashBox(long id) {
        return cashBoxDao.getCashBoxById(id);
    }

    public Completable insertCashBox(CashBox cashBox) {
        configureOrderId(cashBox.getInfoWithCash().getCashBoxInfo());
        return cashBoxDao.insert(cashBox,cashBoxEntryDao);
    }

    public Single<Long> insertCashBoxInfo(CashBox.InfoWithCash infoWithCash) { //TODO insert in index
        configureOrderId(infoWithCash.getCashBoxInfo());
        return cashBoxDao.insert(infoWithCash.getCashBoxInfo());
    }

    /**
     * Configures the orderId by getting CashBox orderId and incrementing it by one
     * @param cashBoxInfo the cashBoxInfo which orderId is going to be configured
     */
    private void configureOrderId(@NonNull CashBoxInfo cashBoxInfo) {
        List<CashBox.InfoWithCash> temp = cashBoxesInfo.getValue();
        if(cashBoxInfo.getOrderId()==NO_ORDER_ID) {
            long orderId = temp == null || temp.isEmpty() ? NO_ORDER_ID + 1 :
                    temp.get(0).getCashBoxInfo().getOrderId() + 1;
            cashBoxInfo.setOrderId(orderId);
        }
    }

    public Completable updateCashBoxInfo(CashBox.InfoWithCash cashBoxInfo) {
        return cashBoxDao.update(cashBoxInfo.getCashBoxInfo());
    }

    public Completable moveCashBoxInfo(CashBox.InfoWithCash infoWithCash, long toOrderPos) {
        return cashBoxDao.moveCashBoxToOrderPos(infoWithCash.getCashBoxInfo().getId(),
                infoWithCash.getCashBoxInfo().getOrderId(), toOrderPos);
    }

    public Completable deleteCashBox(CashBox.InfoWithCash cashBoxInfo) {
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

    public Completable deleteEntry(CashBox.Entry entry) {
        return  cashBoxEntryDao.delete(entry);
    }

    public Single<Integer> deleteAllEntries(long cashBoxId) {
        return cashBoxEntryDao.deleteAll(cashBoxId);
    }

    /*public void insertCashBoxInfo(CashBox.CashBoxInfo cashBoxInfo) throws SQLiteConstraintException {
        try {
            new InsertCashBoxAsyncTask(cashBoxDao).execute(cashBoxInfo);
        } catch (RuntimeException e) {
            Throwable throwable = e.getCause();
            LogUtil.error("Prueba", throwable.toString(),throwable);
//            if(throwable instanceof  SQLiteConstraintException)
//                throw (SQLiteConstraintException) throwable;
//            else
//                throw e;
        }
    }

    public void updateCashBox(CashBox.CashBoxInfo cashBoxInfo) {
        new UpdateCashBoxAsyncTask(cashBoxDao).execute(cashBoxInfo);
    }

    public void deleteCashBoxInfo(CashBox.CashBoxInfo cashBoxInfo) {
        new DeleteCashBoxAsyncTask(cashBoxDao).execute(cashBoxInfo);
    }

    public void deleteAllCashBoxes() {
        new DeleteAllCashBoxAsyncTask(cashBoxDao).execute();
    }

    public void insertEntry(CashBox.Entry entry) {
        new InsertEntryAsyncTask(cashBoxEntryDao).execute(entry);
    }

    public void deleteEntry(CashBox.Entry entry) {
        new DeleteEntryAsyncTask(cashBoxEntryDao).execute(entry);
    }

    public void updateEntry(CashBox.Entry entry) {
        new UpdateEntryAsyncTask(cashBoxEntryDao).execute(entry);
    }

    public void deleteAllEntries(int id) {
        new DeleteAllEntryAsyncTask(cashBoxEntryDao).execute(id);
    }

    private static class InsertCashBoxAsyncTask extends AsyncTask<CashBox.CashBoxInfo,Void,Void> {
        private CashBoxDao cashBoxDao;

        private InsertCashBoxAsyncTask(CashBoxDao cashBoxDao) {
            this.cashBoxDao = cashBoxDao;
        }

        @Override
        protected Void doInBackground(CashBox.CashBoxInfo... cashBoxesInfo) {
            cashBoxesInfo[0].setId(1);
            cashBoxDao.insert(cashBoxesInfo[0]);
            return null;
        }
    }

    private static class UpdateCashBoxAsyncTask extends AsyncTask<CashBox.CashBoxInfo,Void,Void> {
        private CashBoxDao cashBoxDao;

        private UpdateCashBoxAsyncTask(CashBoxDao cashBoxDao) {
            this.cashBoxDao = cashBoxDao;
        }

        @Override
        protected Void doInBackground(CashBox.CashBoxInfo... cashBoxesInfo) {
            cashBoxDao.update(cashBoxesInfo[0]);
            return null;
        }
    }

    private static class DeleteCashBoxAsyncTask extends AsyncTask<CashBox.CashBoxInfo,Void,Void> {
        private CashBoxDao cashBoxDao;

        private DeleteCashBoxAsyncTask(CashBoxDao cashBoxDao) {
            this.cashBoxDao = cashBoxDao;
        }

        @Override
        protected Void doInBackground(CashBox.CashBoxInfo... cashBoxesInfo) {
            cashBoxDao.delete(cashBoxesInfo[0]);
            return null;
        }
    }

    private static class DeleteAllCashBoxAsyncTask extends AsyncTask<Void,Void,Void> {
        private CashBoxDao cashBoxDao;

        private DeleteAllCashBoxAsyncTask(CashBoxDao cashBoxDao) {
            this.cashBoxDao = cashBoxDao;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            cashBoxDao.deleteAll();
            return null;
        }
    }

    private static class InsertEntryAsyncTask extends AsyncTask<CashBox.Entry,Void,Void> {
        private CashBoxEntryDao cashBoxEntryDao;

        private InsertEntryAsyncTask(CashBoxEntryDao cashBoxEntryDao) {
            this.cashBoxEntryDao = cashBoxEntryDao;
        }

        @Override
        protected Void doInBackground(CashBox.Entry... entries) {
            cashBoxEntryDao.insert(entries[0]);
            return null;
        }
    }

    private static class DeleteEntryAsyncTask extends AsyncTask<CashBox.Entry,Void,Void> {
        private CashBoxEntryDao cashBoxEntryDao;

        private DeleteEntryAsyncTask(CashBoxEntryDao cashBoxEntryDao) {
            this.cashBoxEntryDao = cashBoxEntryDao;
        }

        @Override
        protected Void doInBackground(CashBox.Entry... entries) {
            cashBoxEntryDao.delete(entries[0]);
            return null;
        }
    }

    private static class UpdateEntryAsyncTask extends AsyncTask<CashBox.Entry,Void,Void> {
        private CashBoxEntryDao cashBoxEntryDao;

        private UpdateEntryAsyncTask(CashBoxEntryDao cashBoxEntryDao) {
            this.cashBoxEntryDao = cashBoxEntryDao;
        }

        @Override
        protected Void doInBackground(CashBox.Entry... entries) {
            cashBoxEntryDao.update(entries[0]);
            return null;
        }
    }

    private static class DeleteAllEntryAsyncTask extends AsyncTask<Integer,Void,Void> {
        private CashBoxEntryDao cashBoxEntryDao;

        private DeleteAllEntryAsyncTask(CashBoxEntryDao cashBoxEntryDao) {
            this.cashBoxEntryDao = cashBoxEntryDao;
        }

        @Override
        protected Void doInBackground(Integer... integers) {
            cashBoxEntryDao.deleteAll(integers[0]);
            return null;
        }
    }*/
}
