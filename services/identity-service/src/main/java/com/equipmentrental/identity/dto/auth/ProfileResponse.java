package com.equipmentrental.identity.dto.auth;

import java.util.List;

public record ProfileResponse(
        String userId,
        String email,
        List<String> roles,
        String fullName,
        String phone,
        String companyName,
        String taxCode) {}
