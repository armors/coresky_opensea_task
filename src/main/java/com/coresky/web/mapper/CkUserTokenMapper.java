package com.coresky.web.mapper;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.coresky.web.entity.CkUserTokenEntity;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;

/**
 * <p>
 *  Mapper 接口
 * </p>
 *
 * @author 
 * @since 2022-10-31
 */
public interface CkUserTokenMapper extends BaseMapper<CkUserTokenEntity> {

    default QueryWrapper<CkUserTokenEntity> createQueryWrapper() {
        QueryWrapper<CkUserTokenEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("deleted", 0);
        return queryWrapper;
    }

    default CkUserTokenEntity find(String contract, String tokenId) {
        QueryWrapper<CkUserTokenEntity> queryWrapper = createQueryWrapper();
        queryWrapper.eq("contract", contract);
        queryWrapper.eq("token_id", tokenId);
        return selectOne(queryWrapper);
    }

    @Update("UPDATE `ck_user_token` SET " +
            "`state`=#{state}, " +
            "`base_price`=#{basePrice}, " +
            "`listing_time`=#{listingTime}, " +
            "`expiration_time`=#{expirationTime}, " +
            "`listed_price_os`=#{basePrice}, " +
            "`listing_time_os`=#{listingTime}, " +
            "`expiration_time_os`=#{expirationTime}" +
            " WHERE `id`=#{id}")
    void updateTokenSale(
            @Param("id") int id,
            @Param("state") int state,
            @Param("basePrice") String basePrice,
            @Param("listingTime") String listingTime,
            @Param("expirationTime") String expirationTime
    );

    @Update("UPDATE `ck_user_token` SET " +
            "`best_price`=#{bestPrice}, " +
            "`offer_price_os`=#{osOfferPrice}" +
            " WHERE `id`=#{id}")
    void updateOfferPrice(
            @Param("id") int id,
            @Param("bestPrice") String bestPrice,
            @Param("osOfferPrice") String osOfferPrice
    );
}
