package com.hmdp.dto;

import lombok.Data;

/**
 * 用户数据传输对象 - 用于在 Web 层传递脱敏后的用户信息
 * 仅包含公开的用户基本信息，不包含敏感数据（如密码、手机号等）
 */
@Data
public class UserDTO {
    /**
     * 用户唯一标识
     */
    private Long id;

    /**
     * 用户昵称，用于前端展示
     */
    private String nickName;

    /**
     * 用户头像 URL 地址
     */
    private String icon;
}
