package com.equipmentrental.ai.config;

import java.util.Base64;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.config.annotation.method.configuration.EnableReactiveMethodSecurity;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusReactiveJwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.AbstractOAuth2TokenAuthenticationToken;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.web.server.SecurityWebFilterChain;
import reactor.core.publisher.Mono;

@Configuration
@EnableWebFluxSecurity
@EnableReactiveMethodSecurity
public class AiSecurityConfiguration {
    @Bean
    public NimbusReactiveJwtDecoder jwtDecoder(@Value("${security.jwt.secret-base64}") String secretBase64,
                                                @Value("${security.jwt.issuer}") String issuer) {
        byte[] secretBytes;
        try {
            secretBytes = Base64.getDecoder().decode(secretBase64.trim());
        } catch (IllegalArgumentException exception) {
            throw new IllegalStateException("JWT_SECRET_BASE64 is not valid Base64", exception);
        }
        if (secretBytes.length < 32) {
            throw new IllegalStateException("JWT secret must contain at least 32 bytes");
        }
        SecretKey secretKey = new SecretKeySpec(secretBytes, "HmacSHA256");
        NimbusReactiveJwtDecoder decoder = NimbusReactiveJwtDecoder.withSecretKey(secretKey)
                .macAlgorithm(MacAlgorithm.HS256).build();
        decoder.setJwtValidator(JwtValidators.createDefaultWithIssuer(issuer));
        return decoder;
    }

    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http,
            Converter<Jwt, Mono<AbstractOAuth2TokenAuthenticationToken<Jwt>>> authenticationConverter) {
        return http.csrf(ServerHttpSecurity.CsrfSpec::disable)
                .authorizeExchange(exchange -> exchange
                        .pathMatchers("/api/v1/health", "/actuator/**").permitAll()
                        .pathMatchers("/api/v1/chat").hasAnyAuthority("ROLE_ADMIN", "ROLE_MANAGER")
                        .anyExchange().denyAll())
                .oauth2ResourceServer(oauth2 -> oauth2.jwt(jwt -> jwt.jwtAuthenticationConverter(authenticationConverter)))
                .build();
    }

    @Bean
    public Converter<Jwt, Mono<AbstractOAuth2TokenAuthenticationToken<Jwt>>> authenticationConverter() {
        return jwt -> {
            var authorities = jwt.getClaimAsStringList("roles");
            var grantedAuthorities = authorities == null ? java.util.List.<SimpleGrantedAuthority>of()
                    : authorities.stream().map(role -> new SimpleGrantedAuthority("ROLE_" + role)).toList();
            return Mono.just(new JwtAuthenticationToken(jwt, grantedAuthorities));
        };
    }
}
