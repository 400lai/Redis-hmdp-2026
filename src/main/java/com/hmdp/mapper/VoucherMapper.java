package com.hmdp.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hmdp.entity.Voucher;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 优惠券 Mapper 接口 - 用于访问和操作 tb_voucher 表的数据
 */
public interface VoucherMapper extends BaseMapper<Voucher> {

    /**
     * 查询指定店铺的优惠券列表
     * @param shopId 店铺 ID，用于筛选该店铺下的所有优惠券
     * @return 优惠券列表，包含该店铺的所有优惠券信息
     */
    List<Voucher> queryVoucherOfShop(@Param("shopId") Long shopId);
}
