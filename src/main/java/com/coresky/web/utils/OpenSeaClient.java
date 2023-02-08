package com.coresky.web.utils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.coresky.web.mapper.CkCollectionsInfoMapper;
import com.coresky.web.model.EventModel;
import org.apache.commons.lang3.StringUtils;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.drafts.Draft_6455;
import org.java_websocket.handshake.ServerHandshake;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class OpenSeaClient extends WebSocketClient {

    Logger logger = LoggerFactory.getLogger(OpenSeaClient.class);

    CkCollectionsInfoMapper ckCollectionsInfoMapper;

    ConfigurableApplicationContext configurableApplicationContext;

    RedisTools redisTools;

    public OpenSeaClient(String url, ConfigurableApplicationContext cac) throws URISyntaxException {
        super(new URI(url), new Draft_6455());
        configurableApplicationContext = cac;
        redisTools = configurableApplicationContext.getBean(RedisTools.class);
        ckCollectionsInfoMapper = configurableApplicationContext.getBean(CkCollectionsInfoMapper.class);
    }

    @Override
    public void onOpen(ServerHandshake serverHandshake) {
        logger.info("OpenSeaClient:onOpen，检查是否需要添加监控");
        try {
            //关闭所有订阅
            ckCollectionsInfoMapper.closeAll();
            //重新订阅
            ckCollectionsInfoMapper.selectList(ckCollectionsInfoMapper.createQueryWrapper()).forEach(ckCollectionsInfoEntity -> {
                if(ckCollectionsInfoEntity.getStatus() == 1 && !StringUtils.isEmpty(ckCollectionsInfoEntity.getName())) {
                        logger.info("订阅：" + ckCollectionsInfoEntity.getName());
                        Map<String, String> map = new HashMap<>();
                        map.put("topic", "collection:" + ckCollectionsInfoEntity.getName());
                        map.put("event", "phx_join");
                        map.put("payload", "{}");
                        map.put("ref", "0");
                        String key = JSON.toJSONString(map);
                        logger.info(key);
                        send(key.getBytes());
                }
            });
        } catch (Throwable e) {
            logger.info("添加监控失败！");
        }
        //连接成功，记录一次心跳
        redisTools.template.opsForValue().set("stream:last:check:time", String.valueOf(TimeTool.timestamp()));
    }

    /**
     * {"event":"phx_reply","payload":{"response":{},"status":"ok"},"ref":"0","topic":"collection:unidentified-contract-epwlrzbghl"}
     * @param s
     */
    @Override
    public void onMessage(String s) {
        redisTools.template.opsForValue().set("stream:last:check:time", String.valueOf(TimeTool.timestamp()));
        logger.info("OpenSeaClient:onMessage:" + s);
        Map map = (Map) JSONObject.parse(s);
        String topicName = map.get("topic").toString();
        String logInfo = null;
        if(map.get("event").toString().equals("phx_reply")) {
            //记录当前心跳
            Map payload = (Map) JSONObject.parse(map.get("payload").toString());
            if(payload.get("status").equals("ok")) {
                try {
                    if(!StringUtils.isEmpty(topicName) && !Objects.equals(topicName, "phoenix")) {
                        logInfo = "订阅成功！";
                    } else {
                        logger.info("心跳检查完成！");
                    }
                } catch (Throwable e) {
                    logger.info(e.getMessage());
                }
            }
        } else {
            Map payload = (Map) JSONObject.parse(map.get("payload").toString());
            EventModel eventModel = JSONObject.parseObject(payload.get("payload").toString(), EventModel.class);
            String[] nftInfo = eventModel.getItem().getNft_id().split("/");
            logger.info("updateTokenPrice:" + nftInfo[1] + ":" + nftInfo[2] + ":" + "OPENSEA_STREAM");
            Map<String, Object> queue = new HashMap<>();
            queue.put("source", "opensea");
            queue.put("contract", nftInfo[1]);
            queue.put("tokenId", nftInfo[2]);
            queue.put("event", "OPENSEA_STREAM");
            redisTools.template.opsForList().leftPush("user:token:queue", JSONObject.toJSONString(queue));
            logInfo = "Event：" + map.get("event").toString() + ":" + eventModel.getItem().getNft_id();
        }
        if(null != logInfo) {
            topicName = topicName.replace("collection:", "");
            ckCollectionsInfoMapper.updateByTopic(topicName, TimeTool.datetime() +":"+ logInfo);
        }
    }

    @Override
    public void onClose(int i, String s, boolean b) {
        logger.info("OpenSeaClient:onClose:" + s);
        redisTools.template.delete("stream:last:check:status");
        System.exit(SpringApplication.exit(configurableApplicationContext));
    }

    @Override
    public void onError(Exception e) {
        logger.info("OpenSeaClient:onError:" + e.getMessage());
        redisTools.template.delete("stream:last:check:status");
        System.exit(SpringApplication.exit(configurableApplicationContext));
    }
}
