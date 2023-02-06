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
import com.coresky.web.model.RedisModel;
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
//                        String value = redisTools.template.opsForValue().get("time:lock:" + redisModel.getContract() + ":" + redisModel.getTokenId());
//                        if(null != value) {
//                            if((TimeTool.timestamp() - Integer.valueOf(value)) < 60) {
//                                logger.info("1 分钟内，不进行更新！");
//                                return;
//                            }
//                        }
//                        redisTools.template.opsForValue().set("time:lock:" + redisModel.getContract() + ":" + redisModel.getTokenId(),
//                                String.valueOf(TimeTool.timestamp()));
                        //当前数据
                        //CkUserTokenEntity ckUserTokenEntity = ckUserTokenMapper.find(redisModel.getContract(), redisModel.getTokenId());
                        ListOfferModel.OrderInfo orderInfo = null;
                        String osOfferPrice = null;
                        Map<String, Object> ckOrderPrice = null;
                        String ckAuctionPrice = null;
                        //cs 价格
                        ckOrderPrice = queryOrderMergeData(redisModel.getContract(), redisModel.getTokenId());
                        logger.info("cs 返回最低挂单价格 ： " + ckOrderPrice);
                        ckAuctionPrice = queryAuctionMergeData(redisModel.getContract(), redisModel.getTokenId());
                        logger.info("cs 返回最低挂单价格 ： " + ckAuctionPrice);
                        //os 价格
                        try {
                            orderInfo = queryOpenseaListed(redisModel.getContract(), redisModel.getTokenId());
                            logger.info("os 返回最低挂单价格 ： " + orderInfo);
                        } catch (Throwable e) {
                            logger.info("os 返回最低挂单 Error: " + e.getMessage());
                        }
                        try {
                            osOfferPrice = queryOpenseaOffer(redisModel.getContract(), redisModel.getTokenId());
                            logger.info("os 返回最高报价价格 ： " + osOfferPrice);
                        } catch (Throwable e) {
                            logger.info("os 返回最高报价 Error: " + e.getMessage());
                        }
                        Map<String, String> minPrice = diffPrice(ckOrderPrice, orderInfo);
                        String offerPrice = diffOfferPrice(ckAuctionPrice, osOfferPrice);
                        //更新数据库(未在售)
                        if(minPrice.get("basePrice").equals("0")) {
                            ckOrdersMapper.updateUserTokenState(
                                    redisModel.getContract(), redisModel.getTokenId(),
                                    0,
                                    "0", offerPrice,
                                    "0", ckAuctionPrice,
                                    "0", osOfferPrice,
                                    "0"
                            );
                        } else {
                            ckOrdersMapper.updateUserTokenState(
                                    redisModel.getContract(), redisModel.getTokenId(),
                                    -1,
                                    minPrice.get("basePrice"), offerPrice,
                                    null != ckOrderPrice ? ckOrderPrice.get("base_price").toString() : "0", ckAuctionPrice,
                                    null != orderInfo ? orderInfo.getCurrent_price() : "0", osOfferPrice,
                                    minPrice.get("expirationTime")
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

    public Map<String, String> diffPrice(Map<String, Object> ckOrderPrice, ListOfferModel.OrderInfo orderInfo) {
        Map<String, String> result = new HashMap<>();
        result.put("basePrice", "0");
        result.put("expirationTime", "0");
        if(null == orderInfo) {
            if(null != ckOrderPrice) {
                result.put("basePrice", ckOrderPrice.get("base_price").toString());
                result.put("expirationTime", ckOrderPrice.get("expiration_time").toString());
            }
        } else if(null == ckOrderPrice) {
            result.put("basePrice", orderInfo.getCurrent_price());
            result.put("expirationTime", orderInfo.getExpiration_time());
        } else {
            //取价格 小的 作为 挂单价格
            if(new BigDecimal(ckOrderPrice.get("base_price").toString()).compareTo(new BigDecimal(orderInfo.getCurrent_price())) < 0) {
                result.put("basePrice", ckOrderPrice.get("base_price").toString());
                result.put("expirationTime", ckOrderPrice.get("expiration_time").toString());
            } else {
                result.put("basePrice", orderInfo.getCurrent_price());
                result.put("expirationTime", orderInfo.getExpiration_time());
            }
        }
        return result;
    }

    public String diffOfferPrice(String csp, String osp) {
        if(null == csp) {
            if(null != osp) {
                return osp;
            }
        } else if(osp == null) {
            return csp;
        } else {
            //取价格 大的 作为 报价
            if(new BigDecimal(csp).compareTo(new BigDecimal(osp)) > 0) {
                return csp;
            } else {
                return osp;
            }
        }
        return "0";
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

    public String queryAuctionMergeData(String contract, String tokenId) {
        QueryWrapper<CkAuctionEntity> queryWrapper = ckAuctionMapper.createQueryWrapper();
        queryWrapper.eq("state", "0");
        queryWrapper.eq("contract", contract);
        queryWrapper.eq("token_id", tokenId);
        queryWrapper.orderByDesc("base_price");
        queryWrapper.select("base_price, expiration_time");
        List<Map<String, Object>> result = ckAuctionMapper.selectMapsPage(new Page<>(1,1), queryWrapper).getRecords();
        if (result.size() > 0) {
            return result.get(0).get("base_price").toString();
        }
        return null;
    }

    public String queryOpenseaOffer(String contract, String tokenId) throws IOException {
        String url = environment.getProperty("opensea.api") + "/v2/orders/"+ environment.getProperty("opensea.chain") +"/seaport/offers?"+
                "asset_contract_address="+ contract +"&limit=1&token_ids="+tokenId+"&order_by=eth_price&order_direction=desc";
        String value = createHttpBuild(url);
        ListOfferModel listOfferModel = JSON.parseObject(value, ListOfferModel.class);
        if(null != listOfferModel.getOrders()) {
            if(listOfferModel.getOrders().size() > 0) {
                return listOfferModel.getOrders().get(0).getCurrent_price();
            }
        }
        return null;
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
