package com.curtisnewbie.gateway.filter;

import com.auth0.jwt.interfaces.DecodedJWT;
import com.curtisnewbie.common.data.BiContainer;
import com.curtisnewbie.common.trace.TUser;
import com.curtisnewbie.common.trace.TraceUtils;
import com.curtisnewbie.common.util.AssertUtils;
import com.curtisnewbie.common.util.UrlUtils;
import com.curtisnewbie.common.vo.Result;
import com.curtisnewbie.gateway.config.Whitelist;
import com.curtisnewbie.gateway.constants.Attributes;
import com.curtisnewbie.gateway.utils.HttpHeadersUtils;
import com.curtisnewbie.module.jwt.domain.api.JwtDecoder;
import com.curtisnewbie.module.jwt.vo.DecodeResult;
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

import static com.curtisnewbie.gateway.constants.HeaderConst.AUTHORIZATION;
import static com.curtisnewbie.gateway.utils.ServerHttpResponseUtils.write;
import static com.curtisnewbie.service.auth.remote.consts.AuthServiceError.TOKEN_EXPIRED;
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
    @Autowired
    private Whitelist whitelist;
    @Autowired
    private JwtDecoder jwtDecoder;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {

        ServerHttpRequest request = exchange.getRequest();
        final String requestPath = request.getURI().getPath();
        final HttpMethod method = request.getMethod();
        final ServerHttpResponse resp = exchange.getResponse();

        // whitelist, doesn't require authorization
        if (whitelist.isInWhitelist(requestPath, method))
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

        // try to validate the token
        final DecodeResult decodeResult = jwtDecoder.decode(token);
        if (!decodeResult.isValid()) {
            final String msg = decodeResult.isExpired() ? "Token is expired" : "Please login first";
            resp.setStatusCode(HttpStatus.UNAUTHORIZED);
            return writeError(msg, resp);
        }

        // set context attribute
        final TUser tUser = toTUser(decodeResult.getDecodedJWT());
        exchange.getAttributes().put(Attributes.CONTEXT.getKey(), tUser);
        exchange.getAttributes().put(Attributes.TOKEN.getKey(), token);

        // setup the tracing info
        TraceUtils.putTUser(tUser);

        return chain.filter(exchange);
    }

    @Override
    public int getOrder() {
        return FilterOrder.FIRST.getOrder();
    }

    // ------------------------------------------- private helper methods -----------------------------

    private static TUser toTUser(DecodedJWT jwt) {
        return TUser.builder()
                .userId(Integer.parseInt(jwt.getClaim("id").asString()))
                .username(jwt.getClaim("username").asString())
                .role(jwt.getClaim("role").asString())
                .build();
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

    /** Retrieve token info from auth-service */
    @Deprecated
    private Mono<Result<Map<String, String>>> retrieveTokenInfo(final String token) {
        // todo we don't need to validate the token like this, it's a JWT :D
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
