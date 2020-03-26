package com.vibal.utilities.db;

import android.app.Application;
import android.content.Context;

import androidx.annotation.NonNull;

import com.vibal.utilities.R;
import com.vibal.utilities.backgroundTasks.UtilAppAPI;
import com.vibal.utilities.util.LogUtil;

import java.io.IOException;
import java.io.InputStream;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

import static com.vibal.utilities.ui.cashBoxManager.CashBoxManagerActivity.ONLINE_ID;

public class CashBoxOnlineRepository extends CashBoxRepository {
    private static final String BASE_URL = "https://192.168.0.41/util/"; //todo temporal para prueba

    private CashBoxOnlineDao cashBoxOnlineDao;
    private CashBoxEntryOnlineDao cashBoxEntryOnlineDao;
    private static UtilAppAPI utilAppAPI = null;

    public CashBoxOnlineRepository(Application application) throws CertificateException, NoSuchAlgorithmException, KeyStoreException, IOException, KeyManagementException {
        super(application);

        UtilitiesDatabase database = UtilitiesDatabase.getInstance(application);
        cashBoxOnlineDao = database.cashBoxOnlineDao();
        cashBoxEntryOnlineDao = database.cashBoxEntryOnlineDao();
//        setCashBoxesInfo(cashBoxOnlineDao.getAllCashBoxesInfo());
        if (utilAppAPI == null) {
            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .client(getOkHttpClient(application))
                    .addConverterFactory(GsonConverterFactory.create())
                    .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                    .build();
            utilAppAPI = retrofit.create(UtilAppAPI.class);
        }

        //todo temporal
        pruebaHttp();
    }

    @NonNull
    private OkHttpClient getOkHttpClient(Context context) throws CertificateException, NoSuchAlgorithmException, KeyStoreException, KeyManagementException, IOException {
        // Create an SSLContext that uses our TrustManager
        TrustManagerFactory tmf = getTrustManagerFactory(context);
        SSLContext sslContext = SSLContext.getInstance("TLS");
        sslContext.init(null, tmf.getTrustManagers(),null);

        return new OkHttpClient.Builder()
                .hostnameVerifier((hostname, session) -> hostname.equals("192.168.0.41")) // todo temporal para prueba
                .sslSocketFactory(sslContext.getSocketFactory(), (X509TrustManager) (tmf.getTrustManagers()[0]))
                .addInterceptor(chain -> {
                    Request originalRequest = chain.request();
                    Request newRequest = originalRequest.newBuilder()
                            .header(UtilAppAPI.CLIENT_ID, String.valueOf(ONLINE_ID))
                            .header(UtilAppAPI.PASSWORD_HEADER,UtilAppAPI.PASSWORD)
                            .build();
                    LogUtil.debug(newRequest.headers().toString());
                    return chain.proceed(newRequest);
                }).build();
    }

    @NonNull
    private static TrustManagerFactory getTrustManagerFactory(@NonNull Context context) throws CertificateException, IOException, KeyStoreException, NoSuchAlgorithmException, KeyManagementException {
        // Admit ssl certificate
        // Load CAs from an InputStream
        CertificateFactory cf = CertificateFactory.getInstance("X.509");
        Certificate ca;
        try(InputStream cert = context.getResources().openRawResource(R.raw.cert)) {
            ca = cf.generateCertificate(cert);
        }

        // Create a KeyStore containing our trusted CAs
        String keyStoreType = KeyStore.getDefaultType();
        KeyStore keyStore = KeyStore.getInstance(keyStoreType);
        keyStore.load(null,null);
        keyStore.setCertificateEntry("ca",ca);

        // Create a TrustManager that trusts the CAs in our KeyStore
        String tmfAlgorithm = TrustManagerFactory.getDefaultAlgorithm();
        TrustManagerFactory tmf = TrustManagerFactory.getInstance(tmfAlgorithm);
        tmf.init(keyStore);

        return tmf;
    }

    @Override
    protected CashBoxOnlineDao getCashBoxDao() {
        return cashBoxOnlineDao;
    }

    @Override
    protected CashBoxEntryOnlineDao getCashBoxEntryDao() {
        return cashBoxEntryOnlineDao;
    }

    public void pruebaHttp() {
        utilAppAPI.signUp("android")
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe((utilAppResponse) -> LogUtil.debug(utilAppResponse.getMessage()),
                        throwable -> LogUtil.error("Error al llamar", throwable));
    }
}
