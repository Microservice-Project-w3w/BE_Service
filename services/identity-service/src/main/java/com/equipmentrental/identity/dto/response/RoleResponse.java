package com.equipmentrental.identity.dto.response;

import java.util.List;

public record RoleResponse(Long id, String code, String name, String description, boolean systemRole,
                           boolean active, List<RolePermissionResponse> permissions) {
}
