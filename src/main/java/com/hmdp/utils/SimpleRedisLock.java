package com.hmdp.utils;

import cn.hutool.core.lang.UUID;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;

import java.util.Collections;
import java.util.concurrent.TimeUnit;

/**
 * 基于 Redis 的简单分布式锁实现
 * 使用 Redis 的 SETNX 命令实现分布式环境下的互斥锁，支持自动过期防止死锁。
 * 通过 Lua 脚本保证解锁操作的原子性，避免误删其他线程的锁。
 */
public class SimpleRedisLock implements ILock{

    private String name;
    private StringRedisTemplate stringRedisTemplate;
    public SimpleRedisLock(String name, StringRedisTemplate stringRedisTemplate) {
        this.name = name;
        this.stringRedisTemplate = stringRedisTemplate;
    }

    private final static String KEY_PREFIX = "lock:";
    // 线程唯一标识符前缀（UUID + 线程 ID）
    private final static String ID_PREFIX = UUID.randomUUID().toString(true) + "-";
    // final 修饰的静态变量，需要在类加载时完成初始化
    private final static DefaultRedisScript<Long> UNLOCK_SCRIPT;
    static {
        UNLOCK_SCRIPT = new DefaultRedisScript<>();     // 创建脚本对象实例
        UNLOCK_SCRIPT.setLocation(new ClassPathResource("unlock.lua")); // 指定脚本位置
        UNLOCK_SCRIPT.setResultType(Long.class);        // 指定返回值类型
    }

    @Override
    public boolean tryLock(long timeoutSec) {
        // 生成当前线程的唯一标识
        String threadId = ID_PREFIX + Thread.currentThread().getId();
        /*
         * 使用 Redis SETNX 命令尝试获取锁
         * 若 key不存在则设置成功，返回 true；若 key已存在则设置失败，返回 false
         * Redis的setIfAbsent方法返回的是Boolean对象（包装类型），而不是boolean基本类型。
         * 当 Redis 操作失败或出现异常时，可能返回 null。
         */
        Boolean success = stringRedisTemplate.opsForValue()
                .setIfAbsent(KEY_PREFIX + name, threadId, timeoutSec, TimeUnit.SECONDS);
        return Boolean.TRUE.equals(success);
    }

    /**
     * 释放分布式锁
     * 通过 Lua 脚本保证判断锁归属和删除锁的原子性
     */
    @Override
    public void unlock() {
        // 执行解锁 Lua 脚本，传入锁的 key 和当前线程标识
        stringRedisTemplate.execute(
                UNLOCK_SCRIPT,
                Collections.singletonList(KEY_PREFIX + name),
                ID_PREFIX + Thread.currentThread().getId());
    }

//    @Override
//    public void unlock() {
//        // 获取线程标识
//        String threadId = ID_PREFIX + Thread.currentThread().getId();
//        // 获取锁中的标识
//        String id = stringRedisTemplate.opsForValue().get(KEY_PREFIX + name);
//        // 判断标识是否一致
//        if (threadId.equals(id)) {
//            // 释放锁
//            stringRedisTemplate.delete(KEY_PREFIX + name);
//        }
//    }
}
