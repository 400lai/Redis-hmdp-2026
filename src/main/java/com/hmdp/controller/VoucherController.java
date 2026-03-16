package com.hmdp.controller;


import com.hmdp.dto.Result;
import com.hmdp.entity.Voucher;
import com.hmdp.service.IVoucherService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;


/**
 * <p>
 * 优惠券控制器 - 处理优惠券相关的 HTTP 请求，包括普通券、秒杀券的创建和查询
 * </p>
 */
@Tag(name = "优惠券管理", description = "优惠券创建、查询等接口")
@RestController
@RequestMapping("/voucher")
public class VoucherController {

    @Resource
    private IVoucherService voucherService;

    @Operation(summary = "新增普通券", description = "创建新的普通优惠券")
    @PostMapping
    public Result addVoucher(
            @Parameter(description = "优惠券信息", required = true) @RequestBody Voucher voucher) {
        voucherService.save(voucher);
        return Result.ok(voucher.getId());
    }

    @Operation(summary = "新增秒杀券", description = "创建新的秒杀优惠券")
    @PostMapping("seckill")
    public Result addSeckillVoucher(
            @Parameter(description = "优惠券信息", required = true) @RequestBody Voucher voucher) {
        voucherService.addSeckillVoucher(voucher);
        return Result.ok(voucher.getId());
    }

    @Operation(summary = "店铺优惠券列表", description = "查询指定店铺的所有优惠券")
    @GetMapping("/list/{shopId}")
    public Result queryVoucherOfShop(
            @Parameter(description = "店铺 ID", required = true) @PathVariable("shopId") Long shopId) {
       return voucherService.queryVoucherOfShop(shopId);
    }
}
