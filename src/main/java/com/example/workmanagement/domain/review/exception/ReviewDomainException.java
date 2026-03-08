package com.example.workmanagement.domain.review.exception;

import com.example.workmanagement.global.error.ApiErrorCode;
import com.example.workmanagement.global.error.ApiException;

public class ReviewDomainException extends ApiException {

    public ReviewDomainException(ApiErrorCode errorCode, String message) {
        super(errorCode, message);
    }
}

