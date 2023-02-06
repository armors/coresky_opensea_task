package com.coresky.web.entity;

import com.alibaba.fastjson.annotation.JSONField;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Data
@TableName("ck_user_token")
public class CkUserTokenEntity extends BaseEntity {

    private String contract;

    private String tokenId;

    private String basePrice;

    private BigDecimal expirationTime;

    private String listedPrice;

    private String offerPrice;

    private String listedPriceCs;

    private String listedPriceOs;

    private String offerPriceCs;

    private String offerPriceOs;
}
