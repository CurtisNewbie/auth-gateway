package com.curtisnewbie.gateway.config;

import org.springframework.context.annotation.Configuration;

/**
 * @author yongj.zhuang
 */
@Configuration
public class AccessLogConfig {

    /** Check whether this request should be logged */
    public boolean isAccessLogged(String path) {
        // todo store these urls in database
//        return equalsAnyIgnoreCase(path,
//                DefinedPaths.LOGIN_PATH
//        );
        return false;
    }

}
