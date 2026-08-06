package com.equipmentrental.common.web;

import java.time.Instant;

public record ApiResponse<T>(
        boolean success,
        Instant timestamp,
        T data,
        String message,
        String traceId
) {
    public static <T> ApiResponse<T> success(T data) {
        return success(data, null);
    }

    public static <T> ApiResponse<T> success(T data, String message) {
        return new ApiResponse<>(true, Instant.now(), data, message, TraceIdProvider.currentTraceId());
    }
}
