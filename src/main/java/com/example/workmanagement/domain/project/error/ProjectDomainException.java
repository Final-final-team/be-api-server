package com.example.workmanagement.domain.project.error;

import com.example.workmanagement.global.error.DomainException;

public class ProjectDomainException extends DomainException {

    public ProjectDomainException(ProjectErrorCode errorCode) {
        super(errorCode, errorCode.message());
    }

    public ProjectDomainException(ProjectErrorCode errorCode, String message) {
        super(errorCode, message);
    }
}
