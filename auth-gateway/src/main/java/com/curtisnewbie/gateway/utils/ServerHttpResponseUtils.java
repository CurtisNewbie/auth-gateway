package com.curtisnewbie.gateway.utils;

import com.curtisnewbie.common.util.JsonUtils;
import com.curtisnewbie.common.vo.Result;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.server.reactive.ServerHttpResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * Utils for {@link ServerHttpResponse}
 *
 * @author yongj.zhuang
 */
public final class ServerHttpResponseUtils {

    private ServerHttpResponseUtils() {

    }

    /** Write message to response */
    public static Mono<Void> write(final Result<?> result, final ServerHttpResponse resp) {
        try {
            final String msg = JsonUtils.writeValueAsString(result);
            final DataBuffer buf = resp.bufferFactory().wrap(msg.getBytes(StandardCharsets.UTF_8));
            return resp.writeWith(Flux.just(buf));
        } catch (IOException e) {
            throw new IllegalStateException("Failed to write response message", e);
        }
    }

    /** Write error message to response */
    public static Mono<Void> writeError(final String errMsg, final ServerHttpResponse resp) {
        return write(Result.error(errMsg), resp);
    }
}
