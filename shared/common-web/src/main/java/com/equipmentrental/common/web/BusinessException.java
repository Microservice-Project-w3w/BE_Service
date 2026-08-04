package com.equipmentrental.common.web;

import java.util.Map;

public class BusinessException extends RuntimeException {
    private final ErrorCode errorCode;
    private final Map<String, Object> metadata;

    public BusinessException(ErrorCode errorCode) {
        this(errorCode, errorCode == null ? null : errorCode.defaultMessage(), Map.of());
    }

    public BusinessException(ErrorCode errorCode, String message) {
        this(errorCode, message, Map.of());
    }

    public BusinessException(ErrorCode errorCode, String message, Map<String, Object> metadata) {
        super(message == null && errorCode != null ? errorCode.defaultMessage() : message);
        this.errorCode = errorCode == null ? CommonErrorCode.SYSTEM_INTERNAL_ERROR : errorCode;
        this.metadata = metadata == null ? Map.of() : Map.copyOf(metadata);
    }

    public ErrorCode getErrorCode() {
        return errorCode;
    }

    public Map<String, Object> getMetadata() {
        return metadata;
    }
}
