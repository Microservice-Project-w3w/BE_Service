package com.equipmentrental.inventory.config;

import java.util.ArrayList;
import java.util.Collection;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableMethodSecurity
public class InventorySecurityConfiguration {
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, JwtAuthenticationConverter converter) throws Exception {
        http.csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/health", "/actuator/health", "/actuator/info").permitAll()
                        .requestMatchers(HttpMethod.GET, "/internal/equipment/availability").hasAuthority("inventory.availability.read")
                        .requestMatchers(HttpMethod.GET, "/internal/equipment/**").hasAuthority("inventory.equipment.read")
                        .requestMatchers(HttpMethod.POST, "/internal/reservations").hasAuthority("inventory.reservation.create")
                        .requestMatchers(HttpMethod.POST, "/internal/reservations/*/confirm").hasAuthority("inventory.reservation.confirm")
                        .requestMatchers(HttpMethod.POST, "/internal/reservations/*/release").hasAuthority("inventory.reservation.release")
                        .requestMatchers(HttpMethod.POST, "/internal/reservations/*/extend").hasAnyAuthority("rental.contract.update", "rental.contract.sign")
                        .requestMatchers("/internal/equipment/*/checkin", "/internal/equipment/*/checkout")
                        .hasAuthority("inventory.equipment.change-status")
                        .requestMatchers(HttpMethod.GET, "/api/v1/inventory/categories/**", "/api/v1/inventory/equipment-types/**",
                                "/api/v1/inventory/brands/**", "/api/v1/inventory/models/**").hasAuthority("inventory.catalog.read")
                        .requestMatchers("/api/v1/inventory/categories/**", "/api/v1/inventory/equipment-types/**",
                                "/api/v1/inventory/brands/**", "/api/v1/inventory/models/**").hasAuthority("inventory.catalog.manage")
                        .requestMatchers(HttpMethod.GET, "/api/v1/inventory/equipment/*/accessories/**").hasAuthority("inventory.equipment.read")
                        .requestMatchers("/api/v1/inventory/equipment/*/accessories/**").hasAuthority("inventory.equipment.accessory.manage")
                        .requestMatchers(HttpMethod.GET, "/api/v1/inventory/equipment/**").hasAuthority("inventory.equipment.read")
                        .requestMatchers(HttpMethod.POST, "/api/v1/inventory/equipment").hasAuthority("inventory.equipment.create")
                        .requestMatchers(HttpMethod.PUT, "/api/v1/inventory/equipment/**").hasAuthority("inventory.equipment.update")
                        .requestMatchers(HttpMethod.PATCH, "/api/v1/inventory/equipment/*/status").hasAuthority("inventory.equipment.change-status")
                        .requestMatchers(HttpMethod.GET, "/api/v1/inventory/equipment-images/**").hasAuthority("inventory.equipment.read")
                        .requestMatchers("/api/v1/inventory/equipment-images/**").hasAuthority("inventory.equipment.image.manage")
                        .requestMatchers(HttpMethod.GET, "/api/v1/inventory/warehouses/**").hasAuthority("inventory.warehouse.read")
                        .requestMatchers("/api/v1/inventory/warehouses/**").hasAuthority("inventory.warehouse.manage")
                        .requestMatchers("/api/v1/inventory/stock-in/**").hasAuthority("inventory.stock.in")
                        .requestMatchers("/api/v1/inventory/stock-out/**").hasAuthority("inventory.stock.out")
                        .requestMatchers("/api/v1/inventory/transfers/**").hasAuthority("inventory.stock.transfer")
                        .requestMatchers("/api/v1/inventory/stock-audits/**").hasAuthority("inventory.stock.audit")
                        .requestMatchers(HttpMethod.GET, "/api/v1/inventory/reservations/**").hasAuthority("inventory.reservation.read")
                        .requestMatchers(HttpMethod.POST, "/api/v1/inventory/equipment/*/qr/**").hasAuthority("inventory.qr.generate")
                        .requestMatchers(HttpMethod.GET, "/api/v1/inventory/equipment/*/qr/**", "/api/v1/inventory/equipment/qr/**")
                        .hasAuthority("inventory.equipment.read")
                        .anyRequest().authenticated())
                .oauth2ResourceServer(oauth2 -> oauth2.jwt(jwt -> jwt.jwtAuthenticationConverter(converter)));
        return http.build();
    }

    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(this::authorities);
        return converter;
    }

    private Collection<GrantedAuthority> authorities(Jwt jwt) {
        Collection<GrantedAuthority> authorities = new ArrayList<>();
        var roles = jwt.getClaimAsStringList("roles");
        if (roles != null) roles.forEach(role -> authorities.add(new SimpleGrantedAuthority("ROLE_" + role)));
        var permissions = jwt.getClaimAsStringList("permissions");
        if (permissions != null) permissions.forEach(permission -> authorities.add(new SimpleGrantedAuthority(permission)));
        return authorities;
    }
}
