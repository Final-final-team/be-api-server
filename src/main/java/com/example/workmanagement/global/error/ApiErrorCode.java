package com.example.workmanagement.global.error;

import org.springframework.http.HttpStatus;

public enum ApiErrorCode {
    NOT_IMPLEMENTED(HttpStatus.NOT_IMPLEMENTED, "Scaffold created, but the behavior is not implemented yet."),
    VALIDATION_ERROR(HttpStatus.BAD_REQUEST, "Request validation failed."),
    TASK_NOT_FOUND(HttpStatus.NOT_FOUND, "Task was not found."),
    REVIEW_NOT_FOUND(HttpStatus.NOT_FOUND, "Review was not found."),
    INVALID_TASK_STATUS_FOR_REVIEW(HttpStatus.CONFLICT, "Task must be IN_REVIEW before a review can be submitted."),
    REVIEW_STATUS_CONFLICT(HttpStatus.CONFLICT, "Review status does not allow this action."),
    REVIEW_VERSION_CONFLICT(HttpStatus.CONFLICT, "Review version conflict detected."),
    TASK_VERSION_CONFLICT(HttpStatus.CONFLICT, "Task version conflict detected."),
    REJECTION_REASON_REQUIRED(HttpStatus.BAD_REQUEST, "Rejection reason is required."),
    REVIEW_MUTATION_FORBIDDEN(HttpStatus.CONFLICT, "Review content is locked in the current state."),
    REFERENCE_MUTATION_FORBIDDEN(HttpStatus.CONFLICT, "References can only be changed while the review is SUBMITTED."),
    ATTACHMENT_MUTATION_FORBIDDEN(HttpStatus.CONFLICT, "Attachments can only be changed while the review is SUBMITTED."),
    COMMENT_MUTATION_FORBIDDEN(HttpStatus.CONFLICT, "Comments cannot be changed in the current review state."),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "Unexpected server error.");

    private final HttpStatus httpStatus;
    private final String defaultMessage;

    ApiErrorCode(HttpStatus httpStatus, String defaultMessage) {
        this.httpStatus = httpStatus;
        this.defaultMessage = defaultMessage;
    }

    /**
     * 에러 코드에 대응하는 HTTP 상태를 반환한다.
     */
    public HttpStatus getHttpStatus() {
        return httpStatus;
    }

    /**
     * 에러 코드의 기본 메시지를 반환한다.
     */
    public String getDefaultMessage() {
        return defaultMessage;
    }
}
