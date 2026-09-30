package com.equipmentrental.identity.controller;

import com.equipmentrental.identity.dto.request.AdminCreateUserRequest;
import com.equipmentrental.identity.dto.request.AdminResetPasswordRequest;
import com.equipmentrental.identity.dto.request.UpdateUserRoleRequest;
import com.equipmentrental.identity.dto.response.ApiResponse;
import com.equipmentrental.identity.dto.response.UserResponse;
import com.equipmentrental.identity.service.IdentityManagementService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/users")
public class IdentityManagementController {
    private final IdentityManagementService service;
    public IdentityManagementController(IdentityManagementService service) { this.service = service; }

    @GetMapping @PreAuthorize("hasAuthority('identity.user.read')")
    public ApiResponse<List<UserResponse>> list() { return ApiResponse.success(service.list()); }

    @GetMapping("/{id}") @PreAuthorize("hasAuthority('identity.user.read')")
    public ApiResponse<UserResponse> get(@PathVariable Long id) { return ApiResponse.success(service.get(id)); }

    @PostMapping @PreAuthorize("hasAuthority('identity.user.create')")
    public ResponseEntity<ApiResponse<UserResponse>> create(@Valid @RequestBody AdminCreateUserRequest request,
            JwtAuthenticationToken authentication) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(service.create(request, currentUserId(authentication))));
    }

    @PutMapping("/{id}/role") @PreAuthorize("hasAuthority('identity.user.update')")
    public ApiResponse<UserResponse> updateRole(@PathVariable Long id, @Valid @RequestBody UpdateUserRoleRequest request,
            JwtAuthenticationToken authentication) {
        return ApiResponse.success(service.updateRole(id, request.roleCode(), currentUserId(authentication)));
    }

    @PatchMapping("/{id}/lock") @PreAuthorize("hasAuthority('identity.user.lock')")
    public ApiResponse<UserResponse> lock(@PathVariable Long id, JwtAuthenticationToken authentication) {
        return ApiResponse.success(service.lock(id, currentUserId(authentication)));
    }

    @PatchMapping("/{id}/unlock") @PreAuthorize("hasAuthority('identity.user.unlock')")
    public ApiResponse<UserResponse> unlock(@PathVariable Long id, JwtAuthenticationToken authentication) {
        return ApiResponse.success(service.unlock(id, currentUserId(authentication)));
    }

    @PostMapping("/{id}/reset-password") @PreAuthorize("hasAuthority('identity.user.update')")
    public ApiResponse<Void> resetPassword(@PathVariable Long id, @Valid @RequestBody AdminResetPasswordRequest request,
            JwtAuthenticationToken authentication) {
        service.resetPassword(id, request.newPassword(), currentUserId(authentication));
        return ApiResponse.success(null, "Đã đặt lại mật khẩu và thu hồi phiên đăng nhập");
    }

    @DeleteMapping("/{id}") @PreAuthorize("hasAuthority('identity.user.update')")
    public ResponseEntity<Void> delete(@PathVariable Long id, JwtAuthenticationToken authentication) {
        service.softDelete(id, currentUserId(authentication));
        return ResponseEntity.noContent().build();
    }

    private Long currentUserId(JwtAuthenticationToken authentication) {
        return Long.valueOf(authentication.getToken().getSubject());
    }
}
