package com.equipmentrental.organizationcustomer.config;

import java.util.Base64;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;

@Configuration
public class OrganizationJwtDecoderConfiguration {
    @Bean
    JwtDecoder jwtDecoder(
            @Value("${security.jwt.secret-base64}") String secretBase64,
            @Value("${security.jwt.issuer}") String issuer) {
        byte[] secretBytes = Base64.getDecoder().decode(secretBase64.trim());
        if (secretBytes.length < 32) {
            throw new IllegalStateException("JWT secret phải có ít nhất 32 byte");
        }
        SecretKey key = new SecretKeySpec(secretBytes, "HmacSHA256");
        NimbusJwtDecoder decoder = NimbusJwtDecoder.withSecretKey(key)
                .macAlgorithm(MacAlgorithm.HS256)
                .build();
        decoder.setJwtValidator(JwtValidators.createDefaultWithIssuer(issuer));
        return decoder;
    }
}
