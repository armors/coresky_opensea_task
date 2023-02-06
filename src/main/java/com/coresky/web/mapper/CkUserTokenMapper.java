package com.coresky.web.mapper;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.coresky.web.entity.CkUserTokenEntity;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

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
}
