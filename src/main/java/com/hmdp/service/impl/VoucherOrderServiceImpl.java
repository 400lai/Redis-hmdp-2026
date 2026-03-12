package com.hmdp.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.hmdp.dto.Result;
import com.hmdp.entity.VoucherOrder;
import com.hmdp.mapper.VoucherOrderMapper;
import com.hmdp.service.ISeckillVoucherService;
import com.hmdp.service.IVoucherOrderService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hmdp.utils.RedisIdWorker;
import com.hmdp.utils.UserHolder;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.aop.framework.AopContext;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.connection.stream.*;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Slf4j
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

    private final static DefaultRedisScript<Long> SECKILL_SCRIPT;
    static {
        SECKILL_SCRIPT = new DefaultRedisScript<>();     // 创建脚本对象实例
        SECKILL_SCRIPT.setLocation(new ClassPathResource("seckill.lua")); // 指定脚本位置
        SECKILL_SCRIPT.setResultType(Long.class);        // 指定返回值类型
    }

    private final static ExecutorService SECKILL_ORDER_EXECUTOR = Executors.newSingleThreadExecutor();

    /**
     * 初始化方法，在 Bean 构造完成后自动执行
     * 该方法使用@PostConstruct 注解标记，会在依赖注入完成后被 Spring 容器自动调用。
     * 主要功能是启动秒杀订单处理线程，将 VoucherOrderTask 任务提交到线程池，
     * 用于异步处理阻塞队列中的订单创建任务。
     */
    @PostConstruct
    private void init(){
        SECKILL_ORDER_EXECUTOR.submit(new VoucherOrderTask());
    }

    /**
     * 秒杀订单处理任务
     * 该内部类实现了 Runnable 接口，作为独立的线程任务持续从 Redis Stream 中获取订单信息，
     * 并调用 handleVoucherOrder 方法处理订单创建逻辑。采用无限循环确保能持续处理队列中的订单，
     * 通过异常捕获保证单个订单处理失败不影响后续订单。使用消费者组（g1, c1）读取消息，
     * 设置 2 秒阻塞时间等待新消息，读取成功后解析订单数据并处理，最后执行 ACK 确认。
     */
    private class VoucherOrderTask implements Runnable {
        String queueName = "stream.orders";
        @Override
        public void run() {
            while(true){
                try {
                    // 1.获取消息队列中的订单信息 XREADGROUP GROUP g1 c1 COUNT 1 BLOCK 2000 STREAMS stream.orders >
                    List<MapRecord<String, Object, Object>> list = stringRedisTemplate.opsForStream().read(
                            Consumer.from("g1", "c1"),
                            StreamReadOptions.empty().count(1).block(Duration.ofSeconds(2)),
                            StreamOffset.create(queueName, ReadOffset.lastConsumed())
                    );

                    // 2.判断消息获取是否成功
                    if(list == null || list.isEmpty()){
                        // 如果获取失败，说明没有消息，继续下一次循环
                        continue;
                    }

                    // 3.解析消息中的订单信息
                    MapRecord<String, Object, Object> record = list.get(0);
                    Map<Object, Object> values = record.getValue();
                    VoucherOrder voucherOrder = BeanUtil.fillBeanWithMap(values, new VoucherOrder(), true);

                    // 4.如果获取成功，可以下单
                    handleVoucherOrder(voucherOrder);

                    // 5.ACK 确认
                    stringRedisTemplate.opsForStream().acknowledge(queueName, "g1", record.getId());

                } catch (Exception e) {
                    log.error("处理订单异常", e);
                    handlePendingList();
                }
            }
        }

        /**
         * 处理 pending-list 中的异常订单
         * 该方法持续从 Redis Stream 的 pending-list 中读取因异常而未成功处理的订单消息，
         * 通过无限循环确保所有挂起的订单都能被重新处理。使用 ReadOffset.from("0") 从 pending-list 起始位置读取，
         * 每次获取一条消息进行订单处理，处理成功后执行 ACK 确认以从 pending-list 中移除该消息。
         * 当 pending-list 为空时自动退出循环，异常时会短暂休眠后重试。
         */
        private void handlePendingList() {
            while(true){
                try {
                    // 1.获取 pending-list 中的订单信息 XREADGROUP GROUP g1 c1 COUNT 1 STREAMS stream.orders 0
                    List<MapRecord<String, Object, Object>> list = stringRedisTemplate.opsForStream().read(
                            Consumer.from("g1", "c1"),
                            StreamReadOptions.empty().count(1),
                            StreamOffset.create(queueName, ReadOffset.from("0"))
                    );

                    // 2.判断消息获取是否成功
                    if(list == null || list.isEmpty()){
                        // 如果获取失败，说明 pending-list 没有异常消息，结束循环
                        break;
                    }

                    // 3.解析消息中的订单信息
                    MapRecord<String, Object, Object> record = list.get(0);
                    Map<Object, Object> values = record.getValue();
                    VoucherOrder voucherOrder = BeanUtil.fillBeanWithMap(values, new VoucherOrder(), true);

                    // 4.如果获取成功，可以下单
                    handleVoucherOrder(voucherOrder);

                    // 5.ACK 确认
                    stringRedisTemplate.opsForStream().acknowledge(queueName, "g1", record.getId());

                } catch (Exception e) {
                    log.error("处理 pending-list 订单异常", e);
                    try {
                        Thread.sleep(50);
                    } catch (InterruptedException ex) {
                        throw new RuntimeException(ex);
                    }
                }
            }

        }
    }
