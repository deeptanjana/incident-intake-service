package com.example.incidents.exception;

import com.example.incidents.dto.ApiError;
import com.example.incidents.dto.FieldErrorDetail;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.time.LocalDateTime;
import java.util.List;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleValidation(MethodArgumentNotValidException ex, HttpServletRequest request) {
        List<FieldErrorDetail> details = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error -> new FieldErrorDetail(error.getField(), error.getDefaultMessage()))
                .toList();

        log.warn("event=api.error.validation_failed path={} fieldErrorCount={}", request.getRequestURI(), details.size());

        ApiError apiError = new ApiError(
                LocalDateTime.now(),
                HttpStatus.BAD_REQUEST.value(),
                "VALIDATION_ERROR",
                "Request validation failed",
                request.getRequestURI(),
                details
        );
        return ResponseEntity.badRequest().body(apiError);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiError> handleMessageNotReadable(HttpMessageNotReadableException ex, HttpServletRequest request) {
        if (ex.getCause() instanceof InvalidFormatException invalidFormatException
                && invalidFormatException.getTargetType().isEnum()) {

            String fieldName = invalidFormatException.getPath().isEmpty()
                    ? "unknown"
                    : invalidFormatException.getPath().get(0).getFieldName();

            log.warn("event=api.error.invalid_enum_value path={} field={} value={}",
                    request.getRequestURI(), fieldName, invalidFormatException.getValue());

            ApiError apiError = new ApiError(
                    LocalDateTime.now(),
                    HttpStatus.BAD_REQUEST.value(),
                    "INVALID_REQUEST_VALUE",
                    "Invalid value for field: " + fieldName,
                    request.getRequestURI(),
                    List.of(new FieldErrorDetail(fieldName, "must be one of the allowed values"))
            );
            return ResponseEntity.badRequest().body(apiError);
        }

        log.warn("event=api.error.message_not_readable path={} message={}",
                request.getRequestURI(), ex.getMostSpecificCause().getMessage());

        ApiError apiError = new ApiError(
                LocalDateTime.now(),
                HttpStatus.BAD_REQUEST.value(),
                "MALFORMED_REQUEST",
                "Request body is invalid or malformed",
                request.getRequestURI(),
                List.of()
        );
        return ResponseEntity.badRequest().body(apiError);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiError> handleTypeMismatch(MethodArgumentTypeMismatchException ex, HttpServletRequest request) {
        log.warn("event=api.error.type_mismatch path={} parameter={} value={}",
                request.getRequestURI(), ex.getName(), ex.getValue());

        ApiError apiError = new ApiError(
                LocalDateTime.now(),
                HttpStatus.BAD_REQUEST.value(),
                "INVALID_REQUEST_VALUE",
                "Invalid value for parameter: " + ex.getName(),
                request.getRequestURI(),
                List.of(new FieldErrorDetail(ex.getName(), "must be one of the allowed values"))
        );
        return ResponseEntity.badRequest().body(apiError);
    }

    @ExceptionHandler(IncidentNotFoundException.class)
    public ResponseEntity<ApiError> handleNotFound(IncidentNotFoundException ex, HttpServletRequest request) {
        log.warn("event=api.error.incident_not_found path={} message={}", request.getRequestURI(), ex.getMessage());

        ApiError apiError = new ApiError(
                LocalDateTime.now(),
                HttpStatus.NOT_FOUND.value(),
                "INCIDENT_NOT_FOUND",
                ex.getMessage(),
                request.getRequestURI(),
                List.of()
        );
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(apiError);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleGeneric(Exception ex, HttpServletRequest request) {
        log.error("event=api.error.internal_error path={} exceptionType={} message={}",
                request.getRequestURI(), ex.getClass().getSimpleName(), ex.getMessage(), ex);

        ApiError apiError = new ApiError(
                LocalDateTime.now(),
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "INTERNAL_ERROR",
                "Unexpected server error",
                request.getRequestURI(),
                List.of()
        );
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(apiError);
    }
}
