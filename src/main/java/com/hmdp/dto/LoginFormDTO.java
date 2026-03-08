package com.hmdp.dto;

import lombok.Data;

/**
 * 登录表单数据传输对象 - 用于接收用户登录请求的参数
 * 支持两种登录方式：手机号验证码登录和密码登录
 */
@Data
public class LoginFormDTO {
    /**
     * 用户手机号码，用于登录验证和用户标识
     */
    private String phone;

    /**
     * 短信验证码，用于手机号验证码登录方式
     */
    private String code;

    /**
     * 用户密码，用于密码登录方式
     */
    private String password;
}

