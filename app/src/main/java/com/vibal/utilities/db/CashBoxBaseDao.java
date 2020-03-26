package com.vibal.utilities.db;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;

import com.vibal.utilities.modelsNew.CashBox;
import com.vibal.utilities.modelsNew.CashBoxInfo;

import java.util.Collection;
import java.util.Currency;
import java.util.List;

import io.reactivex.Completable;
import io.reactivex.Single;

@Dao
abstract class CashBoxBaseDao {
//    Completable insert(@NonNull CashBox cashBox, @NonNull CashBoxEntryLocalDao cashBoxEntryDao) {
//        if (cashBox.getEntries().isEmpty())
//            return insert(cashBox.getInfoWithCash().getCashBoxInfo()).ignoreElement();
//        else {
//            return insert(cashBox.getInfoWithCash().getCashBoxInfo()).flatMapCompletable(id -> {
//                LogUtil.debug("Prueba", "Id: " + id);
//                ArrayList<Entry> entryArrayList = new ArrayList<>();
//                for (Entry entry : cashBox.getEntries())
//                    entryArrayList.add(entry.getEntryWithCashBoxId(id));
//                return cashBoxEntryDao.insertAll(entryArrayList);
//            });
//        }
//    }

    abstract LiveData<List<CashBox.InfoWithCash>> getAllCashBoxesInfo();

    Single<Long> insert(CashBoxInfo cashBoxInfo) {
        return insertWithoutOrderId(cashBoxInfo)
                .flatMap(id -> configureOrderId(id)
                        .toSingle(() -> id));
    }

    abstract Completable configureOrderId(long cashBoxId);

//    @Insert
    abstract Single<Long> insertWithoutOrderId(CashBoxInfo cashBoxInfo);

//    @Update
    abstract Completable update(CashBoxInfo cashBoxInfo);

//    @Update
    abstract Completable updateAll(Collection<CashBoxInfo> cashBoxInfoCollection);

//    @Delete
    abstract Completable delete(CashBoxInfo cashBoxInfo);

//    @Delete
    abstract Single<Integer> deleteAll();

    abstract LiveData<CashBox.InfoWithCash> getCashBoxInfoWithCashById(long id);

    abstract Single<CashBox> getCashBoxById(long id);

    abstract Completable setCashBoxCurrency(long cashBoxId, Currency currency);

    abstract Completable moveCashBoxToOrderPos(long id, long orderId, long toOrderPos);
}
