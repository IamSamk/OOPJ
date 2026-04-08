package com.ticketing.common.security;

import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.server.authentication.ServerAuthenticationConverter;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
public class BearerTokenServerAuthenticationConverter implements ServerAuthenticationConverter {

    @Override
    public Mono<Authentication> convert(ServerWebExchange exchange) {
        String headerValue = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (headerValue == null || !headerValue.startsWith("Bearer ")) {
            return Mono.empty();
        }
        String token = headerValue.substring(7);
        return Mono.just(new UsernamePasswordAuthenticationToken(token, token));
    }
}
