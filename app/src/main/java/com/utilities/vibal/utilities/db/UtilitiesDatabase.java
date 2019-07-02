package com.utilities.vibal.utilities.db;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;
import androidx.sqlite.db.SupportSQLiteDatabase;

import com.utilities.vibal.utilities.models.CashBox;
import com.utilities.vibal.utilities.util.Converters;

import io.reactivex.Completable;
import io.reactivex.schedulers.Schedulers;

@Database(entities = {CashBoxInfo.class,CashBox.Entry.class},version = 1,exportSchema = false)
@TypeConverters(Converters.class)
public abstract class UtilitiesDatabase extends RoomDatabase {
    private static UtilitiesDatabase INSTANCE = null;

    public abstract CashBoxEntryDao cashBoxEntryDao();
    public abstract CashBoxDao cashBoxDao();

    public static synchronized UtilitiesDatabase getInstance(Context context) {
        if(INSTANCE == null) {
            INSTANCE = Room.databaseBuilder(context.getApplicationContext(), UtilitiesDatabase.class,
                    "utilities_database")
                    .fallbackToDestructiveMigration()
                    .addCallback(roomCallback)
                    .build();
        }
        return INSTANCE;
    }

    private static RoomDatabase.Callback roomCallback = new RoomDatabase.Callback() {
        @Override
        public void onCreate(@NonNull SupportSQLiteDatabase db) {
            super.onCreate(db);
            Completable.create(emitter -> INSTANCE.cashBoxDao().
                    insert(new CashBoxInfo("Example")))
                    .subscribeOn(Schedulers.io())
                    .observeOn(Schedulers.single())
                    .subscribe();


//            new PopulateDBAsyncTask(instance).execute();
        }
    };

//    private static class PopulateDBAsyncTask extends AsyncTask<Void,Void,Void> {
//        private CashBoxDao cashBoxDao;
//
//        private PopulateDBAsyncTask(UtilitiesDatabase database) {
//            cashBoxDao = database.cashBoxDao();
//        }
//
//        @Override
//        protected Void doInBackground(Void... voids) {
//            CashBox cashBox = new CashBox("Example");
//            cashBoxDao.insert(cashBox.getInfoWithCash());
//            return null;
//        }
//    }
}
