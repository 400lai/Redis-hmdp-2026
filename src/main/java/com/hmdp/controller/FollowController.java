package com.hmdp.controller;


import com.hmdp.dto.Result;
import com.hmdp.service.IFollowService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;


/**
 *  关注控制器
 */
@Tag(name = "关注管理", description = "用户关注、取关、关注列表等接口")
@RestController
@RequestMapping("/follow")
public class FollowController {

    @Resource
    private IFollowService followService;

    @Operation(summary = "关注/取消关注", description = "关注或取消关注指定用户")
    @PutMapping("/{id}/{isFollow}")
    public Result follow(
            @Parameter(description = "要关注或取消关注的用户 ID", required = true) @PathVariable("id") Long followUserId,
            @Parameter(description = "true 表示关注，false 表示取消关注", required = true) @PathVariable("isFollow") Boolean isFollow) {
        return followService.follow(followUserId, isFollow);
    }

    @Operation(summary = "是否关注", description = "判断当前登录用户是否已关注指定用户")
    @GetMapping("/or/not/{id}")
    public Result isFollow(
            @Parameter(description = "目标用户 ID", required = true) @PathVariable("id") Long followUserId) {
        return followService.isFollow(followUserId);
    }

    @Operation(summary = "共同关注", description = "查询当前用户与指定用户的共同关注列表")
    @GetMapping("/common/{id}")
    public Result followCommons(
            @Parameter(description = "目标用户 ID", required = true) @PathVariable("id") Long id){
        return followService.followCommons(id);
    }
}
