package com.example.workmanagement.global.error;

import com.example.workmanagement.global.response.ApiResponse;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import com.fasterxml.jackson.databind.exc.InvalidNullException;
import com.fasterxml.jackson.databind.exc.MismatchedInputException;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import org.springframework.context.MessageSourceResolvable;
import org.springframework.dao.DataAccessException;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.Objects;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // ----- domain exception handler

    @ResponseBody
    @ExceptionHandler(DomainException.class)
    public ResponseEntity<ApiResponse<Void>> handleException(HttpServletRequest req, DomainException e) {

        ErrorCode errorCode = e.errorCode();
        String errorMessage = errorCode.message();

        return setAttributeAndGetResponseEntity(req, e, errorCode, errorMessage);
    }

    // ----- http exception handlers

    @ResponseBody
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ApiResponse<Void>> handleHttpRequestMethodNotSupportedException(
            HttpServletRequest req,
            HttpRequestMethodNotSupportedException e
    ) {

        ErrorCode errorCode = CommonErrorCode.UNSUPPORTED_HTTP_METHOD;
        String errorMessage = errorCode.message()
                + "(given: " + e.getMethod() + ", supported: " + e.getSupportedHttpMethods() + ")";

        return setAttributeAndGetResponseEntity(req, e, errorCode, errorMessage);
    }

    @ResponseBody
    @ExceptionHandler(MissingRequestHeaderException.class)
    public ResponseEntity<ApiResponse<Void>> handleMissingRequestHeaderException(
            HttpServletRequest req,
            MissingRequestHeaderException e
    ) {

        ErrorCode errorCode = CommonErrorCode.MISSING_HTTP_HEADER;
        String errorMessage = errorCode.message() + "(missing: " + e.getHeaderName() + ")";

        return setAttributeAndGetResponseEntity(req, e, errorCode, errorMessage);
    }

    // ----- servlet exceptions

    @ResponseBody
    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleNoResourceFoundException(
            HttpServletRequest req,
            NoResourceFoundException e)
    {

        ErrorCode errorCode = CommonErrorCode.NO_STATIC_RESOURCE;
        String errorMessage = errorCode.message() + ": " + e.getMessage();

        return setAttributeAndGetResponseEntity(req, e, errorCode, errorMessage);
    }

    @ResponseBody
    @ExceptionHandler(ServletException.class)
    public ResponseEntity<ApiResponse<Void>> handleServletException(HttpServletRequest req, ServletException e) {

        ErrorCode errorCode = CommonErrorCode.SERVLET_EXCEPTION;
        String errorMessage = errorCode.message() + ": " + e.getMessage();

        return setAttributeAndGetResponseEntity(req, e, errorCode, errorMessage);
    }

    // ----- invalid argument exception handlers

    @ResponseBody
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleMethodArgumentNotValidException(
            HttpServletRequest req,
            MethodArgumentNotValidException e
    ) {

        ErrorCode errorCode = CommonErrorCode.INVALID_ARGUMENT_ERROR;
        String fieldErrorMessage = e.getBindingResult().getFieldErrors()
                .stream()
                .map(fieldError -> fieldError.getField() + ": " + fieldError.getDefaultMessage())
                .collect(Collectors.joining(", "));
        String errorMessage = errorCode.message() + "(" + fieldErrorMessage + ")";

        return setAttributeAndGetResponseEntity(req, e, errorCode, errorMessage);
    }

    @ResponseBody
    @ExceptionHandler(BindException.class)
    public ResponseEntity<ApiResponse<Void>> handleBindException(HttpServletRequest req, BindException e) {

        ErrorCode errorCode = CommonErrorCode.INVALID_ARGUMENT_ERROR;
        String fieldErrorMessage = e.getFieldErrors()
                .stream()
                .map(fieldError ->
                        "\nfield: " + fieldError.getField()
                                + ", rejectedValue: " + fieldError.getRejectedValue()
                                + ", message: " + (fieldError.getDefaultMessage() == null ? "" : fieldError.getDefaultMessage())
                )
                .collect(Collectors.joining(", "));
        String errorMessage = errorCode.message() + "(" + fieldErrorMessage + ")";

        return setAttributeAndGetResponseEntity(req, e, errorCode, errorMessage);
    }

    @ResponseBody
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiResponse<Void>> handleConstraintViolationException(
            HttpServletRequest req,
            ConstraintViolationException e
    ) {

        ErrorCode errorCode = CommonErrorCode.INVALID_ARGUMENT_ERROR;
        String fieldErrorMessage = e.getConstraintViolations()
                .stream()
                .map(violation ->
                        "\nfield: " + (violation.getPropertyPath() == null ? "" : violation.getPropertyPath().toString())
                                + ", rejectedValue: " + violation.getInvalidValue()
                                + ", message: " + (violation.getMessage() == null ? "" : violation.getMessage())
                )
                .collect(Collectors.joining(", "));
        String errorMessage = errorCode.message() + "(" + fieldErrorMessage + ")";

        return setAttributeAndGetResponseEntity(req, e, errorCode, errorMessage);
    }

    @ResponseBody
    @ExceptionHandler(HandlerMethodValidationException.class)
    public ResponseEntity<ApiResponse<Void>> handleHandlerMethodValidationException(
            HttpServletRequest req,
            HandlerMethodValidationException e
    ) {

        ErrorCode errorCode = CommonErrorCode.INVALID_ARGUMENT_ERROR;
        String fieldErrorMessage = e.getParameterValidationResults()
                .stream()
                .map(result ->
                        result.getMethodParameter().getParameterName()
                                + ": "
                                + result.getResolvableErrors()
                                .stream()
                                .map(MessageSourceResolvable::getDefaultMessage)
                                .filter(Objects::nonNull)
                                .collect(Collectors.joining(", "))
                )
                .collect(Collectors.joining(", "));
        String errorMessage = errorCode.message() + "(" + fieldErrorMessage + ")";

        return setAttributeAndGetResponseEntity(req, e, errorCode, errorMessage);
    }

    @ResponseBody
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiResponse<Void>> handleHttpMessageNotReadableException(
            HttpServletRequest req,
            HttpMessageNotReadableException e
    ) {

        ErrorCode errorCode = CommonErrorCode.INVALID_ARGUMENT_ERROR;
        String fieldErrorMessage = "unknown";
        if (e.getCause() instanceof MismatchedInputException cause) {
            fieldErrorMessage = cause.getPath()
                    .stream()
                    .map(reference -> reference.getFieldName() + ": " + reference.getDescription())
                    .collect(Collectors.joining(", "));
        }

        String reasonPhrase = "";
        if (e.getCause() instanceof InvalidNullException cause) {
            reasonPhrase = "필수 값입니다. 누락되었거나 null일 수 없습니다.";
        } else if (e.getCause() instanceof InvalidFormatException cause) {
            reasonPhrase = "'" + cause.getValue() + "'은(는) 유효한 값이 아닙니다.";
        } else {
            reasonPhrase = "입력 형식이 올바르지 않습니다.";
        }

        String errorMessage = errorCode.message() + " - " + reasonPhrase + "(" + fieldErrorMessage + ")";

        return setAttributeAndGetResponseEntity(req, e, errorCode, errorMessage);
    }

    @ResponseBody
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ApiResponse<Void>> handleMissingServletRequestParameterException(
            HttpServletRequest req,
            MissingServletRequestParameterException e
    ) {

        ErrorCode errorCode = CommonErrorCode.INVALID_ARGUMENT_ERROR;
        String errorMessage = errorCode.message()
                + "(name: " + e.getParameterName() + ", type: " + e.getParameterType() + ")";

        return setAttributeAndGetResponseEntity(req, e, errorCode, errorMessage);
    }

    @ResponseBody
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiResponse<Void>> handleMethodArgumentTypeMismatchException(
            HttpServletRequest req,
            MethodArgumentTypeMismatchException e
    ) {

        ErrorCode errorCode = CommonErrorCode.INVALID_ARGUMENT_ERROR;
        String errorMessage = errorCode.message()
                + "(name: " + e.getParameter().getParameterName()
                + ", required type: " + e.getRequiredType()
                + ", given type: " + e.getParameter().getParameterType()
                + ", given value: " + e.getValue()
                + ")";

        return setAttributeAndGetResponseEntity(req, e, errorCode, errorMessage);
    }

    // ----- persistence exception handlers

    @ResponseBody
    @ExceptionHandler(DataAccessException.class)
    public ResponseEntity<ApiResponse<Void>> handleDataAccessException(HttpServletRequest req, DataAccessException e) {

        ErrorCode errorCode = CommonErrorCode.UNEXPECTED_DATABASE_ERROR;
        String errorMessage = errorCode.message();

        ApiResponse<Void> body = ApiResponse.error(errorCode.code(), errorMessage);
        return ResponseEntity.status(errorCode.httpStatus()).body(body);
    }

    // ----- uncaught exception handler

    @ResponseBody
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleException(HttpServletRequest req, Exception e) {

        ErrorCode errorCode = CommonErrorCode.UNEXPECTED_SERVER_ERROR;
        String errorMessage = errorCode.message();

        ApiResponse<Void> body = ApiResponse.error(errorCode.code(), errorMessage);
        return ResponseEntity.status(errorCode.httpStatus()).body(body);
    }

    // ----- helpers

    private ResponseEntity<ApiResponse<Void>> setAttributeAndGetResponseEntity(
            HttpServletRequest req, Exception e, ErrorCode errorCode, String errorMessage) {

        ApiResponse<Void> body = ApiResponse.error(errorCode.code(), errorMessage);
        return ResponseEntity.status(errorCode.httpStatus()).body(body);
    }
}
