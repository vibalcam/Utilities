package com.vibal.utilities.persistence.repositories;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteConstraintException;

import androidx.annotation.NonNull;

import com.vibal.utilities.BuildConfig;
import com.vibal.utilities.models.CashBoxInfo;
import com.vibal.utilities.models.Entry;
import com.vibal.utilities.models.EntryOnline;
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
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.SingleSource;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;
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
    private static final int TIMEOUT_CHANGES = 30; // in seconds
    private static final int NUM_RETRIES_CHANGES = 3;
    private static final String KEY_ONLINE_PREFERENCE = "com.vibal.utilities.persistence.ONLINE_NOTIFICATIONS";

    private static final Pattern PATTERN_VALID_USERNAME = Pattern.compile("\\W");
    private static long ONLINE_ID = 0;
    private static CashBoxOnlineRepository INSTANCE = null;
    private static UtilAppAPI UTILAPP_API = null;
    private final TreeMap<Long, Long> receivedNotifications = new TreeMap<>();
    private CashBoxOnlineDao cashBoxOnlineDao;
    private CashBoxEntryOnlineDao cashBoxEntryOnlineDao;
    private SharedPreferences notificationsPreference = null;

    //    private CashBoxOnlineRepository(Application application) throws CertificateException,
    private CashBoxOnlineRepository(Context context) throws CertificateException,
            NoSuchAlgorithmException, KeyStoreException, IOException, KeyManagementException {
        super(context);
        context = context.getApplicationContext();

        // Database
        UtilitiesDatabase database = UtilitiesDatabase.getInstance(context);
        cashBoxOnlineDao = database.cashBoxOnlineDao();
        cashBoxEntryOnlineDao = database.cashBoxEntryOnlineDao();

        // Confirmed notifications
        if (notificationsPreference == null)
            notificationsPreference = context.getSharedPreferences(KEY_ONLINE_PREFERENCE, Context.MODE_PRIVATE);
        // Load received notifications
        notificationsPreference.getAll().forEach((s, o) -> receivedNotifications.put(Long.parseLong(s), (Long) o));

        // Retrofit api
        if (UTILAPP_API == null) {
            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl(BuildConfig.BASE_URL)
                    .client(getOkHttpClient(context))
                    .addConverterFactory(GsonConverterFactory.create())
                    .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                    .build();
            UTILAPP_API = retrofit.create(UtilAppAPI.class);
        }
    }

    public static boolean isOnlineIdSet() {
        return ONLINE_ID != 0;
    }

    public static void setOnlineId(long onlineId) {
        if (isOnlineIdSet() && onlineId != 0)
            throw new IllegalArgumentException("There is already an existing onlineId");
        else
            ONLINE_ID = onlineId;
    }

    //    public static CashBoxOnlineRepository getInstance(Application application) throws CertificateException,
    public static CashBoxOnlineRepository getInstance(Context context) throws CertificateException,
            NoSuchAlgorithmException, KeyStoreException, KeyManagementException, IOException {
        if (INSTANCE == null)
            INSTANCE = new CashBoxOnlineRepository(context);
        return INSTANCE;
    }

    // TCP/HTTP/DNS (depending on the port, 53=DNS, 80=HTTP, etc.)
    private static boolean isOnline() {
        try {
            int timeoutMs = 1500;
            Socket sock = new Socket();
            SocketAddress socketAddress = new InetSocketAddress("8.8.8.8", 53);

            sock.connect(socketAddress, timeoutMs);
            sock.close();

            return true;
        } catch (IOException e) {
            return false;
        }
    }

