package com.utilities.vibal.utilities.db;

import android.content.Context;
import android.os.AsyncTask;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;
import androidx.sqlite.db.SupportSQLiteDatabase;

import com.utilities.vibal.utilities.models.CashBox;
import com.utilities.vibal.utilities.util.Converters;

@Database(entities = {CashBox.CashBoxInfo.class,CashBox.Entry.class},version = 1,exportSchema = false)
@TypeConverters(Converters.class)
public abstract class UtilitiesDatabase extends RoomDatabase {
    private static UtilitiesDatabase instance;

    public abstract CashBoxEntryDao cashBoxEntryDao();
    public abstract CashBoxDao cashBoxDao();

    public static synchronized UtilitiesDatabase getInstance(Context context) {
        if(instance==null) {
            instance = Room.databaseBuilder(context.getApplicationContext(), UtilitiesDatabase.class,
                    "utilities_database")
                    .fallbackToDestructiveMigration()
                    .addCallback(roomCallback)
                    .build();
        }
        return instance;
    }

    private static RoomDatabase.Callback roomCallback = new RoomDatabase.Callback() {
        @Override
        public void onCreate(@NonNull SupportSQLiteDatabase db) {
            super.onCreate(db);
            new PopulateDBAsyncTask(instance).execute();
        }
    };

    private static class PopulateDBAsyncTask extends AsyncTask<Void,Void,Void> {
        private CashBoxDao cashBoxDao;

        private PopulateDBAsyncTask(UtilitiesDatabase database) {
            cashBoxDao = database.cashBoxDao();
        }

        @Override
        protected Void doInBackground(Void... voids) {
            CashBox cashBox = new CashBox("Example");
            cashBoxDao.insert(cashBox.getCashBoxInfo());
            return null;
        }
    }
}
