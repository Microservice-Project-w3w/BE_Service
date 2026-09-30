package com.equipmentrental.identity.service;

import com.equipmentrental.identity.dto.request.PermissionRequest;
import com.equipmentrental.identity.dto.request.RolePermissionAssignmentRequest;
import com.equipmentrental.identity.dto.request.RoleRequest;
import com.equipmentrental.identity.dto.response.PermissionResponse;
import com.equipmentrental.identity.dto.response.RolePermissionResponse;
import com.equipmentrental.identity.dto.response.RoleResponse;
import com.equipmentrental.identity.entity.Permission;
import com.equipmentrental.identity.entity.Role;
import com.equipmentrental.identity.entity.RolePermission;
import com.equipmentrental.identity.repository.PermissionRepository;
import com.equipmentrental.identity.repository.RoleRepository;
import com.equipmentrental.identity.repository.UserRepository;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@Transactional
public class RoleManagementService {
    private final RoleRepository roles;
    private final PermissionRepository permissions;
    private final UserRepository users;
    private final AuditLogService auditLogs;

    public RoleManagementService(RoleRepository roles, PermissionRepository permissions, UserRepository users,
                                 AuditLogService auditLogs) {
        this.roles = roles;
        this.permissions = permissions;
        this.users = users;
        this.auditLogs = auditLogs;
    }

    @Transactional(readOnly = true)
    public List<RoleResponse> listRoles() { return roles.findAll().stream().map(this::toRoleResponse).toList(); }

    @Transactional(readOnly = true)
    public RoleResponse getRole(Long id) { return toRoleResponse(role(id)); }

    public RoleResponse createRole(RoleRequest request, Long actorUserId) {
        String code = normalized(request.code());
        if (roles.findByCode(code).isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Mã vai trò đã tồn tại");
        }
        Role role = new Role(code, request.name().trim(), trimToNull(request.description()), false,
                request.active() == null || request.active());
        Role saved = roles.save(role);
        auditLogs.record(actorUserId, "CREATE_ROLE", "ROLE", saved.getId(), "{\"code\":\"" + code + "\"}");
        return toRoleResponse(saved);
    }

    public RoleResponse updateRole(Long id, RoleRequest request, Long actorUserId) {
        Role role = role(id);
        if (role.isSystemRole() && !role.getCode().equals(normalized(request.code()))) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Không thể đổi mã vai trò hệ thống");
        }
        String code = normalized(request.code());
        roles.findByCode(code).filter(other -> !other.getId().equals(id)).ifPresent(other -> {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Mã vai trò đã tồn tại");
        });
        role.setCode(code);
        role.setName(request.name().trim());
        role.setDescription(trimToNull(request.description()));
        if (!role.isSystemRole()) {
            role.setActive(request.active() == null || request.active());
        }
        auditLogs.record(actorUserId, "UPDATE_ROLE", "ROLE", id, "{\"code\":\"" + code + "\"}");
        return toRoleResponse(roles.save(role));
    }

    public void deleteRole(Long id, Long actorUserId) {
        Role role = role(id);
        if (role.isSystemRole()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Không thể xóa vai trò hệ thống");
        }
        if (users.existsByRoleId(id)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Vai trò đang được gán cho tài khoản");
        }
        roles.delete(role);
        auditLogs.record(actorUserId, "DELETE_ROLE", "ROLE", id, "{}");
    }

    public RoleResponse replacePermissions(Long roleId, List<RolePermissionAssignmentRequest> assignments,
                                           Long actorUserId) {
        Role role = role(roleId);
        Set<RolePermission> replacements = new LinkedHashSet<>();
        Set<String> seenCodes = new LinkedHashSet<>();
        for (RolePermissionAssignmentRequest assignment : assignments) {
            String permissionCode = normalized(assignment.permissionCode());
            if (!seenCodes.add(permissionCode)) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Permission bị lặp: " + permissionCode);
            }
            Permission permission = permissions.findByCode(permissionCode)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST,
                            "Permission không hợp lệ: " + permissionCode));
            replacements.add(new RolePermission(role, permission, assignment.dataScope()));
        }
        role.replaceRolePermissions(replacements);
        Role saved = roles.save(role);
        auditLogs.record(actorUserId, "ASSIGN_PERMISSION", "ROLE", roleId,
                "{\"permissionCount\":" + assignments.size() + "}");
        return toRoleResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<PermissionResponse> listPermissions() {
        return permissions.findAll().stream().map(this::toPermissionResponse).toList();
    }

    public PermissionResponse createPermission(PermissionRequest request, Long actorUserId) {
        String code = normalized(request.code());
        if (permissions.findByCode(code).isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Mã permission đã tồn tại");
        }
        Permission saved = permissions.save(new Permission(code, request.name().trim(),
                request.domain().trim().toLowerCase(Locale.ROOT), trimToNull(request.description())));
        auditLogs.record(actorUserId, "CREATE_PERMISSION", "PERMISSION", saved.getId(),
                "{\"code\":\"" + code + "\"}");
        return toPermissionResponse(saved);
    }

    public PermissionResponse updatePermission(Long id, PermissionRequest request, Long actorUserId) {
        Permission permission = permissions.findById(id).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy permission"));
        String code = normalized(request.code());
        permissions.findByCode(code).filter(other -> !other.getId().equals(id)).ifPresent(other -> {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Mã permission đã tồn tại");
        });
        permission.setCode(code);
        permission.setName(request.name().trim());
        permission.setDomain(request.domain().trim().toLowerCase(Locale.ROOT));
        permission.setDescription(trimToNull(request.description()));
        Permission saved = permissions.save(permission);
        auditLogs.record(actorUserId, "UPDATE_PERMISSION", "PERMISSION", id,
                "{\"code\":\"" + code + "\"}");
        return toPermissionResponse(saved);
    }

    private Role role(Long id) {
        return roles.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy vai trò"));
    }

    private RoleResponse toRoleResponse(Role role) {
        List<RolePermissionResponse> rolePermissions = role.getRolePermissions().stream()
                .map(item -> new RolePermissionResponse(item.getPermission().getCode(), item.getPermission().getName(),
                        item.getDataScope())).toList();
        return new RoleResponse(role.getId(), role.getCode(), role.getName(), role.getDescription(),
                role.isSystemRole(), role.isActive(), rolePermissions);
    }

    private PermissionResponse toPermissionResponse(Permission permission) {
        return new PermissionResponse(permission.getId(), permission.getCode(), permission.getName(),
                permission.getDomain(), permission.getDescription());
    }

    private String normalized(String value) { return value.trim().toUpperCase(Locale.ROOT); }
    private String trimToNull(String value) { return value == null || value.isBlank() ? null : value.trim(); }
}
