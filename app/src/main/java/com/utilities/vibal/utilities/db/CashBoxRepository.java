package com.utilities.vibal.utilities.db;

import android.app.Application;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;

import com.utilities.vibal.utilities.models.CashBox;

import java.util.Collection;
import java.util.List;

import io.reactivex.Completable;
import io.reactivex.Single;

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

    public LiveData<CashBox> getCashBox(long id) {
//        return cashBoxDao.getCashBoxById(id);

        LiveData<CashBox.InfoWithCash> cashBoxInfoWithCashLiveData = cashBoxDao.getCashBoxInfoWithCashById(id);
        LiveData<List<CashBox.Entry>> entriesLiveData = cashBoxDao.getEntriesByCashBoxId(id);

        MediatorLiveData<CashBox> liveDataMerger = new MediatorLiveData<>();
        liveDataMerger.setValue(new CashBox("Loading..."));

        liveDataMerger.addSource(cashBoxInfoWithCashLiveData,
                infoWithCash -> liveDataMerger.getValue().setInfoWithCash(infoWithCash));
        liveDataMerger.addSource(entriesLiveData,
                entries -> liveDataMerger.getValue().setEntries(entries));

        return liveDataMerger;
    }

    public Single<Long> insertCashBoxInfo(CashBox.InfoWithCash cashBoxInfo) {
        return cashBoxDao.insert(cashBoxInfo.getCashBoxInfo());
    }

    public Completable updateCashBoxInfo(CashBox.InfoWithCash cashBoxInfo) {
        return cashBoxDao.update(cashBoxInfo.getCashBoxInfo());
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
