package com.equipmentrental.identity.dto.request;

import com.equipmentrental.identity.entity.UserStatus;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.Set;

public record UserCreateRequest(
        @NotBlank @Size(max = 150) String fullName,
        @NotBlank @Email @Size(max = 150) String email,
        @NotBlank @Size(min = 8, max = 72) String password,
        @NotBlank String roleCode,
        Long organizationId,
        Set<Long> branchIds,
        Long customerId,
        UserStatus status,
        Boolean emailVerified) {}
