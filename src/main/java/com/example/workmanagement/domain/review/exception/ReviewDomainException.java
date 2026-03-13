package com.example.workmanagement.domain.review.exception;

import com.example.workmanagement.domain.review.error.ReviewErrorCode;
import com.example.workmanagement.global.error.DomainException;

public class ReviewDomainException extends DomainException {

    public ReviewDomainException(ReviewErrorCode errorCode) {
        super(errorCode, errorCode.message());
    }

    public ReviewDomainException(ReviewErrorCode errorCode, String message) {
        super(errorCode, message);
    }
}
