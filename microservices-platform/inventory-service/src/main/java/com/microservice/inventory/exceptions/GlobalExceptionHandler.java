package com.microservice.inventory.exceptions;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.orm.jpa.JpaSystemException;
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

    private final String SERVICE = "inventory-service";

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
                .map(error -> {
                    List<String> allowedValues = null;

                    if ("referenceType".equals(error.getField())) {
                        allowedValues = getAllowedReferenceTypesByPath(request.getRequestURI());
                    }

                    return new ApiFieldError(
                            error.getField(),
                            error.getDefaultMessage(),
                            allowedValues
                    );
                })
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

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ApiError> handleResponseStatusException(
            ResponseStatusException ex,
            HttpServletRequest request) {

        log.warn("[{}] ResponseStatusException at path={} status={} message={}",
                SERVICE,
                request.getRequestURI(),
                ex.getStatusCode(),
                ex.getReason());

        ApiFieldError fieldError = new ApiFieldError(
                "request",
                ex.getReason(),
                null
        );

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
                "Invalid value in request body. Check JSON format and field types.",
                null
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
                        error.getMessage(),
                        null
                ))
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
                "Database integrity error. Check unique fields and required values.",
                null
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

    @ExceptionHandler(ObjectOptimisticLockingFailureException.class)
    public ResponseEntity<ApiError> handleOptimisticLockingException(
            ObjectOptimisticLockingFailureException ex,
            HttpServletRequest request) {

        log.warn("[{}] Optimistic locking conflict at path={} message={}",
                SERVICE,
                request.getRequestURI(),
                ex.getMessage());

        ApiFieldError fieldError = new ApiFieldError(
                "inventory",
                "Inventory was modified by another transaction. Please retry.",
                null
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

    @ExceptionHandler(JpaSystemException.class)
    public ResponseEntity<ApiError> handleJpaSystemException(
            JpaSystemException ex,
            HttpServletRequest request) {

        log.error("[{}] JPA system error at path={} message={}",
                SERVICE,
                request.getRequestURI(),
                ex.getMessage());

        String message = "Database contains invalid date/time values.";

        if (ex.getMessage() != null && ex.getMessage().contains("Zero date value prohibited")) {
            message = "Database contains invalid zero date values in audit fields (created_at / updated_at).";
        }

        ApiFieldError fieldError = new ApiFieldError(
                "database",
                message,
                null
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

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleGenericException(
            Exception ex,
            HttpServletRequest request) {

        log.error("[{}] Unexpected error at path={}", SERVICE, request.getRequestURI(), ex);

        ApiFieldError fieldError = new ApiFieldError(
                "server",
                "An unexpected internal error occurred",
                null
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

    private List<String> getAllowedReferenceTypesByPath(String path) {
        if (path.matches(".*/api/v1/inventory/\\d+/add$")) {
            return List.of("RESTOCK", "MANUAL_ADJUSTMENT");
        }

        if (path.matches(".*/api/v1/inventory/\\d+/reserve$")
                || path.matches(".*/api/v1/inventory/\\d+/release$")
                || path.matches(".*/api/v1/inventory/\\d+/confirm-output$")) {
            return List.of("ORDER");
        }

        return List.of("ORDER", "RESTOCK", "MANUAL_ADJUSTMENT", "INITIAL_STOCK");
    }
}