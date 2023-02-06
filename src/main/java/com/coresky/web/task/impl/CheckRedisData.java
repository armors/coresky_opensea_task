package com.coresky.web.task.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.coresky.web.entity.CkAuctionEntity;
import com.coresky.web.entity.CkOrdersEntity;
import com.coresky.web.mapper.CkAuctionMapper;
import com.coresky.web.mapper.CkOrdersMapper;
import com.coresky.web.model.ListOfferModel;
import com.coresky.web.model.RedisModel;
import com.coresky.web.utils.OpenSeaClient;
import com.coresky.web.utils.RedisTools;
import com.coresky.web.utils.TimeTool;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.validator.constraints.pl.REGON;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.IOException;
import java.net.URISyntaxException;
import java.time.Duration;
import java.util.List;
import java.util.Map;

@Component
public class CheckRedisData {

    Logger logger = LoggerFactory.getLogger(CheckRedisData.class);

    @Autowired
    private Environment environment;

    @Autowired
    private RedisTools redisTools;

    @Resource
    private CkAuctionMapper ckAuctionMapper;

    @Resource
    private CkOrdersMapper ckOrdersMapper;

    private int taskSize;

    private int maxTaskSize = 10;

    public void startTask() {
        if(taskSize >= maxTaskSize) {
            logger.info("线程数量已"+maxTaskSize+"达到！");
            return;
        }
        taskSize++;
        logger.info("检查 Redis ["+ taskSize +"/"+ maxTaskSize +"] 数据！");
        try {
            while (true) {
                String data = redisTools.template.opsForList().rightPop("user:token:queue", Duration.ofSeconds(30));
                logger.info("检查数据：" + data);
                if(!StringUtils.isEmpty(data)) {
                    // os 价格
                    try {
                        RedisModel redisModel = JSONObject.parseObject(data, RedisModel.class);
                        ListOfferModel.OrderInfo orderInfo = null;
                        String osOfferPrice = null;
                        try {
                            orderInfo = queryOpenseaListed(redisModel.getContract(), redisModel.getTokenId());
                            logger.info("os 返回最低挂单价格 ： " + orderInfo);
                        } catch (Throwable e) {
                            logger.info("查询opensea价格错误" + e.getMessage());
                        }
                        try {
                            osOfferPrice = queryOpenseaOffer(redisModel.getContract(), redisModel.getTokenId());
                            logger.info("os 返回最高报价价格 ： " + osOfferPrice);
                        } catch (Throwable e) {
                            logger.info("查询opensea价格错误" + e.getMessage());
                        }
                        // cs 价格
                        Map<String, Object> ckOrderPrice = queryOrderMergeData(redisModel.getContract(), redisModel.getTokenId());
                        String ckOrderPriceValue = null != ckOrderPrice ? ckOrderPrice.get("base_price").toString() : null;
                        String ckExpirationTimeValue = null != ckOrderPrice ? ckOrderPrice.get("expiration_time").toString() : null;
                        Map<String, Object> ckAuctionPrice = queryAuctionMergeData(redisModel.getContract(), redisModel.getTokenId());
                        String ckAuctionPriceValue = null != ckAuctionPrice ? ckAuctionPrice.get("base_price").toString() : null;
                        logger.info("查询 cs 价格：" + ckOrderPriceValue + " -> " + ckAuctionPriceValue);
                        String listedPrice = StringUtils.isEmpty(ckOrderPriceValue) ? (null != orderInfo ? orderInfo.getCurrent_price() : null) : ckOrderPriceValue;
                        String expirationTime = StringUtils.isEmpty(ckExpirationTimeValue) ? (null != orderInfo ? orderInfo.getExpiration_time() : null) : ckExpirationTimeValue;
                        String offerPrice = StringUtils.isEmpty(ckAuctionPriceValue) ? osOfferPrice : ckAuctionPriceValue;
                        //更新数据库(未在售)
                        if(StringUtils.isEmpty(listedPrice)) {
                            ckOrdersMapper.updateUserTokenState(
                                    redisModel.getContract(), redisModel.getTokenId(),
                                    0,
                                    listedPrice, offerPrice,
                                    ckOrderPriceValue, ckAuctionPriceValue,
                                    (null != orderInfo ? orderInfo.getCurrent_price() : null), osOfferPrice,
                                    expirationTime
                            );
                        } else {
                            ckOrdersMapper.updateUserTokenState(
                                    redisModel.getContract(), redisModel.getTokenId(),
                                    -1,
                                    listedPrice, offerPrice,
                                    ckOrderPriceValue, ckAuctionPriceValue,
                                    (null != orderInfo ? orderInfo.getCurrent_price() : null), osOfferPrice,
                                    expirationTime
                            );
                        }
                    } catch (Throwable e) {
                        logger.info("解析失败：" + e.getMessage());
                    }
                }
            }
        } catch (Throwable e) {
            taskSize--;
        }
    }

