package com.vibal.utilities.persistence.db;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;

import com.vibal.utilities.models.CashBox;
import com.vibal.utilities.models.CashBoxInfo;

import java.util.Collection;
import java.util.Currency;
import java.util.List;

import io.reactivex.Completable;
import io.reactivex.Single;

@Dao
public abstract class CashBoxBaseDao {
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

    public abstract LiveData<List<CashBox.InfoWithCash>> getAllCashBoxesInfo();

    public Single<Long> insert(CashBoxInfo cashBoxInfo) {
        return insertWithoutOrderId(cashBoxInfo)
                .flatMap(id -> configureOrderId(id)
                        .toSingle(() -> id));
    }

    abstract Completable configureOrderId(long cashBoxId);

    //    @Insert
    abstract Single<Long> insertWithoutOrderId(CashBoxInfo cashBoxInfo);

    //    @Update
    public abstract Completable update(CashBoxInfo cashBoxInfo);

    //    @Update
    abstract Completable updateAll(Collection<CashBoxInfo> cashBoxInfoCollection);

    //    @Delete
    public abstract Completable delete(CashBoxInfo cashBoxInfo);

    //    @Delete
    public abstract Single<Integer> deleteAll();

    public abstract LiveData<CashBox.InfoWithCash> getCashBoxInfoWithCashById(long id);

    public abstract Single<CashBox> getCashBoxById(long id);

    public abstract Completable setCashBoxCurrency(long cashBoxId, Currency currency);

    public abstract Completable moveCashBoxToOrderPos(long id, long orderId, long toOrderPos);
}
