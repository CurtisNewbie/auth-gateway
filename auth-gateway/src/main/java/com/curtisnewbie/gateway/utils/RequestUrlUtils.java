package com.curtisnewbie.gateway.utils;

import org.springframework.http.HttpMethod;

/**
 * @author yongj.zhuang
 */
public final class RequestUrlUtils {

    private RequestUrlUtils() {

    }

    /** Remove all query parameters if it's a get request */
    public static String tripOffParam(String path, HttpMethod method) {
        if (path == null || method == null) return null;

        if (method == HttpMethod.GET) {
            int i = path.indexOf("?");
            if (i != -1)
                path = path.substring(0, i);
        }
        return path;
    }
}
