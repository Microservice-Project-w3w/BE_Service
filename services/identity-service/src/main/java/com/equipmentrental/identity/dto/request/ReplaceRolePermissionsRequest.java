package com.equipmentrental.identity.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public record ReplaceRolePermissionsRequest(@NotNull List<@Valid RolePermissionAssignmentRequest> permissions) {
}
