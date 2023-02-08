package com.coresky.web.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Data
@TableName("ck_collections_msg")
public class CkCollectionsInfoEntity extends BaseEntity {
    
    private String name;
    private int status;
    private String log;
}
