package com.curtisnewbie.gateway.recorder;

import com.curtisnewbie.common.util.Runner;
import com.curtisnewbie.service.auth.messaging.services.AuthMessageDispatcher;
import com.curtisnewbie.service.auth.remote.vo.AccessLogInfoVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * <p>
 * AccessLog Recorder based on MQ
 * </p>
 *
 * @author yongjie.zhuang
 */
@Slf4j
@Component
public class MqAccessLogRecorder implements AccessLogRecorder {

    @Autowired
    private AuthMessageDispatcher dispatcher;

    @Override
    public void recordAccess(RecordAccessCmd cmd) {
        AccessLogInfoVo acsLog = new AccessLogInfoVo();
        acsLog.setIpAddress(cmd.getRemoteAddr());
        acsLog.setAccessTime(LocalDateTime.now());
        acsLog.setUserId(cmd.getUserId());
        acsLog.setUsername(cmd.getUsername());

        Runner.runSafely(() -> dispatcher.dispatchAccessLog(acsLog), e -> log.warn("Unable to save access-log", e));
    }
}
