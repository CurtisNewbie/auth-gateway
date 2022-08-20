package com.curtisnewbie.gateway.config;

import org.springframework.context.annotation.Configuration;

import static com.curtisnewbie.common.util.ValueUtils.equalsAnyIgnoreCase;

/**
 * Whitelist
 *
 * @author yongj.zhuang
 */
@Configuration
public class Whitelist {

    private static final String INFO_PATH = "/auth-service/open/api/user/info";

    private static final String LOGIN_PATH = "/auth-service/open/api/user/login";
    private static final String REG_PATH = "/auth-service/open/api/user/register/request";
    private static final String FILE_DOWNLOAD_PATH = "/file-service/open/api/file/token/download";
    private static final String FANTAHSEA_DOWNLOAD_PATH = "/fantahsea/open/api/gallery/image/download";


    /** Check whether this request is in whitelist */
    public boolean isInWhitelist(String path) {
        // todo store these urls in database
        return equalsAnyIgnoreCase(path,
                LOGIN_PATH,
                REG_PATH,
                FILE_DOWNLOAD_PATH,
                FANTAHSEA_DOWNLOAD_PATH
        );
    }

    /** Check whether the path requires permission */
    public boolean requiresPermission(String path) {
        return !equalsAnyIgnoreCase(path, INFO_PATH);
    }

}
