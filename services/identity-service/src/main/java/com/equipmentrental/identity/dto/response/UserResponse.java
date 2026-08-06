package com.equipmentrental.identity.dto.response;

import com.equipmentrental.identity.entity.UserStatus;
import java.util.Set;

public record UserResponse(Long id, String fullName, String email, String roleCode, UserStatus status,
                           boolean emailVerified, Long organizationId, Set<Long> branchIds) {
}
