package com.curtisnewbie.gateway.config;

import com.curtisnewbie.gateway.constants.DefinedPaths;
import org.springframework.context.annotation.Configuration;

import static com.curtisnewbie.common.util.ValueUtils.equalsAnyIgnoreCase;

/**
 * @author yongj.zhuang
 */
@Configuration
public class AccessLogConfig {

    /** Check whether this request should be logged */
    public boolean isAccessLogged(String path) {
        // todo store these urls in database
        return equalsAnyIgnoreCase(path,
                DefinedPaths.LOGIN_PATH
        );
    }

}
