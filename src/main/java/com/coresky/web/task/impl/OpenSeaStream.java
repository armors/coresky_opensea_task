package com.coresky.web.task.impl;

import com.coresky.web.utils.OpenSeaClient;
import com.coresky.web.utils.RedisTools;
import com.coresky.web.utils.StringTools;
import com.coresky.web.utils.TimeTool;
import org.apache.commons.lang3.StringUtils;
import org.java_websocket.client.WebSocketClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.net.URISyntaxException;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

@Component
public class OpenSeaStream {

    Logger logger = LoggerFactory.getLogger(OpenSeaStream.class);

    @Autowired
    private Environment environment;

    private OpenSeaClient webSocketClient;

    @Autowired
    private RedisTools redisTools;

    @Autowired
    private ConfigurableApplicationContext configurableApplicationContext;

    public void startTask() throws URISyntaxException {
        redisTools.template.opsForValue().set("stream:last:check:time", String.valueOf(TimeTool.timestamp()));
        String url = environment.getProperty("opensea.url") + "?token=60e3ddec66674376a7522204e5fc6701";
        logger.info("启动服务！" + url);
        webSocketClient = new OpenSeaClient(url, configurableApplicationContext);
        webSocketClient.connect();
    }

    public void phoenix() throws URISyntaxException {
        String status = redisTools.template.opsForValue().get("stream:last:check:status");
        if(null != status && status.equals("reboot")) {
            logger.info("重启指令，重启监控！");
            redisTools.template.delete("stream:last:check:status");
            System.exit(SpringApplication.exit(configurableApplicationContext));
            return;
        }
        if(null != webSocketClient) {
            long lastTime = TimeTool.timestamp() - Long.valueOf(redisTools.template.opsForValue().get("stream:last:check:time"));
            logger.info("心跳检查: " + lastTime);
            if(lastTime > 70) {
                redisTools.template.delete("stream:last:check:status");
                System.exit(SpringApplication.exit(configurableApplicationContext));
            } else {
                if(webSocketClient.isOpen()) {
                    webSocketClient.send("{\"topic\":\"phoenix\",\"event\":\"heartbeat\",\"payload\":{},\"ref\":0}".getBytes());
                }
            }
        }
    }
}
