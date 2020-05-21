package com.vibal.utilities.viewModels;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.vibal.utilities.modelsNew.CashBox;
import com.vibal.utilities.modelsNew.CashBoxInfo;
import com.vibal.utilities.modelsNew.Entry;
import com.vibal.utilities.modelsNew.PeriodicEntryPojo;
import com.vibal.utilities.persistence.repositories.CashBoxRepository;
import com.vibal.utilities.util.LogUtil;

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Currency;
import java.util.List;

import io.reactivex.Completable;
import io.reactivex.Single;

import static com.vibal.utilities.modelsNew.CashBoxInfo.NO_ID;

public abstract class CashBoxViewModel extends AndroidViewModel {
    private static final String TAG = "PruebaCashBoxViewModel";

    private LiveData<List<CashBox.InfoWithCash>> cashBoxesInfo;
    private LiveData<CashBox> cashBox;

    protected CashBoxViewModel(@NonNull Application application) throws CertificateException, NoSuchAlgorithmException, KeyStoreException, KeyManagementException, IOException {
        super(application);
        cashBoxesInfo = initializeRepository(application).getCashBoxesInfo();
    }

    protected abstract CashBoxRepository getRepository();

    protected abstract CashBoxRepository initializeRepository(Application application) throws CertificateException, NoSuchAlgorithmException, KeyStoreException, IOException, KeyManagementException;

    public LiveData<List<CashBox.InfoWithCash>> getCashBoxesInfo() {
        return cashBoxesInfo;
    }

    protected void setCashBoxesInfo(LiveData<List<CashBox.InfoWithCash>> cashBoxesInfo) {
        this.cashBoxesInfo = cashBoxesInfo;
    }

    @NonNull
    public LiveData<CashBox> getCurrentCashBox() {
        LogUtil.debug(TAG, "Id cashBox: " + getCurrentCashBoxId());
        return cashBox;
    }

    public long getCurrentCashBoxId() {
        return cashBox == null || cashBox.getValue() == null ? NO_ID :
                cashBox.getValue().getInfoWithCash().getId();
    }

    public void setCurrentCashBoxId(long currentCashBoxId) {
        // If different, get the current cashbox
        if (this.getCurrentCashBoxId() != currentCashBoxId)
            cashBox = getRepository().getOrderedCashBox(currentCashBoxId);
    }

    public Single<CashBox> getCashBox(long id) {
        return getRepository().getCashBox(id);
    }

    public Completable addCashBoxInfo(@NonNull CashBox.InfoWithCash cashBoxInfo) {
        return getRepository().insertCashBoxInfo(cashBoxInfo.getCashBoxInfo()).ignoreElement();
    }

    /**
     * CAREFUL: ADDING CASHBOX BEFORE ANOTHER ONE, DOES NOT ASSURE ORDER ID
     */
    public Completable addCashBox(@NonNull CashBox cashBox) {
        return getRepository().insertCashBox(cashBox);
    }

    public Completable changeCashBoxName(@NonNull CashBox.InfoWithCash cashBoxInfo, @NonNull String newName)
            throws IllegalArgumentException {
        CashBoxInfo changedCashBoxInfo = cashBoxInfo.getCashBoxInfo().clone();
        changedCashBoxInfo.setName(newName);
        return getRepository().updateCashBoxInfo(changedCashBoxInfo);
    }

    public Completable setCurrency(long cashBoxId, @NonNull Currency currency) {
        return getRepository().setCashBoxCurrency(cashBoxId, currency);
    }

    public Completable setCurrentCashBoxCurrency(@NonNull Currency currency) {
        return setCurrency(getCurrentCashBoxId(), currency);
    }

    public Completable deleteCashBoxInfo(@NonNull CashBox.InfoWithCash infoWithCash) {
        //Cancel works associated with the CashBox
        return getRepository().deleteCashBox(infoWithCash.getCashBoxInfo());
    }

    public Single<Integer> deleteAllCashBoxes() {
        //Cancel all works associated with CashBoxes
        return getRepository().deleteAllCashBoxes();
    }

    public Completable duplicateCashBox(@NonNull CashBox cashBox, @NonNull String newName) {
        CashBox cashBoxClone = cashBox.cloneContents();
        cashBoxClone.setName(newName);
        return addCashBox(cashBoxClone);
    }

    public Completable duplicateCashBox(long cashBoxId, @NonNull String newName) {
        return getRepository().getCashBox(cashBoxId)
                .flatMapCompletable(cashBox -> duplicateCashBox(cashBox, newName));
    }

    public Completable moveCashBox(@NonNull CashBox.InfoWithCash infoWithCash, int toIndex) {
        List<CashBox.InfoWithCash> cashBoxInfoList = cashBoxesInfo.getValue();
        if (cashBoxInfoList == null)
            return Completable.error(new IllegalArgumentException("Null list of CashBoxes"));
        else if (toIndex < 0 || toIndex >= cashBoxInfoList.size())
            return Completable.error(new IndexOutOfBoundsException("Cannot move to index in list"));

        return getRepository().moveCashBoxInfo(infoWithCash,
                cashBoxInfoList.get(toIndex).getCashBoxInfo().getOrderId());
    }

    public Completable addEntryToCurrentCashBox(@NonNull Entry entry) {
        return addEntry(getCurrentCashBoxId(), entry);
    }

    public Completable addEntry(long cashBoxId, @NonNull Entry entry) {
        return getRepository().insertEntry(entry.getEntryWithCashBoxId(cashBoxId));
    }

    public Completable addAllEntriesToCurrentCashBox(@NonNull List<Entry> entries) {
        return addAllEntries(getCurrentCashBoxId(), entries);
    }

    private Completable addAllEntries(long cashBoxId, @NonNull Collection<Entry> entries) {
        LogUtil.debug(TAG, entries.toString());
        ArrayList<Entry> entryArrayList = new ArrayList<>();
        for (Entry entry : entries)
            entryArrayList.add(entry.getEntryWithCashBoxId(cashBoxId));
        return getRepository().insertEntries(entryArrayList);
    }

    public Completable addAllEntries(@NonNull Collection<Entry> entries) {
        return getRepository().insertEntries(entries);
    }

    public Completable updateEntry(Entry entry) {
        return getRepository().updateEntry(entry);
    }

    public Completable modifyEntry(@NonNull Entry entry, double amount, String info, Calendar date) {
        return getRepository().modifyEntry(entry.getId(), amount, info, date);
    }

    public Completable deleteEntry(Entry entry) {
        return getRepository().deleteEntry(entry);
    }

    public Single<Integer> deleteAllEntriesFromCurrentCashBox() {
        return getRepository().deleteAllEntries(getCurrentCashBoxId());
    }

    public Completable addPeriodicEntryWorkRequest(@NonNull PeriodicEntryPojo.PeriodicEntryWorkRequest workRequest) {
        return getRepository().addPeriodicEntryWorkRequest(workRequest);
    }

    public Single<List<Entry>> getGroupEntries(Entry entry) {
        return getRepository().getGroupEntries(entry.getGroupId());
    }

    public Completable modifyGroupEntry(Entry entry, double amount, String info, Calendar date) {
        return getRepository().modifyGroupEntry(entry.getGroupId(), amount, info, date);
    }

    public Single<Integer> deleteGroupEntries(Entry entry) {
        return getRepository().deleteGroupEntries(entry.getGroupId());
    }

    // Periodic Entries
//    public Completable deletePeriodicInactive() {
//        return repository.deletePeriodicInactive();
//    }
}
