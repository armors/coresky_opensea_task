package com.coresky.web.mapper;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.coresky.web.entity.CkOrdersEntity;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

/**
 * <p>
 *  Mapper 接口
 * </p>
 *
 * @author 
 * @since 2022-10-31
 */
public interface CkOrdersMapper extends BaseMapper<CkOrdersEntity> {

    default QueryWrapper<CkOrdersEntity> createQueryWrapper() {
        QueryWrapper<CkOrdersEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("deleted", 0);
        return queryWrapper;
    }

    @Update("UPDATE `ck_user_token` SET `state`=#{state},`base_price`=#{listedPrice},`expiration_time`=#{expirationTime},`listed_price`=#{listedPrice},`offer_price`=#{offerPrice}"+
            ",`listed_price_cs`=#{ckOrderPriceValue},`listed_price_os`=#{osListedPrice},"+
            "`offer_price_cs`=#{ckAuctionPriceValue},`offer_price_os`=#{osOfferPrice} "+
            "WHERE `contract`=#{contract} AND `token_id`=#{tokenId}")
    int updateUserTokenState(
            @Param("contract") String contract,
            @Param("tokenId") String tokenId,
            @Param("state") Integer state,
            @Param("listedPrice") String listedPrice,
            @Param("offerPrice") String offerPrice,
            @Param("ckOrderPriceValue") String ckOrderPriceValue,
            @Param("ckAuctionPriceValue") String ckAuctionPriceValue,
            @Param("osListedPrice") String osListedPrice,
            @Param("osOfferPrice") String osOfferPrice,
            @Param("expirationTime") String expirationTime
    );
}
