package com.example.workmanagement.global.error;

import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

public enum CommonErrorCode implements ErrorCode {

    UNEXPECTED_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "UNEXPECTED_SERVER_ERROR", "예기치 못한 서버 에러"),
    UNEXPECTED_DATABASE_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "UNEXPECTED_DATABASE_EXCEPTION", "예기치 못한 서버 데이터베이스 에러"),
    UNSUPPORTED_HTTP_METHOD(HttpStatus.BAD_REQUEST, "UNSUPPORTED_HTTP_METHOD", "지원되지 않은 HTTP 메서드"),
    INVALID_ARGUMENT_ERROR(HttpStatus.BAD_REQUEST, "INVALID_ARGUMENT", "유효하지 않은 인자(공통 예외)"),
    MISSING_HTTP_HEADER(HttpStatus.BAD_REQUEST, "MISSING_HTTP_HEADER", "필수 HTTP 요청 헤더 누락"),
    SERVLET_EXCEPTION(HttpStatus.BAD_REQUEST, "SERVLET_EXCEPTION", "예기치 못한 서블릿 예외 발생(대개 지원하지 않는 방식으로 요청한 경우 발생합니다.)"),
    NO_STATIC_RESOURCE(HttpStatus.NOT_FOUND, "NO_STATIC_RESOURCE", "정적 자원 없음"),
    // ... 필요할 때 추가
    ;

    private HttpStatusCode httpStatus;
    private String code;
    private String message;

    CommonErrorCode(HttpStatusCode httpStatus, String code, String message) {
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
