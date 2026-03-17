package com.example.workmanagement.domain.user.exception;

import com.example.workmanagement.global.error.ErrorCode;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

public enum UserErrorCode implements ErrorCode {
    USER_UNAUTHENTICATED(HttpStatus.UNAUTHORIZED, "인증 정보가 없습니다."),
    USER_INVALID_AUTH_SUBJECT(HttpStatus.UNAUTHORIZED, "인증 주체(subject)가 유효하지 않습니다."),
    USER_NOT_FOUND(HttpStatus.UNAUTHORIZED, "회원을 찾을 수 없습니다."),
    USER_REFRESH_TOKEN_MISSING(HttpStatus.UNAUTHORIZED, "리프레시 토큰이 없습니다."),
    USER_REFRESH_TOKEN_INVALID(HttpStatus.UNAUTHORIZED, "리프레시 토큰이 유효하지 않습니다."),
    USER_REFRESH_TOKEN_EXPIRED(HttpStatus.UNAUTHORIZED, "리프레시 토큰이 만료되었습니다."),
    USER_INVALID_ARGUMENT(HttpStatus.BAD_REQUEST, "회원/인증 도메인 입력값이 유효하지 않습니다."),
    USER_INVALID_OIDC_CLAIMS(HttpStatus.UNAUTHORIZED, "OIDC 인증 클레임이 유효하지 않습니다."),
    USER_EMAIL_NOT_VERIFIED_FOR_AUTO_LINK(HttpStatus.CONFLICT, "이메일 미검증 계정은 자동 연동할 수 없습니다."),
    USER_SOCIAL_ACCOUNT_USER_MISMATCH(HttpStatus.CONFLICT, "소셜 계정의 사용자 매핑과 이메일 사용자 매핑이 일치하지 않습니다."),
    USER_SOCIAL_ACCOUNT_ORPHANED(HttpStatus.CONFLICT, "소셜 계정과 회원 매핑 상태가 비정상입니다.");

    private final HttpStatusCode httpStatus;
    private final String defaultMessage;

    UserErrorCode(HttpStatusCode httpStatus, String defaultMessage) {
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
