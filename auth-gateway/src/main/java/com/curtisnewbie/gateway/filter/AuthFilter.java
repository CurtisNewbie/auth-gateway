package com.curtisnewbie.gateway.filter;

import com.curtisnewbie.common.trace.TUser;
import com.curtisnewbie.common.trace.TraceUtils;
import com.curtisnewbie.common.util.UrlUtils;
import com.curtisnewbie.common.vo.Result;
import com.curtisnewbie.gateway.utils.HttpHeadersUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.Map;
import java.util.Optional;

import static com.curtisnewbie.common.util.ValueUtils.equalsAnyIgnoreCase;
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

        ServerHttpRequest request = exchange.getRequest();
        final String requestPath = request.getURI().getPath();
        final HttpMethod method = request.getMethod();
        final ServerHttpResponse resp = exchange.getResponse();

        // whitelist, doesn't require authorization
        if (isInWhitelist(requestPath, method))
            return chain.filter(exchange);

        // permit only open api requests
        if (!isOpenApi(requestPath)) {
            resp.setStatusCode(HttpStatus.UNAUTHORIZED);
            return writeError("Not permitted", resp);
        }

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

    // todo store these urls in database
    /** Check whether this request is in whitelist */
    private boolean isInWhitelist(String path, HttpMethod method) {
        if (method == HttpMethod.GET) {
            int i = path.indexOf("?");
            if (i != -1)
                path = path.substring(0, i);
        }

        return equalsAnyIgnoreCase(path,
                "/auth-service/open/api/user/login",
                "/auth-service/open/api/user/register/request",
                "/file-service/open/api/file/token/download");
    }

    /**
     * Check whether this request's path is open api, only open api can be accessed externally
     * <p>
     * E.g., '/auth-service/open/...' is an open api
     * <p>
     * But '/auth-service/some/api/...' is not
     */
    private boolean isOpenApi(final String path) {
        final String segAfterService = UrlUtils.segment(1, path);
        return segAfterService != null && segAfterService.equals("open");
    }

    /** Validate the token */
    private Mono<Result<Map<String, String>>> validateToken(final String token) {
        return webClientBuilder.build()
                .get()
                .uri("http://auth-service/open/api/token/user?token=" + token)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<Result<Map<String, String>>>() {
                });
    }

    /** Write error message to response */
    private Mono<Void> writeError(final String errMsg, final ServerHttpResponse resp) {
        return write(Result.error(errMsg), resp);
    }

}
