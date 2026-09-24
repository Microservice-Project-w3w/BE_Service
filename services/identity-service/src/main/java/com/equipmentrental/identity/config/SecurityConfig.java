package com.equipmentrental.identity.config;

import com.equipmentrental.common.security.JwtAuthoritiesConverter;
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
        SecretKey secretKey = createSecretKey(secretBase64);

        return new NimbusJwtEncoder(
                new ImmutableSecret<SecurityContext>(secretKey)
        );
    }


    @Bean
    public JwtDecoder jwtDecoder(
            @Value("${security.jwt.secret-base64}") String secretBase64,
            @Value("${security.jwt.issuer}") String issuer
    ) {
        SecretKey secretKey = createSecretKey(secretBase64);

        NimbusJwtDecoder decoder = NimbusJwtDecoder
                .withSecretKey(secretKey)
                .macAlgorithm(MacAlgorithm.HS256)
                .build();
        decoder.setJwtValidator(JwtValidators.createDefaultWithIssuer(issuer));
        return decoder;
    }

    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter(
            JwtAuthoritiesConverter authoritiesConverter
    ) {
        JwtAuthenticationConverter authenticationConverter =
                new JwtAuthenticationConverter();

        authenticationConverter.setJwtGrantedAuthoritiesConverter(
                authoritiesConverter
        );

        return authenticationConverter;
    }
////////////
    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            JwtAuthenticationConverter jwtAuthenticationConverter
    ) throws Exception {

        http
                .csrf(csrf -> csrf.disable())

                .sessionManagement(session ->
                        session.sessionCreationPolicy(
                                SessionCreationPolicy.STATELESS
                        )
                )

                .authorizeHttpRequests(authorize ->
                        authorize
                                // Các API công khai, không cần JWT
                                .requestMatchers(
                                        "/health",
                                        "/api/v1/auth/register",
                                        "/api/v1/auth/login",
                                        "/api/v1/auth/refresh",
                                        "/api/v1/auth/reset-password",
                                        "/api/v1/auth/confirm-reset-password"
                                )
                                .permitAll()

                                // Các API khác bắt buộc đăng nhập
                                .anyRequest()
                                .authenticated()
                )

                .oauth2ResourceServer(oauth2 ->
                        oauth2.jwt(jwt ->
                                jwt.jwtAuthenticationConverter(
                                        jwtAuthenticationConverter
                                )
                        )
                );

        return http.build();
    }

    private SecretKey createSecretKey(String secretBase64) {
        try {
            byte[] secretBytes = Base64
                    .getDecoder()
                    .decode(secretBase64.trim());

            if (secretBytes.length < 32) {
                throw new IllegalStateException(
                        "JWT secret phải có ít nhất 32 byte"
                );
            }

            return new SecretKeySpec(
                    secretBytes,
                    "HmacSHA256"
            );
        } catch (IllegalArgumentException exception) {
            throw new IllegalStateException(
                    "JWT_SECRET_BASE64 không phải Base64 hợp lệ",
                    exception
            );
        }
    }
}
