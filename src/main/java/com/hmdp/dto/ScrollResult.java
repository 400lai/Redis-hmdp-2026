package com.hmdp.dto;

import lombok.Data;

import java.util.List;

/**
 * 滚动查询结果封装类
 * 用于分页查询场景，特别是基于时间戳的滚动加载（如关注列表、点赞列表等）
 * 包含查询结果列表、最小时间戳和偏移量，支持前端实现下拉刷新和上拉加载
 */
@Data
public class ScrollResult {
    // 查询结果列表，包含具体的数据对象
    private List<?> list;

    // 最小时间戳，用于下次查询的起始时间，实现滚动分页
    private Long minTime;

    // 偏移量，表示从最小时间戳开始跳过的记录数，用于精确定位
    private Integer offset;
}
