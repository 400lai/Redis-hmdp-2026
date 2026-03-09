package com.hmdp.utils;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import com.hmdp.dto.UserDTO;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 登录拦截器，用于验证用户登录状态
 * 实现 HandlerInterceptor 接口，拦截请求检查用户是否已登录
 */
public class LoginInterceptor implements HandlerInterceptor {

    /**
     * 预处理请求，在目标方法执行前调用，用于用户登录验证
     * @param request HTTP 请求对象，用于获取 session 和用户信息
     * @param response HTTP 响应对象，用于设置响应状态码
     * @param handler 被调用的处理器对象，可以是 Controller 方法或其他处理程序
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 1.判断是否需要拦截（ThreadLocal中是否有用户）
        if(UserHolder.getUser() == null){
            // 没有，需要拦截，设置状态码
            response.setStatus(401);
            // 拦截
            return false;
        }
        // 有用户，则放行
        return true;
    }

}
