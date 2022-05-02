package com.curtisnewbie.gateway.filter;

import com.curtisnewbie.common.trace.TUser;
import com.curtisnewbie.gateway.constants.Attributes;
import com.curtisnewbie.gateway.recorder.AccessLogRecorder;
import com.curtisnewbie.gateway.recorder.RecordAccessCmd;
import com.curtisnewbie.gateway.utils.HttpHeadersUtils;
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
@Component
public class RecordFilter implements GlobalFilter, Ordered {

    private static final String X_FORWARDED_FOR = "X-Forwarded-For";

    @Autowired
    private AccessLogRecorder accessLogRecorder;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {

        final ServerHttpRequest request = exchange.getRequest();

        final TUser tUser = exchange.getAttribute(Attributes.CONTEXT.getKey()); // nullable
        recordAccessLog(request, tUser);

        return chain.filter(exchange);
    }

    @Override
    public int getOrder() {
        return FilterOrder.SECOND.getOrder();
    }

    private void recordAccessLog(ServerHttpRequest request, @Nullable TUser tUser) {
        InetSocketAddress remoteAddress = request.getRemoteAddress();
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

        accessLogRecorder.recordAccess(RecordAccessCmd.builder()
                .remoteAddr(remoteAddr)
                .userId(userId)
                .username(username)
                .build());
    }

}