//    @NonNull
//    private static TrustManagerFactory getTrustManagerFactory(@NonNull Context context) throws CertificateException, IOException, KeyStoreException, NoSuchAlgorithmException {
//        // Admit ssl certificate
//        // Load CAs from an InputStream
//        CertificateFactory cf = CertificateFactory.getInstance("X.509");
//        Certificate ca;
//        try (InputStream cert = context.getResources().openRawResource(R.raw.cert)) {
//            ca = cf.generateCertificate(cert);
//        }
//
//        // Create a KeyStore containing our trusted CAs
//        String keyStoreType = KeyStore.getDefaultType();
//        KeyStore keyStore = KeyStore.getInstance(keyStoreType);
//        keyStore.load(null, null);
//        keyStore.setCertificateEntry("ca", ca);
//
//        // Create a TrustManager that trusts the CAs in our KeyStore
//        String tmfAlgorithm = TrustManagerFactory.getDefaultAlgorithm();
//        TrustManagerFactory tmf = TrustManagerFactory.getInstance(tmfAlgorithm);
//        tmf.init(keyStore);
//
//        return tmf;
//    }

    @NonNull
    private OkHttpClient getOkHttpClient(Context context) throws CertificateException, NoSuchAlgorithmException, KeyStoreException, KeyManagementException, IOException {
        // Create an SSLContext that uses our TrustManager
//        TrustManagerFactory tmf = getTrustManagerFactory(context);
//        SSLContext sslContext = SSLContext.getInstance("TLS");
//        sslContext.init(null, tmf.getTrustManagers(), null);

        OkHttpClient.Builder httpClientBuilder = new OkHttpClient.Builder()
//                .sslSocketFactory(sslContext.getSocketFactory(), (X509TrustManager) (tmf.getTrustManagers()[0]))
                .addInterceptor(chain -> {
                    if (!isOnline())
                        throw new UtilAppException.NoConnectivityException();

                    // Include client id and pwd in headers
                    Request originalRequest = chain.request();
                    Request newRequest = originalRequest.newBuilder()
                            .header(UtilAppAPI.CLIENT_ID, String.valueOf(ONLINE_ID))
                            .header(UtilAppAPI.PASSWORD_HEADER, BuildConfig.ONLINE_PWD)
                            .build();
                    return chain.proceed(newRequest);
                });
        // for usage in debug
        if (BuildConfig.DEBUG_MODE)
            httpClientBuilder.hostnameVerifier((hostname, session) -> hostname.equals("192.168.0.41"));

        return httpClientBuilder.build();
    }

    @Override
    protected CashBoxOnlineDao getCashBoxDao() {
        return cashBoxOnlineDao;
    }

    @Override
    protected CashBoxEntryOnlineDao getCashBoxEntryDao() {
        return cashBoxEntryOnlineDao;
    }

    // CashBoxes

    public Completable signUp(String username, Context context) throws NullPointerException, UtilAppException {
        if (username == null) // should never happen
            throw new NullPointerException("Username is null");

        final String finalUsername = username.trim();
        Matcher matcher;

        if (finalUsername.length() > UtilAppAPI.MAX_LENGTH_USERNAME)
            throw new UtilAppException("Username max length is " + UtilAppAPI.MAX_LENGTH_USERNAME);
        else if ((matcher = PATTERN_VALID_USERNAME.matcher(finalUsername)).find())
            throw new UtilAppException(finalUsername.charAt(matcher.start()) + " not allowed");

        // Do http call and save value to shared preferences
        return UTILAPP_API.signUp(finalUsername)
                .flatMap(new CheckResponseErrorFunction<>())
                .flatMapCompletable(utilAppResponse -> {
                    long id = Long.parseLong(utilAppResponse.getMessage());
                    if (context.getSharedPreferences(CASHBOX_MANAGER_PREFERENCE, Context.MODE_PRIVATE)
                            .edit()
                            .putString(CashBoxManagerActivity.USERNAME_KEY, finalUsername)
                            .putLong(CashBoxManagerActivity.CLIENT_ID_KEY, id)
                            .commit()) {
                        ONLINE_ID = id;
                        return Completable.complete();
                    } else
                        return Completable.error(new UtilAppException());
                });
    }

    public Single<String> deleteUser() {
        return UTILAPP_API.deleteUser()
                .flatMap(new CheckResponseErrorFunction<>())
                .map(UtilAppResponse::getMessage);
    }

    @Override
    public Single<Long> insertCashBoxInfo(@NonNull CashBoxInfo cashBoxInfo) {
        return cashBoxOnlineDao.checkNameAvailable(cashBoxInfo.getName())
                .flatMap(exists -> {
                    if (!exists)
                        throw new SQLiteConstraintException("Name already in use: " + cashBoxInfo.getName());

                    return UTILAPP_API.operationCashbox(new UtilAppRequest(INSERT, 1))
                            .flatMap(new CheckResponseErrorFunction<>())
                            .flatMap(modificationResponse -> {
                                if (modificationResponse.operationSuccessful(1)) {
                                    long cashBoxId = modificationResponse.getValue(1);
                                    CashBoxInfo cashBoxCopy = cashBoxInfo.cloneContents(cashBoxId);
                                    return super.insertCashBoxInfo(cashBoxCopy)
                                            .flatMap(aLong -> cashBoxOnlineDao.setCashBoxAccepted(cashBoxId, true)
                                                    .toSingleDefault(aLong));
                                } else {
                                    return Single.error(new UtilAppException());
                                }
                            });
                });
    }

    @Override
    public Completable deleteCashBox(@NonNull CashBoxInfo cashBoxInfo) {
        final long id = cashBoxInfo.getId();
        if (id == ID_ALL)
            return Completable.error(new UtilAppException("Id cannot be equal to " + ID_ALL));
        else
            return UTILAPP_API.operationCashbox(new UtilAppRequest(DELETE, id))
                    .flatMap(new CheckResponseErrorFunction<>())
                    .flatMapCompletable(modificationResponse -> {
                        if (modificationResponse.operationSuccessful(id))
                            return super.deleteCashBox(cashBoxInfo);
                        else
                            return Completable.error(new UtilAppException());
                    });
    }

    @Override
    public Single<Integer> deleteAllCashBoxes() {
        return UTILAPP_API.operationCashbox(new UtilAppRequest(DELETE, ID_ALL))
                .flatMap(new CheckResponseErrorFunction<>())
                .flatMap(modificationResponse -> {
                    if (modificationResponse.operationSuccessful(ID_ALL))
                        return super.deleteAllCashBoxes();
                    else
                        return Single.error(new UtilAppException());
                });
    }

    public Completable sendInvitationToCashBox(@NonNull String username, long cashBoxId) {
        if (cashBoxId <= 0)
            return Completable.error(new UtilAppException("Id cannot be less or equal to 0"));
        else
            return UTILAPP_API.sendInvitation(new UtilAppRequest.InvitationRequest(UPDATE,
                    cashBoxId, username.trim()))
                    .flatMap(new CheckResponseErrorFunction<>())
                    .flatMapCompletable(modificationResponse -> {
                        if (modificationResponse.operationSuccessful(cashBoxId))
                            return Completable.complete();
                        else
                            return Completable.error(new UtilAppException("Username not found"));
                    });
    }

    public Completable acceptInvitationToCashBox(long cashBoxId) {
        if (cashBoxId <= 0)
            return Completable.error(new UtilAppException("Id cannot be less or equal to 0"));
        else
            return UTILAPP_API.acceptInvitation(new UtilAppRequest(CASHBOX_INV, cashBoxId))
                    .flatMap(new CheckResponseErrorFunction<>())
                    .flatMapCompletable(entriesResponse ->
                            cashBoxEntryOnlineDao.insertAllJSON(entriesResponse.getEntries(cashBoxId))
                                    .andThen(cashBoxOnlineDao.setCashBoxAccepted(cashBoxId, true)));
    }

    // Entries

    @Override
    public Completable insertEntry(Entry entry) {
        return UTILAPP_API.operationEntry(new UtilAppRequest.EntryRequest(INSERT, 1, entry))
                .flatMap(new CheckResponseErrorFunction<>())
                .flatMapCompletable(modificationResponse -> {
                    if (modificationResponse.operationSuccessful(1)) {
                        long id = modificationResponse.getValue(1);
                        return super.insertEntry(entry.cloneContents(id, entry.getCashBoxId()));
                    } else
                        return Completable.error(new UtilAppException());
                });
    }

    @Override
    public Completable insertEntries(@NonNull Collection<Entry> entries) {
        Completable completable = Completable.complete();
        for (Entry entry : entries)
            completable = completable.andThen(insertEntry(entry));
        return completable;
    }

    private Completable updateEntry(@NonNull Entry entry, Completable localUpdate) {
        final long id = entry.getId();
        return UTILAPP_API.operationEntry(new UtilAppRequest.EntryRequest(UPDATE, id, entry))
                .flatMap(new CheckResponseErrorFunction<>())
                .flatMapCompletable(modificationResponse -> {
                    if (modificationResponse.operationSuccessful(id))
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
        Completable completable = Completable.complete();
        for (Entry entry : entries)
            completable = completable.andThen(updateEntry(entry));
        return completable;
    }

    @Override
    public Completable modifyEntry(long id, double amount, String info, Calendar date) {
        return updateEntry(new Entry(id, CashBoxInfo.NO_ID, amount, info, date),
                super.modifyEntry(id, amount, info, date));
    }

    private Completable deleteEntry(@NonNull Entry entry, Completable localUpdate) {
        final long id = entry.getId();
        return UTILAPP_API.deleteEntry(new UtilAppRequest(DELETE, id))
                .flatMap(new CheckResponseErrorFunction<>())
                .flatMapCompletable(modificationResponse -> {
                    if (modificationResponse.operationSuccessful(id))
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
        Completable completable = Completable.complete();
        for (Entry entry : entries)
            completable = completable.andThen(deleteEntry(entry, Completable.complete()));
        return completable;
    }

    @Override
    public Single<Integer> deleteAllEntries(long cashBoxId) {
        if (cashBoxId == CashBoxInfo.NO_ID)
            throw new IllegalArgumentException("Not valid cashbox id");

        return getCashBoxEntryDao().getCashBoxEntriesIds(cashBoxId)
                .flatMap(integers -> {
                    ArrayList<Entry> entries = new ArrayList<>();
                    integers.forEach(integer -> entries.add(new Entry(integer)));
                    return deleteEntriesServer(entries)
                            .andThen(super.deleteAllEntries(cashBoxId));
                });
    }

    @Override
    public Completable modifyGroupEntry(long groupId, double amount, String info, Calendar date) {
        if (groupId == Entry.NO_GROUP)
            throw new IllegalArgumentException("Default group id cannot be deleted");

        return getCashBoxEntryDao().getGroupIds(groupId)
                .flatMapCompletable(integers -> {
                    Completable completable = Completable.complete();
                    for (int id : integers)
                        completable = completable.andThen(modifyEntry(id, amount, info, date));
                    return completable;
                });
    }

    @Override
    public Single<Integer> deleteGroupEntries(long groupId) {
        if (groupId == Entry.NO_GROUP)
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

    public Completable getCompletableChanges() {
        return UTILAPP_API.getChanges()
                .flatMap(new CheckResponseErrorFunction<>())
                .flatMapCompletable(changesResponse -> {
                    LogUtil.debug(TAG, "Processing " + changesResponse.getChanges().size() + " changes");
//                    Thread.sleep(1000);
//                    LogUtil.debug(TAG, "Half " + changesResponse.getChanges().size() + " changes");
//                    Thread.sleep(1000);
//                    LogUtil.debug(TAG, "Finish " + changesResponse.getChanges().size() + " changes");

                    // If no new changes
                    if (changesResponse.getChanges().size() < 1) {
                        if (notificationsPreference.getAll().isEmpty())
                            return Completable.complete();
                        else
                            return confirmReceivedChanges();
                    }

                    // Process changes
                    Completable operation;
                    Completable completable = Completable.complete();
                    for (UtilAppResponse.ChangesNotification change : changesResponse.getChanges()) {
                        LogUtil.debug(TAG, "Processing " + change.getNotificationId() + "...");

                        // Check if its an older version of another notification
                        if (change.getOperationCode() != CASHBOX_INV &&
                                receivedNotifications.tailMap(change.getNotificationId()).containsValue(change.getId())) {
                            if (notificationsPreference.edit()
                                    .putLong(Long.toString(change.getNotificationId()), change.getId())
                                    .commit()) {
                                receivedNotifications.put(change.getNotificationId(), change.getId());
                            }
                            continue; // skip since its an older version
                        }

                        switch (change.getOperationCode()) {
                            case CASHBOX_INV:
                                operation = cashBoxOnlineDao.insert(new CashBoxInfo(change.getId(), change.getInfo()))
                                        .onErrorResumeNext(throwable -> {
                                            if (throwable instanceof SQLiteConstraintException)
                                                return cashBoxOnlineDao.insert(new CashBoxInfo(
                                                        change.getId(), change.getInfo())
                                                        .fixName(Long.toString(change.getId())));
                                            else
                                                return Single.error(throwable);
                                        }).ignoreElement();
                                break;
                            case INSERT:
                                operation = cashBoxEntryOnlineDao.insert(changeNotificationToEntry(change));
                                break;
                            case UPDATE:
                                operation = cashBoxEntryOnlineDao.setChangeDate(change.getId(), Calendar.getInstance())
                                        .andThen(cashBoxEntryOnlineDao.copyAsNonViewedOld(change.getId()))
                                        .andThen(cashBoxEntryOnlineDao.modify(change.getId(), change.getAmount(),
                                                change.getInfo(), change.getDateAsCalendar()));
                                break;
                            case DELETE:
                                operation = cashBoxEntryOnlineDao.copyAsNonViewedOld(change.getId(), Calendar.getInstance())
                                        .andThen(cashBoxEntryOnlineDao.delete(change.getId()));
                                break;
                            default:
                                operation = null;
                        }
                        if (operation != null) {
                            receivedNotifications.put(change.getNotificationId(), change.getId()); // add to received notifications
                            operation = operation.andThen(Completable.fromAction(() -> {
                                // add notification id to shared preference, or delete from received if failed
                                if (!notificationsPreference.edit()
                                        .putLong(Long.toString(change.getNotificationId()), change.getId())
                                        .commit()) {
                                    receivedNotifications.remove(change.getNotificationId());
                                }
                            })).onErrorComplete(throwable -> {
                                // delete from received notifications
                                receivedNotifications.remove(change.getNotificationId());
                                LogUtil.error(TAG, "changes: ", throwable);
                                return true;
                            }).doOnComplete(() -> LogUtil.debug(TAG, "realizado: " + receivedNotifications.toString()));
                            completable = completable.andThen(operation);
                        }
                    }

                    return completable.andThen(confirmReceivedChanges());
                });
    }

    public Observable<Object> getChanges() {
        // network calls not on main thread
        return getCompletableChanges()
                .subscribeOn(Schedulers.io())
                .toObservable()
                .publish()
                .autoConnect();
    }

    private EntryOnline changeNotificationToEntry(UtilAppResponse.ChangesNotification change) {
        return new EntryOnline(change.getId(), change.getCashBoxId(), change.getAmount(), change.getInfo(),
                change.getDateAsCalendar(), Entry.NO_GROUP, Calendar.getInstance());
    }

    private Completable confirmReceivedChanges() {
        Set<Long> sentConfirmations = new HashSet<>();

        return Completable.fromAction(() -> {
            // first get the notifications to confirm
            for (String s : notificationsPreference.getAll().keySet())
                sentConfirmations.add(Long.parseLong(s));
        }).andThen(
                // send confirmations
                UTILAPP_API.confirmReceivedChanges(sentConfirmations)
                        .flatMap(new CheckResponseErrorFunction<>())
                        .flatMapCompletable(appResponse -> Completable.fromAction(() -> {
                            // Delete confirmed notifications
                            SharedPreferences.Editor editor = notificationsPreference.edit();
                            for (Long aLong : sentConfirmations) {
                                editor.remove(Long.toString(aLong));
                                receivedNotifications.remove(aLong);
                            }
                            editor.apply();
                        })).timeout(TIMEOUT_CHANGES, TimeUnit.SECONDS)
                        .retry(NUM_RETRIES_CHANGES, throwable ->
                                !(throwable instanceof UtilAppException.NoConnectivityException))
        );
    }

//    public Observable<Object> startupUpdate() {
//        return confirmReceivedChanges()
//                .andThen(getChanges());
//    }

    // View changes

    public Single<List<EntryOnline.EntryChanges>> getNonViewedEntries(long cashBoxId) {
        return cashBoxEntryOnlineDao.getNonViewedEntriesByCashBoxId(cashBoxId)
                .map(entryOnlines -> {
                    Map<Long, EntryOnline> entryMap = new HashMap<>();
                    TreeSet<EntryOnline.EntryChanges> entryChanges = new TreeSet<>();
                    EntryOnline entryObtained;

                    // Old and new
                    for (EntryOnline entry : entryOnlines) {
                        // Get old/new version
                        entryObtained = entryMap.remove(-entry.getId());
                        if (entryObtained == null) // no partner, so just store
                            entryMap.put(entry.getId(), entry);
                        else // group with partner
                            entryChanges.add(new EntryOnline.EntryChanges(entry, entryObtained));
                    }
                    // Only old or new (insert or delete)
                    entryMap.forEach((aLong, entryOnline) ->
                            entryChanges.add(new EntryOnline.EntryChanges(entryOnline)));
                    return new ArrayList<>(entryChanges);
                });
    }

    public Completable doViewedEntries(long cashBoxId) {
        return cashBoxEntryOnlineDao.deleteOldEntries(cashBoxId)
                .andThen(cashBoxEntryOnlineDao.setViewedAll(cashBoxId));
    }

    private static class CheckResponseErrorFunction<T extends UtilAppResponse>
            implements Function<T, SingleSource<T>> {
        @Override
        public SingleSource<T> apply(@NonNull T response) {
            if (response.isSuccessful())
                return Single.just(response);
            else
                return Single.error(new UtilAppException(response.getMessage()));
        }
    }
}
