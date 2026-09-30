package com.equipmentrental.identity.service;

import com.equipmentrental.identity.dto.response.AuditLogResponse;
import com.equipmentrental.identity.entity.AuditLog;
import com.equipmentrental.identity.entity.User;
import com.equipmentrental.identity.repository.AuditLogRepository;
import com.equipmentrental.identity.repository.UserRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class AuditLogService {
    private final AuditLogRepository auditLogs;
    private final UserRepository users;

    public AuditLogService(AuditLogRepository auditLogs, UserRepository users) {
        this.auditLogs = auditLogs;
        this.users = users;
    }

    public void record(Long actorUserId, String actionCode, String entityType, Object entityId, String details) {
        User actor = actorUserId == null ? null : users.getReferenceById(actorUserId);
        auditLogs.save(new AuditLog(actor, actionCode, entityType,
                entityId == null ? null : entityId.toString(), details));
    }

    @Transactional(readOnly = true)
    public List<AuditLogResponse> list(Long userId, String actionCode, int limit) {
        List<AuditLog> entries = userId == null
                ? auditLogs.findTop200ByOrderByCreatedAtDesc()
                : auditLogs.findTop200ByUserIdOrderByCreatedAtDesc(userId);
        String normalizedAction = actionCode == null ? null : actionCode.trim().toUpperCase();
        return entries.stream()
                .filter(log -> normalizedAction == null || normalizedAction.isBlank()
                        || normalizedAction.equals(log.getActionCode()))
                .limit(limit)
                .map(this::toResponse)
                .toList();
    }

    private AuditLogResponse toResponse(AuditLog log) {
        User actor = log.getUser();
        return new AuditLogResponse(log.getId(), actor == null ? null : actor.getId(),
                actor == null ? null : actor.getEmail(), log.getActionCode(), log.getEntityType(),
                log.getEntityId(), log.getDetails(), log.getCreatedAt());
    }
}
