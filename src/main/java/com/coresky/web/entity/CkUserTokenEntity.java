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

    private String address;

    private String contract;

    private int contractType;

    private String tokenId;

    private Integer amount;

    private String name;

    private String image;

    @JSONField(serialize = false)
    @JsonIgnore
    private String attributes;

    private Integer state;

    private String description;

    private String tokenUri;

    private Integer isShow;

    private String media;

    private String unkey;

    private String basePrice;

    @TableField(exist = false)
    private String bestPrice;

    private BigDecimal listingTime;

    private BigDecimal expirationTime;

    private long completeTime;

    @TableField(exist = false)
    private Map<String, Object> ckCollectionsInfoEntity;

    @TableField(exist = false)
    private List<CkOrdersEntity> ckOrdersEntityList;

    @TableField(exist = false)
    private List<CkAuctionEntity> ckAuctionEntityList;

    //关注状态
    @TableField(exist = false)
    private boolean followStatus = false;

    private String expirationTimeCs;

    private String expirationTimeOs;

    private String listingTimeCs;

    private String listingTimeOs;

    private String listedPriceCs;

    private String listedPriceOs;

    private String offerPriceCs;

    private String offerPriceOs;

    //图片转换
    public String getOriImage() {
        if(null == image) {
            return "";
        }
        if(image.startsWith("ipfs://")) {
            return image.replace("ipfs://","https://ipfs.io/ipfs/");
        } else {
            return image;
        }
    }
}
