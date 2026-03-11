package com.example.workmanagement.global.error;

import org.springframework.http.HttpStatusCode;

public interface ApiErrorCode {
    HttpStatusCode httpStatusCode();
    String code();
    String message();
}
