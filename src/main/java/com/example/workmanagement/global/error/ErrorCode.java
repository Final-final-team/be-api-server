package com.example.workmanagement.global.error;

import org.springframework.http.HttpStatusCode;

public interface ErrorCode {

    HttpStatusCode httpStatus();

    String code();
    
    String message();
}
