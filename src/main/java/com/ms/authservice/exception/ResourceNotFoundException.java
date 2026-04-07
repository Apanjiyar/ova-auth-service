package com.ms.authservice.exception;

import com.ms.authservice.enums.ErrorCode;
import org.springframework.http.HttpStatus;

public class ResourceNotFoundException extends BaseException {

    public ResourceNotFoundException(String resource, String field, Object value) {
        super(
                ErrorCode.RESOURCE_NOT_FOUND,
                HttpStatus.NOT_FOUND,
                String.format("%s not found with %s: %s", resource, field, value)
        );
    }
}