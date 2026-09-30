package com.equipmentrental.identity.dto.response;

import com.equipmentrental.identity.entity.UserStatus;
import java.time.LocalDateTime;
import java.util.Set;

public record UserResponse(
        Long id,
        String fullName,
        String email,
        String phone,
        String roleCode,
        Long organizationId,
        Set<Long> branchIds,
        UserStatus status,
        boolean emailVerified,
        LocalDateTime lastLoginAt,
        LocalDateTime createdAt) {}
