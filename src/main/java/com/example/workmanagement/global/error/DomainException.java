package com.example.workmanagement.global.error;

public class DomainException extends RuntimeException {

    private final ApiErrorCode errorCode;

    protected DomainException(ApiErrorCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    public ApiErrorCode getErrorCode() {
        return errorCode;
    }
}

