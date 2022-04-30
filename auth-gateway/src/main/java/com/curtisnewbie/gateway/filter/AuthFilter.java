package com.curtisnewbie.gateway.filter;

import com.curtisnewbie.common.data.ChainedMap;
import com.curtisnewbie.common.trace.TUser;
import com.curtisnewbie.common.trace.TraceUtils;
import com.curtisnewbie.common.vo.Result;
import com.curtisnewbie.gateway.constants.HeaderConst;
import com.curtisnewbie.gateway.utils.HttpHeadersUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.net.URLEncoder;
import java.util.Map;
import java.util.Optional;

import static com.curtisnewbie.gateway.constants.HeaderConst.AUTHORIZATION;
import static com.curtisnewbie.gateway.utils.ServerHttpResponseUtils.write;
import static org.springframework.util.StringUtils.hasText;

/**
 * Authentication filter
 *
 * @author yongjie.zhuang
 */
@Slf4j
@Component
public class AuthFilter implements GlobalFilter, Ordered {

    @Autowired
    private WebClient.Builder webClientBuilder;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        // permit login request
        if (isLoginRequest(exchange))
            return chain.filter(exchange);

        final ServerHttpResponse resp = exchange.getResponse();

        // extract token from HttpHeaders
        final Optional<String> tokenOpt = HttpHeadersUtils.getFirst(exchange.getRequest().getHeaders(), AUTHORIZATION);
        final String token;
        if (!tokenOpt.isPresent() || !hasText(token = tokenOpt.get())) {
            resp.setStatusCode(HttpStatus.UNAUTHORIZED);
            return writeError("Please login first", resp);
        }

        return validateToken(token)
                .flatMap(result -> {
                    if (result.hasError()) {
                        resp.setStatusCode(HttpStatus.BAD_REQUEST);
                        return write(result, resp);
                    }

                    // authorized, setup the tracing info
                    setupTraceInfo(result.getData());

                    return chain.filter(exchange);
                });
    }

    @Override
    public int getOrder() {
        return FilterOrder.SECOND.getOrder();
    }

    // ------------------------------------------- private helper methods -----------------------------

    private static void setupTraceInfo(Map<String, String> data) {
        final TUser tu = TUser.builder()
                .userId(Integer.parseInt(data.get("id")))
                .username(data.get("username"))
                .role(data.get("role"))
                .build();
        TraceUtils.putTUser(tu);
    }

    /** Check whether this request is a login request */
    private boolean isLoginRequest(final ServerWebExchange exg) {
        // todo change url after we refactor auth-service
        return exg.getRequest().getURI().getPath().equalsIgnoreCase("/auth-service/api/token/login-for-token");
    }

    /** Validate the token */
    private Mono<Result<Map<String, String>>> validateToken(final String token) {
        return webClientBuilder.build()
                .get()
                .uri("http://auth-service/api/token/user?token=" + token)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<Result<Map<String, String>>>() {
                });
    }

    /** Write error message to response */
    private Mono<Void> writeError(final String errMsg, final ServerHttpResponse resp) {
        return write(Result.error(errMsg), resp);
    }

}
