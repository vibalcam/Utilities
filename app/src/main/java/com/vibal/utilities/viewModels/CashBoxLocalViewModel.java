package com.vibal.utilities.viewModels;

import android.app.Application;

import androidx.annotation.NonNull;

import com.vibal.utilities.models.InfoWithCash;
import com.vibal.utilities.persistence.repositories.CashBoxLocalRepository;
import com.vibal.utilities.persistence.repositories.CashBoxRepository;

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;

import io.reactivex.Completable;

public class CashBoxLocalViewModel extends CashBoxViewModel {
    private CashBoxLocalRepository repository;

    public CashBoxLocalViewModel(@NonNull Application application) throws CertificateException, NoSuchAlgorithmException, KeyStoreException, IOException, KeyManagementException {
        super(application);
    }

    @Override
    protected CashBoxRepository getRepository() {
        return repository;
    }

    @Override
    protected CashBoxRepository initializeRepository(Application application) {
        repository = CashBoxLocalRepository.getInstance(application);
        return repository;
    }

    public Completable restore(InfoWithCash infoWithCash) {
        return repository.restore(infoWithCash.getCashBoxInfo());
    }
}
