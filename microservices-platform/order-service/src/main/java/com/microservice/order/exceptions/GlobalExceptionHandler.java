package com.microservice.order.exceptions;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

import feign.FeignException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final String SERVICE = "order-service";

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

        ApiFieldError fieldError = new ApiFieldError("sku", ex.getMessage());

        ApiError apiError = ApiError.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.CONFLICT.value())
                .error("Conflict")
                .path(request.getRequestURI())
                .errors(List.of(fieldError))
                .build();

        return ResponseEntity.status(HttpStatus.CONFLICT).body(apiError);
    }

    @ExceptionHandler(org.springframework.http.converter.HttpMessageNotReadableException.class)
    public ResponseEntity<ApiError> handleHttpMessageNotReadable(
            org.springframework.http.converter.HttpMessageNotReadableException ex,
            HttpServletRequest request) {

        log.warn("[{}] Malformed JSON request at path={} message={}",
                SERVICE,
                request.getRequestURI(),
                ex.getMessage());

        ApiFieldError fieldError = new ApiFieldError(
                "requestBody",
                "Invalid value in request body. Check enum values like status."
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

    @ExceptionHandler(ProductNotAvailableForOrderException.class)
    public ResponseEntity<ApiError> handleProductNotAvailable(
            ProductNotAvailableForOrderException ex,
            HttpServletRequest request) {

        log.warn("[{}] Product not available for order at path={} productId={} status={}",
                SERVICE,
                request.getRequestURI(),
                ex.getProductId(),
                ex.getCurrentStatus());

        ApiFieldError fieldError = ApiFieldError.builder()
                .field("productId")
                .message("Product " + ex.getProductId() + " is not available for purchase. Current status: " + ex.getCurrentStatus() + ". Only ACTIVE products can be ordered.")
                .allowedValues(List.of("ACTIVE"))
                .rejectedValues(List.of("INACTIVE", "OUT_OF_STOCK", "DISCONTINUED"))
                .build();

        ApiError apiError = ApiError.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.BAD_REQUEST.value())
                .error("Bad Request")
                .path(request.getRequestURI())
                .errors(List.of(fieldError))
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

        ApiError apiError = ApiError.builder()
                .timestamp(LocalDateTime.now())
                .status(ex.getStatusCode().value())
                .error(ex.getStatusCode().toString())
                .path(request.getRequestURI())
                .errors(List.of(new ApiFieldError("request", ex.getReason())))
                .build();

        return ResponseEntity.status(ex.getStatusCode()).body(apiError);
    }

    @ExceptionHandler(FeignException.class)
    public ResponseEntity<ApiError> handleFeignException(
            FeignException ex,
            HttpServletRequest request) {

        int status = ex.status();
        String message = resolveFeignErrorMessage(ex, status);

        log.warn("[{}] Feign client error at path={} status={} message={}",
                SERVICE,
                request.getRequestURI(),
                status,
                message);

        HttpStatus httpStatus = status >= 500
                ? HttpStatus.BAD_GATEWAY
                : HttpStatus.valueOf(status);

        ApiError apiError = ApiError.builder()
                .timestamp(LocalDateTime.now())
                .status(httpStatus.value())
                .error(httpStatus.getReasonPhrase())
                .path(request.getRequestURI())
                .errors(List.of(new ApiFieldError("product-service", message)))
                .build();

        return ResponseEntity.status(httpStatus).body(apiError);
    }

    private String resolveFeignErrorMessage(FeignException ex, int status) {
        if (status == 404) {
            return "Product not found";
        }
        if (status >= 400 && status < 500) {
            return "Product service returned client error (status " + status + ")";
        }
        return "Product service unavailable";
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleUnexpectedException(
            Exception ex,
            HttpServletRequest request) {

        log.error("[{}] Unexpected error at path={}", SERVICE, request.getRequestURI(), ex);

        ApiError apiError = ApiError.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .error("Internal Server Error")
                .path(request.getRequestURI())
                .errors(List.of(new ApiFieldError("server", "Unexpected server error")))
                .build();

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(apiError);
    }
}