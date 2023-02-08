package com.coresky.web.mapper;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.coresky.web.entity.CkCollectionsInfoEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
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
public interface CkCollectionsInfoMapper extends BaseMapper<CkCollectionsInfoEntity> {

    default QueryWrapper<CkCollectionsInfoEntity> createQueryWrapper() {
        QueryWrapper<CkCollectionsInfoEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("deleted", 0);
        return queryWrapper;
    }

    @Update("UPDATE `ck_collections_msg` SET `rev_status`=0,`log`=\"重启服务，订阅关闭\"")
    int closeAll();

    @Update("UPDATE `ck_collections_msg` SET `rev_status`=1,`log`=#{log} WHERE `name`= #{topic}")
    int updateByTopic(
            @Param("topic") String topic,
            @Param("log") String log
    );
}
