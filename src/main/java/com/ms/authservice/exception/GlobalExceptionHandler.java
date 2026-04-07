package com.ms.authservice.exception;

import com.ms.authservice.dto.ApiResponse;
import com.ms.authservice.dto.ErrorDetail;
import com.ms.authservice.enums.ErrorCode;
import com.ms.authservice.util.ApiResponseUtil;
import jakarta.validation.ConstraintViolationException;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.nio.file.AccessDeniedException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BaseException.class)
    public ResponseEntity<ApiResponse<Object>> handleBaseException(BaseException ex) {

        log.error("Handled BaseException: {}", ex.getMessage());

        return new ResponseEntity<>(
                ApiResponseUtil.failure(
                        ex.getErrorCode().name(),
                        ex.getMessage(),
                        ex.getErrors()
                ),
                ex.getStatus()
        );
    }

    // 🔹 Validation Errors (@Valid)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Object>> handleValidationException(
            MethodArgumentNotValidException ex) {

        List<ErrorDetail> errors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(err -> new ErrorDetail(err.getField(), err.getDefaultMessage()))
                .collect(Collectors.toList());

        return new ResponseEntity<>(
                ApiResponseUtil.failure(
                        ErrorCode.VALIDATION_ERROR.name(),
                        "Validation failed",
                        errors
                ),
                HttpStatus.BAD_REQUEST
        );
    }

    // 🔹 Constraint Violations (e.g., @RequestParam)
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiResponse<Object>> handleConstraintViolation(
            ConstraintViolationException ex) {

        List<ErrorDetail> errors = ex.getConstraintViolations()
                .stream()
                .map(v -> new ErrorDetail(v.getPropertyPath().toString(), v.getMessage()))
                .collect(Collectors.toList());

        return new ResponseEntity<>(
                ApiResponseUtil.failure(
                        ErrorCode.VALIDATION_ERROR.name(),
                        "Invalid parameters",
                        errors
                ),
                HttpStatus.BAD_REQUEST
        );
    }

    // 🔹 Spring Security Exceptions
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiResponse<Object>> handleAccessDenied(AccessDeniedException ex) {

        return new ResponseEntity<>(
                ApiResponseUtil.failure(
                        ErrorCode.FORBIDDEN.name(),
                        "Access denied",
                        null
                ),
                HttpStatus.FORBIDDEN
        );
    }

    // 🔹 Catch-all (VERY IMPORTANT)
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Object>> handleGlobalException(Exception ex) {

        log.error("Unhandled Exception", ex);

        return new ResponseEntity<>(
                ApiResponseUtil.failure(
                        ErrorCode.INTERNAL_SERVER_ERROR.name(),
                        "Something went wrong",
                        null
                ),
                HttpStatus.INTERNAL_SERVER_ERROR
        );
    }
}

