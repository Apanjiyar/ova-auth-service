package com.ms.authservice.exception;

import com.ms.authservice.dto.ErrorDetail;
import com.ms.authservice.enums.ErrorCode;
import lombok.Getter;
import org.springframework.http.HttpStatus;

import java.util.Collections;
import java.util.List;

@Getter
public class BaseException extends RuntimeException {

    private final ErrorCode errorCode;
    private final HttpStatus status;
    private final List<ErrorDetail> errors;

    public BaseException(ErrorCode errorCode, HttpStatus status, String message) {
        super(message);
        this.errorCode = errorCode;
        this.status = status;
        this.errors = Collections.emptyList();
    }

    public BaseException(ErrorCode errorCode, HttpStatus status, String message, List<ErrorDetail> errors) {
        super(message);
        this.errorCode = errorCode;
        this.status = status;
        this.errors = errors;
    }
}