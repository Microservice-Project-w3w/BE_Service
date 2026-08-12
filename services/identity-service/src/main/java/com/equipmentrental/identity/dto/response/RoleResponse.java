package com.equipmentrental.identity.dto.response;

import com.equipmentrental.common.security.DataScope;
import java.util.List;

public record RoleResponse(
        Long id,
        String code,
        String name,
        String description,
        boolean systemRole,
        boolean active,
        List<PermissionScopeResponse> permissions) {
    public record PermissionScopeResponse(String permissionCode, DataScope dataScope) {}
}
