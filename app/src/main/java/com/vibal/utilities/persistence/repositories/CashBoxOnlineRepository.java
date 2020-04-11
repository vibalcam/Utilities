package com.vibal.utilities.persistence.repositories;

import android.app.Application;
import android.content.Context;
import android.database.sqlite.SQLiteConstraintException;

import androidx.annotation.NonNull;

import com.vibal.utilities.R;
import com.vibal.utilities.modelsNew.CashBoxInfo;
import com.vibal.utilities.modelsNew.Entry;
import com.vibal.utilities.persistence.db.CashBoxEntryOnlineDao;
import com.vibal.utilities.persistence.db.CashBoxOnlineDao;
import com.vibal.utilities.persistence.db.UtilitiesDatabase;
import com.vibal.utilities.persistence.retrofit.UtilAppAPI;
import com.vibal.utilities.persistence.retrofit.UtilAppException;
import com.vibal.utilities.persistence.retrofit.UtilAppRequest;
import com.vibal.utilities.persistence.retrofit.UtilAppResponse;
import com.vibal.utilities.ui.cashBoxManager.CashBoxManagerActivity;
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
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

import io.reactivex.Completable;
import io.reactivex.CompletableSource;
import io.reactivex.Single;
import io.reactivex.SingleSource;
import io.reactivex.functions.Function;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

import static com.vibal.utilities.persistence.retrofit.UtilAppAPI.CASHBOX_INV;
import static com.vibal.utilities.persistence.retrofit.UtilAppAPI.DELETE;
import static com.vibal.utilities.persistence.retrofit.UtilAppAPI.ID_ALL;
import static com.vibal.utilities.persistence.retrofit.UtilAppAPI.INSERT;
import static com.vibal.utilities.persistence.retrofit.UtilAppAPI.UPDATE;
import static com.vibal.utilities.ui.cashBoxManager.CashBoxManagerActivity.CASHBOX_MANAGER_PREFERENCE;

public class CashBoxOnlineRepository extends CashBoxRepository {
    private static final String TAG = "PruebaOnlineRepo";

    private static final String BASE_URL = "https://192.168.0.41/util/"; //todo temporal para prueba
    private static long ONLINE_ID = 0;

    private static CashBoxOnlineRepository INSTANCE = null;
    private static final Pattern PATTERN_VALID_USERNAME = Pattern.compile("\\W");

    private CashBoxOnlineDao cashBoxOnlineDao;
    private CashBoxEntryOnlineDao cashBoxEntryOnlineDao;
    private static UtilAppAPI utilAppAPI = null;

    public static boolean isOnlineIdSet() {
        return ONLINE_ID != 0;
    }

    public static void setOnlineId(long onlineId) {
        if(isOnlineIdSet())
            throw new IllegalArgumentException("There is already an existing onlineId");
        else
            ONLINE_ID = onlineId;
    }

    public static CashBoxOnlineRepository getInstance(Application application) throws CertificateException, NoSuchAlgorithmException, KeyStoreException, KeyManagementException, IOException {
        if(INSTANCE == null)
            INSTANCE = new CashBoxOnlineRepository(application);
        return INSTANCE;
    }

    private CashBoxOnlineRepository(Application application) throws CertificateException, NoSuchAlgorithmException, KeyStoreException, IOException, KeyManagementException {
        super(application);

        UtilitiesDatabase database = UtilitiesDatabase.getInstance(application);
        cashBoxOnlineDao = database.cashBoxOnlineDao();
        cashBoxEntryOnlineDao = database.cashBoxEntryOnlineDao();
        if (utilAppAPI == null) {
            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .client(getOkHttpClient(application))
                    .addConverterFactory(GsonConverterFactory.create())
                    .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                    .build();
            utilAppAPI = retrofit.create(UtilAppAPI.class);
        }

//        pruebaHttp();
    }

