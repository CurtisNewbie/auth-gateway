package com.curtisnewbie.gateway.filter;

import com.auth0.jwt.interfaces.DecodedJWT;
import com.curtisnewbie.common.trace.TUser;
import com.curtisnewbie.common.trace.TraceUtils;
import com.curtisnewbie.common.util.UrlUtils;
import com.curtisnewbie.common.vo.Result;
import com.curtisnewbie.gateway.components.TestResAccessReq;
import com.curtisnewbie.gateway.components.TestResAccessResp;
import com.curtisnewbie.gateway.config.Whitelist;
import com.curtisnewbie.gateway.constants.Attributes;
import com.curtisnewbie.gateway.utils.HttpHeadersUtils;
import com.curtisnewbie.gateway.utils.RequestUrlUtils;
import com.curtisnewbie.module.jwt.domain.api.JwtDecoder;
import com.curtisnewbie.module.jwt.vo.DecodeResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static com.curtisnewbie.gateway.constants.HeaderConst.AUTHORIZATION;
import static com.curtisnewbie.gateway.utils.ServerHttpResponseUtils.writeError;
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
    private Whitelist whitelist;
    @Autowired
    private JwtDecoder jwtDecoder;
    @Autowired
    private WebClient.Builder webClientBuilder;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {

        ServerHttpRequest request = exchange.getRequest();
        final String requestPath = RequestUrlUtils.stripOffParam(request.getURI().getPath(), request.getMethod());
        exchange.getAttributes().put(Attributes.PATH.getKey(), requestPath);
        final ServerHttpResponse resp = exchange.getResponse();

        // extract token from HttpHeaders
        final Optional<String> tokenOpt = HttpHeadersUtils.getFirst(exchange.getRequest().getHeaders(), AUTHORIZATION);
        String token = null;
        TUser user = null;

        // requests may or may not be authenticated, some requests are 'PUBLIC', we just try to extract the user info from it
        if (tokenOpt.isPresent() && hasText(token = tokenOpt.get())) {
            final DecodeResult decodeResult = jwtDecoder.decode(token); // try to validate the token
            if (decodeResult.isValid()) {
                user = toTUser(decodeResult.getDecodedJWT()); // decode TUser object
                // set context attribute
                exchange.getAttributes().put(Attributes.TUSER.getKey(), user);
                exchange.getAttributes().put(Attributes.TOKEN.getKey(), token);

                // setup the tracing info
                TraceUtils.putTUser(user);
            }
        }

        // whitelist, doesn't require authorization
        if (whitelist.isInWhitelist(requestPath)) {
            return chain.filter(exchange);
        }

        // test resource access
        TestResAccessReq tra = new TestResAccessReq();
        tra.setRoleNo(user != null ? user.getRoleNo() : "");
        tra.setUrl(requestPath);
        tra.setMethod(request.getMethodValue());

        final Mono<Result<TestResAccessResp>> resMono = testResourceAccess(tra);
        return resMono.flatMap(res -> {

            if (!res.isOk()) {
                resp.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR);
                log.error("Failed to test resource access, requestPath: {}, code: {}, msg: {}", requestPath, res.getErrorCode(), res.getMsg());
                return writeError("Unknown Server Error", resp);
            }

            if (!res.getData().isValid()) {
                resp.setStatusCode(HttpStatus.FORBIDDEN);
                return writeError("Not permitted", resp);
            }

            return chain.filter(exchange);
        });
    }

    @Override
    public int getOrder() {
        return FilterOrder.FIRST.getOrder();
    }

    // ------------------------------------------- private helper methods -----------------------------

    /** Test user's access the resource, it may also be a unauthenticated call where the roleNo is null */
    private Mono<Result<TestResAccessResp>> testResourceAccess(TestResAccessReq req) {
        return webClientBuilder.build()
                .post()
                .uri("http://goauth/remote/path/resource/access-test")
                .bodyValue(req)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<Result<TestResAccessResp>>() {
                });
    }

    private static TUser toTUser(DecodedJWT jwt) {
        String ss = jwt.getClaim("services").asString();
        if (ss == null) ss = "";

        return TUser.builder()
                .userId(Integer.parseInt(jwt.getClaim("id").asString()))
                .username(jwt.getClaim("username").asString())
                .role(jwt.getClaim("role").asString())
                .userNo(jwt.getClaim("userno").asString())
                .roleNo(jwt.getClaim("roleno").asString())
                .services(Arrays.asList(ss.split(",")))
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

    /** Extract service name from request path */
    private static String serviceName(final String requestPath) {
        if (requestPath == null) return null;
        return UrlUtils.segment(0, requestPath);
    }

    /** Check if the user is permitted to use the service */
    private static boolean isUserPermittedToUseService(TUser user, String serviceName) {
        if (user.getRole().equals("admin")) return true;

        final List<String> services = user.getServices();
        if (serviceName == null) return true; // it will be a 404 anyway
        if (services == null) return false;

        // this list will be pretty small
        return services.contains(serviceName);
    }
}
