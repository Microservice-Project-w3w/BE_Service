package com.equipmentrental.identity.dto.request;

import com.equipmentrental.identity.entity.UserStatus;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.Set;

public record AdminCreateUserRequest(
        @NotBlank @Size(max = 150) String fullName,
        @NotBlank @Email @Size(max = 150) String email,
        @NotBlank @Size(min = 8, max = 100) String password,
        @NotBlank String roleCode,
        Long organizationId,
        Set<Long> branchIds,
        UserStatus status,
        Boolean emailVerified) {}
