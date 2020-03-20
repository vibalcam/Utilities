package com.vibal.utilities.modelsNew;

import com.google.gson.annotations.SerializedName;

public class UtilAppResponse {
    @SerializedName("success")
    private int success;
    @SerializedName("message")
    private String message;

    public int getSuccess() {
        return success;
    }

    public void setSuccess(int success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
