package com.equipmentrental.identity.dto.auth;

public record RegisterResponse(
        Long userId,
        String email,
        String status,
        String message
) {
}
