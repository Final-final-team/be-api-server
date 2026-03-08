package com.example.workmanagement.global.error;

import org.springframework.http.HttpStatus;

public enum ApiErrorCode {
    NOT_IMPLEMENTED(HttpStatus.NOT_IMPLEMENTED, "Scaffold created, but the behavior is not implemented yet."),
    VALIDATION_ERROR(HttpStatus.BAD_REQUEST, "Request validation failed."),
    ACTOR_HEADER_MISSING(HttpStatus.BAD_REQUEST, "X-Actor-Id header is required."),
    TASK_NOT_FOUND(HttpStatus.NOT_FOUND, "Task was not found."),
    REVIEW_NOT_FOUND(HttpStatus.NOT_FOUND, "Review was not found."),
    REVIEW_REFERENCE_NOT_FOUND(HttpStatus.NOT_FOUND, "Review reference was not found."),
    REVIEW_ATTACHMENT_NOT_FOUND(HttpStatus.NOT_FOUND, "Review attachment was not found."),
    REVIEW_COMMENT_NOT_FOUND(HttpStatus.NOT_FOUND, "Review comment was not found."),
    REVIEW_ADDITIONAL_REVIEWER_NOT_FOUND(HttpStatus.NOT_FOUND, "Review additional reviewer was not found."),
    REVIEW_SUBMIT_NOT_ALLOWED(HttpStatus.CONFLICT, "Task is not in a state that allows review submission."),
    REVIEW_RESUBMISSION_NOT_ALLOWED(HttpStatus.CONFLICT, "Review cannot be resubmitted in the current state."),
    REVIEW_UPDATE_NOT_ALLOWED(HttpStatus.CONFLICT, "Review content cannot be updated in the current state."),
    REVIEW_APPROVAL_NOT_ALLOWED(HttpStatus.CONFLICT, "Review cannot be approved in the current state."),
    REVIEW_REJECTION_NOT_ALLOWED(HttpStatus.CONFLICT, "Review cannot be rejected in the current state."),
    REVIEW_CANCEL_NOT_ALLOWED(HttpStatus.CONFLICT, "Review cannot be cancelled in the current state."),
    REFERENCE_ASSIGN_NOT_ALLOWED(HttpStatus.CONFLICT, "References can only be assigned while the review is SUBMITTED."),
    REFERENCE_UNASSIGN_NOT_ALLOWED(HttpStatus.CONFLICT, "References can only be removed while the review is SUBMITTED."),
    ATTACHMENT_ADD_NOT_ALLOWED(HttpStatus.CONFLICT, "Attachments can only be added while the review is SUBMITTED."),
    ATTACHMENT_REMOVE_NOT_ALLOWED(HttpStatus.CONFLICT, "Attachments can only be removed while the review is SUBMITTED."),
    ADDITIONAL_REVIEWER_ASSIGN_NOT_ALLOWED(
            HttpStatus.CONFLICT,
            "Additional reviewers can only be assigned while the review is SUBMITTED."
    ),
    ADDITIONAL_REVIEWER_UNASSIGN_NOT_ALLOWED(
            HttpStatus.CONFLICT,
            "Additional reviewers can only be removed while the review is SUBMITTED."
    ),
    COMMENT_CREATE_NOT_ALLOWED(HttpStatus.CONFLICT, "Comments cannot be created in the current review state."),
    COMMENT_UPDATE_NOT_ALLOWED(HttpStatus.CONFLICT, "Comments cannot be updated in the current review state."),
    COMMENT_DELETE_NOT_ALLOWED(HttpStatus.CONFLICT, "Comments cannot be deleted in the current review state."),
    REVIEW_VERSION_CONFLICT(HttpStatus.CONFLICT, "Review version conflict detected."),
    TASK_VERSION_CONFLICT(HttpStatus.CONFLICT, "Task version conflict detected."),
    REJECTION_REASON_REQUIRED(HttpStatus.BAD_REQUEST, "Rejection reason is required."),
    REVIEW_ALREADY_SUBMITTED_FOR_TASK_VERSION(
            HttpStatus.CONFLICT,
            "A submitted review already exists for the current task version."
    ),
    REFERENCE_ALREADY_ASSIGNED(HttpStatus.CONFLICT, "The user is already assigned as a reference."),
    ADDITIONAL_REVIEWER_ALREADY_ASSIGNED(HttpStatus.CONFLICT, "The user is already assigned as an additional reviewer."),
    REVIEW_SUBMIT_FORBIDDEN(HttpStatus.FORBIDDEN, "The actor is not allowed to submit a review."),
    REVIEW_UPDATE_FORBIDDEN(HttpStatus.FORBIDDEN, "The actor is not allowed to update the review."),
    REVIEW_APPROVAL_FORBIDDEN(HttpStatus.FORBIDDEN, "The actor is not allowed to approve the review."),
    REVIEW_REJECTION_FORBIDDEN(HttpStatus.FORBIDDEN, "The actor is not allowed to reject the review."),
    REVIEW_CANCEL_FORBIDDEN(HttpStatus.FORBIDDEN, "The actor is not allowed to cancel the review."),
    REFERENCE_ASSIGN_FORBIDDEN(HttpStatus.FORBIDDEN, "The actor is not allowed to assign references."),
    REFERENCE_UNASSIGN_FORBIDDEN(HttpStatus.FORBIDDEN, "The actor is not allowed to remove references."),
    ATTACHMENT_ADD_FORBIDDEN(HttpStatus.FORBIDDEN, "The actor is not allowed to add attachments."),
    ATTACHMENT_REMOVE_FORBIDDEN(HttpStatus.FORBIDDEN, "The actor is not allowed to remove attachments."),
    ADDITIONAL_REVIEWER_ASSIGN_FORBIDDEN(
            HttpStatus.FORBIDDEN,
            "The actor is not allowed to assign additional reviewers."
    ),
    ADDITIONAL_REVIEWER_UNASSIGN_FORBIDDEN(
            HttpStatus.FORBIDDEN,
            "The actor is not allowed to remove additional reviewers."
    ),
    COMMENT_CREATE_FORBIDDEN(HttpStatus.FORBIDDEN, "The actor is not allowed to create comments."),
    COMMENT_UPDATE_FORBIDDEN(HttpStatus.FORBIDDEN, "The actor is not allowed to update comments."),
    COMMENT_DELETE_FORBIDDEN(HttpStatus.FORBIDDEN, "The actor is not allowed to delete comments."),
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
