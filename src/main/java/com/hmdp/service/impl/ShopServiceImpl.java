package com.hmdp.service.impl;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hmdp.dto.Result;
import com.hmdp.entity.Shop;
import com.hmdp.mapper.ShopMapper;
import com.hmdp.service.IShopService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hmdp.utils.CacheClient;
import com.hmdp.utils.RedisConstants;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.geo.Distance;
import org.springframework.data.geo.GeoResult;
import org.springframework.data.geo.GeoResults;
import org.springframework.data.redis.connection.RedisGeoCommands;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.domain.geo.GeoReference;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static com.hmdp.utils.RedisConstants.*;
import static com.hmdp.utils.SystemConstants.DEFAULT_PAGE_SIZE;

@Slf4j
@Service
public class ShopServiceImpl extends ServiceImpl<ShopMapper, Shop> implements IShopService {

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Resource
    private CacheClient cacheClient;

    /**
     * 根据商铺 ID 查询店铺信息，采用缓存优先策略（先查 Redis，再查数据库）
     */
    @Override
    public Result queryById(Long id) {
        // 缓存穿透
        Shop shop = cacheClient.queryWithPassThrough(CACHE_SHOP_KEY, id, Shop.class, this::getById, CACHE_SHOP_TTL, TimeUnit.MINUTES);

        // 互斥锁解决缓存击穿
        // Shop shop = queryWithMutexLock(id);

        // 逻辑过期解决缓存击穿
        // Shop shop = cacheClient.queryWithLogicalExpire(CACHE_SHOP_KEY, id, Shop.class, this::getById, CACHE_SHOP_TTL, TimeUnit.MINUTES);
        if (shop == null) {
            return Result.fail("店铺不存在");
        }

        // 7.返回shop信息
        return Result.ok(shop);
    }

//    public Shop queryWithPassThrough(Long id) {
//        String key = RedisConstants.CACHE_SHOP_KEY + id;
//        // 1.从redis查询商铺缓存
//        String shopJson = stringRedisTemplate.opsForValue().get(key);
//
//        // 2.判断是否存在
//        if (StrUtil.isNotBlank(shopJson)) {
//            // 3.存在，直接返回
//            return JSONUtil.toBean(shopJson, Shop.class);
//        }
//
//        // 判断命中的是否是空值，用于处理缓存穿透问题
//        if(shopJson != null){
//            // 返回一个错误信息
//            return null;
//        }
//
//        // 4.不存在，根据id查询数据库
//        Shop shop = getById(id);
//
//        // 5.不存在，返回错误并缓存空值防止缓存穿透
//        if(shop == null){
//            // 将空值写入redis
//            stringRedisTemplate.opsForValue().set(key, "", RedisConstants.CACHE_NULL_TTL, TimeUnit.MINUTES);
//            // 返回错误信息
//            return null;
//        }
//
//        // 6.存在，写入redis
//        stringRedisTemplate.opsForValue().set(key, JSONUtil.toJsonStr(shop), RedisConstants.CACHE_SHOP_TTL, TimeUnit.MINUTES);
//
//        // 7.返回shop信息
//        return shop;
//    }

    /**
     * 根据商铺 ID 查询店铺信息，使用互斥锁方式解决缓存击穿问题
     * - 查询缓存：优先从 Redis 获取店铺数据，命中则直接返回
     * - 缓存穿透处理：空值也写入缓存，防止恶意查询
     * - 互斥锁保护：未命中时使用分布式锁保证只有一个线程查询数据库
     * - 失败重试：获取锁失败则休眠 50ms 后递归重试
     * - 重建缓存：查询数据库后将结果写入 Redis 并释放锁
     */
    public Shop queryWithMutexLock(Long id) {
        String key = CACHE_SHOP_KEY + id;
        // 1.从redis查询商铺缓存
        String shopJson = stringRedisTemplate.opsForValue().get(key);

        // 2.判断是否存在
        if (StrUtil.isNotBlank(shopJson)) {
            // 3.存在，直接返回
            return JSONUtil.toBean(shopJson, Shop.class);
        }

        // 判断命中的是否是空值，用于处理缓存穿透问题
        if(shopJson != null){
            // 返回一个错误信息
            return null;
        }

        // 4.实现缓存重建
        // 4.1 获取互斥锁
        String lockKey = RedisConstants.LOCK_SHOP_KEY + id;
        Shop shop = null;
        try {
            boolean isLock = tryLock(lockKey);
            // 4.2 判断是否获取成功
            if(!isLock){
                // 4.3 失败，则休眠并重试
                Thread.sleep(50);
                return queryWithMutexLock(id);
            }

            // 4.4 成功，根据id查询数据库
            shop = getById(id);
            // 模拟重建的延时
            Thread.sleep(200);

            // 5.不存在，返回错误并缓存空值防止缓存穿透
            if(shop == null){
                // 将空值写入redis
                stringRedisTemplate.opsForValue().set(key, "", RedisConstants.CACHE_NULL_TTL, TimeUnit.MINUTES);
                // 返回错误信息
                return null;
            }

            // 6.存在，写入redis
            stringRedisTemplate.opsForValue().set(key, JSONUtil.toJsonStr(shop), CACHE_SHOP_TTL, TimeUnit.MINUTES);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } finally {
            // 7.释放互斥锁
            unLock(lockKey);
        }

        // 8.返回shop信息
        return shop;
    }

