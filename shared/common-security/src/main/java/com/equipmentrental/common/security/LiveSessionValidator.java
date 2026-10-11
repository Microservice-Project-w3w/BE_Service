package com.equipmentrental.common.security;

import java.util.Objects;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.security.oauth2.core.*;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import com.fasterxml.jackson.databind.JsonNode;

/** No positive cache: a revoked session is rejected on the next protected request. */
public final class LiveSessionValidator implements OAuth2TokenValidator<Jwt> {
    private final RestClient identity;
    LiveSessionValidator(RestClient identity) { this.identity=identity; }

    public LiveSessionValidator(String identityBaseUrl) {
        var factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(1500);
        factory.setReadTimeout(2500);
        identity = RestClient.builder().baseUrl(identityBaseUrl).requestFactory(factory).build();
    }

    @Override
    public OAuth2TokenValidatorResult validate(Jwt token) {
        if (token.getClaimAsString("sessionId") == null) return denied();
        try {
            JsonNode body = identity.get().uri("/api/v1/auth/me")
                    .headers(h -> h.setBearerAuth(token.getTokenValue())).retrieve().body(JsonNode.class);
            if (body == null || !Objects.equals(body.path("data").path("userId").asText(), token.getSubject()))
                return denied();
            return OAuth2TokenValidatorResult.success();
        } catch (RestClientException exception) {
            // Fail closed, including when Identity is unavailable. Never log bearer tokens.
            return denied();
        }
    }

    private OAuth2TokenValidatorResult denied() {
        return OAuth2TokenValidatorResult.failure(new OAuth2Error("invalid_token",
                "Phiên đã bị thu hồi hoặc không thể xác minh với Identity", null));
    }
}
