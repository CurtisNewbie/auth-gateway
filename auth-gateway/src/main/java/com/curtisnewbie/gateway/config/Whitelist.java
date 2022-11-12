package com.curtisnewbie.gateway.config;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;

import static com.curtisnewbie.common.util.ValueUtils.equalsAnyIgnoreCase;
import static com.curtisnewbie.gateway.constants.DefinedPaths.*;

/**
 * Whitelist
 *
 * @author yongj.zhuang
 */
@Slf4j
@Data
@Configuration
@ConfigurationProperties(prefix = "whitelist")
public class Whitelist implements InitializingBean {

    /** whitelist.request-url */
    private String[] requestUrl;

    @Override
    public void afterPropertiesSet() throws Exception {
        log.info("Whitelist, request-url: {}", Arrays.toString(requestUrl));
    }

    /** Check whether this request is in whitelist */
    public boolean isInWhitelist(String path) {
        return equalsAnyIgnoreCase(path, requestUrl);
    }

    /** Check whether the path requires permission */
    public boolean requiresPermission(String path) {
        return !equalsAnyIgnoreCase(path, INFO_PATH);
    }
}
