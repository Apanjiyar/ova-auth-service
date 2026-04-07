package com.ms.authservice.enums;

public enum ErrorCode {
    // Generic
    INTERNAL_SERVER_ERROR,
    BAD_REQUEST,

    // Validation
    VALIDATION_ERROR,

    // Auth
    UNAUTHORIZED,
    FORBIDDEN,

    // Resource
    RESOURCE_NOT_FOUND,

    // Business
    BUSINESS_ERROR
}