    public Map<String, Object> queryOrderMergeData(String contract, String tokenId) {
        QueryWrapper<CkOrdersEntity> queryWrapper = ckOrdersMapper.createQueryWrapper();
        queryWrapper.eq("state", "0");
        queryWrapper.eq("type", "1");
        queryWrapper.eq("contract", contract);
        queryWrapper.eq("token_id", tokenId);
        queryWrapper.orderByAsc("base_price");
        queryWrapper.select("base_price, expiration_time");
        List<Map<String, Object>> result = ckOrdersMapper.selectMapsPage(new Page<>(1,1), queryWrapper).getRecords();
        if (result.size() > 0) {
            return result.get(0);
        }
        return null;
    }

    public Map<String, Object> queryAuctionMergeData(String contract, String tokenId) {
        QueryWrapper<CkAuctionEntity> queryWrapper = ckAuctionMapper.createQueryWrapper();
        queryWrapper.eq("state", "0");
        queryWrapper.eq("contract", contract);
        queryWrapper.eq("token_id", tokenId);
        queryWrapper.orderByDesc("base_price");
        queryWrapper.select("base_price, expiration_time");
        List<Map<String, Object>> result = ckAuctionMapper.selectMapsPage(new Page<>(1,1), queryWrapper).getRecords();
        if (result.size() > 0) {
            return result.get(0);
        }
        return null;
    }

    public String queryOpenseaOffer(String contract, String tokenId) throws IOException {
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url(environment.getProperty("opensea.api") + "/v2/orders/"+ environment.getProperty("opensea.chain") +"/seaport/offers?"+
                        "asset_contract_address="+ contract +"&limit=1&token_ids="+tokenId+"&order_by=eth_price&order_direction=desc")
                .get()
                .addHeader("accept", "application/json")
                .build();
        Response response = client.newCall(request).execute();
        ListOfferModel listOfferModel = JSON.parseObject(response.body().string(), ListOfferModel.class);
        if(listOfferModel.getOrders().size() > 0) {
            return listOfferModel.getOrders().get(0).getCurrent_price();
        }
        return null;
    }

    public ListOfferModel.OrderInfo queryOpenseaListed(String contract, String tokenId) throws IOException {
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url(environment.getProperty("opensea.api") + "/v2/orders/"+ environment.getProperty("opensea.chain") +"/seaport/listings?"+
                        "asset_contract_address="+ contract +"&limit=1&token_ids="+tokenId+"&order_by=eth_price&order_direction=asc")
                .get()
                .addHeader("accept", "application/json")
                .build();
        Response response = client.newCall(request).execute();
        ListOfferModel listOfferModel = JSON.parseObject(response.body().string(), ListOfferModel.class);
        if(listOfferModel.getOrders().size() > 0) {
            return listOfferModel.getOrders().get(0);
        }
        return null;
    }
}
