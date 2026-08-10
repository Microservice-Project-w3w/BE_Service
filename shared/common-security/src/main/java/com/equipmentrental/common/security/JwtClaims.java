package com.equipmentrental.common.security;

import java.time.Instant;
import java.util.Set;

public record JwtClaims(
        String subject, Set<String> roles, Set<String> permissions, Instant issuedAt, Instant expiresAt) {}
