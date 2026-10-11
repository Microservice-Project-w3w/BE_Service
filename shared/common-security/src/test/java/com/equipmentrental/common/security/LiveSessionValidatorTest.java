package com.equipmentrental.common.security;
import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestClient;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.http.*;
import org.springframework.security.oauth2.jwt.Jwt;
import static org.assertj.core.api.Assertions.*;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.*;
import static org.springframework.test.web.client.response.MockRestResponseCreators.*;
class LiveSessionValidatorTest {
    private Jwt token() { return Jwt.withTokenValue("test-token").header("alg","HS256").subject("4").claim("sessionId","8").build(); }
    @Test void validatesEveryRequestWithoutPositiveCaching() {
        var builder=RestClient.builder();var server=MockRestServiceServer.bindTo(builder).build();
        var validator=new LiveSessionValidator(builder.build());
        server.expect(requestTo("/api/v1/auth/me")).andExpect(header("Authorization","Bearer test-token"))
                .andRespond(withSuccess("{\"data\":{\"userId\":\"4\"}}",MediaType.APPLICATION_JSON));
        assertThat(validator.validate(token()).hasErrors()).isFalse();server.verify();server.reset();
        server.expect(requestTo("/api/v1/auth/me")).andRespond(withStatus(HttpStatus.UNAUTHORIZED));
        assertThat(validator.validate(token()).hasErrors()).isTrue();server.verify();
    }
    @Test void failsClosedWhenIdentityIsUnavailable() {
        var builder=RestClient.builder();var server=MockRestServiceServer.bindTo(builder).build();
        server.expect(requestTo("/api/v1/auth/me")).andRespond(withStatus(HttpStatus.SERVICE_UNAVAILABLE));
        assertThat(new LiveSessionValidator(builder.build()).validate(token()).hasErrors()).isTrue();
    }
    @Test void rejectsForeignProfileAndMissingSession() {
        var builder=RestClient.builder();var server=MockRestServiceServer.bindTo(builder).build();
        server.expect(requestTo("/api/v1/auth/me")).andRespond(withSuccess("{\"data\":{\"userId\":\"5\"}}",MediaType.APPLICATION_JSON));
        var validator=new LiveSessionValidator(builder.build());assertThat(validator.validate(token()).hasErrors()).isTrue();
        assertThat(validator.validate(Jwt.withTokenValue("test").header("alg","HS256").subject("4").build()).hasErrors()).isTrue();
        server.verify();
    }
}
