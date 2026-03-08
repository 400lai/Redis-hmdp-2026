package com.hmdp.service;

import com.hmdp.dto.Result;
import com.hmdp.entity.Voucher;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * 优惠券服务接口 - 定义优惠券相关的业务操作方法
 */
public interface IVoucherService extends IService<Voucher> {

    /**
     * 查询指定店铺的优惠券列表
     * @param shopId 店铺 ID，用于筛选该店铺下的所有优惠券
     * @return 操作结果，成功返回该店铺的优惠券列表
     */
    Result queryVoucherOfShop(Long shopId);

    /**
     * 新增秒杀优惠券（同时保存优惠券和秒杀信息）
     * @param voucher 优惠券对象，包含优惠券基本信息和秒杀活动信息（库存、开始时间、结束时间）
     */
    void addSeckillVoucher(Voucher voucher);
}
