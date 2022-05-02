package com.curtisnewbie.gateway.filter;

import com.curtisnewbie.common.trace.TUser;
import com.curtisnewbie.gateway.config.Whitelist;
import com.curtisnewbie.gateway.constants.Attributes;
import com.curtisnewbie.gateway.recorder.AccessLogRecorder;
import com.curtisnewbie.gateway.recorder.RecordAccessCmd;
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

/**
 * Record filter
 *
 * @author yongj.zhuang
 */
@Component
public class RecordFilter implements GlobalFilter, Ordered {

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

        final Integer userId;
        final String username;
        if (tUser != null) {
            userId = tUser.getUserId();
            username = tUser.getUsername();
        } else {
            userId = 0;
            username = "anonymous";
        }

        final InetAddress address = remoteAddress.getAddress(); // nullable
        accessLogRecorder.recordAccess(RecordAccessCmd.builder()
                .remoteAddr(address == null ? "unknown" : address.toString())
                .userId(userId)
                .username(username)
                .build());
    }

}
