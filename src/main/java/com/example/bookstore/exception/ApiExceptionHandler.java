package com.example.bookstore.exception;

import com.example.bookstore.dto.ApiError;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.dao.DataIntegrityViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.OptimisticLockingFailureException;
import jakarta.persistence.OptimisticLockException;
import org.springframework.http.*;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.*;

@RestControllerAdvice
public class ApiExceptionHandler {
    private static final Logger log = LoggerFactory.getLogger(ApiExceptionHandler.class);

    @ExceptionHandler(NoSuchElementException.class)
    ResponseEntity<ApiError> notFound(NoSuchElementException ex, HttpServletRequest req) {
        log.warn("Resource not found for {} {}: {}", req.getMethod(), req.getRequestURI(), ex.getMessage());
        return response(HttpStatus.NOT_FOUND, "Resource not found", ex.getMessage(), req, Map.of());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    ResponseEntity<ApiError> badRequest(IllegalArgumentException ex, HttpServletRequest req) {
        log.warn("Bad request for {} {}: {}", req.getMethod(), req.getRequestURI(), ex.getMessage());
        return response(HttpStatus.BAD_REQUEST, "Bad request", ex.getMessage(), req, Map.of());
    }

    @ExceptionHandler(PaymentConflictException.class)
    ResponseEntity<ApiError> paymentConflict(PaymentConflictException ex, HttpServletRequest req) {
        log.warn("Payment conflict for {} {}: {}", req.getMethod(), req.getRequestURI(), ex.getMessage());
        return response(HttpStatus.CONFLICT, "Payment conflict", ex.getMessage(), req, Map.of());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    ResponseEntity<ApiError> validation(MethodArgumentNotValidException ex, HttpServletRequest req) {
        log.warn("Request validation failed for {} {}", req.getMethod(), req.getRequestURI());
        Map<String, String> errors = new LinkedHashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(e -> errors.putIfAbsent(e.getField(), e.getDefaultMessage()));
        return response(HttpStatus.BAD_REQUEST, "Validation failed", "One or more fields are invalid", req, errors);
    }

    @ExceptionHandler({OptimisticLockingFailureException.class, OptimisticLockException.class})
    ResponseEntity<ApiError> concurrentUpdate(RuntimeException ex, HttpServletRequest req) {
        log.warn("Concurrent update conflict for {} {}", req.getMethod(), req.getRequestURI());
        return response(HttpStatus.CONFLICT, "Concurrent update", "The resource changed while this request was being processed. Please retry.", req, Map.of());
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    ResponseEntity<ApiError> conflict(DataIntegrityViolationException ex, HttpServletRequest req) {
        log.warn("Data integrity conflict for {} {}", req.getMethod(), req.getRequestURI());
        return response(HttpStatus.CONFLICT, "Data conflict", "The operation conflicts with existing data", req, Map.of());
    }

    private ResponseEntity<ApiError> response(HttpStatus status, String error, String message, HttpServletRequest req, Map<String, String> details) {
        return ResponseEntity.status(status).body(new ApiError(Instant.now(), status.value(), error, message, req.getRequestURI(), details));
    }
}
