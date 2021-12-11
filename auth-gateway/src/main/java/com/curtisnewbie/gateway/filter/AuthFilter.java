package com.curtisnewbie.gateway.filter;

import com.curtisnewbie.common.util.JsonUtils;
import com.curtisnewbie.common.vo.Result;
import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpResponse;
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
public class AuthFilter implements GlobalFilter {

    private static final String JWT_TOKEN = "token";

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        // permit login request
        if (isLoginRequest(exchange))
            return chain.filter(exchange);

        HttpHeaders headers = exchange.getRequest().getHeaders();
        ServerHttpResponse resp = exchange.getResponse();

        // extract token from HttpHeaders
        Optional<String> tokenOpt = getFirstToken(headers);

        // token not present or invalid, user should sign-in first
        if (!tokenOpt.isPresent() || !isTokenValid(tokenOpt.get())) {
            writeErrorMsg("Please login first", resp);
            return Mono.empty();
        }

        return chain.filter(exchange);
    }

    /** Check whether this request is a login request */
    private boolean isLoginRequest(final ServerWebExchange exg) {
        return exg.getRequest().getURI().getPath().startsWith("/login");
    }

    /** Validate the token */
    private boolean isTokenValid(final String token) {

        // todo impl this
        return false;
    }

    /** Extract the first token from headers */
    private Optional<String> getFirstToken(final HttpHeaders headers) {
        if (headers.containsKey(JWT_TOKEN))
            return Optional.empty();

        List<String> tokens = headers.get(JWT_TOKEN);
        if (tokens.isEmpty())
            return Optional.empty();

        // always get the first token
        return Optional.ofNullable(tokens.get(0));
    }

    /** Write error message to response */
    private void writeErrorMsg(final String errMsg, final ServerHttpResponse resp) {
        try {
            final String errorMsg = JsonUtils.writeValueAsString(Result.error(errMsg));
            final DataBuffer buf = resp.bufferFactory().wrap(errorMsg.getBytes(StandardCharsets.UTF_8));
            resp.writeWith(Flux.just(buf));
        } catch (IOException e) {
            throw new IllegalStateException("Failed to deserialize response messgae", e);
        }
    }
}
