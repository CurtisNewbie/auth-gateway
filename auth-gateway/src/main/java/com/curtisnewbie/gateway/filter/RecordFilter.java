package com.curtisnewbie.gateway.filter;

import com.curtisnewbie.common.trace.TUser;
import com.curtisnewbie.common.util.Runner;
import com.curtisnewbie.gateway.constants.Attributes;
import com.curtisnewbie.gateway.utils.RequestUrlUtils;
import com.curtisnewbie.service.auth.messaging.services.AuthMessageDispatcher;
import com.curtisnewbie.service.auth.remote.vo.AccessLogInfoVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.List;

import static com.curtisnewbie.gateway.utils.HttpHeadersUtils.getAll;

/**
 * Record filter
 *
 * @author yongj.zhuang
 */
@Slf4j
@Component
public class RecordFilter implements GlobalFilter, Ordered {

    private static final String X_FORWARDED_FOR = "X-Forwarded-For";

    @Autowired
    private AuthMessageDispatcher dispatcher;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {

        final ServerHttpRequest request = exchange.getRequest();

        final String requestPath = exchange.getAttribute(Attributes.PATH.getKey());
        final TUser tUser = exchange.getAttribute(Attributes.TUSER.getKey());
        final String token = exchange.getAttribute(Attributes.TOKEN.getKey());
        recordAccessLog(request, requestPath, tUser, token);

        return chain.filter(exchange);
    }

    @Override
    public int getOrder() {
        return FilterOrder.SECOND.getOrder();
    }

    private void recordAccessLog(final ServerHttpRequest request, final String path, @Nullable final TUser tUser,
                                 @Nullable final String token) {

        final InetSocketAddress remoteAddress = request.getRemoteAddress();
        if (remoteAddress == null)
            return; // disconnected already

        final int userId;
        final String username;
        if (tUser != null) {
            userId = tUser.getUserId();
            username = tUser.getUsername();
        } else {
            userId = 0;
            username = "anonymous";
        }

        // try to get remote address from header
        String remoteAddr = null;
        List<String> forwarded = getAll(request.getHeaders(), X_FORWARDED_FOR);
        if (!forwarded.isEmpty())
            remoteAddr = forwarded.get(0); // first one is the client's address

        if (remoteAddr == null) {
            final InetAddress address = remoteAddress.getAddress(); // nullable
            remoteAddr = address == null ? "unknown" : address.getHostAddress();
        }

        final AccessLogInfoVo p = new AccessLogInfoVo();
        p.setIpAddress(remoteAddr);
        p.setUserId(userId);
        p.setUsername(username);
        p.setUrl(path);
        p.setToken(token);
        Runner.runSafely(() -> dispatcher.dispatchAccessLog(p), e -> log.warn("Unable to dispatch access-log", e));
    }

}
