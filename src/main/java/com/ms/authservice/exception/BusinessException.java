package com.ms.authservice.exception;

import com.ms.authservice.enums.ErrorCode;
import org.springframework.http.HttpStatus;

public class BusinessException extends BaseException {

    public BusinessException(String message) {
        super(ErrorCode.BUSINESS_ERROR, HttpStatus.UNPROCESSABLE_ENTITY, message);
    }
}