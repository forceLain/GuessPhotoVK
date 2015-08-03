package com.forcelain.android.guessphotovk.api;

public class ApiException extends RuntimeException {
    public static final int ERROR_CODE_UNKNOWN = -1;
    private final int errorCode;
    private final String errorMessage;

    public ApiException(int errorCode, String errorMessage) {
        super(errorMessage);
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
