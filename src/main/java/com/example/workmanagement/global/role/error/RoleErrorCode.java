package com.example.workmanagement.global.role.error;

import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import com.example.workmanagement.global.error.ErrorCode;

public enum RoleErrorCode implements ErrorCode {

    ROLE_NOT_FOUND(HttpStatus.NOT_FOUND, "ROLE_NOT_FOUND", "역할을 찾을 수 없습니다"),
    ROLE_ALREADY_EXISTS(HttpStatus.CONFLICT, "ROLE_ALREADY_EXISTS", "이미 존재하는 역할 코드입니다"),
    SYSTEM_ROLE_IMMUTABLE(HttpStatus.BAD_REQUEST, "SYSTEM_ROLE_IMMUTABLE", "시스템 역할은 수정/삭제할 수 없습니다"),
    ROLE_IN_USE(HttpStatus.CONFLICT, "ROLE_IN_USE", "현재 사용 중인 역할은 삭제할 수 없습니다"),
    LAST_LEADER_CANNOT_BE_REMOVED(HttpStatus.BAD_REQUEST, "LAST_LEADER_CANNOT_BE_REMOVED", "마지막 리더 역할은 제거할 수 없습니다"),
    ROLE_ACCESS_DENIED(HttpStatus.FORBIDDEN, "ROLE_ACCESS_DENIED", "역할 관리 권한이 없습니다")

    ;

    private HttpStatusCode httpStatus;
    private String code;
    private String message;

    RoleErrorCode(HttpStatusCode httpStatus, String code, String message) {
        this.httpStatus = httpStatus;
        this.code = code;
        this.message = message;
    }

    @Override
    public HttpStatusCode httpStatus() {
        return httpStatus;
    }

    @Override
    public String code() {
        return code;
    }

    @Override
    public String message() {
        return message;
    }
}
