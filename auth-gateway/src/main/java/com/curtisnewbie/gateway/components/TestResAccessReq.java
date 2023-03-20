package com.curtisnewbie.gateway.components;

import lombok.Data;

/**
 * @author yongj.zhuang
 */
@Data
public class TestResAccessReq {
    private String roleNo;
    private String url;
    private String method;
}
