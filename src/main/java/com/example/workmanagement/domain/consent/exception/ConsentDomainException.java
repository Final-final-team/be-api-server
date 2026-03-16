package com.example.workmanagement.domain.consent.exception;

import com.example.workmanagement.global.error.DomainException;

public class ConsentDomainException extends DomainException {

    public ConsentDomainException(ConsentErrorCode errorCode) {
        super(errorCode, errorCode.message());
    }

    public ConsentDomainException(ConsentErrorCode errorCode, String message) {
        super(errorCode, message);
    }
}
