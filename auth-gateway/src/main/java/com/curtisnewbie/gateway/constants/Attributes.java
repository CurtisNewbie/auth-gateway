package com.curtisnewbie.gateway.constants;

import lombok.Getter;

/**
 * Exchange Attributes
 *
 * @author yongj.zhuang
 */
@Getter
public enum Attributes {

    /** TUser */
    TUSER("tuser"),

    /** JWT Token */
    TOKEN("token");

    private final String key;

    Attributes(String key) {
        this.key = key;
    }
}
