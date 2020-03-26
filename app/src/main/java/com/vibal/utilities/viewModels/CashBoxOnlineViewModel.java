package com.vibal.utilities.viewModels;

import android.app.Application;

import androidx.annotation.NonNull;

import com.vibal.utilities.db.CashBoxOnlineRepository;

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;

public class CashBoxOnlineViewModel extends CashBoxViewModel {
    public CashBoxOnlineViewModel(@NonNull Application application) throws CertificateException, NoSuchAlgorithmException, KeyStoreException, KeyManagementException, IOException {
        super(application, new CashBoxOnlineRepository(application));
    }
}
