package com.equipmentrental.common.security;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.jwt.Jwt;

class CommonSecurityTest {
    private final JwtAuthoritiesConverter converter = new JwtAuthoritiesConverter();
    private final DataScopeAuthorizer authorizer = new DataScopeAuthorizer();

    @Test
    void convertsPermissionsAndPrefixesRolesOnlyOnce() {
        Jwt jwt = jwtWithClaims("permissions", List.of("rental.quotation.approve", "rental.quotation.approve"),
                "roles", List.of("SUPER_ADMIN", "ROLE_ORG_ADMIN"));

        assertThat(converter.convert(jwt)).extracting(authority -> authority.getAuthority())
                .containsExactlyInAnyOrder("rental.quotation.approve", "ROLE_SUPER_ADMIN", "ROLE_ORG_ADMIN");
    }

    @Test
    void convertsBranchIdsFromNumericAndStringClaims() {
        CurrentUser user = new CurrentUserProvider().fromJwt(jwtWithClaims("branchIds", List.of(1, 2L, "3", "invalid")));

        assertThat(user.branchIds()).containsExactlyInAnyOrder(1L, 2L, 3L);
    }

    @Test
    void deniesOtherOrganizationAndAllowsSuperAdmin() {
        CurrentUser organizationUser = new CurrentUser("user-1", "name", 10L, Set.of(3L), Set.of("ORG_ADMIN"), Set.of(), "session");
        CurrentUser superAdmin = new CurrentUser("user-2", "name", 10L, Set.of(), Set.of("SUPER_ADMIN"), Set.of(), "session");

        assertThat(authorizer.canAccessOrganization(organizationUser, 11L)).isFalse();
        assertThat(authorizer.canAccessOrganization(superAdmin, 11L)).isTrue();
    }

    @Test
    void allowsOwnerOnlyWhenUserIdsMatch() {
        CurrentUser user = new CurrentUser("user-1", "name", 10L, Set.of(), Set.of(), Set.of(), "session");

        assertThat(authorizer.canAccessOwner(user, "user-1")).isTrue();
        assertThat(authorizer.canAccessOwner(user, "user-2")).isFalse();
    }

    private Jwt jwtWithClaims(Object... entries) {
        Jwt.Builder builder = Jwt.withTokenValue("token").header("alg", "none").subject("user-1");
        for (int index = 0; index < entries.length; index += 2) {
            builder.claim((String) entries[index], entries[index + 1]);
        }
        return builder.build();
    }
}
