package com.utilities.vibal.utilities.db;

import android.app.Application;
import android.database.sqlite.SQLiteConstraintException;
import android.os.AsyncTask;

import androidx.lifecycle.LiveData;

import com.utilities.vibal.utilities.models.CashBox;
import com.utilities.vibal.utilities.util.LogUtil;

import java.util.List;

public class CashBoxRepository {
    private CashBoxDao cashBoxDao;
    private CashBoxEntryDao cashBoxEntryDao;
    private LiveData<List<CashBox>> cashBoxes;

    public CashBoxRepository(Application application) {
        UtilitiesDatabase database = UtilitiesDatabase.getInstance(application);
        cashBoxDao = database.cashBoxDao();
        cashBoxEntryDao = database.cashBoxEntryDao();
        cashBoxes = cashBoxDao.getAllCashBoxes();
    }

    public LiveData<List<CashBox>> getCashBoxes() {
        return cashBoxes;
    }

    public Single

    public void insertCashBoxInfo(CashBox.CashBoxInfo cashBoxInfo) throws SQLiteConstraintException {
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

    public void deleteCashBox(CashBox.CashBoxInfo cashBoxInfo) {
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
    }
}
