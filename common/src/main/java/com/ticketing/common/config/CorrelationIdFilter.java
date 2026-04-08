package com.ticketing.common.config;

import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Component
public class CorrelationIdFilter implements WebFilter {

    public static final String HEADER_NAME = "X-Correlation-Id";

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        String correlationId = exchange.getRequest().getHeaders().getFirst(HEADER_NAME);
        if (correlationId == null || correlationId.isBlank()) {
            correlationId = UUID.randomUUID().toString();
        }
        exchange.getResponse().getHeaders().add(HEADER_NAME, correlationId);
        MDC.put(HEADER_NAME, correlationId);
        String finalCorrelationId = correlationId;
        return chain.filter(exchange)
                .contextWrite(context -> context.put(HEADER_NAME, finalCorrelationId))
                .doFinally(signalType -> MDC.remove(HEADER_NAME));
    }
}
