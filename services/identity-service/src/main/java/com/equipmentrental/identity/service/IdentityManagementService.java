package com.equipmentrental.identity.service;

import com.equipmentrental.identity.dto.request.AdminCreateUserRequest;
import com.equipmentrental.identity.dto.response.UserResponse;
import com.equipmentrental.identity.entity.Role;
import com.equipmentrental.identity.entity.User;
import com.equipmentrental.identity.entity.UserStatus;
import com.equipmentrental.identity.repository.RoleRepository;
import com.equipmentrental.identity.repository.UserRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@Transactional
public class IdentityManagementService {
    private final UserRepository users;
    private final RoleRepository roles;
    private final PasswordEncoder passwordEncoder;
    private final SessionService sessions;
    private final AuditLogService auditLogs;

    public IdentityManagementService(UserRepository users, RoleRepository roles,
            PasswordEncoder passwordEncoder, SessionService sessions, AuditLogService auditLogs) {
        this.users = users;
        this.roles = roles;
        this.passwordEncoder = passwordEncoder;
        this.sessions = sessions;
        this.auditLogs = auditLogs;
    }

    @Transactional(readOnly = true)
    public List<UserResponse> list() {
        return users.findAll().stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public UserResponse get(Long id) { return toResponse(user(id)); }

    public UserResponse create(AdminCreateUserRequest request, Long actorUserId) {
        String email = request.email().trim().toLowerCase(Locale.ROOT);
        if (users.existsByEmailIgnoreCase(email)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email đã được sử dụng");
        }
        User user = new User();
        user.setFullName(request.fullName().trim());
        user.setEmail(email);
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        user.setRole(role(request.roleCode()));
        user.setOrganizationId(request.organizationId());
        user.setBranchIds(request.branchIds());
        user.setStatus(request.status() == null ? UserStatus.ACTIVE : request.status());
        user.setEmailVerified(request.emailVerified() == null || request.emailVerified());
        user.setFailedLoginAttempts(0);
        user.setCreatedBy(user(actorUserId));
        user.setUpdatedBy(user(actorUserId));
        User saved = users.save(user);
        auditLogs.record(actorUserId, "CREATE_ACCOUNT", "USER", saved.getId(), "{\"email\":\"" + email + "\"}");
        return toResponse(saved);
    }

    public UserResponse updateRole(Long id, String roleCode, Long actorUserId) {
        User user = user(id);
        user.setRole(role(roleCode));
        user.setUpdatedBy(user(actorUserId));
        sessions.revokeAllForUser(id, "ROLE_CHANGED");
        User saved = users.save(user);
        auditLogs.record(actorUserId, "CHANGE_ROLE", "USER", id, "{\"roleCode\":\"" + user.getRole().getCode() + "\"}");
        return toResponse(saved);
    }

    public UserResponse lock(Long id, Long actorUserId) {
        User user = user(id);
        user.setStatus(UserStatus.LOCKED);
        user.setLockedUntil(null);
        user.setUpdatedBy(user(actorUserId));
        sessions.revokeAllForUser(id, "ADMIN_LOCKED");
        User saved = users.save(user);
        auditLogs.record(actorUserId, "LOCK_ACCOUNT", "USER", id, "{}");
        return toResponse(saved);
    }

    public UserResponse unlock(Long id, Long actorUserId) {
        User user = user(id);
        user.setStatus(UserStatus.ACTIVE);
        user.setLockedUntil(null);
        user.setFailedLoginAttempts(0);
        user.setUpdatedBy(user(actorUserId));
        User saved = users.save(user);
        auditLogs.record(actorUserId, "UNLOCK_ACCOUNT", "USER", id, "{}");
        return toResponse(saved);
    }

    public void resetPassword(Long id, String newPassword, Long actorUserId) {
        User user = user(id);
        user.setPasswordHash(passwordEncoder.encode(newPassword));
        user.setFailedLoginAttempts(0);
        user.setLockedUntil(null);
        user.setUpdatedBy(user(actorUserId));
        users.save(user);
        sessions.revokeAllForUser(id, "ADMIN_PASSWORD_RESET");
        auditLogs.record(actorUserId, "RESET_PASSWORD", "USER", id, "{}");
    }

    public void softDelete(Long id, Long actorUserId) {
        if (id.equals(actorUserId)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Không thể xóa tài khoản đang đăng nhập");
        }
        User user = user(id);
        user.setStatus(UserStatus.DELETED);
        user.setUpdatedBy(user(actorUserId));
        sessions.revokeAllForUser(id, "ADMIN_DELETED");
        users.save(user);
        auditLogs.record(actorUserId, "DELETE_ACCOUNT", "USER", id, "{}");
    }

    private User user(Long id) {
        return users.findDetailedById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy tài khoản"));
    }

    private Role role(String code) {
        return roles.findByCode(code.trim().toUpperCase(Locale.ROOT))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Vai trò không hợp lệ"));
    }

    private UserResponse toResponse(User user) {
        return new UserResponse(user.getId(), user.getFullName(), user.getEmail(), user.getPhone(),
                user.getRole().getCode(), user.getOrganizationId(), user.getBranchIds(), user.getStatus(),
                user.isEmailVerified(), user.getLastLoginAt(), user.getCreatedAt());
    }
}
