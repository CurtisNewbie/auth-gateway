package com.curtisnewbie.gateway.filter;

import lombok.Getter;
import org.springframework.core.Ordered;

/**
 * Order of Filters
 *
 * @author yongj.zhuang
 */
@Getter
public enum FilterOrder {

    FIRST(Ordered.HIGHEST_PRECEDENCE),

    SECOND(after(FIRST));

    private final int order;

    FilterOrder(int order) {
        this.order = order;
    }

    private static int after(FilterOrder f) {
        return f.getOrder() + 1;
    }
}
