package com.equipmentrental.identity.dto.request;

import jakarta.validation.constraints.NotBlank;

public record UpdateUserRoleRequest(@NotBlank String roleCode) {}
