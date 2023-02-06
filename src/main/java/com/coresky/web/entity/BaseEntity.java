package com.coresky.web.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.alibaba.fastjson.annotation.JSONField;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
//@JsonInclude(JsonInclude.Include.NON_NULL)
public class BaseEntity {

    @TableId(type = IdType.AUTO)
    private Integer id;

    @JSONField(serialize = false)
    @JsonIgnore
    private Integer deleted;

    @TableField(fill = FieldFill.INSERT)
    private Long createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Long updateTime;

//    //自定义返回时间字符串
//    public String getCreateDate() {
//        if(null == createTime) {
//            return "";
//        }
//        return StringTools.toDateYmdHis(createTime);
//    }
//
//    //自定义返回时间字符串
//    public String getUpdateDate() {
//        if(null == updateTime) {
//            return "";
//        }
//        return StringTools.toDateYmdHis(updateTime);
//    }
}
