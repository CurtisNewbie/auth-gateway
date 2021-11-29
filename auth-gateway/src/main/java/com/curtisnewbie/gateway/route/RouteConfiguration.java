package com.curtisnewbie.gateway.route;

import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


/**
 * Configuration of Routes
 *
 * @author yongjie.zhuang
 */
@Configuration
public class RouteConfiguration {

    /** 'service' header name */
    private static final String SERVICE_HEADER = "service";

    /** file-service on 'service' header */
    private static final String FILE_SERVICE_HEADER = "file-service";
    /** auth-service on 'service' header */
    private static final String AUTH_SERVICE_HEADER = "auth-service";

    /** url of file-service */
    private static final String FILE_SERVICE_URL = "https://file-service:8082";

    /** url of auth-service */
    private static final String AUTH_SERVICE_URL = "https://auth-service:8084";

    @Bean
    public RouteLocator routes(RouteLocatorBuilder builder) {
        return builder.routes()
                .route(p -> p
                        .header(SERVICE_HEADER, FILE_SERVICE_HEADER)
                        .uri(FILE_SERVICE_URL))
                .route(p -> p
                        .header(SERVICE_HEADER, AUTH_SERVICE_HEADER)
                        .uri(AUTH_SERVICE_URL))
                .build();
    }
}
