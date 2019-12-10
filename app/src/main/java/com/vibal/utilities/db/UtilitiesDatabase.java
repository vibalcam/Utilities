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
import com.vibal.utilities.modelsNew.CashBoxInfo;
import com.vibal.utilities.modelsNew.PeriodicEntryPojo;
import com.vibal.utilities.util.Converters;

@Database(entities = {CashBoxInfo.class, CashBox.Entry.class,
        PeriodicEntryPojo.PeriodicEntryWorkInfo.class}, version = 2, exportSchema = false)
@TypeConverters(Converters.class)
public abstract class UtilitiesDatabase extends RoomDatabase {
    static final Migration MIGRATION_1_2 = new Migration(1, 2) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            // New Table CashBoxInfo
            database.execSQL("CREATE TABLE new_cashBoxesInfo_table (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                    "name TEXT NOT NULL COLLATE NOCASE, " +
                    "orderId INTEGER NOT NULL, " +
                    "deleted INTEGER NOT NULL DEFAULT 0)");
            database.execSQL("INSERT INTO new_cashBoxesInfo_table (id,name,orderId) " +
                    "SELECT id, name, orderId FROM cashBoxesInfo_table");
            database.execSQL("DROP TABLE cashBoxesInfo_table");
            database.execSQL("ALTER TABLE new_cashBoxesInfo_table RENAME TO cashBoxesInfo_table");

            // Create Indexes
            database.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS index_cashBoxesInfo_table_name " +
                    "ON cashBoxesInfo_table (name)");
            database.execSQL("CREATE INDEX IF NOT EXISTS index_cashBoxesInfo_table_deleted " +
                    "ON cashBoxesInfo_table (deleted)");

            // New Table Entries
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