//    private BlockingQueue<VoucherOrder> orderTasks = new ArrayBlockingQueue<>(1024 * 1024);
//    private class VoucherOrderTask implements Runnable {
//        @Override
//        public void run() {
//            while(true){
//                try {
//                    // 1.获取队列中的订单信息
//                    VoucherOrder voucherOrder = orderTasks.take();
//                    // 2.创建订单
//                    handleVoucherOrder(voucherOrder);
//
//                } catch (Exception e) {
//                    log.error("处理订单异常", e);
//                }
//            }
//        }
//    }

    /**
     * 处理优惠券订单创建
     * 该方法负责在异步线程中处理订单创建，通过 Redisson 分布式锁实现"一人一单"控制，
     * 确保同一用户的订单请求串行执行，防止并发下单导致的数据不一致问题。
     * 通过 AOP 代理调用事务方法，保证订单创建的原子性和一致性。
     */
    private void handleVoucherOrder(VoucherOrder voucherOrder) {
        // 1.获取用户
        Long userId = voucherOrder.getUserId();
        // 2.创建锁对象
        RLock lock = redissonClient.getLock("lock:order:" + userId);
        // 3.尝试获取锁，设置锁的过期时间为 120 秒
        boolean isLock = lock.tryLock();
        // 4.如果获取锁失败，说明该用户已经有请求在处理中
        if (!isLock) {
            // 获取锁失败，返回错误或重试
            log.error("不允许重复下单");
            return ;
        }

        try {
            // 通过代理调用创建订单方法，保证事务一致性
            proxy.createVoucherOrder(voucherOrder);
        } finally {
            // 无论是否异常，都要释放锁
            lock.unlock();
        }
    }

    /**
     * AOP 代理对象引用
     * 用于在异步线程中通过代理调用带有@Transactional 注解的方法，确保事务注解生效。
     * 该代理对象在 seckillVoucher 方法中通过 AopContext.currentProxy() 获取并赋值。
     */
    private IVoucherOrderService proxy;

    /**
     * 秒杀优惠券下单方法
     * 该方法实现高并发场景下的秒杀下单流程，通过 Lua 脚本在 Redis 中预扣减库存，
     * 校验用户的购买资格（库存是否充足、是否重复下单等）。
     * 具备购买资格的订单会被放入阻塞队列，由后台异步线程处理实际订单创建。
     */
    @Override
    public Result seckillVoucher(Long voucherId) {
        // 获取登录用户id
        Long userId = UserHolder.getUser().getId();
        // 生成全局唯一订单 ID
        long orderId = redisIdWorker.nextId("order");

        // 1.执行lua脚本
        Long result = stringRedisTemplate.execute(
                SECKILL_SCRIPT,
                Collections.emptyList(),
                voucherId.toString(), userId.toString(),String.valueOf(orderId)
        );

        // 2.判断结果是否为0
        int resultCode = result.intValue();
        if(resultCode != 0){
            // 2.1 不为0，代表没有购买资格
            return Result.fail(resultCode == 1 ? "库存不足" : "不能重复下单");
        }

        // 3.获取代理对象
        proxy = (IVoucherOrderService) AopContext.currentProxy();

        // 4.返回订单id
        return Result.ok(orderId);
    }