    @NonNull
    private OkHttpClient getOkHttpClient(Context context) throws CertificateException, NoSuchAlgorithmException, KeyStoreException, KeyManagementException, IOException {
        // Create an SSLContext that uses our TrustManager
        TrustManagerFactory tmf = getTrustManagerFactory(context);
        SSLContext sslContext = SSLContext.getInstance("TLS");
        sslContext.init(null, tmf.getTrustManagers(),null);

        return new OkHttpClient.Builder()
                .hostnameVerifier((hostname, session) -> hostname.equals("192.168.0.41")) // todo temporal para debug
                .sslSocketFactory(sslContext.getSocketFactory(), (X509TrustManager) (tmf.getTrustManagers()[0]))
                .addInterceptor(chain -> {
                    // Include client id and pwd in headers
                    Request originalRequest = chain.request();
                    Request newRequest = originalRequest.newBuilder()
                            .header(UtilAppAPI.CLIENT_ID, String.valueOf(ONLINE_ID))
                            .header(UtilAppAPI.PASSWORD_HEADER,UtilAppAPI.PASSWORD)
                            .build();
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

//    public void pruebaHttp() {
//        utilAppAPI.signUp("android")
//                .subscribeOn(Schedulers.io())
//                .observeOn(AndroidSchedulers.mainThread())
//                .subscribe((utilAppResponse) -> LogUtil.debug(utilAppResponse.getMessage()),
//                        throwable -> LogUtil.error("Error al llamar", throwable));
//    }

    // CashBoxes

    public Completable signUp(String username, Context context) throws NullPointerException, UtilAppException {
        if(username == null) // should never happen
            throw new NullPointerException("Username is null");

        final String finalUsername = username.trim();
        Matcher matcher;

        if(finalUsername.length() > UtilAppAPI.MAX_LENGTH_USERNAME)
            throw new UtilAppException("Username max length is " + UtilAppAPI.MAX_LENGTH_USERNAME);
        else if((matcher = PATTERN_VALID_USERNAME.matcher(finalUsername)).find())
            throw new UtilAppException(finalUsername.charAt(matcher.start()) + " not allowed");

        // Do http call and save value to shared preferences
        return utilAppAPI.signUp(finalUsername)
                .flatMap(new CheckResponseErrorFunction<>())
                .flatMapCompletable(utilAppResponse -> {
                    long id = Long.parseLong(utilAppResponse.getMessage());
                    if(context.getSharedPreferences(CASHBOX_MANAGER_PREFERENCE, Context.MODE_PRIVATE)
                            .edit()
                            .putString(CashBoxManagerActivity.USERNAME_KEY,finalUsername)
                            .putLong(CashBoxManagerActivity.CLIENT_ID_KEY,id)
                            .commit()) {
                        ONLINE_ID = id;
                        return Completable.complete();
                    } else
                        return Completable.error(new UtilAppException());
                });
    }

    @Override
    public Single<Long> insertCashBoxInfo(@NonNull CashBoxInfo cashBoxInfo) {
        return cashBoxOnlineDao.checkNameAvailable(cashBoxInfo.getName())
                .flatMap(exists -> {
                    if(!exists)
                        throw new SQLiteConstraintException("Name already in use");

                    return utilAppAPI.operationCashbox(new UtilAppRequest(INSERT,1))
                            .flatMap(new CheckResponseErrorFunction<>())
                            .flatMap(modificationResponse -> {
                                if (modificationResponse.operationSuccessful(1)) {
                                    long cashBoxId = modificationResponse.getValue(1);
                                    CashBoxInfo cashBoxCopy = cashBoxInfo.cloneContents(cashBoxId);
                                    return super.insertCashBoxInfo(cashBoxCopy);

//                        return super.insertCashBoxInfo(cashBoxCopy)
//                                .onErrorResumeNext(throwable ->
//                                        super.insertCashBoxInfo(cashBoxCopy.fixName(Long.toString(cashBoxId))));
                                } else {
                                    return Single.error(new UtilAppException());
                                }
                            });
                });
    }

    @Override
    public Completable deleteCashBox(@NonNull CashBoxInfo cashBoxInfo) {
        final long id = cashBoxInfo.getId();
        if(id==ID_ALL)
            return Completable.error(new UtilAppException("Id cannot be equal to " + ID_ALL));
        else
            return utilAppAPI.operationCashbox(new UtilAppRequest(DELETE, id))
                    .flatMap(new CheckResponseErrorFunction<>())
                    .flatMapCompletable(modificationResponse -> {
                        if(modificationResponse.operationSuccessful(id))
                            return super.deleteCashBox(cashBoxInfo);
                        else
                            return Completable.error(new UtilAppException());
                    });
    }

    @Override
    public Single<Integer> deleteAllCashBoxes() {
        return utilAppAPI.operationCashbox(new UtilAppRequest(DELETE, ID_ALL))
                .flatMap(new CheckResponseErrorFunction<>())
                .flatMap(modificationResponse -> {
                    if(modificationResponse.operationSuccessful(ID_ALL))
                        return super.deleteAllCashBoxes();
                    else
                        return Single.error(new UtilAppException());
                });
    }

    public Completable sendInvitationToCashBox(@NonNull String username, long cashBoxId) {
        if(cashBoxId<=0)
            return Completable.error(new UtilAppException("Id cannot be less or equal to 0"));
        else
            return utilAppAPI.sendInvitation(new UtilAppRequest.InvitationRequest(UPDATE,
                    cashBoxId,username.trim()))
                    .flatMap(new CheckResponseErrorFunction<>())
                    .flatMapCompletable(modificationResponse -> {
                        if(modificationResponse.operationSuccessful(cashBoxId))
                            return Completable.complete();
                        else
                            return Completable.error(new UtilAppException("Username not found"));
                    });
    }

    //todo accept invitation

    // Entries


    @Override
    public Completable insertEntry(Entry entry) {
        return utilAppAPI.operationEntry(new UtilAppRequest.EntryRequest(INSERT, 1, entry))
                .flatMap(new CheckResponseErrorFunction<>())
                .flatMapCompletable(modificationResponse -> {
                    if(modificationResponse.operationSuccessful(1)) {
                        long id = modificationResponse.getValue(1);
                        return super.insertEntry(entry.cloneContents(id, entry.getCashBoxId()));
                    } else
                        return Completable.error(new UtilAppException());
                });
    }

    @Override
    public Completable insertEntries(@NonNull Collection<Entry> entries) {
//        if(entries.size()<1)
//            return Completable.complete();
        Completable completable = Completable.complete();
        for (Entry entry : entries)
            completable = completable.andThen(insertEntry(entry));
        return completable;
    }

    private Completable updateEntry(@NonNull Entry entry, Completable localUpdate) {
        final long id = entry.getId();
        return utilAppAPI.operationEntry(new UtilAppRequest.EntryRequest(UPDATE, id,entry))
                .flatMap(new CheckResponseErrorFunction<>())
                .flatMapCompletable(modificationResponse -> {
                    if(modificationResponse.operationSuccessful(id))
                        return localUpdate;
                    else
                        return Completable.error(new UtilAppException());
                });
    }

    @Override
    public Completable updateEntry(Entry entry) {
        return updateEntry(entry, super.updateEntry(entry));
    }

    public Completable updateEntries(@NonNull Collection<Entry> entries) {
//        if(entries.size()<1)
//            return Completable.complete();
        Completable completable = Completable.complete();
        for(Entry entry : entries)
            completable = completable.andThen(updateEntry(entry));
        return completable;
    }

    @Override
    public Completable modifyEntry(long id, double amount, String info, Calendar date) {
        return updateEntry(new Entry(id,CashBoxInfo.NO_ID,amount,info,date),
                super.modifyEntry(id, amount, info, date));
    }

    private Completable deleteEntry(@NonNull Entry entry, Completable localUpdate) {
        final long id = entry.getId();
        return utilAppAPI.deleteEntry(new UtilAppRequest(DELETE,id))
                .flatMap(new CheckResponseErrorFunction<>())
                .flatMapCompletable(modificationResponse -> {
                    if(modificationResponse.operationSuccessful(id))
                        return localUpdate;
                    else
                        return Completable.error(new UtilAppException());
                });
    }

    @Override
    public Completable deleteEntry(Entry entry) {
        return deleteEntry(entry, super.deleteEntry(entry));
    }

    private Completable deleteEntriesServer(@NonNull Collection<Entry> entries) {
        if(entries.size()<1)
            return Completable.complete();
        Completable completable = Completable.complete();
        for(Entry entry : entries)
            completable = completable.andThen(deleteEntry(entry, Completable.complete()));
        return completable;
    }

//    public Completable deleteEntriesById(Collection<Integer> entriesIds) {
//        if(entriesIds.size()<1)
//            return Completable.complete();
//        Completable completable = Completable.complete();
//        for(Integer id : entriesIds) {
//            completable = completable.andThen(
//                    utilAppAPI.deleteEntry(new UtilAppRequest(UtilAppRequest.DELETE, id))
//                            .flatMap(new CheckResponseErrorFunction<>())
//                            .flatMapCompletable(modificationResponse -> {
//
//                            })
//
//            );
//        }
//        return completable;
//    }

    @Override
    public Single<Integer> deleteAllEntries(long cashBoxId) {
        if(cashBoxId == CashBoxInfo.NO_ID)
            throw new IllegalArgumentException("Not valid cashbox id");

        return getCashBoxEntryDao().getCashBoxEntriesIds(cashBoxId)
                .flatMap(integers -> {
                    ArrayList<Entry> entries = new ArrayList<>();//todo
                    integers.forEach(integer -> entries.add(new Entry(integer)));
                    return deleteEntriesServer(entries)
                            .andThen(super.deleteAllEntries(cashBoxId));
                });

//        return super.deleteAllEntries(cashBoxId);
    }

    @Override
    public Completable modifyGroupEntry(long groupId, double amount, String info, Calendar date) {
        if(groupId == Entry.NO_GROUP)
            throw new IllegalArgumentException("Default group id cannot be deleted");

        return getCashBoxEntryDao().getGroupIds(groupId)
                .flatMapCompletable(integers -> {
                    Completable completable = Completable.complete();
                    for(int id : integers)
                        completable = completable.andThen(modifyEntry(id,amount,info,date));
                    return completable;
                });
    }

    @Override
    public Single<Integer> deleteGroupEntries(long groupId) {
        if(groupId == Entry.NO_GROUP)
            throw new IllegalArgumentException("Default group id cannot be deleted");

        return getCashBoxEntryDao().getGroupIds(groupId)
                .flatMap(integers -> {
                    ArrayList<Entry> entries = new ArrayList<>();
                    integers.forEach(integer -> entries.add(new Entry(integer)));
                    return deleteEntriesServer(entries)
                            .andThen(super.deleteGroupEntries(groupId));
                });
    }

    // Download changes

    public Completable getChanges() {
        return utilAppAPI.getChanges()
                .flatMap(new CheckResponseErrorFunction<>())
                .flatMapCompletable(changesResponse -> {
                    LogUtil.debug(TAG,"Processing " + changesResponse.getChanges().size() + " changes");

                    // If no new changes
                    if(changesResponse.getChanges().size()<1)
                        return Completable.complete();

                    // Process changes
                    HashSet<Long> notificationIds = new HashSet<>();
                    Completable operation;
                    Completable completable = Completable.complete();
                    for(UtilAppResponse.ChangesNotification change : changesResponse.getChanges()) {
//                        switch (change.getOperationCode()) {
//                            case CASHBOX_INV:
//                                completable = completable.andThen(
//                                        insertCashBoxInfo(new CashBoxInfo(change.getId(),change.getInfo())))
//                                        .ignoreElement();
//                                break;
//                            case INSERT:
//                                completable = completable.andThen(
//                                        insertEntry(changeNotificationToEntry(change)));
//                                break;
//                            case UPDATE:
//                                completable = completable.andThen(
//                                        modifyEntry(change.getId(),change.getAmount(),
//                                                change.getInfo(),change.getDate()));
//                                break;
//                            case DELETE:
//                                completable = completable.andThen(
//                                        deleteEntry(new Entry(change.getId())));
//                        }

                        switch (change.getOperationCode()) {
                            case CASHBOX_INV:
                                operation = insertCashBoxInfo(new CashBoxInfo(change.getId(),change.getInfo()))
                                        .ignoreElement();
                                break;
                            case INSERT:
                                operation = insertEntry(changeNotificationToEntry(change));
                                break;
                            case UPDATE:
                                operation = modifyEntry(change.getId(),change.getAmount(),
                                                change.getInfo(),change.getDate());
                                break;
                            case DELETE:
                                operation = deleteEntry(new Entry(change.getId()));
                                break;
                            default:
                                operation = null;
                        }
                        if (operation != null) {
                            operation = operation.andThen((CompletableSource) co ->
                                    notificationIds.add(change.getNotificationId()))
                                    .onErrorComplete();
                            completable = completable.andThen(operation);
                        }
                    }

                    return completable.andThen(confirmReceivedChanges(notificationIds));
                });
    }

    private Entry changeNotificationToEntry(UtilAppResponse.ChangesNotification change) {
        return new Entry(change.getId(),change.getCashBoxId(),change.getAmount(),change.getInfo(),
                change.getDate(), Entry.NO_GROUP);
    }

    public Completable confirmReceivedChanges(Set<Long> notificationIds) {
        return utilAppAPI.confirmReceivedChanges(notificationIds)
                .flatMap(new CheckResponseErrorFunction<>())
                .ignoreElement();
        //todo save notifications not send to send after
        //todo retry checking connection
    }

    private static class CheckResponseErrorFunction<T extends UtilAppResponse>
            implements Function<T, SingleSource<T>> {
        @Override
        public SingleSource<T> apply(@NonNull T response) {
            if(response.isSuccessful())
                return Single.just(response);
            else
                return Single.error(new UtilAppException(response.getMessage()));
        }
    }
}
