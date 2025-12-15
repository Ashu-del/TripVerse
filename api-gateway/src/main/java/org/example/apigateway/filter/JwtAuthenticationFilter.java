package org.example.apigateway.filter;

import org.springframework.http.*;
import org.example.apigateway.security.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
public class JwtAuthenticationFilter implements GlobalFilter, Ordered {

    private final JwtUtil jwtUtil;

    @Autowired
    public JwtAuthenticationFilter(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String path = exchange.getRequest().getPath().value();

        // 1. Allow /auth/** without token
        if (path.startsWith("/auth/")) {
            return chain.filter(exchange);
        }

        // 2. Read Authorization header
        String authHeader = exchange.getRequest()
                .getHeaders()
                .getFirst(HttpHeaders.AUTHORIZATION);

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return unauthorized(exchange, "Missing or invalid Authorization header");
        }

        String token = authHeader.substring(7);

        // 3. Validate token
        if (!jwtUtil.isTokenValid(token)) {
            return unauthorized(exchange, "Invalid or expired token");
        }

        // 4. Extract user info and add as headers for downstream services
        String userId = jwtUtil.extractUserId(token);
        String username = jwtUtil.extractUsername(token);
        String role = jwtUtil.extractRole(token);

        ServerWebExchange mutatedExchange = exchange.mutate()
                .request(builder -> builder
                        .header("X-User-Id", userId)
                        .header("X-Username", username)
                        .header("X-User-Role", role)
                )
                .build();

        return chain.filter(mutatedExchange);

    }

    private Mono<Void> unauthorized(ServerWebExchange exchange, String message) {
        exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
        exchange.getResponse().getHeaders().setContentType(MediaType.TEXT_PLAIN);
        byte[] bytes = message.getBytes();
        return exchange.getResponse().writeWith(
                Mono.just(exchange.getResponse()
                        .bufferFactory()
                        .wrap(bytes))
        );
    }

    @Override
    public int getOrder() {
        return -1;
    }
}
