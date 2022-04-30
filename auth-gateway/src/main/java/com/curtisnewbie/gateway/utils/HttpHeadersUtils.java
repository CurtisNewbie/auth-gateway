package com.curtisnewbie.gateway.utils;

import org.springframework.http.HttpHeaders;
import org.springframework.util.Assert;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Utils for {@link HttpHeaders}
 *
 * @author yongj.zhuang
 */
public final class HttpHeadersUtils {

    private HttpHeadersUtils() {

    }

    /** Get all values of the header */
    public static List<String> getAll(final HttpHeaders headers, final String key) {
        Assert.notNull(headers, "headers == null");
        Assert.notNull(headers, "key == null");

        if (!headers.containsKey(key))
            return Collections.emptyList();

        List<String> tokens = headers.get(key);
        if (tokens == null || tokens.isEmpty())
            return Collections.emptyList();

        return tokens;
    }

    /** Get the first value of the header */
    public static Optional<String> getFirst(final HttpHeaders headers, final String key) {
        Assert.notNull(headers, "headers == null");
        Assert.notNull(headers, "key == null");

        List<String> l = getAll(headers, key);
        if (l.isEmpty())
            return Optional.empty();

        return Optional.ofNullable(l.get(0));
    }
}
