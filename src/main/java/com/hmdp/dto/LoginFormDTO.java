package com.hmdp.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 登录表单数据传输对象 - 用于接收用户登录请求的参数
 * 支持两种登录方式：手机号验证码登录和密码登录
 */
@Data
@Schema(description = "登录表单数据")
public class LoginFormDTO {
    @Schema(description = "手机号码", example = "13800138000")
    private String phone;

    @Schema(description = "短信验证码", example = "123456")
    private String code;

    @Schema(description = "密码", example = "password123")
    private String password;
}

