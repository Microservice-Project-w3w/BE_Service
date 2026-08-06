package com.equipmentrental.common.web;

import java.time.Instant;
import java.util.List;

public record ApiErrorResponse(
        boolean success,
        Instant timestamp,
        int status,
        String code,
        String message,
        String path,
        String traceId,
        List<ApiErrorDetail> details
) {
    public static ApiErrorResponse of(ErrorCode errorCode, String message, String path, List<ApiErrorDetail> details) {
        ErrorCode resolvedErrorCode = errorCode == null ? CommonErrorCode.SYSTEM_INTERNAL_ERROR : errorCode;
        return new ApiErrorResponse(
                false,
                Instant.now(),
                resolvedErrorCode.status().value(),
                resolvedErrorCode.code(),
                resolvedMessage(resolvedErrorCode, message),
                path,
                TraceIdProvider.currentTraceId(),
                details == null ? List.of() : List.copyOf(details)
        );
    }

    private static String resolvedMessage(ErrorCode errorCode, String message) {
        return message == null || message.isBlank() ? errorCode.defaultMessage() : message;
    }
}
