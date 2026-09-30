package com.equipmentrental.identity.controller;

import com.equipmentrental.identity.dto.request.PermissionRequest;
import com.equipmentrental.identity.dto.request.ReplaceRolePermissionsRequest;
import com.equipmentrental.identity.dto.request.RoleRequest;
import com.equipmentrental.identity.dto.response.ApiResponse;
import com.equipmentrental.identity.dto.response.PermissionResponse;
import com.equipmentrental.identity.dto.response.RoleResponse;
import com.equipmentrental.identity.service.RoleManagementService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class RoleManagementController {
    private final RoleManagementService service;

    public RoleManagementController(RoleManagementService service) { this.service = service; }

    @GetMapping("/api/v1/roles")
    @PreAuthorize("hasAuthority('identity.role.read')")
    public ApiResponse<List<RoleResponse>> listRoles() { return ApiResponse.success(service.listRoles()); }

    @GetMapping("/api/v1/roles/{id}")
    @PreAuthorize("hasAuthority('identity.role.read')")
    public ApiResponse<RoleResponse> getRole(@PathVariable Long id) { return ApiResponse.success(service.getRole(id)); }

    @PostMapping("/api/v1/roles")
    @PreAuthorize("hasAuthority('identity.role.create')")
    public ResponseEntity<ApiResponse<RoleResponse>> createRole(@Valid @RequestBody RoleRequest request,
                                                                  JwtAuthenticationToken authentication) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(service.createRole(request, actor(authentication))));
    }

    @PutMapping("/api/v1/roles/{id}")
    @PreAuthorize("hasAuthority('identity.role.update')")
    public ApiResponse<RoleResponse> updateRole(@PathVariable Long id, @Valid @RequestBody RoleRequest request,
                                                 JwtAuthenticationToken authentication) {
        return ApiResponse.success(service.updateRole(id, request, actor(authentication)));
    }

    @DeleteMapping("/api/v1/roles/{id}")
    @PreAuthorize("hasAuthority('identity.role.delete')")
    public ResponseEntity<Void> deleteRole(@PathVariable Long id, JwtAuthenticationToken authentication) {
        service.deleteRole(id, actor(authentication));
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/api/v1/roles/{id}/permissions")
    @PreAuthorize("hasAuthority('identity.permission.assign')")
    public ApiResponse<RoleResponse> replacePermissions(@PathVariable Long id,
                                                          @Valid @RequestBody ReplaceRolePermissionsRequest request,
                                                          JwtAuthenticationToken authentication) {
        return ApiResponse.success(service.replacePermissions(id, request.permissions(), actor(authentication)));
    }

    @GetMapping("/api/v1/permissions")
    @PreAuthorize("hasAuthority('identity.permission.read')")
    public ApiResponse<List<PermissionResponse>> listPermissions() { return ApiResponse.success(service.listPermissions()); }

    @PostMapping("/api/v1/permissions")
    @PreAuthorize("hasAuthority('identity.permission.assign')")
    public ResponseEntity<ApiResponse<PermissionResponse>> createPermission(@Valid @RequestBody PermissionRequest request,
                                                                              JwtAuthenticationToken authentication) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(service.createPermission(request, actor(authentication))));
    }

    @PutMapping("/api/v1/permissions/{id}")
    @PreAuthorize("hasAuthority('identity.permission.assign')")
    public ApiResponse<PermissionResponse> updatePermission(@PathVariable Long id, @Valid @RequestBody PermissionRequest request,
                                                              JwtAuthenticationToken authentication) {
        return ApiResponse.success(service.updatePermission(id, request, actor(authentication)));
    }

    private Long actor(JwtAuthenticationToken authentication) { return Long.valueOf(authentication.getToken().getSubject()); }
}
