package com.example.workmanagement.domain.user.exception;

import com.example.workmanagement.global.error.DomainException;

public class UserDomainException extends DomainException {

    public UserDomainException(UserErrorCode errorCode) {
        super(errorCode, errorCode.message());
    }

    public UserDomainException(UserErrorCode errorCode, String message) {
        super(errorCode, message);
    }
}
