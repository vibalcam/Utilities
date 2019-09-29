package com.utilities.vibal.utilities.db;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;
import androidx.sqlite.db.SupportSQLiteDatabase;

import com.utilities.vibal.utilities.modelsNew.CashBox;
import com.utilities.vibal.utilities.util.Converters;

import io.reactivex.Completable;
import io.reactivex.schedulers.Schedulers;

@Database(entities = {CashBoxInfo.class, CashBox.Entry.class,
        PeriodicEntryPojo.PeriodicEntryWorkInfo.class},version = 1,exportSchema = false)
@TypeConverters(Converters.class)
public abstract class UtilitiesDatabase extends RoomDatabase {
    private static UtilitiesDatabase INSTANCE = null;

    public abstract CashBoxEntryDao cashBoxEntryDao();
    public abstract CashBoxDao cashBoxDao();
    public abstract PeriodicEntryWorkDao periodicEntryWorkDao();

    public static synchronized UtilitiesDatabase getInstance(Context context) {
        if(INSTANCE == null) {
            INSTANCE = Room.databaseBuilder(context.getApplicationContext(), UtilitiesDatabase.class,
                    "utilities_database")
//                    .fallbackToDestructiveMigration()
                    .addCallback(roomCallback)
                    .build();
        }
        return INSTANCE;
    }

    //TODO change from past to actual version
    private static RoomDatabase.Callback roomCallback = new RoomDatabase.Callback() {
        @Override
        public void onCreate(@NonNull SupportSQLiteDatabase db) {
            super.onCreate(db);
            Completable.create(emitter -> INSTANCE.cashBoxDao().
                    insert(new CashBoxInfo("Example")))
                    .subscribeOn(Schedulers.io())
                    .observeOn(Schedulers.single())
                    .subscribe();
        }
    };
}
