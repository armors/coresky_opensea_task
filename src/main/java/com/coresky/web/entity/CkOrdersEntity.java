package com.coresky.web.entity;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.coresky.web.utils.StringTools;
import lombok.Data;
import org.apache.commons.codec.digest.DigestUtils;

import java.math.BigDecimal;

@Data
@TableName("ck_orders")
public class CkOrdersEntity extends BaseEntity {

    private String exchange;

    private String maker;

    private String taker;

    private String basePrice;

    private BigDecimal makerRelayerFee;

    private BigDecimal takerRelayerFee;

    private BigDecimal makerProtocolFee;

    private BigDecimal takerProtocolFee;

    private String feeRecipient;

    private int feeMethod;

    private int contractType;

    private int side;

    private int saleKind;

    private String target;

    private BigDecimal howToCall;

    private String calldata;

    private String replacementPattern;

    private String staticTarget;

    private String staticExtradata;

    private String paymentToken;

    private BigDecimal extra;

    private BigDecimal listingTime;

    private BigDecimal expirationTime;

    private long completeTime;

    private BigDecimal salt;

    private BigDecimal v;

    private String r;

    private String s;

    private String sign;

    private String contract;

    private String tokenId;

    private int state;

    private String name;

    private String image;

    private String attributes;

    private String description;

    private String tokenUri;

    private String hashKey;

    private String hash;

    private int collectionId;

    private String txHash;

    private int type;

    private int auctionId;

    private String unkey;

    private int amount;

    @TableField(exist = false)
    private CkCollectionsInfoEntity ckCollectionsInfoEntity;
}
