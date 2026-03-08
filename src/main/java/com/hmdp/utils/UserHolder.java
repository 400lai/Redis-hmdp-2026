package com.hmdp.utils;

import com.hmdp.dto.UserDTO;

/**
 * 用户信息持有者 - 基于 ThreadLocal 实现当前线程内用户信息的共享和传递，避免参数频繁传递
 */
public class UserHolder {
    private static final ThreadLocal<UserDTO> tl = new ThreadLocal<>();

    /**
     * 保存当前登录用户信息到 ThreadLocal
     * @param user 当前登录用户的 DTO 对象，包含用户 ID、昵称等信息
     */
    public static void saveUser(UserDTO user){
        tl.set(user);
    }

    /**
     * 获取当前线程中的用户信息
     * @return 当前登录用户的 DTO 对象，如果未设置则返回 null
     */
    public static UserDTO getUser(){
        return tl.get();
    }

    /**
     * 移除当前线程的用户信息，防止内存泄漏
     */
    public static void removeUser(){
        tl.remove();
    }
}
