package com.equipmentrental.identity.security;

import com.equipmentrental.identity.entity.User;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.stereotype.Service;

@Service
public class JwtService {

    private final JwtEncoder jwtEncoder;
    private final String issuer;
    private final long accessTokenMinutes;

    public JwtService(
            JwtEncoder jwtEncoder,
            @Value("${security.jwt.issuer}") String issuer,
            @Value("${security.jwt.access-token-minutes}") long accessTokenMinutes) {
        this.jwtEncoder = jwtEncoder;
        this.issuer = issuer;
        this.accessTokenMinutes = accessTokenMinutes;
    }

    public String generateAccessToken(User user, Long sessionId) {
        Instant now = Instant.now();
        Instant expiresAt = now.plus(Duration.ofMinutes(accessTokenMinutes));

        JwtClaimsSet.Builder claims = JwtClaimsSet.builder()
                .issuer(issuer)
                .issuedAt(now)
                .expiresAt(expiresAt)
                .subject(String.valueOf(user.getId()))
                .claim("preferred_username", user.getEmail())
                .claim("username", user.getEmail())
                .claim("userId", user.getId())
                .claim("roles", List.of(user.getRole().getCode()))
                .claim(
                        "permissions",
                        user.getRole().getPermissions().stream()
                                .map(permission -> permission.getCode())
                                .sorted()
                                .toList())
                .claim("branchIds", user.getBranchIds());

        if (sessionId != null) {
            claims.claim("sessionId", String.valueOf(sessionId));
        }

        if (user.getOrganizationId() != null) {
            claims.claim("organizationId", user.getOrganizationId());
        }

        if (user.getCustomerId() != null) {
            claims.claim("customerId", user.getCustomerId());
        }

        JwsHeader header = JwsHeader.with(MacAlgorithm.HS256).build();

        JwtEncoderParameters parameters = JwtEncoderParameters.from(header, claims.build());

        return jwtEncoder.encode(parameters).getTokenValue();
    }

    public long getExpiresInSeconds() {
        return Duration.ofMinutes(accessTokenMinutes).toSeconds();
    }
}
