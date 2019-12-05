package com.vibal.utilities.db;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

import com.vibal.utilities.modelsNew.CashBox;
import com.vibal.utilities.util.Converters;

@Database(entities = {CashBoxInfo.class, CashBox.Entry.class,
        PeriodicEntryPojo.PeriodicEntryWorkInfo.class}, version = 2, exportSchema = false)
@TypeConverters(Converters.class)
public abstract class UtilitiesDatabase extends RoomDatabase {
    private static UtilitiesDatabase INSTANCE = null;

    @NonNull
    public static synchronized UtilitiesDatabase getInstance(@NonNull Context context) {
        if (INSTANCE == null) {
            INSTANCE = Room.databaseBuilder(context.getApplicationContext(), UtilitiesDatabase.class,
                    "utilities_database")
                    .addMigrations(MIGRATION_1_2)
//                    .fallbackToDestructiveMigration()
//                    .addCallback(roomCallback)
                    .build();
        }
        return INSTANCE;
    }

    public abstract CashBoxEntryDao cashBoxEntryDao();

    public abstract CashBoxDao cashBoxDao();

    public abstract PeriodicEntryWorkDao periodicEntryWorkDao();

    static final Migration MIGRATION_1_2 = new Migration(1,2) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            // New Table entries
            database.execSQL("CREATE TABLE new_entries_table (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                    "cashBoxId INTEGER NOT NULL, " +
                    "amount REAL NOT NULL, " +
                    "date INTEGER, " +
                    "info TEXT, " +
                    "groupId INTEGER NOT NULL DEFAULT 0," +
                    "FOREIGN KEY(cashBoxId) REFERENCES cashBoxesInfo_table(id) " +
                    "ON UPDATE CASCADE ON DELETE CASCADE )");
            database.execSQL("INSERT INTO new_entries_table (id, cashBoxId, amount, date, info) " +
                    "SELECT id, cashBoxId, amount , date, info FROM entries_table");
            database.execSQL("DROP TABLE entries_table");
            database.execSQL("ALTER TABLE new_entries_table RENAME TO entries_table");

            // Create Index
            database.execSQL("CREATE INDEX IF NOT EXISTS index_entries_table_cashBoxId " +
                    "ON entries_table (cashBoxId)");
        }
    };

    // todo migration deleted group
    static final Migration MIGRATION_2_3 = new Migration(2,3) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {

        }
    };

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
