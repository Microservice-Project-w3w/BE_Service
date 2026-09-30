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

    public IdentityManagementService(UserRepository users, RoleRepository roles,
            PasswordEncoder passwordEncoder, SessionService sessions) {
        this.users = users;
        this.roles = roles;
        this.passwordEncoder = passwordEncoder;
        this.sessions = sessions;
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
        return toResponse(users.save(user));
    }

    public UserResponse updateRole(Long id, String roleCode, Long actorUserId) {
        User user = user(id);
        user.setRole(role(roleCode));
        user.setUpdatedBy(user(actorUserId));
        sessions.revokeAllForUser(id, "ROLE_CHANGED");
        return toResponse(users.save(user));
    }

    public UserResponse lock(Long id, Long actorUserId) {
        User user = user(id);
        user.setStatus(UserStatus.LOCKED);
        user.setLockedUntil(null);
        user.setUpdatedBy(user(actorUserId));
        sessions.revokeAllForUser(id, "ADMIN_LOCKED");
        return toResponse(users.save(user));
    }

    public UserResponse unlock(Long id, Long actorUserId) {
        User user = user(id);
        user.setStatus(UserStatus.ACTIVE);
        user.setLockedUntil(null);
        user.setFailedLoginAttempts(0);
        user.setUpdatedBy(user(actorUserId));
        return toResponse(users.save(user));
    }

    public void resetPassword(Long id, String newPassword, Long actorUserId) {
        User user = user(id);
        user.setPasswordHash(passwordEncoder.encode(newPassword));
        user.setFailedLoginAttempts(0);
        user.setLockedUntil(null);
        user.setUpdatedBy(user(actorUserId));
        users.save(user);
        sessions.revokeAllForUser(id, "ADMIN_PASSWORD_RESET");
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
