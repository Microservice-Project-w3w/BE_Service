package com.equipmentrental.identity.dto.auth;

public record AuthResponse(
        String accessToken,
        String tokenType,
        long expiresIn,
        Long userId,
        String email,
        String fullName,
        String role
) {
}
