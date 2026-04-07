package com.ms.authservice.util;

import com.ms.authservice.dto.ApiResponse;
import com.ms.authservice.dto.ErrorDetail;
import com.ms.authservice.dto.Meta;

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class ApiResponseUtil {

    public static <T> ApiResponse<T> success(T data, String message) {
        return ApiResponse.<T>builder()
                .success(true)
                .code("SUCCESS")
                .message(message)
                .data(data)
                .errors(Collections.emptyList())
                .meta(buildMeta())
                .build();
    }

    public static <T> ApiResponse<T> failure(String code, String message, List<ErrorDetail> errors) {
        return ApiResponse.<T>builder()
                .success(false)
                .code(code)
                .message(message)
                .data(null)
                .errors(errors)
                .meta(buildMeta())
                .build();
    }

    private static Meta buildMeta() {
        return Meta.builder()
                .timestamp(Instant.now().toString())
                .requestId(UUID.randomUUID().toString())
                .build();
    }
}