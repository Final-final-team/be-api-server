package com.example.workmanagement.domain.project.error;

import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import com.example.workmanagement.global.error.ErrorCode;

public enum ProjectErrorCode implements ErrorCode {

    PROJECT_NOT_FOUND(HttpStatus.NOT_FOUND, "PROJECT_NOT_FOUND", "프로젝트를 찾을 수 없습니다"),
    PROJECT_ACCESS_DENIED(HttpStatus.FORBIDDEN, "PROJECT_ACCESS_DENIED", "프로젝트에 접근할 권한이 없습니다"),
    PROJECT_ALREADY_ARCHIVED(HttpStatus.CONFLICT, "PROJECT_ALREADY_ARCHIVED", "이미 아카이브된 프로젝트입니다"),
    PROJECT_SYSTEM_ROLE_BOOTSTRAP_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "PROJECT_SYSTEM_ROLE_BOOTSTRAP_FAILED", "프로젝트 기본 역할 초기화에 실패했습니다"),

    PROJECT_MEMBER_NOT_FOUND(HttpStatus.NOT_FOUND, "PROJECT_MEMBER_NOT_FOUND", "프로젝트 멤버를 찾을 수 없습니다"),
    PROJECT_MEMBER_ALREADY_EXISTS(HttpStatus.CONFLICT, "PROJECT_MEMBER_ALREADY_EXISTS", "이미 프로젝트 소속된 멤버입니다"),
    PROJECT_MEMBER_ALREADY_INACTIVE(HttpStatus.BAD_REQUEST, "PROJECT_MEMBER_ALREADY_INACTIVE", "이미 탈퇴된 멤버입니다")

    ;

    private HttpStatusCode httpStatus;
    private String code;
    private String message;

    ProjectErrorCode(HttpStatusCode httpStatus, String code, String message) {
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
