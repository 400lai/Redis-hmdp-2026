package com.hmdp.controller;


import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hmdp.dto.Result;
import com.hmdp.dto.UserDTO;
import com.hmdp.entity.Blog;
import com.hmdp.entity.User;
import com.hmdp.service.IBlogService;
import com.hmdp.service.IUserService;
import com.hmdp.utils.SystemConstants;
import com.hmdp.utils.UserHolder;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.hmdp.utils.SystemConstants.MAX_PAGE_SIZE;

/**
 * 博客控制器 - 处理探店博文相关的 HTTP 请求，包括发布、点赞、查询个人博客和热门博客等操作
 */
@Tag(name = "博客管理", description = "探店博文发布、点赞、查询等接口")
@RestController
@RequestMapping("/blog")
public class BlogController {

    @Resource
    private IBlogService blogService;

    @Operation(summary = "发布博客", description = "发布新的探店博文")
    @PostMapping
    public Result saveBlog(
            @Parameter(description = "博文信息", required = true) @RequestBody Blog blog) {
        return blogService.saveBlog(blog);
    }

    @Operation(summary = "点赞博客", description = "为指定博文点赞")
    @PutMapping("/like/{id}")
    public Result likeBlog(
            @Parameter(description = "博文 ID", required = true) @PathVariable("id") Long id) {
        return blogService.likeBlog(id);
    }

    @Operation(summary = "查询我的博客", description = "查询当前登录用户的博客列表")
    @GetMapping("/of/me")
    public Result queryMyBlog(
            @Parameter(description = "当前页码", required = true) @RequestParam(value = "current", defaultValue = "1") Integer current) {
        UserDTO user = UserHolder.getUser();
        Page<Blog> page = blogService.query()
                .eq("user_id", user.getId()).page(new Page<>(current, MAX_PAGE_SIZE));
        List<Blog> records = page.getRecords();
        return Result.ok(records);
    }

    @Operation(summary = "热门博客", description = "查询热门博客列表（按点赞数降序）")
    @GetMapping("/hot")
    public Result queryHotBlog(
            @Parameter(description = "当前页码", required = true) @RequestParam(value = "current", defaultValue = "1") Integer current) {
        return blogService.queryHotBlog(current);
    }

    @Operation(summary = "查询博客详情", description = "根据 ID 查询博客详细信息")
    @GetMapping("/{id}")
    public Result queryBlogById(
            @Parameter(description = "博客 ID", required = true) @PathVariable("id") Long id){
        return blogService.queryBlogById(id);
    }

    @Operation(summary = "博客点赞用户", description = "查询博客的点赞用户列表")
    @GetMapping("/likes/{id}")
    public Result queryBlogLikes(
            @Parameter(description = "博客 ID", required = true) @PathVariable("id") Long id) {
        return blogService.queryBlogLikes(id);
    }

    @Operation(summary = "查询用户博客", description = "查询指定用户的博客列表")
    @GetMapping("/of/user")
    public Result queryBlogByUserId(
            @Parameter(description = "当前页码", required = true) @RequestParam(value = "current", defaultValue = "1") Integer current,
            @Parameter(description = "用户 ID", required = true) @RequestParam("id") Long id) {
        Page<Blog> page = blogService.query()
                .eq("user_id", id).page(new Page<>(current, SystemConstants.MAX_PAGE_SIZE));
        List<Blog> records = page.getRecords();
        return Result.ok(records);
    }

    @Operation(summary = "关注人博客", description = "查询关注的人发布的博客")
    @GetMapping("/of/follow")
    public Result queryBlogOfFollow(
            @Parameter(description = "最大时间戳", required = true) @RequestParam("lastId") Long max,
            @Parameter(description = "偏移量", required = true) @RequestParam(value = "offset", defaultValue = "0") Integer offset
    ){
        return blogService.queryBlogOfFollow(max, offset);
    }

}
