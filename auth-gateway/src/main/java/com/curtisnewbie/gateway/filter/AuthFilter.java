package com.curtisnewbie.gateway.filter;

import com.curtisnewbie.common.trace.TraceUtils;
import com.curtisnewbie.common.vo.Result;
import com.curtisnewbie.gateway.constants.HeaderConst;
import com.curtisnewbie.gateway.utils.HttpHeadersUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.Optional;

import static com.curtisnewbie.gateway.utils.ServerHttpResponseUtils.write;

/**
 * Authentication filter
 *
 * @author yongjie.zhuang
 */
@Slf4j
@Component
public class AuthFilter implements GlobalFilter, Ordered {

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        // permit login request
        if (isLoginRequest(exchange))
            return chain.filter(exchange);

        final HttpHeaders reqHeaders = exchange.getRequest().getHeaders();
        final ServerHttpResponse resp = exchange.getResponse();

        // extract token from HttpHeaders
        final Optional<String> tokenOpt = HttpHeadersUtils.getFirst(reqHeaders, HeaderConst.AUTHORIZATION);
        if (!tokenOpt.isPresent()) {
            resp.setStatusCode(HttpStatus.UNAUTHORIZED);
            return writeError("Please login first", resp);
        }

        final String token = tokenOpt.get();
        return validateToken(token)
                .flatMap(result -> {
                    if (result.hasError()) {
                        resp.setStatusCode(HttpStatus.BAD_REQUEST);
                        return write(result, resp);
                    }

                    // todo get some validation info from auth-service, so that we can record more information in the baggage
                    // authorized, setup the tracing info
                    TraceUtils.put(TraceUtils.AUTH_TOKEN, token);

                    return chain.filter(exchange);
                });
    }

    /** Check whether this request is a login request */
    private boolean isLoginRequest(final ServerWebExchange exg) {
        // todo change url after we refactor auth-service
        return exg.getRequest().getURI().getPath().equalsIgnoreCase("/auth-service/api/login");
    }

    /** Validate the token */
    private Mono<Result<String>> validateToken(final String token) {
        Assert.notNull(token, "token is empty");
        return WebClient.create().get()
                .uri("http://auth-service/remote/user/token" + token) // todo change url after we refactor auth-service
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<Result<String>>() {
                });
    }

    /** Write error message to response */
    private Mono<Void> writeError(final String errMsg, final ServerHttpResponse resp) {
        return write(Result.error(errMsg), resp);
    }

    @Override
    public int getOrder() {
        return FilterOrder.SECOND.getOrder();
    }
}
