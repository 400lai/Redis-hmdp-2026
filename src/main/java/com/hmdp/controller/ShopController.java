package com.hmdp.controller;


import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hmdp.dto.Result;
import com.hmdp.entity.Shop;
import com.hmdp.service.IShopService;
import com.hmdp.utils.SystemConstants;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;


/**
 * 商铺控制器 - 处理商铺相关的 HTTP 请求，包括查询、新增、更新和分页查询等操作
 */
@Tag(name = "商铺管理", description = "商铺查询、新增、修改等接口")
@RestController
@RequestMapping("/shop")
public class ShopController {

    @Resource
    public IShopService shopService;

    @Operation(summary = "查询商铺详情", description = "根据商铺 ID 查询商铺详细信息")
    @GetMapping("/{id}")
    public Result queryShopById(
            @Parameter(description = "商铺 ID", required = true) @PathVariable("id") Long id) {
        return shopService.queryById(id);
    }

    @Operation(summary = "新增商铺", description = "创建新的商铺信息")
    @PostMapping
    public Result saveShop(
            @Parameter(description = "商铺数据", required = true) @RequestBody Shop shop) {
        shopService.save(shop);
        return Result.ok(shop.getId());
    }

    @Operation(summary = "更新商铺", description = "更新指定商铺的信息")
    @PutMapping
    public Result updateShop(
            @Parameter(description = "商铺数据", required = true) @RequestBody Shop shop) {
        return shopService.update(shop);
    }

    @Operation(summary = "按类型查询商铺", description = "根据店铺类型和地理位置查询店铺列表")
    @GetMapping("/of/type")
    public Result queryShopByType(
            @Parameter(description = "店铺类型 ID", required = true) @RequestParam(value = "typeId") Integer typeId,
            @Parameter(description = "当前页码", required = true) @RequestParam(value = "current", defaultValue = "1") Integer current,
            @Parameter(description = "目标经度", required = false) @RequestParam(value = "x", required = false) Double x,
            @Parameter(description = "目标纬度", required = false) @RequestParam(value = "y", required = false) Double y
    ) {
        return shopService.queryShopByType(typeId, current, x, y);
    }

    @Operation(summary = "按名称查询商铺", description = "根据商铺名称关键字分页查询商铺信息")
    @GetMapping("/of/name")
    public Result queryShopByName(
            @Parameter(description = "商铺名称关键字", required = false) @RequestParam(value = "name", required = false) String name,
            @Parameter(description = "页码", required = true) @RequestParam(value = "current", defaultValue = "1") Integer current
    ) {
        Page<Shop> page = shopService.query()
                .like(StrUtil.isNotBlank(name), "name", name)
                .page(new Page<>(current, SystemConstants.MAX_PAGE_SIZE));
        return Result.ok(page.getRecords());
    }
}
