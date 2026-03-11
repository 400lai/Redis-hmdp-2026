package com.hmdp.service.impl;

import com.hmdp.dto.Result;
import com.hmdp.entity.SeckillVoucher;
import com.hmdp.entity.VoucherOrder;
import com.hmdp.mapper.VoucherOrderMapper;
import com.hmdp.service.ISeckillVoucherService;
import com.hmdp.service.IVoucherOrderService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hmdp.utils.RedisIdWorker;
import com.hmdp.utils.SimpleRedisLock;
import com.hmdp.utils.UserHolder;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.aop.framework.AopContext;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDateTime;


@Service
public class VoucherOrderServiceImpl extends ServiceImpl<VoucherOrderMapper, VoucherOrder> implements IVoucherOrderService {

    @Resource
    private ISeckillVoucherService seckillVoucherService;

    @Resource
    private RedisIdWorker redisIdWorker;

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Resource
    private RedissonClient redissonClient;

    /**
     * 秒杀优惠券下单方法
     * 执行秒杀前的前置校验，包括秒杀时间、库存等，并通过同步锁确保同一用户的请求串行执行
     */
    @Override
    public Result seckillVoucher(Long voucherId) {
        // 1.查询优惠券
        SeckillVoucher voucher = seckillVoucherService.getById(voucherId);

        // 2.校验秒杀是否已开始
        if (voucher.getBeginTime().isAfter(LocalDateTime.now())) {
            return Result.fail("秒杀尚未开始");
        }

        // 3.校验秒杀是否已结束
        if (voucher.getEndTime().isBefore(LocalDateTime.now())) {
            return Result.fail("秒杀已经结束！");
        }

        // 4.校验库存是否充足
        if (voucher.getStock() < 1) {
            return Result.fail("库存不足！");
        }

        // 5.获取当前登录用户的 ID
        Long userId = UserHolder.getUser().getId();

        // 创建Redis分布式锁对象，用于实现线程隔离
        // SimpleRedisLock lock = new SimpleRedisLock("order:" + userId, stringRedisTemplate);
        RLock lock = redissonClient.getLock("lock:order:" + userId);
        // 尝试获取锁，设置锁的过期时间为 120 秒
        boolean isLock = lock.tryLock();
        // 如果获取锁失败，说明该用户已经有请求在处理中
        if (!isLock) {
            // 获取锁失败，返回错误或重试
            return Result.fail("请勿重复下单！");
        }

        try {
            // 获取当前服务的 AOP代理对象，确保@Transactional 事务注解生效
            IVoucherOrderService proxy = (IVoucherOrderService) AopContext.currentProxy();
            // 通过代理调用创建订单方法，保证事务一致性
            return proxy.createVoucherOrder(voucherId);
        } finally {
            // 无论是否异常，都要释放锁
            lock.unlock();
        }
    }

    /**
     * 创建秒杀订单方法
     * 在事务中执行一人一单校验、库存扣减和订单创建
     */
    @Transactional
    public Result createVoucherOrder(Long voucherId) {
        // 5.获取当前登录用户的 ID
        Long userId = UserHolder.getUser().getId();

        // 5.1 查询用户是否已购买过该优惠券
        int count = query().eq("user_id", userId)
                .eq("voucher_id", voucherId)    // 指定优惠券id
                .count();

        // 5.2 校验是否超过购买限制
        if (count > 0) {
            return Result.fail("该商品每人限购1份，您已超过购买限制");
        }

        // 6.扣减库存并使用乐观锁防止超卖
        boolean success = seckillVoucherService.update()
                .setSql("stock = stock - 1")
                .eq("voucher_id", voucherId)
                .gt("stock", 0)    // where id = ? and stock > 0
                .update();

        if (!success) {
            // 扣减库存失败
            return Result.fail("库存不足！");
        }

        // 7.创建订单并设置订单信息
        VoucherOrder voucherOrder = new VoucherOrder();
        // 7.1 生成全局唯一订单 ID
        long orderId = redisIdWorker.nextId("order");
        voucherOrder.setId(orderId);

        // 7.2 设置用户 ID
        voucherOrder.setUserId(userId);

        // 7.3 设置优惠券 ID
        voucherOrder.setVoucherId(voucherId);
        save(voucherOrder);

        // 8.返回订单ID
        return Result.ok(orderId);
    }
}