//    @Override
//    public Result seckillVoucher(Long voucherId) {
//        // 获取登录用户id
//        Long userId = UserHolder.getUser().getId();
//
//        // 1.执行lua脚本
//        Long result = stringRedisTemplate.execute(
//                SECKILL_SCRIPT,
//                Collections.emptyList(),
//                voucherId.toString(), userId.toString()
//        );
//
//        // 2.判断结果是否为0
//        int resultCode = result.intValue();
//        if(resultCode != 0){
//            // 2.1 不为0，代表没有购买资格
//            return Result.fail(resultCode == 1 ? "库存不足" : "不能重复下单");
//        }
//
//        // 2.2 为0，有购买资格，把下单信息保存到阻塞队列
//        VoucherOrder voucherOrder = new VoucherOrder();
//        // 2.3 生成全局唯一订单 ID
//        long orderId = redisIdWorker.nextId("order");
//        voucherOrder.setId(orderId);
//        // 2.4 设置用户 ID
//        voucherOrder.setUserId(userId);
//        // 2.5 设置优惠券 ID
//        voucherOrder.setVoucherId(voucherId);
//        // 2.6 放入阻塞队列
//        orderTasks.add(voucherOrder);
//
//        // 3.获取代理对象
//        proxy = (IVoucherOrderService) AopContext.currentProxy();
//
//        // 4.返回订单id
//        return Result.ok(orderId);
//    }

    /**
     * 秒杀优惠券下单方法
     * 执行秒杀前的前置校验，包括秒杀时间、库存等，并通过同步锁确保同一用户的请求串行执行
     */
//    @Override
//    public Result seckillVoucher(Long voucherId) {
//        // 1.查询优惠券
//        SeckillVoucher voucher = seckillVoucherService.getById(voucherId);
//
//        // 2.校验秒杀是否已开始
//        if (voucher.getBeginTime().isAfter(LocalDateTime.now())) {
//            return Result.fail("秒杀尚未开始");
//        }
//
//        // 3.校验秒杀是否已结束
//        if (voucher.getEndTime().isBefore(LocalDateTime.now())) {
//            return Result.fail("秒杀已经结束！");
//        }
//
//        // 4.校验库存是否充足
//        if (voucher.getStock() < 1) {
//            return Result.fail("库存不足！");
//        }
//
//        // 5.获取当前登录用户的 ID
//        Long userId = UserHolder.getUser().getId();
//
//        // 创建Redis分布式锁对象，用于实现线程隔离
//        // SimpleRedisLock lock = new SimpleRedisLock("order:" + userId, stringRedisTemplate);
//        RLock lock = redissonClient.getLock("lock:order:" + userId);
//        // 尝试获取锁，设置锁的过期时间为 120 秒
//        boolean isLock = lock.tryLock();
//        // 如果获取锁失败，说明该用户已经有请求在处理中
//        if (!isLock) {
//            // 获取锁失败，返回错误或重试
//            return Result.fail("请勿重复下单！");
//        }
//
//        try {
//            // 获取当前服务的 AOP代理对象，确保@Transactional 事务注解生效
//            IVoucherOrderService proxy = (IVoucherOrderService) AopContext.currentProxy();
//            // 通过代理调用创建订单方法，保证事务一致性
//            return proxy.createVoucherOrder(voucherId);
//        } finally {
//            // 无论是否异常，都要释放锁
//            lock.unlock();
//        }
//    }

    /**
     * 创建秒杀订单（事务方法）
     * 该方法在事务中执行订单创建的核心逻辑，包括一人一单校验、库存扣减和订单保存。
     * 使用@Transactional 注解保证操作的原子性，任一环节失败都会回滚整个事务。
     * 通过乐观锁（stock > 0 条件）防止超卖问题，确保库存数据的准确性。
     */
    @Transactional
    public void createVoucherOrder(VoucherOrder voucherOrder) {
        // 5.获取当前登录用户的 ID
        Long userId = voucherOrder.getId();

        // 5.1 查询用户是否已购买过该优惠券
        int count = query().eq("user_id", userId)
                .eq("voucher_id", voucherOrder.getVoucherId())    // 指定优惠券id
                .count();

        // 5.2 校验是否超过购买限制
        if (count > 0) {
            log.error("该商品每人限购1份，您已超过购买限制");
            return ;
        }

        // 6.扣减库存并使用乐观锁防止超卖
        boolean success = seckillVoucherService.update()
                .setSql("stock = stock - 1")
                .eq("voucher_id", voucherOrder.getVoucherId())
                .gt("stock", 0)    // where id = ? and stock > 0
                .update();

        if (!success) {
            // 扣减库存失败
            log.error("库存不足！");
            return ;
        }

        // 7.创建订单并设置订单信息
        save(voucherOrder);
    }
}
