package com.equipmentrental.identity.dto.request;

import com.equipmentrental.identity.entity.UserStatus;
import jakarta.validation.constraints.*;
import java.util.Set;

public record AdminUpdateUserRequest(
        @NotBlank @Size(max = 150) String fullName,
        @NotBlank @Email @Size(max = 150)
        @Pattern(regexp = "(?i)^[A-Za-z0-9._%+-]+@gmail\\.com$", message = "Hệ thống chỉ chấp nhận địa chỉ Gmail") String email,
        @Size(max = 30) String phone,
        @NotBlank String roleCode,
        Long organizationId,
        Set<Long> branchIds,
        @NotNull UserStatus status) {}
