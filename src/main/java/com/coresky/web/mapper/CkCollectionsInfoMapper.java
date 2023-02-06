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
    
    default CkCollectionsInfoEntity findByContract(String contract) {
        QueryWrapper<CkCollectionsInfoEntity> wrapper = createQueryWrapper();
        wrapper.eq("contract", contract.toLowerCase());
        return selectOne(wrapper);
    }

    default HashMap<String, CkCollectionsInfoEntity> selectByContractsToHashMap(List<String> list) {
        QueryWrapper<CkCollectionsInfoEntity> wrapper = createQueryWrapper();
        wrapper.select("id, name, info, contract");
        wrapper.in("contract", list);
        HashMap<String, CkCollectionsInfoEntity> hashMap = new HashMap<>();
        List<CkCollectionsInfoEntity> collectionsInfoList = selectList(wrapper);
        for (CkCollectionsInfoEntity c: collectionsInfoList) {
            hashMap.put(c.getContract().toLowerCase(), c);
        }
        return hashMap;
    }

    @Update("UPDATE `ck_collections_info` SET `follow_count`=`follow_count`+1 WHERE `id`= #{id}")
    int followCountInc(@Param("id") Integer id);

    @Update("UPDATE `ck_collections_info` SET `follow_count`=`follow_count`-1 WHERE `contract`= #{contract}")
    int followCountDec(@Param("contract") String contract);

    @Update("UPDATE `ck_collections_info` SET `opensea_rev_status`=0,`opensea_log`=\"重启服务，订阅关闭\"")
    int closeAll();

    @Update("UPDATE `ck_collections_info` SET `opensea_rev_status`=1,`opensea_log`=#{log} WHERE `opensea_name`= #{topic}")
    int updateByTopic(
            @Param("topic") String topic,
            @Param("log") String log
    );
}
