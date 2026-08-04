package com.equipmentrental.common.web;

public record ApiResponse<T>(boolean success, T data, String message) {
    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(true, data, null);
    }
}
