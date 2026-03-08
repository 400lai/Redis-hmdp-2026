package com.hmdp.controller;


import com.hmdp.dto.Result;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 优惠券订单控制器 - 处理秒杀下单请求
 */
@RestController
@RequestMapping("/voucher-order")
public class VoucherOrderController {

    /**
     * 秒杀优惠券下单接口
     * @param voucherId 优惠券 ID
     * @return 处理结果，返回失败信息（功能未完成）
     */
    @PostMapping("seckill/{id}")
    public Result seckillVoucher(@PathVariable("id") Long voucherId) {
        return Result.fail("功能未完成");
    }
}
