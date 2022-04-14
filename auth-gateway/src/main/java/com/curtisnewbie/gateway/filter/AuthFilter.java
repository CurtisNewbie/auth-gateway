package com.curtisnewbie.gateway.filter;

import com.curtisnewbie.common.util.JsonUtils;
import com.curtisnewbie.common.vo.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
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

    // todo return a mono
    /** Validate the token */
    private boolean isTokenValid(final String token) {
//        buildAuthClient()
//                .post()
//                .contentType(MediaType.APPLICATION_JSON)
//                .body(Mono.just(null), Object.class) // todo

        // todo impl this
        return false;
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
    private void writeErrorMsg(final String errMsg, final ServerHttpResponse resp) {
        try {
            final String errorMsg = JsonUtils.writeValueAsString(Result.error(errMsg));
            final DataBuffer buf = resp.bufferFactory().wrap(errorMsg.getBytes(StandardCharsets.UTF_8));
            resp.writeWith(Flux.just(buf));
        } catch (IOException e) {
            throw new IllegalStateException("Failed to deserialize response messgae", e);
        }
    }

    @Override
    public int getOrder() {
        return HIGHEST_PRECEDENCE; // first
    }

    private WebClient buildAuthClient() {
        // todo endpoint used to validate and exchange token
        return WebClient.builder()
                .baseUrl("http://auth-service/")
                .build();
    }
}
