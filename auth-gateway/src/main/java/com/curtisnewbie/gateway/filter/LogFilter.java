package com.curtisnewbie.gateway.filter;

import com.curtisnewbie.common.trace.TUser;
import com.curtisnewbie.common.util.StopWatchUtils;
import com.curtisnewbie.gateway.constants.Attributes;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;
import org.springframework.util.StopWatch;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.List;

import static com.curtisnewbie.gateway.utils.HttpHeadersUtils.getAll;

/**
 * Log filter
 *
 * @author yongj.zhuang
 */
@Slf4j
@Component
public class LogFilter implements GlobalFilter, Ordered {

    private static final String X_FORWARDED_FOR = "X-Forwarded-For";

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        final ServerHttpRequest request = exchange.getRequest();
        final String requestPath = exchange.getAttribute(Attributes.PATH.getKey());

        final TUser tUser = exchange.getAttribute(Attributes.TUSER.getKey());
        logRequest(request, requestPath, tUser);

        final StopWatch sw = StopWatchUtils.startNew();
        return chain.filter(exchange).then(Mono.fromRunnable(() -> {
            sw.stop();
            log.info("Request handled for {}ms, path: '{}'", sw.getTotalTimeMillis(), requestPath);
        }));
    }

    @Override
    public int getOrder() {
        return FilterOrder.SECOND.getOrder();
    }

    private void logRequest(final ServerHttpRequest request, final String path, @Nullable final TUser tUser) {
        final List<String> userAgents = request.getHeaders().get("user-agent");
        final int userId;
        final String username;
        if (tUser != null) {
            userId = tUser.getUserId();
            username = tUser.getUsername();
        } else {
            userId = 0;
            username = "anonymous";
        }
        final List<String> forwarded = getAll(request.getHeaders(), X_FORWARDED_FOR);
        log.info("Inbound request received path: '{}', userId: '{}', username: '{}', x-forwarded-for: '{}', userAgents: {}",
                path, userId, username, forwarded, userAgents);
    }

}
