package com.equipmentrental.billing.config;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;

import java.util.Base64;

@Configuration
public class JwtConfig {

    @Bean
    public JwtDecoder jwtDecoder(
            @Value("${security.jwt.secret-base64}") String secretBase64,
            @Value("${security.jwt.issuer}") String issuer
    ) {

        byte[] keyBytes = Base64.getDecoder().decode(secretBase64);

        SecretKey secretKey =
                new SecretKeySpec(keyBytes, "HmacSHA256");

        NimbusJwtDecoder decoder =
                NimbusJwtDecoder
                        .withSecretKey(secretKey)
                        .macAlgorithm(MacAlgorithm.HS256)
                        .build();

        decoder.setJwtValidator(
                JwtValidators.createDefaultWithIssuer(issuer)
        );

        return decoder;
    }
}