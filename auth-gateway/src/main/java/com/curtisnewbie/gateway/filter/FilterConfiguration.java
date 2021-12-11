package com.curtisnewbie.gateway.filter;

import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * <p>
 * <p>
 * Configuration of filters
 * </p>
 *
 * @author yongjie.zhuang
 */
@Configuration
public class FilterConfiguration {

    @Bean
    public GlobalFilter customFilter() {
        return new LogFilter();
    }
}
