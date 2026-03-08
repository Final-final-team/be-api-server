package com.example.workmanagement.global.response;

import java.time.Instant;

public record ApiResponse<T>(
        boolean success,
        String code,
        String message,
        T data,
        Instant timestamp
) {
    /**
     * 데이터가 포함된 성공 응답을 생성한다.
     */
    public static <T> ApiResponse<T> success(String message, T data) {
        return new ApiResponse<>(true, "SUCCESS", message, data, Instant.now());
    }

    /**
     * 데이터가 없는 성공 응답을 생성한다.
     */
    public static ApiResponse<Void> success(String message) {
        return new ApiResponse<>(true, "SUCCESS", message, null, Instant.now());
    }
}
