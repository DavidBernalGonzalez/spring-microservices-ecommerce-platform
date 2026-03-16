package com.microservice.product.exceptions;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final String SERVICE = "product-service";

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleValidationErrors(
            MethodArgumentNotValidException ex,
            HttpServletRequest request) {

        log.warn("[{}] Validation error at path={} message={}",
                SERVICE,
                request.getRequestURI(),
                ex.getMessage());

        List<ApiFieldError> errors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error -> new ApiFieldError(
                        error.getField(),
                        error.getDefaultMessage()))
                .toList();

        ApiError apiError = ApiError.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.BAD_REQUEST.value())
                .error("Bad Request")
                .path(request.getRequestURI())
                .errors(errors)
                .build();

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(apiError);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiError> handleIllegalArgumentException(
            IllegalArgumentException ex,
            HttpServletRequest request) {

        log.warn("[{}] Business validation error at path={} message={}",
                SERVICE,
                request.getRequestURI(),
                ex.getMessage());

        ApiFieldError fieldError = new ApiFieldError("business", ex.getMessage());

        ApiError apiError = ApiError.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.CONFLICT.value())
                .error("Conflict")
                .path(request.getRequestURI())
                .errors(List.of(fieldError))
                .build();

        return ResponseEntity.status(HttpStatus.CONFLICT).body(apiError);
    }

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ApiError> handleResponseStatusException(
            ResponseStatusException ex,
            HttpServletRequest request) {

        log.warn("[{}] ResponseStatusException at path={} status={} message={}",
                SERVICE,
                request.getRequestURI(),
                ex.getStatusCode(),
                ex.getReason());

        ApiFieldError fieldError = new ApiFieldError("request", ex.getReason());

        ApiError apiError = ApiError.builder()
                .timestamp(LocalDateTime.now())
                .status(ex.getStatusCode().value())
                .error(ex.getStatusCode().toString())
                .path(request.getRequestURI())
                .errors(List.of(fieldError))
                .build();

        return ResponseEntity.status(ex.getStatusCode()).body(apiError);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiError> handleHttpMessageNotReadable(
            HttpMessageNotReadableException ex,
            HttpServletRequest request) {

        log.warn("[{}] Malformed JSON request at path={} message={}",
                SERVICE,
                request.getRequestURI(),
                ex.getMessage());

        ApiFieldError fieldError = new ApiFieldError(
                "requestBody",
                "Invalid value in request body. Check enum values, JSON format, and field types."
        );

        ApiError apiError = ApiError.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.BAD_REQUEST.value())
                .error("Bad Request")
                .path(request.getRequestURI())
                .errors(List.of(fieldError))
                .build();

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(apiError);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiError> handleConstraintViolationException(
            ConstraintViolationException ex,
            HttpServletRequest request) {

        log.warn("[{}] Constraint violation at path={} message={}",
                SERVICE,
                request.getRequestURI(),
                ex.getMessage());

        List<ApiFieldError> errors = ex.getConstraintViolations()
                .stream()
                .map(error -> new ApiFieldError(
                        error.getPropertyPath().toString(),
                        error.getMessage()))
                .toList();

        ApiError apiError = ApiError.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.BAD_REQUEST.value())
                .error("Bad Request")
                .path(request.getRequestURI())
                .errors(errors)
                .build();

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(apiError);
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ApiError> handleIllegalStateException(
            IllegalStateException ex,
            HttpServletRequest request) {

        log.error("[{}] Data integrity error at path={} message={}",
                SERVICE,
                request.getRequestURI(),
                ex.getMessage());

        ApiError apiError = ApiError.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .error("Internal Server Error")
                .path(request.getRequestURI())
                .errors(List.of(new ApiFieldError("product", ex.getMessage())))
                .build();

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(apiError);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiError> handleDataIntegrityViolationException(
            DataIntegrityViolationException ex,
            HttpServletRequest request) {

        log.warn("[{}] Data integrity violation at path={} message={}",
                SERVICE,
                request.getRequestURI(),
                ex.getMessage());

        ApiFieldError fieldError = new ApiFieldError(
                "database",
                "Database integrity error. Check unique fields like SKU or required values."
        );

        ApiError apiError = ApiError.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.CONFLICT.value())
                .error("Conflict")
                .path(request.getRequestURI())
                .errors(List.of(fieldError))
                .build();

        return ResponseEntity.status(HttpStatus.CONFLICT).body(apiError);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleGenericException(
            Exception ex,
            HttpServletRequest request) {

        log.error("[{}] Unexpected error at path={}", SERVICE, request.getRequestURI(), ex);

        ApiFieldError fieldError = new ApiFieldError(
                "server",
                "An unexpected internal error occurred"
        );

        ApiError apiError = ApiError.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .error("Internal Server Error")
                .path(request.getRequestURI())
                .errors(List.of(fieldError))
                .build();

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(apiError);
    }
}