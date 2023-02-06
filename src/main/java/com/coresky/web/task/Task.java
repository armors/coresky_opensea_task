package com.coresky.web.task;

import com.coresky.web.task.impl.CheckRedisData;
import com.coresky.web.task.impl.OpenSeaStream;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import javax.annotation.Resource;
import java.net.URISyntaxException;

@Component
@EnableAsync
public class Task {

    @Resource
    OpenSeaStream openSeaStream;

    @Resource
    CheckRedisData checkRedisData;

    /**
     * 心跳
     */
    @Scheduled(fixedRate = 20000)
    @Async
    public void openSeaStream() throws URISyntaxException {
        openSeaStream.phoenix();
    }

    /**
     * 数据处理
     */
    @Scheduled(fixedRate = 10000)
    @Async
    public void checkRedisData() throws URISyntaxException {
        checkRedisData.startTask();
    }
}
