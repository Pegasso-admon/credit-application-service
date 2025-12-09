package com.coopcredit.infrastructure.exception;

import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import java.net.URI;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Global exception handler for the application.
 * Implements RFC 7807 Problem Detail for standardized error responses.
 * Handles validation errors, persistence errors, security errors, and business
 * logic errors.
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final String TYPE_PREFIX = "https://api.coopcredit.com/errors/";
    private static final String TRACE_ID_KEY = "traceId";
    private static final String TIMESTAMP_KEY = "timestamp";
    private static final String ERRORS_KEY = "errors";

    /**
     * Handles validation errors from @Valid annotations.
     *
     * @param ex      the validation exception
     * @param request the web request
     * @return ProblemDetail with validation errors
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail handleValidationErrors(
            MethodArgumentNotValidException ex,
            WebRequest request) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });

        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
                HttpStatus.BAD_REQUEST,
                "Validation failed for one or more fields");
        problemDetail.setType(URI.create(TYPE_PREFIX + "validation-error"));
        problemDetail.setTitle("Validation Error");
        problemDetail.setProperty(ERRORS_KEY, errors);
        problemDetail.setProperty(TIMESTAMP_KEY, Instant.now());
        problemDetail.setProperty(TRACE_ID_KEY, generateTraceId());

        log.warn("Validation error: {}", errors);
        return problemDetail;
    }

    /**
     * Handles constraint violation exceptions.
     *
     * @param ex      the constraint violation exception
     * @param request the web request
     * @return ProblemDetail with constraint violations
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public ProblemDetail handleConstraintViolation(
            ConstraintViolationException ex,
            WebRequest request) {
        Map<String, String> errors = ex.getConstraintViolations().stream()
                .collect(Collectors.toMap(
                        violation -> violation.getPropertyPath().toString(),
                        ConstraintViolation::getMessage));

        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
                HttpStatus.BAD_REQUEST,
                "Constraint validation failed");
        problemDetail.setType(URI.create(TYPE_PREFIX + "constraint-violation"));
        problemDetail.setTitle("Constraint Violation");
        problemDetail.setProperty(ERRORS_KEY, errors);
        problemDetail.setProperty(TIMESTAMP_KEY, Instant.now());
        problemDetail.setProperty(TRACE_ID_KEY, generateTraceId());

        log.warn("Constraint violation: {}", errors);
        return problemDetail;
    }

    /**
     * Handles entity not found exceptions.
     *
     * @param ex      the entity not found exception
     * @param request the web request
     * @return ProblemDetail for not found error
     */
    @ExceptionHandler(EntityNotFoundException.class)
    public ProblemDetail handleEntityNotFound(
            EntityNotFoundException ex,
            WebRequest request) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
                HttpStatus.NOT_FOUND,
                ex.getMessage());
        problemDetail.setType(URI.create(TYPE_PREFIX + "not-found"));
        problemDetail.setTitle("Entity Not Found");
        problemDetail.setProperty(TIMESTAMP_KEY, Instant.now());
        problemDetail.setProperty(TRACE_ID_KEY, generateTraceId());

        log.warn("Entity not found: {}", ex.getMessage());
        return problemDetail;
    }

    /**
     * Handles data integrity violations such as unique constraint violations.
     *
     * @param ex      the data integrity violation exception
     * @param request the web request
     * @return ProblemDetail for data integrity error
     */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ProblemDetail handleDataIntegrityViolation(
            DataIntegrityViolationException ex,
            WebRequest request) {
        String message = "Data integrity violation occurred";
        if (ex.getMessage() != null && ex.getMessage().contains("duplicate key")) {
            message = "A record with the same unique identifier already exists";
        }

        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
                HttpStatus.CONFLICT,
                message);
        problemDetail.setType(URI.create(TYPE_PREFIX + "data-integrity"));
        problemDetail.setTitle("Data Integrity Violation");
        problemDetail.setProperty(TIMESTAMP_KEY, Instant.now());
        problemDetail.setProperty(TRACE_ID_KEY, generateTraceId());

        log.error("Data integrity violation: {}", ex.getMessage());
        return problemDetail;
    }

    /**
     * Handles access denied exceptions.
     *
     * @param ex      the access denied exception
     * @param request the web request
     * @return ProblemDetail for forbidden error
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ProblemDetail handleAccessDenied(
            AccessDeniedException ex,
            WebRequest request) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
                HttpStatus.FORBIDDEN,
                "Access denied: insufficient permissions");
        problemDetail.setType(URI.create(TYPE_PREFIX + "access-denied"));
        problemDetail.setTitle("Access Denied");
        problemDetail.setProperty(TIMESTAMP_KEY, Instant.now());
        problemDetail.setProperty(TRACE_ID_KEY, generateTraceId());

        log.warn("Access denied: {}", ex.getMessage());
        return problemDetail;
    }

    /**
     * Handles bad credentials exceptions.
     *
     * @param ex      the bad credentials exception
     * @param request the web request
     * @return ProblemDetail for unauthorized error
     */
    @ExceptionHandler(BadCredentialsException.class)
    public ProblemDetail handleBadCredentials(
            BadCredentialsException ex,
            WebRequest request) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
                HttpStatus.UNAUTHORIZED,
                "Invalid username or password");
        problemDetail.setType(URI.create(TYPE_PREFIX + "bad-credentials"));
        problemDetail.setTitle("Invalid Credentials");
        problemDetail.setProperty(TIMESTAMP_KEY, Instant.now());
        problemDetail.setProperty(TRACE_ID_KEY, generateTraceId());

        log.warn("Bad credentials attempt");
        return problemDetail;
    }

    /**
     * Handles illegal argument exceptions.
     *
     * @param ex      the illegal argument exception
     * @param request the web request
     * @return ProblemDetail for bad request error
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ProblemDetail handleIllegalArgument(
            IllegalArgumentException ex,
            WebRequest request) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
                HttpStatus.BAD_REQUEST,
                ex.getMessage());
        problemDetail.setType(URI.create(TYPE_PREFIX + "illegal-argument"));
        problemDetail.setTitle("Invalid Argument");
        problemDetail.setProperty(TIMESTAMP_KEY, Instant.now());
        problemDetail.setProperty(TRACE_ID_KEY, generateTraceId());

        log.warn("Illegal argument: {}", ex.getMessage());
        return problemDetail;
    }

    /**
     * Handles all other uncaught exceptions.
     *
     * @param ex      the exception
     * @param request the web request
     * @return ProblemDetail for internal server error
     */
    @ExceptionHandler(Exception.class)
    public ProblemDetail handleGlobalException(
            Exception ex,
            WebRequest request) {
        String traceId = generateTraceId();

        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "An unexpected error occurred");
        problemDetail.setType(URI.create(TYPE_PREFIX + "internal-error"));
        problemDetail.setTitle("Internal Server Error");
        problemDetail.setProperty(TIMESTAMP_KEY, Instant.now());
        problemDetail.setProperty(TRACE_ID_KEY, traceId);

        log.error("Unexpected error [traceId={}]: ", traceId, ex);
        return problemDetail;
    }

    private String generateTraceId() {
        return UUID.randomUUID().toString();
    }
}