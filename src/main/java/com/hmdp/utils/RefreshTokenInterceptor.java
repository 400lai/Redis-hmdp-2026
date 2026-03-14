package com.hmdp.utils;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import com.hmdp.dto.UserDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static com.hmdp.utils.RedisConstants.LOGIN_USER_KEY;
import static com.hmdp.utils.RedisConstants.LOGIN_USER_TTL;

/**
 * 刷新 Token 的拦截器，用于在请求处理过程中验证用户身份并自动刷新 token 有效期
 * 该拦截器通过检查请求头中的 token，从 Redis 中获取用户信息并保存到 ThreadLocal，
 * 同时刷新 token 的过期时间，实现用户登录状态的自动续期
 */
@Slf4j
public class RefreshTokenInterceptor implements HandlerInterceptor {
    // Spring创建的对象，依靠Spring IoC管理，实现依赖注入
    // LoginInterceptor是自己创建的对象，没有Spring IoC管理，无法实现依赖注入
    private StringRedisTemplate stringRedisTemplate;

    public RefreshTokenInterceptor(StringRedisTemplate stringRedisTemplate) {
        this.stringRedisTemplate = stringRedisTemplate;
    }

    /**
     * 预处理请求，在目标方法执行前调用，用于用户登录验证
     * @param request HTTP 请求对象，用于获取 session 和用户信息
     * @param response HTTP 响应对象，用于设置响应状态码
     * @param handler 被调用的处理器对象，可以是 Controller 方法或其他处理程序
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 1.获取请求头中的token
        String token = request.getHeader("authorization");
        if (StrUtil.isBlank(token)) {
            return true;
        }

        // 2.基于token获取redis中的用户
        String tokenKey = LOGIN_USER_KEY + token;
        // log.info("RefreshTokenInterceptor - tokenKey: {}", tokenKey);
        Map<Object, Object> userMap = stringRedisTemplate.opsForHash().entries(tokenKey);

        // 3.判断用户是否存在
        if(userMap.isEmpty()){
            return true;
        }
        // 4.将查询到的Hash数据转为UserDTO对象
        UserDTO userDTO = BeanUtil.fillBeanWithMap(userMap, new UserDTO(), false);

        // 5.存在，保存用户信息到 ThreadLocal
        UserHolder.saveUser(userDTO);

        // 6.刷新token有效期
        stringRedisTemplate.expire(tokenKey, LOGIN_USER_TTL, TimeUnit.MINUTES);
        // 7.放行
        return true;
    }

    /**
     * 后处理方法，在整个请求完成后调用（包括视图渲染完成），用于清理资源
     * @param request HTTP 请求对象
     * @param response HTTP 响应对象
     * @param handler 被调用的处理器对象
     */
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        // 移除用户
        UserHolder.removeUser();
    }
}
