package com.example.workmanagement.global.error;

public abstract class ApiException extends RuntimeException {

    private final ApiErrorCode errorCode;

    protected ApiException(ApiErrorCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    public ApiErrorCode getErrorCode() {
        return errorCode;
    }
}

