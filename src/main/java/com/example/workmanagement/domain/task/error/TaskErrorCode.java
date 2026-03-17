package com.example.workmanagement.domain.task.error;

import com.example.workmanagement.global.error.ErrorCode;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

public enum TaskErrorCode implements ErrorCode {
    TASK_NOT_FOUND(HttpStatus.NOT_FOUND, "업무를 찾을 수 없습니다."),

    TASK_INVALID_ARGUMENT(HttpStatus.BAD_REQUEST, "업무 요청값이 유효하지 않습니다."),
    TASK_TITLE_REQUIRED(HttpStatus.BAD_REQUEST, "업무 제목은 필수입니다."),
    TASK_TITLE_LENGTH_OUT_OF_RANGE(HttpStatus.BAD_REQUEST, "업무 제목 길이는 2자 이상 120자 이하여야 합니다."),
    TASK_DESCRIPTION_LENGTH_EXCEEDED(HttpStatus.BAD_REQUEST, "업무 설명 길이가 제한을 초과했습니다."),
    TASK_DATE_RANGE_INVALID(HttpStatus.BAD_REQUEST, "업무 시작일은 마감일보다 늦을 수 없습니다."),
    TASK_PRIORITY_INVALID(HttpStatus.BAD_REQUEST, "업무 우선순위 값이 유효하지 않습니다."),
    TASK_STATUS_INVALID(HttpStatus.BAD_REQUEST, "업무 상태 값이 유효하지 않습니다."),
    TASK_ACTOR_IS_TARGET(HttpStatus.BAD_REQUEST, "할당 요청에서 대상 회원이 자기자신일 수 없습니다."),

    TASK_STATUS_TRANSITION_NOT_ALLOWED(HttpStatus.CONFLICT, "현재 상태에서는 요청한 상태 전이가 허용되지 않습니다."),
    TASK_UPDATE_NOT_ALLOWED(HttpStatus.CONFLICT, "현재 상태에서는 업무를 수정할 수 없습니다."),
    TASK_ASSIGNMENT_NOT_ALLOWED(HttpStatus.CONFLICT, "현재 상태에서는 담당자 할당 또는 해제가 허용되지 않습니다."),
    TASK_ALREADY_ASSIGNED(HttpStatus.CONFLICT, "이미 담당자로 지정된 사용자입니다."),
    TASK_ASSIGNEE_NOT_ASSIGNED(HttpStatus.CONFLICT, "현재 담당자가 아닌 사용자는 해제할 수 없습니다."),

    TASK_PROJECT_MEMBERSHIP_REQUIRED(HttpStatus.FORBIDDEN, "프로젝트 소속 사용자만 업무에 접근할 수 있습니다."),
    TASK_CREATE_FORBIDDEN(HttpStatus.FORBIDDEN, "업무를 생성할 권한이 없습니다."),
    TASK_UPDATE_FORBIDDEN(HttpStatus.FORBIDDEN, "업무를 수정할 권한이 없습니다."),
    TASK_ASSIGN_FORBIDDEN(HttpStatus.FORBIDDEN, "업무 할당 또는 해제 권한이 없습니다."),
    TASK_START_FORBIDDEN(HttpStatus.FORBIDDEN, "업무를 시작할 권한이 없습니다."),
    TASK_FORCE_COMPLETE_FORBIDDEN(HttpStatus.FORBIDDEN, "업무를 강제 완료할 권한이 없습니다."),

    TASK_INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "업무 처리 중 서버 오류가 발생했습니다.");

    private final HttpStatusCode httpStatus;
    private final String defaultMessage;

    TaskErrorCode(HttpStatusCode httpStatus, String defaultMessage) {
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
