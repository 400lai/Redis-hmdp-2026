package com.hmdp.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import cn.hutool.core.lang.UUID;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hmdp.dto.LoginFormDTO;
import com.hmdp.dto.Result;
import com.hmdp.dto.UserDTO;
import com.hmdp.entity.User;
import com.hmdp.mapper.UserMapper;
import com.hmdp.service.IUserService;
import com.hmdp.utils.RegexUtils;
import cn.hutool.core.util.RandomUtil;
import com.hmdp.utils.UserHolder;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.BitFieldSubCommands;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static com.hmdp.utils.RedisConstants.*;
import static com.hmdp.utils.SystemConstants.USER_NICK_NAME_PREFIX;

@Slf4j
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements IUserService {

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    /**
     * 发送手机短信验证码
     * @param phone 手机号码，用于接收短信验证码
     * @param session HTTP 会话对象，用于保存生成的验证码供后续验证使用
     * @return 操作结果，手机号格式正确返回成功，否则返回"手机号格式错误!"
     */
    @Override
    public Result sendCode(String phone, HttpSession session) {
        // 1. 校验手机号
        if (RegexUtils.isPhoneInvalid(phone)) {
            // 2.如果不符合，返回错误信息
            return Result.fail("手机号格式错误!");
        }

        // 3.符合，生成验证码
        String code = RandomUtil.randomNumbers(6);

        // 4.保存验证码到 redis
        stringRedisTemplate.opsForValue().set(LOGIN_CODE_KEY + phone, code, LOGIN_CODE_TTL, TimeUnit.MINUTES);

        // 5.发送验证码
        log.debug("发送短信验证码成功，验证码：{}", code);

        // 6.返回结果
        return Result.ok();
    }

    /**
     * 用户登录功能，支持手机号验证码登录和新用户自动注册
     * @param loginForm 登录表单数据，包含手机号和短信验证码
     * @param session HTTP 会话对象，用于保存登录状态信息
     * @return 操作结果，登录成功返回 token，失败返回相应错误信息
     */
    @Override
    public Result login(LoginFormDTO loginForm, HttpSession session) {
        // 1.校验手机号
        String phone = loginForm.getPhone();
        if (RegexUtils.isPhoneInvalid(phone)) {
            // 2.如果不符合，返回错误信息
            return Result.fail("手机号格式错误!");
        }

        // 3.从redis获取验证码并校验
        String cacheCode = stringRedisTemplate.opsForValue().get(LOGIN_CODE_KEY + phone);
        String code = loginForm.getCode();
        if(cacheCode == null || !cacheCode.equals(code)){
            // 不一致，报错
            return Result.fail("验证码错误!");
        }

        // 4.一致，根据手机号查询用户 select * from tb_user where phone = ?
        User user = query().eq("phone", phone).one();

        // 5.判断用户是否存在
        if(user == null){
            // 6.不存在，创建新用户并保存
            user = createUserWithPhone(phone);
        }

        // 7.保存用户信息到redis
        // 7.1.随机生成token，作为登录令牌
        String token = UUID.randomUUID().toString(true);

        // 7.2.将User对象转为HashMap存储
        UserDTO userDTO = BeanUtil.copyProperties(user, UserDTO.class);
        // 转换过程忽略 null 值，并将所有字段值转换为String类型以适配 Redis Hash 结构
        Map<String, Object> userMap = BeanUtil.beanToMap(userDTO, new HashMap<>(),
                CopyOptions.create()
                        .setIgnoreNullValue(true)
                        .setFieldValueEditor((fieldName, fieldValue) -> fieldValue.toString()));

        // 7.3.存储
        String tokenKey = LOGIN_USER_KEY + token;
        stringRedisTemplate.opsForHash().putAll(tokenKey, userMap);

        // 7.4.设置token有效期
        stringRedisTemplate.expire(tokenKey, LOGIN_USER_TTL, TimeUnit.MINUTES);

        // 8.返回token
        return Result.ok(token);
    }

    /**
     * 用户签到功能
     * 将当前用户在本月的签到记录写入 Redis，使用位图存储以节省空间
     */
    @Override
    public Result sign() {
        // 1.获取当前登录用户 ID
        Long userId = UserHolder.getUser().getId();

        // 2.获取当前日期时间
        LocalDateTime now = LocalDateTime.now();

        // 3.构建 Redis 位图 Key：用户签到前缀 + 用户 ID + 年月
        String keySuffix = now.format(DateTimeFormatter.ofPattern("yyyyMM"));
        String key = USER_SIGN_KEY + userId + keySuffix;

        // 4.获取今天是本月的第几天（1-31）
        int dayOfMonth = now.getDayOfMonth();

        // 5.使用 Redis SETBIT 命令记录签到，offset 从 0 开始：SETBIT key offset 1
        stringRedisTemplate.opsForValue().setBit(key, dayOfMonth - 1, true);
        return Result.ok();
    }

    /**
     * 查询用户连续签到天数
     * 通过 Redis 位图存储的签到记录，从当前日期往前统计连续签到的天数
     * @return Result 返回连续签到天数
     */
    @Override
    public Result signCount() {
        // 1.获取当前登录用户 ID
        Long userId = UserHolder.getUser().getId();

        // 2.获取当前日期时间
        LocalDateTime now = LocalDateTime.now();

        // 3.构建 Redis 位图 Key：用户签到前缀 + 用户 ID + 年月
        String keySuffix = now.format(DateTimeFormatter.ofPattern("yyyyMM"));
        String key = USER_SIGN_KEY + userId + keySuffix;

        // 4.获取今天是本月的第几天（1-31）
        int dayOfMonth = now.getDayOfMonth();
        // 使用 Redis BITFIELD 命令获取本月截止今天的签到记录，返回一个无符号整数(十进制)
        // BITFIELD sign:5:202203 GET u14 0
        List<Long> result = stringRedisTemplate.opsForValue().bitField(
                key,
                // 创建 Redis BITFIELD 命令构建器，用于构造位图字段操作命令
                BitFieldSubCommands.create().
                        // 配置 BITFIELD 的 GET 操作：读取从偏移量 0 开始、位数为 dayOfMonth 的无符号整数，用于
                        get(BitFieldSubCommands.BitFieldType.unsigned(dayOfMonth)).valueAt(0)
        );

        // 判断查询结果是否为空
        if(result == null || result.size() == 0){
            // 没有任何签到结果
            return Result.ok(0);
        }
        Long num = result.get(0);
        if (num == null || num == 0L){
            return Result.ok(0);
        }

        // 通过位运算统计从低位开始连续的 1 的个数（即连续签到天数）
        int count = 0;
        while(true){
            // 6.1 让这个数字与1做与运算，得到数字的最后一个bit位
            // 检查最低位是否为 1（已签到）
            if((num & 1) == 0){
                // 如果是0，说明未签到，结束
                break;
            }else{
                // 如果不为0，说明已签到，计数器+1
                count++;
            }
            /// 无符号右移一位，继续检查下一个bit位
            num >>>= 1;     // num = num >>> 1
        }
        return Result.ok(count);
    }

    /**
     * 通过手机号创建新用户
     * @param phone 用户手机号
     * @return 创建的用户对象
     */
    private User createUserWithPhone(String phone) {
        // 1.创建用户
        User user = new User();
        user.setPhone(phone);
        user.setNickName(USER_NICK_NAME_PREFIX + RandomUtil.randomString(10));

        // 2.保存用户
        save(user);
        return user;
    }
}
