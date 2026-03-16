package com.example.workmanagement.global.role.error;

import com.example.workmanagement.global.error.DomainException;

public class RoleDomainException extends DomainException {

    public RoleDomainException(RoleErrorCode errorCode) {
        super(errorCode, errorCode.message());
    }

    public RoleDomainException(RoleErrorCode errorCode, String message) {
        super(errorCode, message);
    }
}
