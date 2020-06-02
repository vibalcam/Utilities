package com.vibal.utilities.persistence.retrofit;

import java.util.Collection;

import io.reactivex.Single;
import retrofit2.http.Body;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface UtilAppAPI {
    // Request codes
    String REQ_URL = "utilApp.php";
    String REQ_CODE = "reqCode: ";
    int REQ_DELETE_USER = -1;
    int REQ_CASHBOXES = 0;
    int REQ_ENTRIES = 1;
    int REQ_CHANGES_GET = 2;
    int REQ_CHANGES_RCV = 3;
    int ID_ALL = -1;

    // Operation codes
    int DELETE = -1;
    int CASHBOX_INV = 0;
    int INSERT = 1;
    int UPDATE = 2;

    // Variables
    String NOTIFICATION_ID = "nid";
    String USERNAME = "userName";
    String OP_CODE = "opCode";
    String AMOUNT = "amount";
    String ID = "id";
    String INVITATION = "inv";
    String INFO = "info";
    String DATE = "date";
    String CASHBOX_ID = "cashBoxId";

    // Authentication
    String CLIENT_ID = "ClientId";
    String PASSWORD_HEADER = "pwd";

    // Other constants
    int MAX_LENGTH_USERNAME = 15;

    @FormUrlEncoded
    @POST(REQ_URL)
    Single<UtilAppResponse> signUp(@Field(USERNAME) String username);

    //    @FormUrlEncoded
    @Headers(REQ_CODE + REQ_CASHBOXES)
    @POST(REQ_URL)
    Single<UtilAppResponse.ModificationResponse> operationCashbox(@Body UtilAppRequest request);
//    Single<UtilAppResponse.ModificationResponse> createCashbox(@Field("cashBoxes") UtilAppRequest request);

    @Headers(REQ_CODE + REQ_CASHBOXES)
    @POST(REQ_URL)
    Single<UtilAppResponse.EntriesResponse> acceptInvitation(@Body UtilAppRequest request);

    @Headers(REQ_CODE + REQ_CASHBOXES)
    @POST(REQ_URL)
    Single<UtilAppResponse.ModificationResponse> sendInvitation(@Body UtilAppRequest.InvitationRequest request);

//    @Headers(REQ_CODE+REQ_CASHBOXES)
//    @POST("utilApp.php")
//    Single<UtilAppResponse.ModificationResponse> delete(@Body UtilAppRequest request);

    @Headers(REQ_CODE + REQ_ENTRIES)
    @POST(REQ_URL)
    Single<UtilAppResponse.ModificationResponse> operationEntry(@Body UtilAppRequest.EntryRequest request);

    @Headers(REQ_CODE + REQ_ENTRIES)
    @POST(REQ_URL)
    Single<UtilAppResponse.ModificationResponse> deleteEntry(@Body UtilAppRequest request);

    @Headers(REQ_CODE + REQ_CHANGES_GET)
    @POST(REQ_URL)
    Single<UtilAppResponse.ChangesResponse> getChanges();

    @Headers(REQ_CODE + REQ_CHANGES_RCV)
    @POST(REQ_URL)
    Single<UtilAppResponse> confirmReceivedChanges(@Body Collection<Long> notificationIds);

    @Headers(REQ_CODE + REQ_DELETE_USER)
    @POST(REQ_URL)
    Single<UtilAppResponse> deleteUser();
}
