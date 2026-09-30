package com.equipmentrental.identity.dto.response;

import com.equipmentrental.identity.security.DataScope;

public record RolePermissionResponse(String permissionCode, String permissionName, DataScope dataScope) {
}
