package com.equipmentrental.identity.controller;

import com.equipmentrental.common.web.ApiResponse;
import com.equipmentrental.identity.dto.request.PermissionRequest;
import com.equipmentrental.identity.dto.request.RolePermissionRequest;
import com.equipmentrental.identity.dto.request.RoleRequest;
import com.equipmentrental.identity.dto.request.UserRoleRequest;
import com.equipmentrental.identity.dto.response.PermissionResponse;
import com.equipmentrental.identity.dto.response.RoleResponse;
import com.equipmentrental.identity.dto.response.SessionResponse;
import com.equipmentrental.identity.dto.response.UserResponse;
import com.equipmentrental.identity.service.IdentityManagementService;
import com.equipmentrental.identity.service.SessionService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1")
public class IdentityManagementController {
    private final IdentityManagementService service;
    private final SessionService sessionService;

    public IdentityManagementController(IdentityManagementService service, SessionService sessionService) {
        this.service = service;
        this.sessionService = sessionService;
    }

    @GetMapping("/roles")
    @PreAuthorize("hasAuthority('identity.role.read')")
    public ApiResponse<List<RoleResponse>> roles() {
        return ApiResponse.success(service.roles());
    }

    @PostMapping("/roles")
    @PreAuthorize("hasAuthority('identity.role.create')")
    public ApiResponse<RoleResponse> createRole(@Valid @RequestBody RoleRequest request) {
        return ApiResponse.success(service.createRole(request));
    }

    @PutMapping("/roles/{id}")
    @PreAuthorize("hasAuthority('identity.role.update')")
    public ApiResponse<RoleResponse> updateRole(@PathVariable Long id, @Valid @RequestBody RoleRequest request) {
        return ApiResponse.success(service.updateRole(id, request));
    }

    @DeleteMapping("/roles/{id}")
    @PreAuthorize("hasAuthority('identity.role.delete')")
    public ApiResponse<Void> deleteRole(@PathVariable Long id) {
        service.deleteRole(id);
        return ApiResponse.success(null);
    }

    @PutMapping("/roles/{id}/permissions")
    @PreAuthorize("hasAuthority('identity.permission.assign')")
    public ApiResponse<RoleResponse> permissions(
            @PathVariable Long id, @Valid @RequestBody RolePermissionRequest request) {
        return ApiResponse.success(service.replaceRolePermissions(id, request));
    }

    @GetMapping("/permissions")
    @PreAuthorize("hasAuthority('identity.permission.read')")
    public ApiResponse<List<PermissionResponse>> permissions() {
        return ApiResponse.success(service.permissions());
    }

    @PostMapping("/permissions")
    @PreAuthorize("hasAuthority('identity.role.update')")
    public ApiResponse<PermissionResponse> createPermission(@Valid @RequestBody PermissionRequest request) {
        return ApiResponse.success(service.createPermission(request));
    }

    @PutMapping("/permissions/{id}")
    @PreAuthorize("hasAuthority('identity.role.update')")
    public ApiResponse<PermissionResponse> updatePermission(
            @PathVariable Long id, @Valid @RequestBody PermissionRequest request) {
        return ApiResponse.success(service.updatePermission(id, request));
    }

    @DeleteMapping("/permissions/{id}")
    @PreAuthorize("hasAuthority('identity.role.update')")
    public ApiResponse<Void> deletePermission(@PathVariable Long id) {
        service.deletePermission(id);
        return ApiResponse.success(null);
    }

    @GetMapping("/users/{id}")
    @PreAuthorize("hasAuthority('identity.user.read')")
    public ApiResponse<UserResponse> user(@PathVariable Long id) {
        return ApiResponse.success(service.getUser(id));
    }

    @PutMapping("/users/{id}/role")
    @PreAuthorize("hasAuthority('identity.user.update')")
    public ApiResponse<UserResponse> assignRole(@PathVariable Long id, @Valid @RequestBody UserRoleRequest request) {
        return ApiResponse.success(service.assignRole(id, request.roleCode()));
    }

    @PatchMapping("/users/{id}/lock")
    @PreAuthorize("hasAuthority('identity.user.lock')")
    public ApiResponse<UserResponse> lock(@PathVariable Long id) {
        return ApiResponse.success(service.lockUser(id));
    }

    @PatchMapping("/users/{id}/unlock")
    @PreAuthorize("hasAuthority('identity.user.unlock')")
    public ApiResponse<UserResponse> unlock(@PathVariable Long id) {
        return ApiResponse.success(service.unlockUser(id));
    }

    @GetMapping("/users/{id}/sessions")
    @PreAuthorize("hasAuthority('identity.session.read')")
    public ApiResponse<List<SessionResponse>> sessions(@PathVariable Long id) {
        return ApiResponse.success(sessionService.listByUser(id));
    }

    @PatchMapping("/sessions/{id}/revoke")
    @PreAuthorize("hasAuthority('identity.session.revoke')")
    public ApiResponse<Void> revokeSession(@PathVariable Long id) {
        service.revokeSession(id);
        return ApiResponse.success(null);
    }
}
