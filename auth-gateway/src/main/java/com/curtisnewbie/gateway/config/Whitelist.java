package com.curtisnewbie.gateway.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;

import static com.curtisnewbie.common.util.ValueUtils.equalsAnyIgnoreCase;

/**
 * Whitelist
 *
 * @author yongj.zhuang
 */
@Configuration
public class Whitelist {

    private static final String LOGIN_PATH = "/auth-service/open/api/user/login";
    private static final String REG_PATH = "/auth-service/open/api/user/register/request";
    private static final String FILE_DOWNLOAD_PATH = "/file-service/open/api/file/token/download";


    /** Check whether this request is in whitelist */
    public boolean isInWhitelist(String path, HttpMethod method) {
        path = preprocessing(path, method);

        // todo store these urls in database
        return equalsAnyIgnoreCase(path,
                LOGIN_PATH,
                REG_PATH,
                FILE_DOWNLOAD_PATH);
    }

    /** Check if it's a login request */
    public boolean isLoginRequest(String path, HttpMethod method) {
        path = preprocessing(path, method);
        return equalsAnyIgnoreCase(path, LOGIN_PATH);
    }

    /** Preprocess the path, e.g., remove all query parameters if it's a get request */
    public static String preprocessing(String path, HttpMethod method) {
        if (method == HttpMethod.GET) {
            int i = path.indexOf("?");
            if (i != -1)
                path = path.substring(0, i);
        }
        return path;
    }
}
