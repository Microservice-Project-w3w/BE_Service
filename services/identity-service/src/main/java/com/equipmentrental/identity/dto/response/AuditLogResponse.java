package com.equipmentrental.identity.dto.response;

import java.time.LocalDateTime;

public record AuditLogResponse(Long id, Long userId, String userEmail, String actionCode, String entityType,
                               String entityId, String details, LocalDateTime createdAt) {
}
