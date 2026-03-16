package com.hmdp.controller;


import cn.hutool.core.bean.BeanUtil;
import com.hmdp.dto.LoginFormDTO;
import com.hmdp.dto.Result;
import com.hmdp.dto.UserDTO;
import com.hmdp.entity.User;
import com.hmdp.entity.UserInfo;
import com.hmdp.service.IUserInfoService;
import com.hmdp.service.IUserService;
import com.hmdp.utils.UserHolder;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;


/**
 * 用户控制器 - 处理用户相关的 HTTP 请求，包括发送验证码、登录、登出、查询用户信息等
 */
@Slf4j
@Tag(name = "用户管理", description = "用户登录、注册、信息查询等接口")
@RestController
@RequestMapping("/user")
public class UserController {

    @Resource
    private IUserService userService;

    @Resource
    private IUserInfoService userInfoService;

    @Operation(summary = "发送验证码", description = "向指定手机号发送短信验证码")
    @PostMapping("code")
    public Result sendCode(
            @Parameter(description = "手机号码", required = true) @RequestParam("phone") String phone,
            HttpSession session) {
        return userService.sendCode(phone, session);
    }

    @Operation(summary = "用户登录", description = "支持验证码登录或密码登录，返回用户信息和 JWT Token")
    @PostMapping("/login")
    public Result login(
            @Parameter(description = "登录表单数据", required = true) @RequestBody LoginFormDTO loginForm,
            HttpSession session){
        return userService.login(loginForm, session);
    }

    @Operation(summary = "用户登出", description = "退出登录状态")
    @PostMapping("/logout")
    public Result logout(){
        return Result.fail("功能未完成");
    }

    @Operation(summary = "获取当前用户信息", description = "获取当前登录用户的详细信息")
    @GetMapping("/me")
    public Result me(){
        UserDTO userDTO = UserHolder.getUser();
        return Result.ok(userDTO);
    }

    @Operation(summary = "查询用户详情", description = "查询指定用户的详细信息")
    @GetMapping("/info/{id}")
    public Result info(
            @Parameter(description = "用户 ID", required = true) @PathVariable("id") Long userId){
        UserInfo info = userInfoService.getById(userId);
        if (info == null) {
            return Result.ok();
        }
        info.setCreateTime(null);
        info.setUpdateTime(null);
        return Result.ok(info);
    }

    @Operation(summary = "查询用户信息", description = "根据用户 ID 查询用户基本信息")
    @GetMapping("/{id}")
    public Result queryUserById(
            @Parameter(description = "用户 ID", required = true) @PathVariable("id") Long userId){
        User user = userService.getById(userId);
        if (user == null) {
            return Result.ok();
        }
        UserDTO userDTO = BeanUtil.copyProperties(user, UserDTO.class);
        return Result.ok(userDTO);
    }

    @Operation(summary = "签到", description = "用户每日签到打卡")
    @PostMapping("/sign")
    public Result sign(){
        return userService.sign();
    }

    @Operation(summary = "获取签到次数", description = "获取当前用户本月累计签到次数")
    @GetMapping("/sign/count")
    public Result signCount(){
        return userService.signCount();
    }
}
