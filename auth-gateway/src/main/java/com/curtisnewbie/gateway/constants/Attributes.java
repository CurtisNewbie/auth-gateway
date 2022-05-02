package com.curtisnewbie.gateway.constants;

import lombok.Getter;

/**
 * Exchange Attributes
 *
 * @author yongj.zhuang
 */
@Getter
public enum Attributes {

    CONTEXT("context");

    private final String key;

    Attributes(String key) {
        this.key = key;
    }
}
