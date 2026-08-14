package com.equipmentrental.organizationcustomer.security;

import static org.assertj.core.api.Assertions.assertThat;

import com.equipmentrental.common.security.CurrentUser;
import com.equipmentrental.common.security.CurrentUserProvider;
import com.equipmentrental.common.security.DataScopeAuthorizer;
import java.util.Set;
import org.junit.jupiter.api.Test;
import java.time.Instant;
import org.junit.jupiter.api.AfterEach;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

class OrganizationDataScopeGuardTest {
    @AfterEach
    void clearContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void managerOnlySeesAssignedBranches() {
        CurrentUserProvider provider = new CurrentUserProvider();
        DataScopeAuthorizer authorizer = new DataScopeAuthorizer();
        Jwt jwt = Jwt.withTokenValue("token")
                .header("alg", "none")
                .subject("4")
                .issuedAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(60))
                .claim("organizationId", 1L)
                .claim("branchIds", Set.of(2L))
                .claim("roles", Set.of("MANAGER"))
                .build();
        SecurityContextHolder.getContext().setAuthentication(new JwtAuthenticationToken(jwt));
        OrganizationDataScopeGuard guard = new OrganizationDataScopeGuard(provider, authorizer);

        assertThat(guard.canAccessOrganization(1L)).isTrue();
        assertThat(guard.canAccessBranch(1L, 2L)).isTrue();
        assertThat(guard.canAccessBranch(1L, 3L)).isFalse();
    }
}
