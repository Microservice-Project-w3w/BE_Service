package com.equipmentrental.rental.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.equipmentrental.common.security.CurrentUser;
import com.equipmentrental.common.security.CurrentUserProvider;
import com.equipmentrental.common.security.DataScopeAuthorizer;
import com.equipmentrental.common.web.BusinessException;
import com.equipmentrental.rental.client.CustomerPortalClient;
import static org.mockito.Mockito.*;
import java.time.Instant;
import java.util.Set;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

class RentalDataScopeGuardTest {
    private final CurrentUserProvider currentUserProvider = new CurrentUserProvider();
    private final DataScopeAuthorizer authorizer = new DataScopeAuthorizer();
    private final CustomerPortalClient portal = mock(CustomerPortalClient.class);
    private final RentalDataScopeGuard guard = new RentalDataScopeGuard(currentUserProvider, authorizer, portal);

    @AfterEach
    void clearContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void customerCanOnlyAccessMatchingCustomerId() {
        authenticateCustomer(9L, 77L);
        when(portal.me(1L)).thenReturn(new CustomerPortalClient.Context(9L, 77L, 1L, 2L, "Customer"));

        guard.requireRentalAccess(1L, 2L, 77L);
        assertThat(guard.customerIdForOwnList(1L, 2L)).isEqualTo(77L);

        assertThatThrownBy(() -> guard.requireRentalAccess(1L, 2L, 78L))
                .isInstanceOf(BusinessException.class);
    }

    @Test
    void staleJwtCustomerIdDoesNotOverrideCurrentPortalLink() {
        authenticateCustomer(9L, 77L);
        when(portal.me(1L)).thenReturn(new CustomerPortalClient.Context(9L, 88L, 1L, 2L, "Customer"));
        assertThat(guard.customerIdForOwnList(1L, 2L)).isEqualTo(88L);
        assertThatThrownBy(() -> guard.requireRentalAccess(1L, 2L, 77L)).isInstanceOf(BusinessException.class);
    }

    @Test
    void cannotReadAnotherBranchOrUseAnotherUsersLink() {
        authenticateCustomer(9L, 77L);
        assertThatThrownBy(() -> guard.customerIdForOwnList(1L, 3L)).isInstanceOf(BusinessException.class);
        verifyNoInteractions(portal);
        when(portal.me(1L)).thenReturn(new CustomerPortalClient.Context(10L, 77L, 1L, 2L, "Other"));
        assertThatThrownBy(() -> guard.requireRentalAccess(1L, 2L, 77L)).isInstanceOf(BusinessException.class);
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
