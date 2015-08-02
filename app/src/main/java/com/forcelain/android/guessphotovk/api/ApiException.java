package com.forcelain.android.guessphotovk.api;

public class ApiException extends RuntimeException {
    private final int errorCode;
    private final String errorMessage;

    public ApiException(int errorCode, String errorMessage) {
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
    }

    public int getErrorCode() {
        return errorCode;
    }

    public String getErrorMessage() {
        return errorMessage;
    }
}
