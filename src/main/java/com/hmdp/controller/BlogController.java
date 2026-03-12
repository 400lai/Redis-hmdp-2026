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
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

import static com.hmdp.utils.SystemConstants.MAX_PAGE_SIZE;

/**
 * 博客控制器 - 处理探店博文相关的 HTTP 请求，包括发布、点赞、查询个人博客和热门博客等操作
 */
@RestController
@RequestMapping("/blog")
public class BlogController {

    @Resource
    private IBlogService blogService;

    /**
     * 发布新的探店博文
     * @param blog 博文信息，包含标题、内容、图片、商铺 ID 等
     * @return 操作结果，成功返回新增的博文 ID
     */
    @PostMapping
    public Result saveBlog(@RequestBody Blog blog) {
        return blogService.saveBlog(blog);
    }

    /**
     * 点赞博文
     * @param id 博文 ID
     * @return 操作结果
     */
    @PutMapping("/like/{id}")
    public Result likeBlog(@PathVariable("id") Long id) {
        return blogService.likeBlog(id);
    }

    /**
     * 查询当前登录用户的博客列表（分页）
     * @param current 当前页码，默认值为 1
     * @return 操作结果，成功返回当前用户的博客列表
     */
    @GetMapping("/of/me")
    public Result queryMyBlog(@RequestParam(value = "current", defaultValue = "1") Integer current) {
        // 获取登录用户
        UserDTO user = UserHolder.getUser();
        // 根据用户查询
        Page<Blog> page = blogService.query()
                .eq("user_id", user.getId()).page(new Page<>(current, MAX_PAGE_SIZE));
        // 获取当前页数据
        List<Blog> records = page.getRecords();
        return Result.ok(records);
    }

    /**
     * 查询热门博客列表（按点赞数降序排列，分页）
     * @param current 当前页码，默认值为 1
     * @return 操作结果，成功返回热门博客列表，包含博主昵称和头像信息
     */
    @GetMapping("/hot")
    public Result queryHotBlog(@RequestParam(value = "current", defaultValue = "1") Integer current) {
        return blogService.queryHotBlog(current);
    }

    /**
     * 根据 ID 查询博客详情
     * @param id 博客 ID
     * @return 操作结果，成功返回博客详情信息，包含博主信息和点赞状态
     */
    @GetMapping("/{id}")
    public Result queryBlogById(@PathVariable("id") Long id){
        return blogService.queryBlogById(id);
    }

    @GetMapping("/likes/{id}")
    public Result queryBlogLikes(@PathVariable("id") Long id) {
        return blogService.queryBlogLikes(id);
    }

    @GetMapping("/of/user")
    public Result queryBlogByUserId(
            @RequestParam(value = "current", defaultValue = "1") Integer current,
            @RequestParam("id") Long id) {
        // 根据用户查询
        Page<Blog> page = blogService.query()
                .eq("user_id", id).page(new Page<>(current, SystemConstants.MAX_PAGE_SIZE));
        // 获取当前页数据
        List<Blog> records = page.getRecords();
        return Result.ok(records);
    }
}
