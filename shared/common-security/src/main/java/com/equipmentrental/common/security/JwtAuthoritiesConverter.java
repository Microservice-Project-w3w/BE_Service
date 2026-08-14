package com.equipmentrental.common.security;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;

public class JwtAuthoritiesConverter implements Converter<Jwt, Collection<GrantedAuthority>> {

    @Override
    public Collection<GrantedAuthority> convert(Jwt jwt) {
        if (jwt == null) {
            return Set.of();
        }
        Set<String> authorityNames = new LinkedHashSet<>();
        CurrentUserProvider.toStringSet(jwt.getClaim("permissions")).forEach(authorityNames::add);
        CurrentUserProvider.toStringSet(jwt.getClaim("roles"))
                .forEach(role -> authorityNames.add(role.startsWith("ROLE_") ? role : "ROLE_" + role));
        return authorityNames.stream()
                .map(SimpleGrantedAuthority::new)
                .map(GrantedAuthority.class::cast)
                .toList();
    }
}
