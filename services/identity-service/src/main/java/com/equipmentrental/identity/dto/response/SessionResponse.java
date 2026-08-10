package com.equipmentrental.identity.dto.response;

import java.time.LocalDateTime;

public record SessionResponse(
        Long id,
        String deviceName,
        String deviceType,
        String ipAddress,
        LocalDateTime loginAt,
        LocalDateTime lastActivityAt,
        LocalDateTime expiresAt,
        LocalDateTime revokedAt,
        String revokedReason) {}
