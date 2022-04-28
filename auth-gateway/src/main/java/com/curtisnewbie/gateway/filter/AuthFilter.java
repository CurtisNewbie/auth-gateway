package com.curtisnewbie.gateway.filter;

import com.curtisnewbie.common.util.JsonUtils;
import com.curtisnewbie.common.vo.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;

/**
 * Authentication filter
 *
 * @author yongjie.zhuang
 */
@Slf4j
@Component
public class AuthFilter implements GlobalFilter, Ordered {

    private static final String AUTHORIZATION_HEADER = "authorization";

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        // permit login request
        if (isLoginRequest(exchange))
            return chain.filter(exchange);

        HttpHeaders headers = exchange.getRequest().getHeaders();
        ServerHttpResponse resp = exchange.getResponse();

        // extract token from HttpHeaders
        final Optional<String> tokenOpt = getFirstToken(headers);
        if (!tokenOpt.isPresent()) {
            writeError("Please login first", resp);
            return Mono.empty();
        }

        final Mono<Result<String>> resultMono = exchangeToken(tokenOpt.get());
        final Result<String> result = resultMono.block();

        if (result == null) {
            writeError("Unexpected Technical Error", resp);
            return Mono.empty();
        }

        if (result.hasError()) {
            write(result, resp);
            return Mono.empty();
        }

        return chain.filter(exchange);
    }

    /** Check whether this request is a login request */
    private boolean isLoginRequest(final ServerWebExchange exg) {
        return exg.getRequest().getURI().getPath().equalsIgnoreCase("/auth-service/api/login");
    }

    /** Validate the token */
    private Mono<Result<String>> exchangeToken(final String token) {
        Assert.notNull(token, "token is empty");
        return WebClient.create().get()
                .uri("http://auth-service/remote/user/token" + token)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<Result<String>>() {
                });
    }

    /** Extract the first token from headers */
    private Optional<String> getFirstToken(final HttpHeaders headers) {
        if (headers.containsKey(AUTHORIZATION_HEADER))
            return Optional.empty();

        List<String> tokens = headers.get(AUTHORIZATION_HEADER);
        if (tokens == null || tokens.isEmpty())
            return Optional.empty();

        // always get the first token
        return Optional.ofNullable(tokens.get(0));
    }

    /** Write error message to response */
    private void writeError(final String errMsg, final ServerHttpResponse resp) {
        write(Result.error(errMsg), resp);
    }

    /** Write error message to response */
    private void write(final Result<?> result, final ServerHttpResponse resp) {
        try {
            resp.setStatusCode(HttpStatus.BAD_REQUEST); // todo which status code do we set?
            final String errorMsg = JsonUtils.writeValueAsString(result);
            final DataBuffer buf = resp.bufferFactory().wrap(errorMsg.getBytes(StandardCharsets.UTF_8));
            resp.writeWith(Flux.just(buf));
        } catch (IOException e) {
            throw new IllegalStateException("Failed to deserialize response message", e);
        }
    }

    @Override
    public int getOrder() {
        return HIGHEST_PRECEDENCE; // first
    }
}
