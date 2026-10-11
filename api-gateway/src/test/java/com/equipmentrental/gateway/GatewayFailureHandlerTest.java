package com.equipmentrental.gateway;

import static org.junit.jupiter.api.Assertions.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.ConnectException;
import java.net.SocketTimeoutException;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;

class GatewayFailureHandlerTest {
    private final ObjectMapper mapper = new ObjectMapper();
    private final GatewayFailureHandler handler = new GatewayFailureHandler(mapper);

    @Test
    void connectionFailureReturns503WithoutInternalDetails() throws Exception {
        var exchange = MockServerWebExchange.from(MockServerHttpRequest.post("/api/v1/ai/chat"));
        exchange.getResponse().getHeaders().set("X-Trace-Id", "test-123");
        handler.handle(exchange, new RuntimeException(new ConnectException("secret-internal-host"))).block();
        assertEquals(HttpStatus.SERVICE_UNAVAILABLE, exchange.getResponse().getStatusCode());
        var body = mapper.readTree(exchange.getResponse().getBodyAsString().block());
        assertEquals("DOWNSTREAM_UNAVAILABLE", body.get("code").asText());
        assertEquals("test-123", body.get("traceId").asText());
        assertFalse(body.toString().contains("secret-internal-host"));
    }

    @Test
    void timeoutReturns504() {
        var exchange = MockServerWebExchange.from(MockServerHttpRequest.get("/api/v1/ai/models"));
        handler.handle(exchange, new SocketTimeoutException()).block();
        assertEquals(HttpStatus.GATEWAY_TIMEOUT, exchange.getResponse().getStatusCode());
    }

    @Test
    void unrelatedApplicationErrorIsNotMisreportedAsUnavailable() {
        var exchange = MockServerWebExchange.from(MockServerHttpRequest.get("/unknown"));
        assertThrows(IllegalArgumentException.class,
                () -> handler.handle(exchange, new IllegalArgumentException("test")).block());
    }

    @Test
    void traceIdIsForwardedAndReturned() {
        var exchange = MockServerWebExchange.from(MockServerHttpRequest.get("/actuator/health").header("X-Trace-Id", "demo-123"));
        new TraceIdFilter().filter(exchange, traced -> {
            assertEquals("demo-123", traced.getRequest().getHeaders().getFirst("X-Trace-Id"));
            return reactor.core.publisher.Mono.empty();
        }).block();
        assertEquals("demo-123", exchange.getResponse().getHeaders().getFirst("X-Trace-Id"));
    }

    @Test
    void invalidTraceIdIsReplaced() {
        var exchange = MockServerWebExchange.from(MockServerHttpRequest.get("/actuator/health").header("X-Trace-Id", "not a valid id"));
        new TraceIdFilter().filter(exchange, traced -> reactor.core.publisher.Mono.empty()).block();
        assertTrue(exchange.getResponse().getHeaders().getFirst("X-Trace-Id").matches("[a-f0-9-]{36}"));
    }
}
