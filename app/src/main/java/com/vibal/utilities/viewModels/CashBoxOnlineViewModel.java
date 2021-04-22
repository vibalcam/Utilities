package com.vibal.utilities.viewModels;

import android.app.Application;

import androidx.annotation.NonNull;

import com.vibal.utilities.exceptions.UtilAppException;
import com.vibal.utilities.models.EntryOnline;
import com.vibal.utilities.persistence.repositories.CashBoxOnlineRepository;
import com.vibal.utilities.persistence.repositories.CashBoxRepository;

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.util.List;

import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.Single;

public class CashBoxOnlineViewModel extends CashBoxViewModel {
    private CashBoxOnlineRepository repository;

    public CashBoxOnlineViewModel(@NonNull Application application) throws CertificateException,
            NoSuchAlgorithmException, KeyStoreException, KeyManagementException, IOException {
        super(application);
    }

    @Override
    protected CashBoxRepository getRepository() {
        return repository;
    }

    @Override
    protected CashBoxRepository initializeRepository(Application application) throws CertificateException,
            NoSuchAlgorithmException, KeyStoreException, IOException, KeyManagementException {
        repository = CashBoxOnlineRepository.getInstance(application);
        return repository;
    }

    public Completable signUp(String username) throws UtilAppException {
        return repository.signUp(username, getApplication());
    }

    public Completable sendInvitation(String username) {
        return repository.sendInvitationToCashBox(username, getCurrentCashBoxId());
    }

    public Completable acceptInvitation(long cashBoxId) {
        return repository.acceptInvitationToCashBox(cashBoxId);
    }

    public Observable<Object> getChanges() {
        return repository.getChanges();
    }

    public Single<List<EntryOnline.EntryChanges>> getNonViewedEntries(long cashBoxId) {
        return repository.getNonViewedEntries(cashBoxId)
                .flatMap(entryChanges -> repository.doViewedEntries(cashBoxId)
                        .toSingleDefault(entryChanges));
    }
}
