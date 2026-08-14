package com.equipmentrental.identity.service;

import com.equipmentrental.common.web.BusinessException;
import com.equipmentrental.common.web.CommonErrorCode;
import com.equipmentrental.identity.dto.request.PermissionRequest;
import com.equipmentrental.identity.dto.request.RolePermissionRequest;
import com.equipmentrental.identity.dto.request.RoleRequest;
import com.equipmentrental.identity.dto.request.UserCreateRequest;
import com.equipmentrental.identity.dto.request.UserScopeRequest;
import com.equipmentrental.identity.dto.response.PermissionResponse;
import com.equipmentrental.identity.dto.response.RoleResponse;
import com.equipmentrental.identity.dto.response.UserResponse;
import com.equipmentrental.identity.entity.Permission;
import com.equipmentrental.identity.entity.Role;
import com.equipmentrental.identity.entity.RolePermission;
import com.equipmentrental.identity.entity.User;
import com.equipmentrental.identity.entity.UserStatus;
import com.equipmentrental.identity.entity.PasswordHistory;
import com.equipmentrental.identity.repository.PermissionRepository;
import com.equipmentrental.identity.repository.RoleRepository;
import com.equipmentrental.identity.repository.UserRepository;
import com.equipmentrental.identity.repository.PasswordHistoryRepository;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class IdentityManagementService {
    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;
    private final UserRepository userRepository;
    private final SessionService sessionService;
    private final PasswordEncoder passwordEncoder;
    private final PasswordHistoryRepository passwordHistoryRepository;

    public IdentityManagementService(
            RoleRepository roleRepository,
            PermissionRepository permissionRepository,
            UserRepository userRepository,
            SessionService sessionService,
            PasswordEncoder passwordEncoder,
            PasswordHistoryRepository passwordHistoryRepository) {
        this.roleRepository = roleRepository;
        this.permissionRepository = permissionRepository;
        this.userRepository = userRepository;
        this.sessionService = sessionService;
        this.passwordEncoder = passwordEncoder;
        this.passwordHistoryRepository = passwordHistoryRepository;
    }

    @Transactional(readOnly = true)
    public List<RoleResponse> roles() {
        return roleRepository.findAll().stream().map(this::roleResponse).toList();
    }

    public RoleResponse createRole(RoleRequest request) {
        if (roleRepository.findByCode(request.code().trim()).isPresent()) {
            throw new BusinessException(CommonErrorCode.RESOURCE_CONFLICT, "Mã vai trò đã tồn tại");
        }
        return roleResponse(roleRepository.save(new Role(
                request.code().trim(),
                request.name().trim(),
                request.description(),
                Boolean.TRUE.equals(request.systemRole()),
                request.active() == null || request.active())));
    }

    public RoleResponse updateRole(Long id, RoleRequest request) {
        Role role = role(id);
        if (!role.getCode().equals(request.code().trim())
                && roleRepository.findByCode(request.code().trim()).isPresent()) {
            throw new BusinessException(CommonErrorCode.RESOURCE_CONFLICT, "Mã vai trò đã tồn tại");
        }
        role.setCode(request.code().trim());
        role.setName(request.name().trim());
        role.setDescription(request.description());
        if (request.systemRole() != null) role.setSystemRole(request.systemRole());
        if (request.active() != null) role.setActive(request.active());
        return roleResponse(roleRepository.save(role));
    }

    public void deleteRole(Long id) {
        Role role = role(id);
        if (role.isSystemRole() || userRepository.existsByRoleId(id)) {
            throw new BusinessException(
                    CommonErrorCode.RESOURCE_CONFLICT, "Không thể xóa vai trò hệ thống hoặc đang được gán");
        }
        roleRepository.delete(role);
    }

    public RoleResponse replaceRolePermissions(Long id, RolePermissionRequest request) {
        Role role = role(id);
        Set<RolePermission> assignments = request.permissions().stream()
                .map(entry -> {
                    Permission permission = permissionRepository
                            .findByCode(entry.permissionCode())
                            .orElseThrow(() -> new BusinessException(
                                    CommonErrorCode.RESOURCE_NOT_FOUND,
                                    "Không tìm thấy permission: " + entry.permissionCode()));
                    return new RolePermission(role, permission, entry.dataScope());
                })
                .collect(Collectors.toSet());
        role.replaceRolePermissions(assignments);
        return roleResponse(roleRepository.save(role));
    }

    @Transactional(readOnly = true)
    public List<PermissionResponse> permissions() {
        return permissionRepository.findAll().stream()
                .map(this::permissionResponse)
                .toList();
    }

    public PermissionResponse createPermission(PermissionRequest request) {
        if (permissionRepository.findByCode(request.code().trim()).isPresent()) {
            throw new BusinessException(CommonErrorCode.RESOURCE_CONFLICT, "Mã permission đã tồn tại");
        }
        return permissionResponse(permissionRepository.save(new Permission(
                request.code().trim(), request.name().trim(), request.domain().trim(), request.description())));
    }

    public PermissionResponse updatePermission(Long id, PermissionRequest request) {
        Permission permission = permissionRepository
                .findById(id)
                .orElseThrow(
                        () -> new BusinessException(CommonErrorCode.RESOURCE_NOT_FOUND, "Không tìm thấy permission"));
        if (!permission.getCode().equals(request.code().trim())
                && permissionRepository.findByCode(request.code().trim()).isPresent()) {
            throw new BusinessException(CommonErrorCode.RESOURCE_CONFLICT, "Mã permission đã tồn tại");
        }
        permission.setCode(request.code().trim());
        permission.setName(request.name().trim());
        permission.setDomain(request.domain().trim());
        permission.setDescription(request.description());
        return permissionResponse(permissionRepository.save(permission));
    }

    public void deletePermission(Long id) {
        permissionRepository.deleteById(id);
    }

    public UserResponse assignRole(Long userId, String roleCode) {
        User user = user(userId);
        Role role = roleRepository
                .findByCode(roleCode.trim())
                .orElseThrow(() -> new BusinessException(CommonErrorCode.RESOURCE_NOT_FOUND, "Không tìm thấy vai trò"));
        if (!role.isActive())
            throw new BusinessException(CommonErrorCode.VALIDATION_FAILED, "Vai trò đã bị vô hiệu hóa");
        user.setRole(role);
        validateUserScope(user);
        return userResponse(userRepository.save(user));
    }

    @Transactional(readOnly = true)
    public List<UserResponse> users() {
        return userRepository.findAllByDeletedAtIsNullOrderByIdAsc().stream()
                .map(this::userResponse)
                .toList();
    }

    public UserResponse createUser(UserCreateRequest request) {
        String email = request.email().trim().toLowerCase(Locale.ROOT);
        if (userRepository.existsByEmailIgnoreCase(email)) {
            throw new BusinessException(CommonErrorCode.RESOURCE_CONFLICT, "Email đã được sử dụng");
        }
        Role role = roleRepository
                .findByCode(request.roleCode().trim())
                .orElseThrow(() -> new BusinessException(CommonErrorCode.RESOURCE_NOT_FOUND, "Không tìm thấy vai trò"));
        if (!role.isActive()) {
            throw new BusinessException(CommonErrorCode.VALIDATION_FAILED, "Vai trò đã bị vô hiệu hóa");
        }

        User user = new User();
        user.setFullName(request.fullName().trim());
        user.setEmail(email);
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        user.setRole(role);
        user.setOrganizationId(request.organizationId());
        user.setBranchIds(request.branchIds());
        user.setCustomerId(request.customerId());
        user.setStatus(request.status() == null ? UserStatus.ACTIVE : request.status());
        user.setEmailVerified(request.emailVerified() == null || request.emailVerified());
        user.setFailedLoginAttempts(0);
        validateUserScope(user);

        User saved = userRepository.save(user);
        passwordHistoryRepository.save(new PasswordHistory(saved, saved.getPasswordHash(), "ADMIN_CREATE"));
        return userResponse(saved);
    }

    public UserResponse updateUserScope(Long id, UserScopeRequest request) {
        User user = user(id);
        user.setOrganizationId(request.organizationId());
        user.setBranchIds(request.branchIds());
        user.setCustomerId(request.customerId());
        validateUserScope(user);
        sessionService.revokeAllForUser(id, "SECURITY_SCOPE_CHANGED");
        return userResponse(userRepository.save(user));
    }

    public UserResponse lockUser(Long id) {
        User user = user(id);
        user.setStatus(UserStatus.LOCKED);
        user.setLockedUntil(null);
        sessionService.revokeAllForUser(id, "ACCOUNT_LOCKED");
        return userResponse(userRepository.save(user));
    }

    public UserResponse unlockUser(Long id) {
        User user = user(id);
        user.setStatus(UserStatus.ACTIVE);
        user.setLockedUntil(null);
        user.setFailedLoginAttempts(0);
        return userResponse(userRepository.save(user));
    }

    public void revokeSession(Long sessionId) {
        sessionService.revoke(sessionId, "REVOKED_BY_ADMIN");
    }

    @Transactional(readOnly = true)
    public UserResponse getUser(Long id) {
        return userResponse(user(id));
    }

    private Role role(Long id) {
        return roleRepository
                .findById(id)
                .orElseThrow(() -> new BusinessException(CommonErrorCode.RESOURCE_NOT_FOUND, "Không tìm thấy vai trò"));
    }

    private User user(Long id) {
        return userRepository
                .findDetailedById(id)
                .orElseThrow(
                        () -> new BusinessException(CommonErrorCode.RESOURCE_NOT_FOUND, "Không tìm thấy người dùng"));
    }

    private void validateUserScope(User user) {
        boolean customer = "CUSTOMER".equals(user.getRole().getCode());
        if (!customer && user.getCustomerId() != null) {
            throw new BusinessException(
                    CommonErrorCode.VALIDATION_FAILED, "Chỉ tài khoản CUSTOMER mới được liên kết customerId");
        }
        if (!user.getBranchIds().isEmpty() && user.getOrganizationId() == null) {
            throw new BusinessException(
                    CommonErrorCode.VALIDATION_FAILED, "Phải có organizationId khi gán phạm vi chi nhánh");
        }
    }

    private PermissionResponse permissionResponse(Permission permission) {
        return new PermissionResponse(
                permission.getId(),
                permission.getCode(),
                permission.getName(),
                permission.getDomain(),
                permission.getDescription());
    }

    private RoleResponse roleResponse(Role role) {
        return new RoleResponse(
                role.getId(),
                role.getCode(),
                role.getName(),
                role.getDescription(),
                role.isSystemRole(),
                role.isActive(),
                role.getRolePermissions().stream()
                        .map(value -> new RoleResponse.PermissionScopeResponse(
                                value.getPermission().getCode(), value.getDataScope()))
                        .sorted(java.util.Comparator.comparing(RoleResponse.PermissionScopeResponse::permissionCode))
                        .toList());
    }

    private UserResponse userResponse(User user) {
        return new UserResponse(
                user.getId(),
                user.getFullName(),
                user.getEmail(),
                user.getRole().getCode(),
                user.getStatus(),
                user.isEmailVerified(),
                user.getOrganizationId(),
                user.getBranchIds(),
                user.getCustomerId());
    }
}
