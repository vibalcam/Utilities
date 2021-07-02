package com.vibal.utilities.persistence.retrofit;

import androidx.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.Collection;
import java.util.List;

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
    int REQ_CASHBOX_GET = 4;
    int REQ_PARTICIPANTS = 5;
    int ID_ALL = -1;

    // Operation codes
    int UPDATE_PARTICIPANT = 12;
    int INSERT_PARTICIPANT = 11;
    int DELETE_PARTICIPANT = -11;
    int DELETE = -1;
    int CASHBOX_INV = 0;
    int INSERT = 1;
    int UPDATE = 2;
    int CASHBOX_RELOAD = 3;

    @IntDef({UPDATE_PARTICIPANT,
            INSERT_PARTICIPANT,
            DELETE_PARTICIPANT,
            DELETE,
            CASHBOX_INV,
            INSERT,
            UPDATE,
            CASHBOX_RELOAD})
    @Retention(RetentionPolicy.SOURCE)
    @interface OperationCode {
    }

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
    String NAME = "name";
    String ENTRY_ID = "eid";
    String IS_FROM = "if";

    // Authentication
    String CLIENT_ID = "ClientId";
    String PASSWORD_HEADER = "pwd";
    String VERSION_HEADER = "clientV";
    String VERSION = "2";

    // Response codes
//    int NON_EXISTENT_WARNING = -2;

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
    Single<UtilAppResponse.EntriesResponse> reloadCashBox(@Body UtilAppRequest request);

    @Headers(REQ_CODE + REQ_CASHBOXES)
    @POST(REQ_URL)
    Single<UtilAppResponse.ModificationResponse> sendInvitation(@Body UtilAppRequest.InvitationRequest request);

//    @Headers(REQ_CODE+REQ_CASHBOXES)
//    @POST("utilApp.php")
//    Single<UtilAppResponse.ModificationResponse> delete(@Body UtilAppRequest request);

    @Headers(REQ_CODE + REQ_ENTRIES)
    @POST(REQ_URL)
    Single<UtilAppResponse.ModificationResponse> operationEntry(@Body UtilAppRequest.EntryInfoRequest request);

    @Headers(REQ_CODE + REQ_ENTRIES)
    @POST(REQ_URL)
    Single<UtilAppResponse.ModificationResponse> deleteEntry(@Body UtilAppRequest request);

    @Headers(REQ_CODE + REQ_PARTICIPANTS)
    @POST(REQ_URL)
    Single<UtilAppResponse.ModificationResponse> operationParticipant(@Body UtilAppRequest.ParticipantRequest request);

    @Headers(REQ_CODE + REQ_PARTICIPANTS)
    @POST(REQ_URL)
    Single<UtilAppResponse.ModificationResponse> operationParticipant(@Body List<UtilAppRequest.ParticipantRequest> requests);

    @Headers(REQ_CODE + REQ_CHANGES_GET)
    @POST(REQ_URL)
    Single<UtilAppResponse.ChangesResponse> getChanges();

    @Headers(REQ_CODE + REQ_CHANGES_RCV)
    @POST(REQ_URL)
    Single<UtilAppResponse> confirmReceivedChanges(@Body Collection<Long> notificationIds);

    @Headers(REQ_CODE + REQ_CASHBOX_GET)
    @POST(REQ_URL)
    Single<UtilAppResponse.ListResponse<String>> getCashBoxParticipants(@Body long id);

    @Headers(REQ_CODE + REQ_DELETE_USER)
    @POST(REQ_URL)
    Single<UtilAppResponse> deleteUser();
}
