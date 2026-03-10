package com.example.workmanagement.global.error;

import com.example.workmanagement.global.response.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import java.time.Instant;
import java.util.stream.Collectors;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 도메인 예외를 표준 에러 응답으로 변환한다.
     */
    @ExceptionHandler(ApiException.class)
    public ResponseEntity<ErrorResponse> handleApiException(ApiException exception, HttpServletRequest request) {
        ApiErrorCode errorCode = exception.getErrorCode();
        return ResponseEntity.status(errorCode.getHttpStatus())
                .body(new ErrorResponse(
                        errorCode.name(),
                        exception.getMessage(),
                        Instant.now(),
                        request.getRequestURI()
                ));
    }

    /**
     * 검증 실패를 요청 오류 응답으로 변환한다.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(
            MethodArgumentNotValidException exception,
            HttpServletRequest request
    ) {
        String message = exception.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.joining(", "));

        return ResponseEntity.status(ApiErrorCode.VALIDATION_ERROR.getHttpStatus())
                .body(new ErrorResponse(
                        ApiErrorCode.VALIDATION_ERROR.name(),
                        message,
                        Instant.now(),
                        request.getRequestURI()
                ));
    }

    /**
     * 애플리케이션 내부 검증 실패를 요청 오류 응답으로 변환한다.
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgumentException(
            IllegalArgumentException exception,
            HttpServletRequest request
    ) {
        return ResponseEntity.status(ApiErrorCode.VALIDATION_ERROR.getHttpStatus())
                .body(new ErrorResponse(
                        ApiErrorCode.VALIDATION_ERROR.name(),
                        exception.getMessage(),
                        Instant.now(),
                        request.getRequestURI()
                ));
    }

    /**
     * 처리되지 않은 예외를 공통 서버 오류 응답으로 변환한다.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleUnexpectedException(Exception exception, HttpServletRequest request) {
        return ResponseEntity.status(ApiErrorCode.INTERNAL_SERVER_ERROR.getHttpStatus())
                .body(new ErrorResponse(
                        ApiErrorCode.INTERNAL_SERVER_ERROR.name(),
                        exception.getMessage(),
                        Instant.now(),
                        request.getRequestURI()
                ));
    }
}
