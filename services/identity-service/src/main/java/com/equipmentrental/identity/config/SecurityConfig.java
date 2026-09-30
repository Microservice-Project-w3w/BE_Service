package com.equipmentrental.identity.config;

import com.equipmentrental.identity.security.JwtAuthoritiesConverter;
import com.nimbusds.jose.jwk.source.ImmutableSecret;
import com.nimbusds.jose.proc.SecurityContext;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }

    @Bean
    public JwtEncoder jwtEncoder(
        @Value("${security.jwt.secret-base64}")
        String secretBase64
    ) {

        SecretKey secretKey =
            createSecretKey(secretBase64);

        return new NimbusJwtEncoder(
            new ImmutableSecret<SecurityContext>(
                secretKey
            )
        );
    }

    @Bean
    public JwtDecoder jwtDecoder(
        @Value("${security.jwt.secret-base64}")
        String secretBase64,
        @Value("${security.jwt.issuer}")
        String issuer
    ) {

        SecretKey secretKey =
            createSecretKey(secretBase64);

        NimbusJwtDecoder decoder =
            NimbusJwtDecoder
                .withSecretKey(secretKey)
                .macAlgorithm(
                    MacAlgorithm.HS256
                )
                .build();

        decoder.setJwtValidator(
            JwtValidators
                .createDefaultWithIssuer(
                    issuer
                )
        );

        return decoder;
    }

    @Bean
    public JwtAuthoritiesConverter jwtAuthoritiesConverter() {
        return new JwtAuthoritiesConverter();
    }

    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter(
        JwtAuthoritiesConverter authoritiesConverter
    ) {

        JwtAuthenticationConverter converter =
            new JwtAuthenticationConverter();

        converter.setJwtGrantedAuthoritiesConverter(
            authoritiesConverter
        );

        return converter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(
        HttpSecurity http,
        JwtAuthenticationConverter converter
    ) throws Exception {

        http
            .csrf(csrf ->
                csrf.disable()
            )

            .sessionManagement(session ->
                session.sessionCreationPolicy(
                    SessionCreationPolicy.STATELESS
                )
            )

            .authorizeHttpRequests(auth ->
                auth
                    .requestMatchers(
                        "/health",
                        "/actuator/health",

                        "/api/v1/auth/register",
                        "/api/v1/auth/login",
                        "/api/v1/auth/refresh",

                        "/api/v1/auth/verification-codes",
                        "/api/v1/auth/verify-email",

                        "/api/v1/auth/password-reset",
                        "/api/v1/auth/reset-password"
                    )
                    .permitAll()
                    .anyRequest()
                    .authenticated()
            )

            .oauth2ResourceServer(oauth2 ->
                oauth2.jwt(jwt ->
                    jwt.jwtAuthenticationConverter(
                        converter
                    )
                )
            );

        return http.build();
    }

    private SecretKey createSecretKey(
        String secretBase64
    ) {

        try {

            byte[] secretBytes =
                Base64.getDecoder()
                    .decode(
                        secretBase64.trim()
                    );

            if (secretBytes.length < 32) {
                throw new IllegalStateException(
                    "JWT secret must contain at least 32 bytes"
                );
            }

            return new SecretKeySpec(
                secretBytes,
                "HmacSHA256"
            );

        } catch (IllegalArgumentException e) {

            throw new IllegalStateException(
                "JWT_SECRET_BASE64 is not valid Base64",
                e
            );
        }
    }
}
