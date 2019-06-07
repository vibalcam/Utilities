package com.utilities.vibal.utilities.db;

import android.app.Application;
import android.os.AsyncTask;

import androidx.lifecycle.LiveData;

import com.utilities.vibal.utilities.models.CashBox;

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

    public void insertCashBox(CashBox cashBox) {
        new InsertCashBoxAsyncTask(cashBoxDao).execute(cashBox);
    }

    public void deleteCashBox(CashBox cashBox) {
        new DeleteCashBoxAsyncTask(cashBoxDao).execute(cashBox);
    }

    public void deleteAllCashBoxes() {
        new DeleteAllCashBoxAsyncTask(cashBoxDao).execute();
    }

    public void updateCashBox(CashBox cashBox) {
        new UpdateCashBoxAsyncTask(cashBoxDao).execute(cashBox);
    }

    public LiveData<List<CashBox>> getCashBoxes() {
        return cashBoxes;
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

    public void deleteAllEntries(String name) {
        new DeleteAllEntryAsyncTask(cashBoxEntryDao).execute();
    }

    private static class InsertCashBoxAsyncTask extends AsyncTask<CashBox,Void,Void> {
        private CashBoxDao cashBoxDao;

        private InsertCashBoxAsyncTask(CashBoxDao cashBoxDao) {
            this.cashBoxDao = cashBoxDao;
        }

        @Override
        protected Void doInBackground(CashBox... cashBoxes) {
            cashBoxDao.insert(cashBoxes[0].getCashBoxInfo());
            return null;
        }
    }

    private static class UpdateCashBoxAsyncTask extends AsyncTask<CashBox,Void,Void> {
        private CashBoxDao cashBoxDao;

        private UpdateCashBoxAsyncTask(CashBoxDao cashBoxDao) {
            this.cashBoxDao = cashBoxDao;
        }

        @Override
        protected Void doInBackground(CashBox... cashBoxes) {
            cashBoxDao.update(cashBoxes[0].getCashBoxInfo());
            return null;
        }
    }

    private static class DeleteCashBoxAsyncTask extends AsyncTask<CashBox,Void,Void> {
        private CashBoxDao cashBoxDao;

        private DeleteCashBoxAsyncTask(CashBoxDao cashBoxDao) {
            this.cashBoxDao = cashBoxDao;
        }

        @Override
        protected Void doInBackground(CashBox... cashBoxes) {
            cashBoxDao.delete(cashBoxes[0].getCashBoxInfo());
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

    private static class DeleteAllEntryAsyncTask extends AsyncTask<String,Void,Void> {
        private CashBoxEntryDao cashBoxEntryDao;

        private DeleteAllEntryAsyncTask(CashBoxEntryDao cashBoxEntryDao) {
            this.cashBoxEntryDao = cashBoxEntryDao;
        }

        @Override
        protected Void doInBackground(String... strings) {
            cashBoxEntryDao.deleteAll(strings[0]);
            return null;
        }
    }
}
