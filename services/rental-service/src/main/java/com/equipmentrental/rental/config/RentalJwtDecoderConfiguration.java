package com.equipmentrental.rental.config;

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
public class RentalJwtDecoderConfiguration {

    @Bean
    public JwtDecoder jwtDecoder(
            @Value("${security.jwt.secret-base64}") String secretBase64,
            @Value("${security.jwt.issuer}") String issuer
    ) {
        byte[] secret = decodeSecret(secretBase64);
        NimbusJwtDecoder decoder = NimbusJwtDecoder.withSecretKey(new SecretKeySpec(secret, "HmacSHA256"))
                .macAlgorithm(MacAlgorithm.HS256)
                .build();
        decoder.setJwtValidator(JwtValidators.createDefaultWithIssuer(issuer));
        return decoder;
    }

    private byte[] decodeSecret(String secretBase64) {
        try {
            byte[] secret = Base64.getDecoder().decode(secretBase64.trim());
            if (secret.length < 32) {
                throw new IllegalStateException("JWT secret phải có ít nhất 32 byte");
            }
            return secret;
        } catch (IllegalArgumentException exception) {
            throw new IllegalStateException("JWT_SECRET_BASE64 không phải Base64 hợp lệ", exception);
        }
    }
}
