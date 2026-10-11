package com.equipmentrental.gateway;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.Map;
import java.util.concurrent.TimeoutException;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebExceptionHandler;
import reactor.core.publisher.Mono;

@Component
@Order(-2)
public class GatewayFailureHandler implements WebExceptionHandler {
    private final ObjectMapper mapper;

    public GatewayFailureHandler(ObjectMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public Mono<Void> handle(ServerWebExchange exchange, Throwable error) {
        if (exchange.getResponse().isCommitted()) return Mono.error(error);
        boolean timeout = false;
        boolean unavailable = false;
        for (Throwable cause = error; cause != null; cause = cause.getCause()) {
            timeout |= cause instanceof TimeoutException || cause instanceof SocketTimeoutException
                    || cause instanceof io.netty.handler.timeout.TimeoutException
                    || cause instanceof ResponseStatusException responseError
                    && responseError.getStatusCode().value() == 504;
            unavailable |= cause instanceof ConnectException || cause instanceof UnknownHostException;
            if (cause.getCause() == cause) break;
        }
        if (!timeout && !unavailable) return Mono.error(error);
        HttpStatus status = timeout ? HttpStatus.GATEWAY_TIMEOUT : HttpStatus.SERVICE_UNAVAILABLE;
        String code = timeout ? "DOWNSTREAM_TIMEOUT" : "DOWNSTREAM_UNAVAILABLE";
        String message = timeout
                ? "Dịch vụ xử lý quá lâu. Vui lòng thử lại sau."
                : "Dịch vụ chưa sẵn sàng. Vui lòng kiểm tra service đang chạy hoặc thử lại sau.";
        try {
            byte[] body = mapper.writeValueAsBytes(Map.of("success", false, "code", code,
                    "message", message, "traceId", exchange.getResponse().getHeaders().getFirst("X-Trace-Id") == null
                            ? "" : exchange.getResponse().getHeaders().getFirst("X-Trace-Id")));
            exchange.getResponse().setStatusCode(status);
            exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);
            exchange.getResponse().getHeaders().setCacheControl("no-store");
            return exchange.getResponse().writeWith(Mono.just(exchange.getResponse().bufferFactory().wrap(body)));
        } catch (Exception encodingError) {
            return Mono.error(encodingError);
        }
    }
}
