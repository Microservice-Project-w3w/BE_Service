package com.equipmentrental.identity.dto.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ProfileUpdateRequest(
        @NotBlank @Size(max = 150) String fullName,
        @Size(max = 30) String phone,
        @Size(max = 255) String companyName,
        @Size(max = 50) String taxCode) {}
