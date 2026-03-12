package com.example.workmanagement.domain.review.error;

import com.example.workmanagement.global.error.ErrorCode;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

public enum ReviewErrorCode implements ErrorCode {
    ACTOR_HEADER_MISSING(HttpStatus.BAD_REQUEST, "X-Actor-Id 헤더는 필수입니다."),
    INVALID_ACTOR_ID_HEADER(HttpStatus.BAD_REQUEST, "X-Actor-Id 헤더는 숫자여야 합니다."),
    REVIEW_VALIDATION_ERROR(HttpStatus.BAD_REQUEST, "검토 요청 검증에 실패했습니다."),
    TASK_NOT_FOUND(HttpStatus.NOT_FOUND, "업무를 찾을 수 없습니다."),
    REVIEW_NOT_FOUND(HttpStatus.NOT_FOUND, "검토를 찾을 수 없습니다."),
    REVIEW_VIEW_FORBIDDEN(HttpStatus.FORBIDDEN, "검토를 조회할 권한이 없습니다."),
    REVIEW_REFERENCE_NOT_FOUND(HttpStatus.NOT_FOUND, "검토 참조자를 찾을 수 없습니다."),
    REVIEW_ATTACHMENT_NOT_FOUND(HttpStatus.NOT_FOUND, "검토 첨부를 찾을 수 없습니다."),
    REVIEW_COMMENT_NOT_FOUND(HttpStatus.NOT_FOUND, "검토 코멘트를 찾을 수 없습니다."),
    REVIEW_ADDITIONAL_REVIEWER_NOT_FOUND(HttpStatus.NOT_FOUND, "추가 검토자를 찾을 수 없습니다."),
    REVIEW_SUBMIT_NOT_ALLOWED(HttpStatus.CONFLICT, "현재 업무 상태에서는 검토를 상신할 수 없습니다."),
    REVIEW_UPDATE_NOT_ALLOWED(HttpStatus.CONFLICT, "현재 검토 상태에서는 본문을 수정할 수 없습니다."),
    REVIEW_APPROVAL_NOT_ALLOWED(HttpStatus.CONFLICT, "현재 검토 상태에서는 승인할 수 없습니다."),
    REVIEW_REJECTION_NOT_ALLOWED(HttpStatus.CONFLICT, "현재 검토 상태에서는 반려할 수 없습니다."),
    REVIEW_CANCEL_NOT_ALLOWED(HttpStatus.CONFLICT, "현재 검토 상태에서는 취소할 수 없습니다."),
    REVIEW_CONTENT_TOO_LONG(HttpStatus.BAD_REQUEST, "검토 본문 길이가 제한을 초과했습니다."),
    REVIEW_REJECTION_REASON_TOO_LONG(HttpStatus.BAD_REQUEST, "반려 사유 길이가 제한을 초과했습니다."),
    REFERENCE_ASSIGN_NOT_ALLOWED(HttpStatus.CONFLICT, "검토가 SUBMITTED 상태일 때만 참조자를 지정할 수 있습니다."),
    REFERENCE_UNASSIGN_NOT_ALLOWED(HttpStatus.CONFLICT, "검토가 SUBMITTED 상태일 때만 참조자를 해제할 수 있습니다."),
    ATTACHMENT_ADD_NOT_ALLOWED(HttpStatus.CONFLICT, "검토가 SUBMITTED 상태일 때만 첨부를 추가할 수 있습니다."),
    ATTACHMENT_REMOVE_NOT_ALLOWED(HttpStatus.CONFLICT, "검토가 SUBMITTED 상태일 때만 첨부를 삭제할 수 있습니다."),
    ADDITIONAL_REVIEWER_ASSIGN_NOT_ALLOWED(
            HttpStatus.CONFLICT,
            "검토가 SUBMITTED 상태일 때만 추가 검토자를 지정할 수 있습니다."
    ),
    ADDITIONAL_REVIEWER_UNASSIGN_NOT_ALLOWED(
            HttpStatus.CONFLICT,
            "검토가 SUBMITTED 상태일 때만 추가 검토자를 해제할 수 있습니다."
    ),
    COMMENT_CREATE_NOT_ALLOWED(HttpStatus.CONFLICT, "현재 검토 상태에서는 코멘트를 작성할 수 없습니다."),
    COMMENT_UPDATE_NOT_ALLOWED(HttpStatus.CONFLICT, "현재 검토 상태에서는 코멘트를 수정할 수 없습니다."),
    COMMENT_DELETE_NOT_ALLOWED(HttpStatus.CONFLICT, "현재 검토 상태에서는 코멘트를 삭제할 수 없습니다."),
    REVIEW_VERSION_CONFLICT(HttpStatus.CONFLICT, "검토 버전 충돌이 발생했습니다."),
    REJECTION_REASON_REQUIRED(HttpStatus.BAD_REQUEST, "반려 사유는 필수입니다."),
    REVIEW_COMMENT_TOO_LONG(HttpStatus.BAD_REQUEST, "코멘트 길이가 제한을 초과했습니다."),
    REVIEW_ALREADY_SUBMITTED_FOR_TASK_VERSION(
            HttpStatus.CONFLICT,
            "해당 업무에는 이미 제출된 검토가 존재합니다."
    ),
    REFERENCE_ALREADY_ASSIGNED(HttpStatus.CONFLICT, "이미 참조자로 지정된 사용자입니다."),
    ADDITIONAL_REVIEWER_ALREADY_ASSIGNED(HttpStatus.CONFLICT, "이미 추가 검토자로 지정된 사용자입니다."),
    REFERENCE_LIMIT_EXCEEDED(HttpStatus.BAD_REQUEST, "참조자 수가 최대 허용 개수를 초과했습니다."),
    ADDITIONAL_REVIEWER_LIMIT_EXCEEDED(HttpStatus.BAD_REQUEST, "추가 검토자 수가 최대 허용 개수를 초과했습니다."),
    ATTACHMENT_LIMIT_EXCEEDED(HttpStatus.BAD_REQUEST, "첨부 수가 최대 허용 개수를 초과했습니다."),
    ATTACHMENT_SIZE_EXCEEDED(HttpStatus.BAD_REQUEST, "첨부 파일 크기가 허용 범위를 초과했습니다."),
    ATTACHMENT_TOTAL_SIZE_EXCEEDED(HttpStatus.BAD_REQUEST, "전체 첨부 용량이 허용 범위를 초과했습니다."),
    ATTACHMENT_EXTENSION_NOT_ALLOWED(HttpStatus.BAD_REQUEST, "허용되지 않은 첨부 확장자입니다."),
    ATTACHMENT_CONTENT_TYPE_NOT_ALLOWED(HttpStatus.BAD_REQUEST, "허용되지 않은 첨부 콘텐츠 타입입니다."),
    REVIEW_SUBMIT_FORBIDDEN(HttpStatus.FORBIDDEN, "검토를 상신할 권한이 없습니다."),
    REVIEW_UPDATE_FORBIDDEN(HttpStatus.FORBIDDEN, "검토를 수정할 권한이 없습니다."),
    REVIEW_APPROVAL_FORBIDDEN(HttpStatus.FORBIDDEN, "검토를 승인할 권한이 없습니다."),
    REVIEW_REJECTION_FORBIDDEN(HttpStatus.FORBIDDEN, "검토를 반려할 권한이 없습니다."),
    REVIEW_CANCEL_FORBIDDEN(HttpStatus.FORBIDDEN, "검토를 취소할 권한이 없습니다."),
    REFERENCE_ASSIGN_FORBIDDEN(HttpStatus.FORBIDDEN, "참조자를 지정할 권한이 없습니다."),
    REFERENCE_UNASSIGN_FORBIDDEN(HttpStatus.FORBIDDEN, "참조자를 해제할 권한이 없습니다."),
    ATTACHMENT_ADD_FORBIDDEN(HttpStatus.FORBIDDEN, "첨부를 추가할 권한이 없습니다."),
    ATTACHMENT_REMOVE_FORBIDDEN(HttpStatus.FORBIDDEN, "첨부를 삭제할 권한이 없습니다."),
    ADDITIONAL_REVIEWER_ASSIGN_FORBIDDEN(
            HttpStatus.FORBIDDEN,
            "추가 검토자를 지정할 권한이 없습니다."
    ),
    ADDITIONAL_REVIEWER_UNASSIGN_FORBIDDEN(
            HttpStatus.FORBIDDEN,
            "추가 검토자를 해제할 권한이 없습니다."
    ),
    COMMENT_CREATE_FORBIDDEN(HttpStatus.FORBIDDEN, "코멘트를 작성할 권한이 없습니다."),
    COMMENT_UPDATE_FORBIDDEN(HttpStatus.FORBIDDEN, "코멘트를 수정할 권한이 없습니다."),
    COMMENT_DELETE_FORBIDDEN(HttpStatus.FORBIDDEN, "코멘트를 삭제할 권한이 없습니다."),
    REVIEW_INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "검토 처리 중 서버 오류가 발생했습니다.");

    private final HttpStatusCode httpStatus;
    private final String defaultMessage;

    ReviewErrorCode(HttpStatusCode httpStatus, String defaultMessage) {
        this.httpStatus = httpStatus;
        this.defaultMessage = defaultMessage;
    }

    @Override
    public HttpStatusCode httpStatus() {
        return httpStatus;
    }

    @Override
    public String code() {
        return name();
    }

    @Override
    public String message() {
        return defaultMessage;
    }
}
