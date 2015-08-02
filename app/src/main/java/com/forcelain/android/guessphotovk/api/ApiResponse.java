package com.forcelain.android.guessphotovk.api;

import com.google.gson.annotations.SerializedName;

public class ApiResponse<T> {
    public T response;
    public ApiError error;

    public static class ApiError {

        @SerializedName("error_code")
        public int errorCode;

        @SerializedName("error_msg")
        public String errorMsg;
    }
}
