package com.vibal.utilities.backgroundTasks;

import com.vibal.utilities.modelsNew.UtilAppResponse;

import io.reactivex.Single;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;

public interface UtilAppAPI {
    String USERNAME = "username";
    String CLIENT_ID = "ClientId";
    String PASSWORD_HEADER = "pwd";
    String PASSWORD = "prueba";

    @FormUrlEncoded
    @POST()
    Single<UtilAppResponse> signUp(@Field(USERNAME) String username);

    //todo finish


}