    // 定义一个缓存重建线程池：线程数设置为10
    // private static final ExecutorService CACHE_REBUILD_EXECUTOR = Executors.newFixedThreadPool(10);

    /**
     * 根据商铺 ID 查询店铺信息，使用逻辑过期方式解决缓存击穿问题
     * - 查询缓存：从 Redis 获取店铺数据，未命中直接返回
     * - 判断过期：解析逻辑过期时间，未过期则直接返回
     * - 异步重建：已过期时获取锁，成功则提交线程池异步重建缓存，失败则直接返回旧数据
     */
//    public Shop queryWithLogicalExpire(Long id) {
//        String key = RedisConstants.CACHE_SHOP_KEY + id;
//        // 1.从redis查询商铺缓存
//        String shopJson = stringRedisTemplate.opsForValue().get(key);
//
//        // 2.判断是否存在
//        if (StrUtil.isBlank(shopJson)) {
//            // 3.未命中，直接返回
//            return null;
//        }
//
//        // 4.命中，需要先把json反序列化为对象
//        RedisData redisData = JSONUtil.toBean(shopJson, RedisData.class);
//        Shop shop = JSONUtil.toBean((JSONObject) redisData.getData(), Shop.class);
//        LocalDateTime expireTime = redisData.getExpireTime();
//
//        // 5.判断是否过期
//        if (expireTime.isAfter(LocalDateTime.now())) {
//            // 5.1 未过期，直接返回店铺信息
//            return shop;
//        }
//
//        // 5.2 已过期，需要缓存重建
//
//        // 6.缓存重建
//        // 6.1 获取互斥锁
//        String lockKey = RedisConstants.LOCK_SHOP_KEY + id;
//        boolean isLock = tryLock(lockKey);
//        // 6.2 判断是否获取锁成功
//        if(isLock){
//            // 6.3 成功，开启独立线程，实现缓存重建
//            CACHE_REBUILD_EXECUTOR.submit(() -> {
//                try {
//                    // 重建缓存
//                    this.saveShop2Redis(id, 20L);
//                } catch (Exception e) {
//                    throw new RuntimeException(e);
//                } finally {
//                    // 释放锁
//                    unLock(lockKey);
//                }
//            });
//        }
//
//        // 6.4 返回过期的商铺信息
//        return shop;
//    }

    /**
     * 使用 setIfAbsent（SETNX）获取分布式锁，锁自动 10 秒过期
     */
    private boolean tryLock(String key){
        Boolean flag = stringRedisTemplate.opsForValue().setIfAbsent(key, "1", 10, TimeUnit.SECONDS);
        return Boolean.TRUE.equals(flag);
    }

    /**
     * 释放分布式锁
     */
    private void unLock(String key){
        stringRedisTemplate.delete(key);
    }

    /**
     * 查询店铺数据，封装逻辑过期时间后写入 Redis，模拟 200ms 延迟
     */
//    private void saveShop2Redis(Long id, Long expireSeconds) throws InterruptedException {
//        // 1.查询店铺数据
//        Shop shop = getById(id);
//        Thread.sleep(200);  // 模拟重建的延迟
//        // 2.封装逻辑过期时间
//        RedisData redisData = new RedisData();
//        redisData.setData(shop);
//        redisData.setExpireTime(LocalDateTime.now().plusSeconds(expireSeconds));
//        // 3.写入Redis
//        stringRedisTemplate.opsForValue().set(RedisConstants.CACHE_SHOP_KEY + id, JSONUtil.toJsonStr(redisData));
//    }

