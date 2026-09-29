package com.equipmentrental.common.web;

public record ApiErrorDetail(String field, String code, String message, Object rejectedValue) {
    public static ApiErrorDetail of(String field, String code, String message, Object rejectedValue) {
        return new ApiErrorDetail(field, code, message, isSensitive(field) ? "[REDACTED]" : rejectedValue);
    }

    private static boolean isSensitive(String field) {
        if (field == null) {
            return false;
        }
        String normalized = field.toLowerCase();
        return normalized.contains("password") || normalized.contains("token") || normalized.contains("secret");
    }
}
