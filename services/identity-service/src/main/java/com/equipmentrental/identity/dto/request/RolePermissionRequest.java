package com.equipmentrental.identity.dto.request;

import com.equipmentrental.common.security.DataScope;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public record RolePermissionRequest(@NotEmpty List<@Valid Entry> permissions) {
    public record Entry(@NotBlank String permissionCode, @NotNull DataScope dataScope) {
    }
}
