package com.coresky.web.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Data
@TableName("ck_collections_info")
public class CkCollectionsInfoEntity extends BaseEntity {
    
    private String name;
    private String info;
    private String contract;
    private String volume;
    private String listed;
    private String image;
    private String bannerImage;
    private String startHeight;
    private String weights;
    private String isCertification;
    private String currencyAddress;
    private String exchangeRate;
    private String isCacheJson;
    private String cacheStatus;
    private String feeContract;
    private String website;
    private String discord;
    private String twitter;
    private String telegram;
    private float total;
    private float holder;
    private String dayVol;
    private String weekVol;
    private String monthVol;
    private String foolPrice;
    private String owner;
    private int followCount;
    private int listReward;
    private int buyReward;
    private int sellReward;
    private int royalty;

    private int openseaStatus;

    private String openseaName;

    private int openseaRevStatus;

    private String openseaLog;
}
