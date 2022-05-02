package com.curtisnewbie.gateway.recorder;

import lombok.Builder;
import lombok.Data;

/**
 * RecordAccess command object
 *
 * @author yongjie.zhuang
 */
@Data
@Builder
public class RecordAccessCmd {

    private String remoteAddr;
    private String username;
    private Integer userId;
}
