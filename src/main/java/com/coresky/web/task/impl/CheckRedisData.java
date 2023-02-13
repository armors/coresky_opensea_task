package com.coresky.web.task.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.coresky.web.entity.CkAuctionEntity;
import com.coresky.web.entity.CkOrdersEntity;
import com.coresky.web.entity.CkUserTokenEntity;
import com.coresky.web.mapper.CkAuctionMapper;
import com.coresky.web.mapper.CkOrdersMapper;
import com.coresky.web.mapper.CkUserTokenMapper;
import com.coresky.web.model.ListOfferModel;
import com.coresky.web.model.PriceModel;
import com.coresky.web.model.RedisModel;
import com.coresky.web.utils.BigDecimalTool;
import com.coresky.web.utils.OpenSeaClient;
import com.coresky.web.utils.RedisTools;
import com.coresky.web.utils.TimeTool;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.apache.commons.lang3.StringUtils;
import org.bouncycastle.pqc.jcajce.provider.lms.BCLMSPrivateKey;
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
import java.math.BigDecimal;
import java.net.URISyntaxException;
import java.time.Duration;
import java.util.HashMap;
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
    private CkUserTokenMapper ckUserTokenMapper;

    private int taskSize;

    private int maxTaskSize = 10;

    public void startTask() {
        if(taskSize >= maxTaskSize) {
            logger.info("Redis 线程数量已满！");
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
                        CkUserTokenEntity ckUserTokenEntity = ckUserTokenMapper.find(redisModel.getContract(), redisModel.getTokenId());
                        if(null == ckUserTokenEntity) {
                            logger.info("数据无记录，不进行更新！");
                            return;
                        }
                        //os 价格
                        try {
                            PriceModel priceModelOs;
                            try {
                                ListOfferModel.OrderInfo orderInfo = queryOpenseaListed(redisModel.getContract(), redisModel.getTokenId());
                                logger.info("os 返回最低挂单价格 ： " + orderInfo);
                                priceModelOs = PriceModel.initModel(
                                        orderInfo.getCurrent_price(),
                                        orderInfo.getListing_time(),
                                        orderInfo.getExpiration_time()
                                );
                            } catch (Throwable e) {
                                priceModelOs = PriceModel.nullModel();
                            }
                            PriceModel priceModelCs = PriceModel.initModel(
                                    ckUserTokenEntity.getListedPriceCs(),
                                    ckUserTokenEntity.getListingTimeCs(),
                                    ckUserTokenEntity.getExpirationTimeCs()
                            );
                            PriceModel priceModel = BigDecimalTool.minValue(priceModelCs, priceModelOs);
                            logger.info("os + " + JSON.toJSONString(priceModelOs));
                            logger.info("cs + " + JSON.toJSONString(priceModelCs));
                            logger.info("pm + " + JSON.toJSONString(priceModel));
                            //无价格，直接更新 cs 价格，推送更新队列
                            if(priceModel.getBasePrice().compareTo(BigDecimal.ZERO) > 0) {
                                ckUserTokenMapper.updateTokenSale(ckUserTokenEntity.getId(), -1,
                                        priceModel.getBasePrice().toString(),
                                        priceModel.getListingTime().toString(),
                                        priceModel.getExpirationTime().toString(),
                                        priceModelOs.getBasePrice().toString(),
                                        priceModelOs.getListingTime().toString(),
                                        priceModelOs.getExpirationTime().toString()
                                );
                            } else {
                                ckUserTokenMapper.updateTokenSale(ckUserTokenEntity.getId(), 0,
                                        "0", "0", "0", "0","0","0"
                                );
                            }
                        } catch (Throwable e) {
                            logger.info("os 返回最低挂单 Error: " + e.getMessage());
                        }
                        try {
                            BigDecimal osOfferPrice = queryOpenseaOffer(redisModel.getContract(), redisModel.getTokenId());
                            logger.info("os 返回最高报价价格 ： " + osOfferPrice);
                            BigDecimal offerPrice = BigDecimalTool.maxValue(osOfferPrice, BigDecimalTool.BigDecimal(ckUserTokenEntity.getOfferPriceCs()));
                            ckUserTokenMapper.updateOfferPrice(ckUserTokenEntity.getId(), offerPrice.toString(), osOfferPrice.toString());
                        } catch (Throwable e) {
                            logger.info("os 返回最高报价 Error: " + e.getMessage());
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

    public BigDecimal queryOpenseaOffer(String contract, String tokenId) throws IOException {
        String url = environment.getProperty("opensea.api") + "/v2/orders/"+ environment.getProperty("opensea.chain") +"/seaport/offers?"+
                "asset_contract_address="+ contract +"&limit=1&token_ids="+tokenId+"&order_by=eth_price&order_direction=desc";
        String value = createHttpBuild(url);
        ListOfferModel listOfferModel = JSON.parseObject(value, ListOfferModel.class);
        if(null != listOfferModel.getOrders()) {
            if(listOfferModel.getOrders().size() > 0) {
                return BigDecimalTool.BigDecimal(listOfferModel.getOrders().get(0).getCurrent_price());
            }
        }
        return BigDecimal.ZERO;
    }

    public ListOfferModel.OrderInfo queryOpenseaListed(String contract, String tokenId) throws IOException {
        String url = environment.getProperty("opensea.api") + "/v2/orders/"+ environment.getProperty("opensea.chain") +"/seaport/listings?"+
                "asset_contract_address="+ contract +"&limit=1&token_ids="+tokenId+"&order_by=eth_price&order_direction=asc";
        String value = createHttpBuild(url);
        ListOfferModel listOfferModel = JSON.parseObject(value, ListOfferModel.class);
        if(null != listOfferModel.getOrders()) {
            if(listOfferModel.getOrders().size() > 0) {
                return listOfferModel.getOrders().get(0);
            }
        }
        return null;
    }

    public String createHttpBuild(String url) throws IOException {
        OkHttpClient client = new OkHttpClient();
        Request.Builder builder = new Request.Builder().url(url).get().addHeader("accept", "application/json");
        if(environment.getProperty("opensea.chain").equals("ethereum")) {
            builder.addHeader("X-API-KEY", environment.getProperty("opensea.key"));
        }
        Response response = client.newCall(builder.build()).execute();
        String value = response.body().string();
        logger.info("os exec: " + url);
        logger.info("os exec: " + value);
        return value;
    }
}
