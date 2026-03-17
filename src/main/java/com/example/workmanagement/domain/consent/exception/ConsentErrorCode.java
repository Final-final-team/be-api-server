package com.example.workmanagement.domain.consent.exception;

import com.example.workmanagement.global.error.ErrorCode;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

public enum ConsentErrorCode implements ErrorCode {
    CONSENT_INVALID_ARGUMENT(HttpStatus.BAD_REQUEST, "동의 도메인 입력값이 유효하지 않습니다."),
    CONSENT_REQUIRED(HttpStatus.FORBIDDEN, "필수 동의가 완료되지 않아 서비스 접근이 차단되었습니다."),
    CONSENT_ITEM_NOT_FOUND(HttpStatus.NOT_FOUND, "동의 항목을 찾을 수 없습니다."),
    CONSENT_NOT_LATEST_VERSION(HttpStatus.CONFLICT, "최신 버전 동의 항목에만 동의할 수 있습니다."),
    CONSENT_DISAGREE_NOT_ALLOWED(HttpStatus.BAD_REQUEST, "MVP에서는 동의 제출 시 동의(agree=true)만 허용합니다.");

    private final HttpStatusCode httpStatus;
    private final String defaultMessage;

    ConsentErrorCode(HttpStatusCode httpStatus, String defaultMessage) {
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
