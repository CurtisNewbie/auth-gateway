package com.curtisnewbie.gateway.filter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * @author yongjie.zhuang
 */
@Slf4j
public class LogFilter implements GlobalFilter {

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        log.info("Requesting URI: {}, service: {}", exchange.getRequest().getURI(), exchange.getRequest().getHeaders().get("service"));
        return chain.filter(exchange);
    }
}
