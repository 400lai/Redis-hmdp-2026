package com.hmdp.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 商铺类型实体类 - 对应数据库表 tb_shop_type，存储商铺分类信息（如美食、购物、娱乐等）
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("tb_shop_type")
public class ShopType implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 类型名称
     */
    private String name;

    /**
     * 图标
     */
    private String icon;

    /**
     * 排序字段，数值越小越靠前
     */
    private Integer sort;

    /**
     * 创建时间（JSON 序列化时忽略此字段）
     */
    @JsonIgnore
    private LocalDateTime createTime;

    /**
     * 更新时间（JSON 序列化时忽略此字段）
     */
    @JsonIgnore
    private LocalDateTime updateTime;

}
