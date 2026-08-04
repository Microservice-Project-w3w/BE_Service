package com.equipmentrental.common.web;

import org.springframework.http.HttpStatus;

public enum CommonErrorCode implements ErrorCode {
    AUTH_UNAUTHENTICATED("AUTH_UNAUTHENTICATED", HttpStatus.UNAUTHORIZED, "Chưa xác thực người dùng"),
    AUTH_TOKEN_INVALID("AUTH_TOKEN_INVALID", HttpStatus.UNAUTHORIZED, "Token không hợp lệ"),
    AUTH_TOKEN_EXPIRED("AUTH_TOKEN_EXPIRED", HttpStatus.UNAUTHORIZED, "Token đã hết hạn"),
    AUTH_PERMISSION_DENIED("AUTH_PERMISSION_DENIED", HttpStatus.FORBIDDEN, "Bạn không có quyền thực hiện thao tác này"),
    AUTH_DATA_SCOPE_DENIED("AUTH_DATA_SCOPE_DENIED", HttpStatus.FORBIDDEN, "Bạn không có quyền truy cập phạm vi dữ liệu này"),
    VALIDATION_FAILED("VALIDATION_FAILED", HttpStatus.BAD_REQUEST, "Dữ liệu đầu vào không hợp lệ"),
    RESOURCE_NOT_FOUND("RESOURCE_NOT_FOUND", HttpStatus.NOT_FOUND, "Không tìm thấy dữ liệu"),
    RESOURCE_CONFLICT("RESOURCE_CONFLICT", HttpStatus.CONFLICT, "Dữ liệu bị xung đột"),
    INVALID_STATUS_TRANSITION("INVALID_STATUS_TRANSITION", HttpStatus.BAD_REQUEST, "Chuyển trạng thái không hợp lệ"),
    INTEGRATION_SERVICE_UNAVAILABLE("INTEGRATION_SERVICE_UNAVAILABLE", HttpStatus.SERVICE_UNAVAILABLE, "Dịch vụ tích hợp hiện không khả dụng"),
    INTEGRATION_TIMEOUT("INTEGRATION_TIMEOUT", HttpStatus.GATEWAY_TIMEOUT, "Dịch vụ tích hợp phản hồi quá thời gian"),
    SYSTEM_INTERNAL_ERROR("SYSTEM_INTERNAL_ERROR", HttpStatus.INTERNAL_SERVER_ERROR, "Hệ thống gặp lỗi không mong muốn");

    private final String code;
    private final HttpStatus status;
    private final String defaultMessage;

    CommonErrorCode(String code, HttpStatus status, String defaultMessage) {
        this.code = code;
        this.status = status;
        this.defaultMessage = defaultMessage;
    }

    @Override
    public String code() {
        return code;
    }

    @Override
    public HttpStatus status() {
        return status;
    }

    @Override
    public String defaultMessage() {
        return defaultMessage;
    }
}
