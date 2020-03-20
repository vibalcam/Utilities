package com.vibal.utilities.db;

import androidx.annotation.NonNull;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Update;

import com.vibal.utilities.modelsNew.CashBox;
import com.vibal.utilities.modelsNew.CashBoxInfo;
import com.vibal.utilities.modelsNew.Entry;
import com.vibal.utilities.util.LogUtil;

import java.util.ArrayList;
import java.util.Collection;

import io.reactivex.Completable;
import io.reactivex.Single;

@Dao
public abstract class CashBoxBaseDao {
    Completable insert(@NonNull CashBox cashBox, @NonNull CashBoxEntryLocalDao cashBoxEntryLocalDao) {
        if (cashBox.getEntries().isEmpty())
            return insert(cashBox.getInfoWithCash().getCashBoxInfo()).ignoreElement();
        else {
            return insert(cashBox.getInfoWithCash().getCashBoxInfo()).flatMapCompletable(id -> {
                LogUtil.debug("Prueba", "Id: " + id);
                ArrayList<Entry> entryArrayList = new ArrayList<>();
                for (Entry entry : cashBox.getEntries())
                    entryArrayList.add(entry.getEntryWithCashBoxId(id));
                return cashBoxEntryLocalDao.insertAll(entryArrayList);
            });
        }
    }

    Single<Long> insert(CashBoxInfo cashBoxInfo) {
        return insertWithoutOrderId(cashBoxInfo)
                .flatMap(id -> configureOrderId(id)
                        .toSingle(() -> id));
    }

    abstract Completable configureOrderId(long cashBoxId);

    @Insert
    abstract Single<Long> insertWithoutOrderId(CashBoxInfo cashBoxInfo);

    @Update
    abstract Completable update(CashBoxInfo cashBoxInfo);

    @Update
    abstract Completable updateAll(Collection<CashBoxInfo> cashBoxInfoCollection);

    @Delete
    abstract Completable delete(CashBoxInfo cashBoxInfo);


}
