package com.equipmentrental.identity.controller;

import com.equipmentrental.identity.dto.response.ApiResponse;
import com.equipmentrental.identity.dto.response.AuditLogResponse;
import com.equipmentrental.identity.service.AuditLogService;
import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/audit-logs")
public class AuditLogController {
    private final AuditLogService service;
    public AuditLogController(AuditLogService service) { this.service = service; }

    @GetMapping
    @PreAuthorize("hasAuthority('identity.user.read')")
    public ApiResponse<List<AuditLogResponse>> list(@RequestParam(required = false) Long userId,
            @RequestParam(required = false) String actionCode,
            @RequestParam(defaultValue = "100") int limit) {
        if (limit < 1 || limit > 200) {
            throw new IllegalArgumentException("limit phải nằm trong khoảng 1 đến 200");
        }
        return ApiResponse.success(service.list(userId, actionCode, limit));
    }
}
