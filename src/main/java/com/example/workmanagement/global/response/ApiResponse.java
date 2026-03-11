package com.example.workmanagement.global.response;

import com.example.workmanagement.global.error.ErrorCode;

public record ApiResponse<T>(
        T data,
        ErrorInfo errorInfo
) {

    public record ErrorInfo(
            String code,
            String message
    ) {
    }

    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(data, null);
    }

    public static <T> ApiResponse<T> noContent() {
        return new ApiResponse<>(null, null);
    }

    public static ApiResponse<Void> error(String code, String message) {
        return new ApiResponse<>(null, new ErrorInfo(code, message));
    }

    public static ApiResponse<Void> error(ErrorCode errorCode) {
        return new ApiResponse<>(null, new ErrorInfo(errorCode.code(), errorCode.message()));
    }
}
