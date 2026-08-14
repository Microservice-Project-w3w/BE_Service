package com.equipmentrental.logistics.security;

import static org.assertj.core.api.Assertions.assertThat;

import com.equipmentrental.common.security.CurrentUser;
import com.equipmentrental.common.security.CurrentUserProvider;
import com.equipmentrental.common.security.DataScopeAuthorizer;
import java.time.Instant;
import java.util.Set;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

class LogisticsDataScopeGuardTest {
    @AfterEach
    void clearContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void customerScopeUsesCustomerClaimAndBranch() {
        CurrentUserProvider provider = new CurrentUserProvider();
        DataScopeAuthorizer authorizer = new DataScopeAuthorizer();
        LogisticsDataScopeGuard guard = new LogisticsDataScopeGuard(provider, authorizer);
        authenticateCustomer(9L, 77L);

        assertThat(guard.canAccessCustomer(1L, 2L, 77L)).isTrue();
        assertThat(guard.canAccessCustomer(1L, 2L, 78L)).isFalse();
    }

    private void authenticateCustomer(Long userId, Long customerId) {
        Jwt jwt = Jwt.withTokenValue("token")
                .header("alg", "none")
                .subject(userId.toString())
                .issuedAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(60))
                .claim("customerId", customerId)
                .claim("organizationId", 1L)
                .claim("branchIds", Set.of(2L))
                .claim("roles", Set.of("CUSTOMER"))
                .build();
        SecurityContextHolder.getContext().setAuthentication(new JwtAuthenticationToken(jwt));
    }
}
