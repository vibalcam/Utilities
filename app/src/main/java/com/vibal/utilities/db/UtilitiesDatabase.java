package com.vibal.utilities.db;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

import com.vibal.utilities.modelsNew.CashBox;
import com.vibal.utilities.util.Converters;

@Database(entities = {CashBoxInfo.class, CashBox.Entry.class,
        PeriodicEntryPojo.PeriodicEntryWorkInfo.class}, version = 1, exportSchema = false)
@TypeConverters(Converters.class)
public abstract class UtilitiesDatabase extends RoomDatabase {
    @Nullable
    private static UtilitiesDatabase INSTANCE = null;

    @Nullable
    public static synchronized UtilitiesDatabase getInstance(@NonNull Context context) {
        if (INSTANCE == null) {
            INSTANCE = Room.databaseBuilder(context.getApplicationContext(), UtilitiesDatabase.class,
                    "utilities_database")
//                    .fallbackToDestructiveMigration()
//                    .addCallback(roomCallback)
                    .build();
        }
        return INSTANCE;
    }

    public abstract CashBoxEntryDao cashBoxEntryDao();

    public abstract CashBoxDao cashBoxDao();

    public abstract PeriodicEntryWorkDao periodicEntryWorkDao();

//    private static RoomDatabase.Callback roomCallback = new RoomDatabase.Callback() {
//        @Override
//        public void onCreate(@NonNull SupportSQLiteDatabase db) {
//            super.onCreate(db);
//            Completable.create(emitter -> INSTANCE.cashBoxDao().
//                    insert(new CashBoxInfo("Example")))
//                    .subscribeOn(Schedulers.io())
//                    .observeOn(Schedulers.single())
//                    .subscribe();
//        }
//    };
}
