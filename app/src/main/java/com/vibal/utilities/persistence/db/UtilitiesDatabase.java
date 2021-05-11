package com.vibal.utilities.persistence.db;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

import com.vibal.utilities.models.CashBoxInfoLocal;
import com.vibal.utilities.models.CashBoxInfoOnline;
import com.vibal.utilities.models.EntryInfo;
import com.vibal.utilities.models.EntryOnlineInfo;
import com.vibal.utilities.models.Participant;
import com.vibal.utilities.models.PeriodicEntryPojo;
import com.vibal.utilities.util.Converters;

@Database(entities = {
        CashBoxInfoLocal.class, CashBoxInfoOnline.class,
        EntryInfo.class, EntryOnlineInfo.class,
        Participant.class, EntryOnlineInfo.Participant.class,
        PeriodicEntryPojo.PeriodicEntryWorkInfo.class
}, views = {
        EntryInfo.class,
        EntryInfo.ParticipantToView.class, EntryOnlineInfo.ParticipantToView.class,
        EntryInfo.ParticipantFromView.class, EntryOnlineInfo.ParticipantFromView.class
}, version = 5, exportSchema = false)
@TypeConverters(Converters.class)
public abstract class UtilitiesDatabase extends RoomDatabase {
    private static final Migration MIGRATION_1_2 = new Migration(1, 2) {
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

    /**
     * Added currency
     */
    private static final Migration MIGRATION_2_3 = new Migration(2, 3) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            database.execSQL("ALTER TABLE cashBoxesInfo_table ADD COLUMN currency TEXT DEFAULT ''");
        }
    };

    /**
     * Added online
     */
    private static final Migration MIGRATION_3_4 = new Migration(3, 4) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            // Complete CashBoxInfo online
            database.execSQL("CREATE TABLE IF NOT EXISTS `cashBoxesOnline_table` (" +
                    "`accepted` INTEGER NOT NULL DEFAULT 0, " +
                    "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                    "`name` TEXT NOT NULL COLLATE NOCASE, " +
                    "`orderId` INTEGER NOT NULL, " +
                    "`currency` TEXT DEFAULT '')");
            database.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS `index_cashBoxesOnline_table_name`" +
                    " ON `cashBoxesOnline_table` (`name`)");

            // Complete EntryBase online
            database.execSQL("CREATE TABLE IF NOT EXISTS `entriesOnline_table` (" +
                    "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                    "`changeDate` INTEGER DEFAULT NULL, " +
                    "`cashBoxId` INTEGER NOT NULL, " +
                    "`amount` REAL NOT NULL, " +
                    "`date` INTEGER, " +
                    "`info` TEXT, " +
                    "`groupId` INTEGER NOT NULL DEFAULT 0, " +
                    "FOREIGN KEY(`cashBoxId`) REFERENCES `cashBoxesOnline_table`(`id`) " +
                    "ON UPDATE CASCADE ON DELETE CASCADE )");
            database.execSQL("CREATE INDEX IF NOT EXISTS `index_entriesOnline_table_cashBoxId` " +
                    "ON `entriesOnline_table` (`cashBoxId`)");
        }
    };

    /**
     * Added participants in entries
     */
    private static final Migration MIGRATION_4_5 = new Migration(4, 5) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            // Add local Participants for entries
            database.execSQL("CREATE TABLE IF NOT EXISTS `entriesParticipants_table` (" +
                    "`name` TEXT NOT NULL COLLATE NOCASE, " +
                    "`entryId` INTEGER NOT NULL, " +
                    "`isFrom` INTEGER NOT NULL, " +
                    "`amount` REAL NOT NULL DEFAULT 1, " +
                    "`onlineId` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                    "FOREIGN KEY(`entryId`) REFERENCES `entries_table`(`id`) " +
                    "ON UPDATE CASCADE ON DELETE CASCADE )");
            database.execSQL("CREATE INDEX IF NOT EXISTS `index_entriesParticipants_table_entryId` " +
                    "ON `entriesParticipants_table` (`entryId`)");
            database.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS " +
                    "`index_entriesParticipants_table_name_entryId_isFrom` " +
                    "ON `entriesParticipants_table` (`name`, `entryId`, `isFrom`)");

            // Add online Participants for entries
            database.execSQL("CREATE TABLE IF NOT EXISTS `entriesOnlineParticipants_table` (" +
                    "`name` TEXT NOT NULL COLLATE NOCASE, " +
                    "`entryId` INTEGER NOT NULL, " +
                    "`isFrom` INTEGER NOT NULL, " +
                    "`amount` REAL NOT NULL DEFAULT 1, " +
                    "`onlineId` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                    "FOREIGN KEY(`entryId`) REFERENCES `entriesOnline_table`(`id`) " +
                    "ON UPDATE CASCADE ON DELETE CASCADE )");
            database.execSQL("CREATE INDEX IF NOT EXISTS " +
                    "`index_entriesOnlineParticipants_table_entryId` " +
                    "ON `entriesOnlineParticipants_table` (`entryId`)");
            database.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS " +
                    "`index_entriesOnlineParticipants_table_name_entryId_isFrom` " +
                    "ON `entriesOnlineParticipants_table` (`name`, `entryId`, `isFrom`)");

            // Create views
            database.execSQL("CREATE VIEW `entriesOnlineAsEntries_view` AS " +
                    "SELECT id,cashBoxId,amount,date,info,groupId FROM entriesOnline_table");
            database.execSQL("CREATE VIEW `toEntriesParticipants_view` AS " +
                    "SELECT * FROM entriesParticipants_table WHERE isFrom==0");
            database.execSQL("CREATE VIEW `toEntriesOnlineParticipants_view` AS " +
                    "SELECT * FROM entriesOnlineParticipants_table WHERE isFrom==0");
            database.execSQL("CREATE VIEW `fromEntriesParticipants_view` AS " +
                    "SELECT * FROM entriesParticipants_table WHERE isFrom==1");
            database.execSQL("CREATE VIEW `fromEntriesOnlineParticipants_view` AS " +
                    "SELECT * FROM entriesOnlineParticipants_table WHERE isFrom==1");

            // Add default participants
            database.execSQL("INSERT INTO entriesParticipants_table (name, entryId, isFrom) " +
                    "SELECT '" + Participant.getSelfName() + "', id, 0 FROM entries_table");
            database.execSQL("INSERT INTO entriesParticipants_table (name, entryId, isFrom) " +
                    "SELECT '" + Participant.getSelfName() + "', id, 1 FROM entries_table");
            database.execSQL("INSERT INTO entriesOnlineParticipants_table (name, entryId, isFrom) " +
                    "SELECT '" + Participant.getSelfName() + "', id, 0 FROM entriesOnline_table");
            database.execSQL("INSERT INTO entriesOnlineParticipants_table (name, entryId, isFrom) " +
                    "SELECT '" + Participant.getSelfName() + "', id, 1 FROM entriesOnline_table");
        }
    };

    private static UtilitiesDatabase INSTANCE = null;

    @NonNull
    public static synchronized UtilitiesDatabase getInstance(@NonNull Context context) {
        if (INSTANCE == null) {
            INSTANCE = Room.databaseBuilder(context.getApplicationContext(), UtilitiesDatabase.class,
                    "utilities_database")
                    .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5)
//                    .fallbackToDestructiveMigration()
//                    .addCallback(roomCallback)
                    .build();
        }
        return INSTANCE;
    }

    public abstract CashBoxEntryLocalDao cashBoxEntryLocalDao();

    public abstract CashBoxLocalDao cashBoxLocalDao();

    public abstract CashBoxEntryOnlineDao cashBoxEntryOnlineDao();

    public abstract CashBoxOnlineDao cashBoxOnlineDao();

    public abstract PeriodicEntryWorkDao periodicEntryWorkDao();

//    private static RoomDatabase.Callback roomCallback = new RoomDatabase.Callback() {
//        @Override
//        public void onCreate(@NonNull SupportSQLiteDatabase db) {
//            super.onCreate(db);
//            Completable.create(emitter -> INSTANCE.cashBoxLocalDao().
//                    insert(new CashBoxInfo("Example")))
//                    .subscribeOn(Schedulers.io())
//                    .observeOn(Schedulers.single())
//                    .subscribe();
//        }
//    };
}
