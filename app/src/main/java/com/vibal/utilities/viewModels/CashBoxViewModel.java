package com.vibal.utilities.viewModels;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.vibal.utilities.models.CashBox;
import com.vibal.utilities.models.CashBoxInfo;
import com.vibal.utilities.models.EntryBase;
import com.vibal.utilities.models.EntryInfo;
import com.vibal.utilities.models.InfoWithCash;
import com.vibal.utilities.models.PeriodicEntryPojo;
import com.vibal.utilities.persistence.repositories.CashBoxRepository;
import com.vibal.utilities.util.LogUtil;

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.util.Calendar;
import java.util.Collection;
import java.util.Currency;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.reactivex.Completable;
import io.reactivex.Single;

import static com.vibal.utilities.models.CashBoxInfo.NO_ID;

public abstract class CashBoxViewModel extends AndroidViewModel {
    private static final String TAG = "PruebaCashBoxViewModel";

    private LiveData<List<InfoWithCash>> cashBoxesInfo;
    private LiveData<CashBox> cashBox;
    private long cashBoxId = NO_ID;
    private final Map<String, LiveData<Double>> participantBalances = new HashMap<>();

    protected CashBoxViewModel(@NonNull Application application) throws CertificateException, NoSuchAlgorithmException, KeyStoreException, KeyManagementException, IOException {
        super(application);
        cashBoxesInfo = initializeRepository(application).getCashBoxesInfo();
    }

    protected abstract CashBoxRepository getRepository();

    protected abstract CashBoxRepository initializeRepository(Application application) throws CertificateException, NoSuchAlgorithmException, KeyStoreException, IOException, KeyManagementException;

    public LiveData<List<InfoWithCash>> getCashBoxesInfo() {
        return cashBoxesInfo;
    }

    protected void setCashBoxesInfo(LiveData<List<InfoWithCash>> cashBoxesInfo) {
        this.cashBoxesInfo = cashBoxesInfo;
    }

    @NonNull
    public LiveData<Double> getCurrentSelfCashBalance() {
        if (getCurrentCashBoxId() == NO_ID)
            throw new IllegalStateException("No cashBox selected for balance");
        // If different, get the current self balance
        LiveData<Double> liveData = participantBalances.get(EntryBase.getSelfName());
        if (liveData == null) {
            liveData = getRepository().getCashBalance(getCurrentCashBoxId(), EntryBase.getSelfName());
            participantBalances.put(EntryBase.getSelfName(), liveData);
        }
        return liveData;
    }

    @NonNull
    public LiveData<CashBox> getCurrentCashBox() {
        LogUtil.debug(TAG, "Id cashBox: " + getCurrentCashBoxId());
        return cashBox;
    }

    public long getCurrentCashBoxId() {
        return cashBoxId;
//        return cashBox == null || cashBox.getValue() == null ? NO_ID :
//                cashBox.getValue().getInfoWithCash().getId();
    }

    public CashBox requireCashBox() {
        if (cashBox == null || cashBox.getValue() == null)
            throw new IllegalStateException("CashBox has not yet been initialized");
        return cashBox.getValue();
    }

    public void setCurrentCashBoxId(long currentCashBoxId) {
        // If different, get the current data
        if (getCurrentCashBoxId() != currentCashBoxId) {
            cashBox = getRepository().getOrderedCashBox(currentCashBoxId);
            participantBalances.clear();
            cashBoxId = currentCashBoxId;
        }
    }

    public Single<CashBox> getCashBox(long id) {
        return getRepository().getCashBox(id);
    }

    public Completable addCashBoxInfo(@NonNull InfoWithCash cashBoxInfo) {
        return getRepository().insertCashBoxInfo(cashBoxInfo.getCashBoxInfo()).ignoreElement();
    }

    /**
     * CAREFUL: ADDING CASHBOX BEFORE ANOTHER ONE, DOES NOT ASSURE ORDER ID
     */
    public Completable addCashBox(@NonNull CashBox cashBox) {
        return getRepository().insertCashBox(cashBox);
    }

    public Completable changeCashBoxName(@NonNull InfoWithCash cashBoxInfo, @NonNull String newName)
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

    public Completable deleteCashBoxInfo(@NonNull InfoWithCash infoWithCash) {
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

    public Completable moveCashBox(@NonNull InfoWithCash infoWithCash, int toIndex) {
        List<InfoWithCash> cashBoxInfoList = cashBoxesInfo.getValue();
        if (cashBoxInfoList == null)
            return Completable.error(new IllegalArgumentException("Null list of CashBoxes"));
        else if (toIndex < 0 || toIndex >= cashBoxInfoList.size())
            return Completable.error(new IndexOutOfBoundsException("Cannot move to index in list"));

        return getRepository().moveCashBoxInfo(infoWithCash,
                cashBoxInfoList.get(toIndex).getCashBoxInfo().getOrderId());
    }

    public Completable addEntryToCurrentCashBox(@NonNull EntryBase<?> entry) {
        return addEntry(getCurrentCashBoxId(), entry);
    }

    public Completable addEntry(long cashBoxId, @NonNull EntryBase<?> entry) {
        return getRepository().insertEntry(cashBoxId, entry);
    }

    public Completable addAllEntriesToCurrentCashBox(@NonNull List<EntryBase<?>> entries) {
        return addAllEntries(getCurrentCashBoxId(), entries);
    }

    private Completable addAllEntries(long cashBoxId, @NonNull Collection<EntryBase<?>> entries) {
        return getRepository().insertEntries(cashBoxId, entries);
    }

    public Completable addAllEntries(@NonNull Collection<? extends EntryBase<?>> entries) {
        return getRepository().insertEntriesRaw(entries);
    }

    public Completable updateEntryInfo(EntryInfo entry) {
        return getRepository().updateEntryInfo(entry);
    }

    public Completable modifyEntryInfo(@NonNull EntryInfo entry, double amount, String info, Calendar date) {
        return getRepository().modifyEntry(entry.getId(), amount, info, date);
    }

    public Completable deleteEntry(EntryBase<?> entry) {
        return getRepository().deleteEntry(entry);
    }

    public Single<Integer> deleteAllEntriesFromCurrentCashBox() {
        return getRepository().deleteAllEntries(getCurrentCashBoxId());
    }

    public Completable addPeriodicEntryWorkRequest(@NonNull PeriodicEntryPojo.PeriodicEntryWorkRequest workRequest) {
        return getRepository().addPeriodicEntryWorkRequest(workRequest);
    }

    public Single<? extends List<? extends EntryBase<?>>> getGroupEntries(EntryInfo entry) {
        return getRepository().getGroupEntries(entry.getGroupId());
    }

    public Completable modifyGroupEntry(EntryInfo entry, double amount, String info, Calendar date) {
        return getRepository().modifyGroupEntry(entry.getGroupId(), amount, info, date);
    }

    public Single<Integer> deleteGroupEntries(EntryBase<?> entry) {
        return getRepository().deleteGroupEntries(entry.getEntryInfo().getGroupId());
    }

    // Periodic Entries
//    public Completable deletePeriodicInactive() {
//        return repository.deletePeriodicInactive();
//    }
}
