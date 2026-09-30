package com.equipmentrental.identity.controller;

import com.equipmentrental.identity.dto.response.ApiResponse;
import com.equipmentrental.identity.dto.response.SessionResponse;
import com.equipmentrental.identity.service.AuditLogService;
import com.equipmentrental.identity.service.SessionService;
import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/users/{userId}/sessions")
public class SessionManagementController {
    private final SessionService sessions;
    private final AuditLogService auditLogs;

    public SessionManagementController(SessionService sessions, AuditLogService auditLogs) {
        this.sessions = sessions;
        this.auditLogs = auditLogs;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('identity.session.read')")
    public ApiResponse<List<SessionResponse>> list(@PathVariable Long userId) {
        return ApiResponse.success(sessions.listByUser(userId));
    }

    @PostMapping("/{sessionId}/revoke")
    @PreAuthorize("hasAuthority('identity.session.revoke')")
    public ApiResponse<Void> revoke(@PathVariable Long userId, @PathVariable Long sessionId,
                                    JwtAuthenticationToken authentication) {
        sessions.revokeForUser(userId, sessionId, "ADMIN_REVOKED");
        auditLogs.record(actor(authentication), "REVOKE_SESSION", "USER_SESSION", sessionId,
                "{\"userId\":" + userId + "}");
        return ApiResponse.success(null, "Đã thu hồi phiên đăng nhập");
    }

    @PostMapping("/revoke-all")
    @PreAuthorize("hasAuthority('identity.session.revoke')")
    public ApiResponse<Void> revokeAll(@PathVariable Long userId, JwtAuthenticationToken authentication) {
        sessions.revokeAllForUser(userId, "ADMIN_REVOKED_ALL");
        auditLogs.record(actor(authentication), "REVOKE_ALL_SESSIONS", "USER", userId, "{}");
        return ApiResponse.success(null, "Đã thu hồi toàn bộ phiên đăng nhập");
    }

    private Long actor(JwtAuthenticationToken authentication) { return Long.valueOf(authentication.getToken().getSubject()); }
}
