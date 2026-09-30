package com.equipmentrental.identity.dto.request;

import com.equipmentrental.identity.security.DataScope;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record RolePermissionAssignmentRequest(
        @NotBlank String permissionCode,
        @NotNull DataScope dataScope) {
}
