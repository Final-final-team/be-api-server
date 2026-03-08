package com.example.workmanagement.global.error;

public class NotYetImplementedException extends ApiException {

    public NotYetImplementedException(String message) {
        super(ApiErrorCode.NOT_IMPLEMENTED, message);
    }
}

