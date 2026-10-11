package com.equipmentrental.gateway;

import java.util.UUID;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class TraceIdFilter implements WebFilter {
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        String incoming = exchange.getRequest().getHeaders().getFirst("X-Trace-Id");
        String traceId = incoming != null && incoming.matches("[A-Za-z0-9_-]{1,64}")
                ? incoming : UUID.randomUUID().toString();
        exchange.getResponse().getHeaders().set("X-Trace-Id", traceId);
        ServerWebExchange traced = exchange.mutate().request(request -> request.headers(
                headers -> headers.set("X-Trace-Id", traceId))).build();
        return chain.filter(traced);
    }
}
