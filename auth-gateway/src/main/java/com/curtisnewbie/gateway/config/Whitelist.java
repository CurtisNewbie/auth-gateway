package com.curtisnewbie.gateway.config;

import org.springframework.context.annotation.Configuration;

import static com.curtisnewbie.common.util.ValueUtils.equalsAnyIgnoreCase;
import static com.curtisnewbie.gateway.constants.DefinedPaths.*;

/**
 * Whitelist
 *
 * @author yongj.zhuang
 */
@Configuration
public class Whitelist {

    /** Check whether this request is in whitelist */
    public boolean isInWhitelist(String path) {
        // todo store these urls in database
        return equalsAnyIgnoreCase(path,
                LOGIN_PATH,
                REG_PATH,
                FILE_DOWNLOAD_PATH,
                FANTAHSEA_DOWNLOAD_PATH,
                MEDIA_STREAMING_PATH
        );
    }

    /** Check whether the path requires permission */
    public boolean requiresPermission(String path) {
        return !equalsAnyIgnoreCase(path, INFO_PATH);
    }

}