    /**
     * 更新商铺信息，并删除对应的 Redis 缓存以保证数据一致性
     */
    @Override
    @Transactional
    public Result update(Shop shop) {
        Long id = shop.getId();
        if (id == null) {
            return Result.fail("店铺id不能为空");
        }
        // 1.更新数据库
        updateById(shop);
        // 2.删除缓存
        stringRedisTemplate.delete(CACHE_SHOP_KEY + id);
        return null;
    }

    /**
     * 根据店铺类型和地理位置查询附近店铺列表
     */
    @Override
    public Result queryShopByType(Integer typeId, Integer current, Double x, Double y) {
        // 1.判断是否需要根据坐标查询
        if(x == null || y == null){
            // 不需要坐标查询，按数据库查询
            Page<Shop> page = query()
                    .eq("type_id", typeId)
                    .page(new Page<>(current, DEFAULT_PAGE_SIZE));
            // 返回数据
            return Result.ok(page.getRecords());
        }

        // 2.计算分页参数（假设每页显示 10 条数据）
        // form：当前页的起始索引（从 0 开始）
        int from = (current - 1) * DEFAULT_PAGE_SIZE;  // 第 1 页：from=0, 第 2 页：from=10
        // end：当前页的结束索引（不包含）
        int end = current * DEFAULT_PAGE_SIZE;         // 第 1 页：end=10, 第 2 页：end=20

        // 3.查询redis、按照距离排序、分页。结果：shopId、distance
        // 3.1 构建 Redis GEO 搜索的 Key，格式为 SHOP_GEO_KEY + 店铺类型 ID
        String key = SHOP_GEO_KEY + typeId;
        // 3.2 执行 Redis GEO 搜索操作：按距离排序并限制返回数量为 end
        GeoResults<RedisGeoCommands.GeoLocation<String>> results = stringRedisTemplate.opsForGeo() // GEOSEARCH key BYLONLAT x y BYRADIUS 10 WITHDISTANCE
                .search(
                        key,
                        GeoReference.fromCoordinate(x, y),      // 以目标经纬度 (x, y) 为中心点
                        new Distance(5000),               // 搜索半径5km范围内的店铺
                        /*
                         * 构建 Redis GEO 搜索参数：
                         * - includeDistance(): 包含距离信息，用于计算店铺与目标位置的距离
                         * - limit(end): 限制返回结果数量，end 为当前页的结束索引，确保返回足够的数据用于分页
                         * 该配置会执行 GEOSEARCH 命令，按照距离由近到远排序，返回 end 个店铺位置信息
                         */
                        RedisGeoCommands.GeoSearchCommandArgs.newGeoSearchArgs().includeDistance().limit(end)
                );

        // 判断 GEO 搜索结果是否为空，为空则返回空列表
        if(results == null){
            return Result.ok(Collections.emptyList());
        }

        // 4.从 GeoResults 对象中提取 GeoResult 列表，包含所有匹配的店铺位置信息
        List<GeoResult<RedisGeoCommands.GeoLocation<String>>> list = results.getContent();

        if(list.size() <= from){
            // 没有下一页了，结束
            return Result.ok(Collections.emptyList());
        }

        // 4.1 截取 form~end 的部分
        // 初始化店铺 ID 列表和距离映射表，容量为搜索结果大小以避免频繁扩容
        List<Long> ids = new ArrayList<>(list.size());
        Map<String, Distance> distanceMap = new HashMap<>(list.size());

        /*
         *假设每页 10 条，查询第 3 页：
         * from = (3-1) * 10 = 20（跳过前 20 条）
         * end = 3 * 10 = 30（Redis 最多返回 30 条）
         * skip(20) 后处理第 21-30 条，共 10 条数据
         */

        // 跳过前 from 条记录实现分页，遍历剩余结果提取店铺 ID 和距离信息
        list.stream().skip(from).forEach(result -> {
            // 4.2 获取店铺id
            String shopIdStr = result.getContent().getName();
            ids.add(Long.valueOf(shopIdStr));
            // 4.3 获取距离
            Distance distance = result.getDistance();
            distanceMap.put(shopIdStr, distance);
        });

        // 5.根据id查询shop
        String idStr = StrUtil.join(",", ids);
        List<Shop> shops = query().in("id", ids).last("ORDER BY FIELD(id," + idStr + ")").list();
        for (Shop shop : shops) {
            shop.setDistance(distanceMap.get(shop.getId().toString()).getValue());
        }

        // 6.返回
        return Result.ok(shops);
    }
}
