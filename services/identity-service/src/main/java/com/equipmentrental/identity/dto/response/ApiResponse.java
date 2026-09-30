package com.equipmentrental.identity.dto.response;

public record ApiResponse<T>(
    boolean success,
    String message,
    T data
) {

    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(
            true,
            "Success",
            data
        );
    }

    public static <T> ApiResponse<T> success(
        T data,
        String message
    ) {
        return new ApiResponse<>(
            true,
            message,
            data
        );
    }
}
