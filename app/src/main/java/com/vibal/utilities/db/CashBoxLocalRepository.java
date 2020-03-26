package com.vibal.utilities.db;

import android.app.Application;

import androidx.lifecycle.LiveData;

import com.vibal.utilities.modelsNew.CashBox;
import com.vibal.utilities.modelsNew.CashBoxInfo;

import java.util.List;

import io.reactivex.Completable;
import io.reactivex.Single;

public class CashBoxLocalRepository extends CashBoxRepository {

    private CashBoxLocalDao cashBoxLocalDao;
    private CashBoxEntryLocalDao cashBoxEntryLocalDao;

    public CashBoxLocalRepository(Application application) {
        super(application);

        UtilitiesDatabase database = UtilitiesDatabase.getInstance(application);
        cashBoxLocalDao = database.cashBoxLocalDao();
        cashBoxEntryLocalDao = database.cashBoxEntryLocalDao();
//        setCashBoxesInfo(cashBoxLocalDao.getAllCashBoxesInfo(false));
    }

    @Override
    protected CashBoxLocalDao getCashBoxDao() {
        return cashBoxLocalDao;
    }

    @Override
    protected CashBoxEntryLocalDao getCashBoxEntryDao() {
        return cashBoxEntryLocalDao;
    }

//    @Override
//    public Completable insertCashBox(@NonNull CashBox cashBox) {
//        if (cashBox.getEntries().isEmpty())
//            return insertCashBoxInfo(cashBox.getInfoWithCash()).ignoreElement();
//        else {
//            return insertCashBoxInfo(cashBox.getInfoWithCash())
//                    .flatMapCompletable(id -> {
//                        LogUtil.debug("Prueba", "Id: " + id);
//                        ArrayList<Entry> entryArrayList = new ArrayList<>();
//                        for (Entry entry : cashBox.getEntries())
//                            entryArrayList.add(entry.getEntryWithCashBoxId(id));
//                        return insertAllEntries(entryArrayList);
//                    });
//        }
//    }

    // Deleted CashBoxes

    public LiveData<List<CashBox.InfoWithCash>> getAllDeletedCashBoxesInfo() {
        return getCashBoxDao().getAllCashBoxesInfo(true);
    }

    public Completable restore(CashBoxInfo cashBoxInfo) {
        return getCashBoxDao().setDeleted(cashBoxInfo.getId(),false);
    }

    public Single<Integer> restoreAll() {
        return getCashBoxDao().setDeletedAll(false);
    }

    public Completable permanentDeleteCashBox(CashBoxInfo cashBoxInfo) {
        return getCashBoxDao().permanentDelete(cashBoxInfo);
    }

    public Single<Integer> clearRecycleBin() {
        return getCashBoxDao().deleteAll();
    }
}
