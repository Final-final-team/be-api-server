package com.example.workmanagement.global.error;

import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

public enum CommonErrorCode implements ApiErrorCode {

    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "C-0001", "내부 서버 에러")
    ;

    private HttpStatusCode httpStatusCode;
    private String code;
    private String message;

    CommonErrorCode(HttpStatusCode httpStatusCode, String code, String message) {
        this.httpStatusCode = httpStatusCode;
        this.code = code;
        this.message = message;
    }


    @Override
    public HttpStatusCode httpStatusCode() {
        return httpStatusCode;
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
