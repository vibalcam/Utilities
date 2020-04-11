package com.vibal.utilities.viewModels;

import android.app.Application;

import androidx.annotation.NonNull;

import com.vibal.utilities.persistence.repositories.CashBoxOnlineRepository;
import com.vibal.utilities.persistence.repositories.CashBoxRepository;
import com.vibal.utilities.persistence.retrofit.UtilAppException;

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;

import io.reactivex.Completable;

public class CashBoxOnlineViewModel extends CashBoxViewModel {
    private CashBoxOnlineRepository repository;

    public CashBoxOnlineViewModel(@NonNull Application application) throws CertificateException, NoSuchAlgorithmException, KeyStoreException, KeyManagementException, IOException {
//        super(application, new CashBoxOnlineRepository(application));
//        super(application, CashBoxOnlineRepository.getInstance(application));
        super(application);
//        repository = CashBoxOnlineRepository.getInstance(application);
    }

    @Override
    protected CashBoxRepository getRepository() {
        return repository;
    }

    @Override
    protected CashBoxRepository initializeRepository(Application application) throws CertificateException, NoSuchAlgorithmException, KeyStoreException, IOException, KeyManagementException {
        repository = CashBoxOnlineRepository.getInstance(application);
        return repository;
    }

    public Completable signUp(String username) throws UtilAppException {
        return repository.signUp(username, getApplication());
    }

    public Completable sendInvitation(String username) {
        return repository.sendInvitationToCashBox(username,getCurrentCashBoxId());
    }
}
