package com.vibal.utilities.persistence.db;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;

import com.vibal.utilities.models.CashBox;
import com.vibal.utilities.models.CashBoxInfo;
import com.vibal.utilities.models.EntryBase;
import com.vibal.utilities.models.InfoWithCash;

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
//                ArrayList<EntryBase> entryArrayList = new ArrayList<>();
//                for (EntryBase entry : cashBox.getEntries())
//                    entryArrayList.add(entry.getEntryWithCashBoxId(id));
//                return cashBoxEntryDao.insertAll(entryArrayList);
//            });
//        }
//    }

    public abstract LiveData<List<InfoWithCash>> getAllCashBoxesInfo();

    public Single<Long> insert(CashBoxInfo cashBoxInfo) {
        return insertWithoutOrderId(cashBoxInfo)
                .flatMap(id -> configureOrderId(id)
                        .toSingle(() -> id));
    }

    abstract Completable configureOrderId(long cashBoxId);

    abstract Single<Long> insertWithoutOrderId(CashBoxInfo cashBoxInfo);

    public abstract Completable update(CashBoxInfo cashBoxInfo);

    abstract Completable updateAll(Collection<CashBoxInfo> cashBoxInfoCollection);

    public abstract Completable delete(CashBoxInfo cashBoxInfo);

    public abstract Single<Integer> deleteAll();

    public abstract LiveData<InfoWithCash> getCashBoxInfoWithCashById(long id);

    abstract Single<InfoWithCash> getSingleCashBoxInfoWithCashById(long id);

    public abstract LiveData<List<String>> getNamesByCashBox(long cashBoxId);

    abstract Single<List<String>> getSingleNamesByCashBox(long cashBoxId);

    public Single<CashBox> getCashBoxById(long id, CashBoxEntryBaseDao entryDao) {
        return getSingleCashBoxInfoWithCashById(id)
                .flatMap(infoWithCash -> entryDao.getSingleEntriesByCashBox(id).flatMap(
                        entries -> getSingleNamesByCashBox(id).map(
                                names -> {
                                    names.add(EntryBase.getSelfName());
                                    return new CashBox(infoWithCash, names, entries);
                                })));
    }

    public abstract Completable setCashBoxCurrency(long cashBoxId, Currency currency);

    public abstract Single<Currency> getCashBoxCurrency(long cashBoxId);

    public abstract Completable moveCashBoxToOrderPos(long id, long orderId, long toOrderPos);
}
