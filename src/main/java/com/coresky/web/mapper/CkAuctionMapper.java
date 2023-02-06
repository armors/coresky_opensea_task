package com.coresky.web.mapper;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.coresky.web.entity.CkAuctionEntity;

/**
 * <p>
 *  Mapper 接口
 * </p>
 *
 * @author 
 * @since 2022-10-31
 */
public interface CkAuctionMapper extends BaseMapper<CkAuctionEntity> {

    default QueryWrapper<CkAuctionEntity> createQueryWrapper() {
        QueryWrapper<CkAuctionEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("deleted", 0);
        return queryWrapper;
    }
}
