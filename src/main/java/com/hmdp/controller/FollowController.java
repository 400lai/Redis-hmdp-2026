package com.hmdp.controller;


import com.hmdp.dto.Result;
import com.hmdp.service.IFollowService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

/**
 *  关注控制器
 */
@RestController
@RequestMapping("/follow")
public class FollowController {

    @Resource
    private IFollowService followService;

    /**
     * 关注或取消关注用户
     * 根据 isFollow 参数决定是关注还是取消关注指定用户
     * @param followUserId 要关注或取消关注的用户 ID
     * @param isFollow true 表示关注，false 表示取消关注
     */
    @PutMapping("/{id}/{isFollow}")
    public Result follow(@PathVariable("id") Long followUserId, @PathVariable("isFollow") Boolean isFollow) {
        return followService.follow(followUserId, isFollow);
    }

    /**
     * 判断当前登录用户是否已关注指定用户
     * 查询当前用户与目标用户之间的关注关系
     * @param followUserId 要查询的目标用户 ID
     * @return 操作结果，成功返回是否关注的布尔值
     */
    @GetMapping("/or/not/{id}")
    public Result isFollow(@PathVariable("id") Long followUserId) {
        return followService.isFollow(followUserId);
    }

    /**
     * 查询当前用户与指定用户的共同关注列表
     * 找出当前登录用户和目标用户都关注的其他用户
     * @param id 目标用户 ID，用于查询与该用户的共同关注列表
     * @return 操作结果，成功返回共同关注的用户列表
     */
    @GetMapping("/common/{id}")
    public Result followCommons(@PathVariable("id") Long id){
        return followService.followCommons(id);
    }
}
