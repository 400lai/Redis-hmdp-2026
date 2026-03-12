package com.hmdp.service.impl;

import cn.hutool.core.util.BooleanUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hmdp.dto.Result;
import com.hmdp.dto.UserDTO;
import com.hmdp.entity.Blog;
import com.hmdp.entity.User;
import com.hmdp.mapper.BlogMapper;
import com.hmdp.service.IBlogService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hmdp.service.IUserService;
import com.hmdp.utils.SystemConstants;
import com.hmdp.utils.UserHolder;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

import static com.hmdp.utils.SystemConstants.MAX_PAGE_SIZE;


@Service
public class BlogServiceImpl extends ServiceImpl<BlogMapper, Blog> implements IBlogService {

    @Resource
    private IUserService userService;
    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Override
    public Result saveBlog(Blog blog) {
        // 获取登录用户
        UserDTO userDTO = UserHolder.getUser();
        blog.setUserId(userDTO.getId());
        // 保存探店博文
        save(blog);
        // 返回id
        return Result.ok(blog.getId());
    }

    /**
     * 根据当前用户的点赞状态进行切换：如果未点赞则点赞，如果已点赞则取消点赞
     * 使用 Redis 的 Set 结构存储点赞用户 ID 集合，保证点赞状态的原子性和一致性
     */
    @Override
    public Result likeBlog(Long id) {
        // 1.获取登录用户
        Long userId = UserHolder.getUser().getId();

        // 2.判断当前登录用户是否已经点赞
        String key = "blog:liked:" + id;
        Boolean isMember = stringRedisTemplate.opsForSet().isMember(key, userId.toString());
        if (!Boolean.TRUE.equals(isMember)) {
            // 3.如果未点赞，可以点赞
            // 3.1 数据库点赞数+1
            boolean isSuccess = update().setSql("liked = liked + 1").eq("id", id).update();
            // 3.2 保存用户到Redis的set集合
            if(isSuccess){
                stringRedisTemplate.opsForSet().add(key, userId.toString());
            }
        }else {
            // 4.如果已点赞，取消点赞
            // 4.1 数据库点赞数-1
            boolean isSuccess = update().setSql("liked = liked - 1").eq("id", id).update();
            // 4.2 把用户从Redis的set集合移除
            if(isSuccess){
                stringRedisTemplate.opsForSet().remove(key, userId.toString());
            }
        }
        return Result.ok();
    }

    /**
     * 查询热门笔记列表
     * 按照点赞数降序排列，分页展示热门笔记，并补充每个笔记的作者信息和点赞状态
     */
    @Override
    public Result queryHotBlog(Integer current) {
        // 按照点赞数降序分页查询博客
        Page<Blog> page = query()
                .orderByDesc("liked")
                .page(new Page<>(current, MAX_PAGE_SIZE));
        // 获取当前页的博客记录列表
        List<Blog> records = page.getRecords();
        // 为每个博客补充作者信息和点赞状态
        records.forEach(blog -> {
            this.queryBlogUser(blog);
            this.isBlogLiked(blog);
        });
        return Result.ok(records);
    }

    /**
     * 根据 ID 查询笔记详情
     * 查询指定笔记的完整信息，包括笔记内容、作者信息和当前用户的点赞状态
     */
    @Override
    public Result queryBlogById(Long id) {
        // 1.根据 ID 查询笔记基本信息
        Blog blog = getById(id);
        if(blog == null){
            return Result.fail("笔记不存在！");
        }

        // 2.查询并设置笔记作者信息
        queryBlogUser(blog);

        // 3.查询blog是否被点赞
        isBlogLiked(blog);

        return Result.ok(blog);
    }

    /**
     * 检查当前登录用户是否已点赞该博客，并设置点赞状态
     */
    private void isBlogLiked(Blog blog) {
        // 1.获取登录用户
        Long userId = UserHolder.getUser().getId();

        // 2.判断当前登录用户是否已经点赞
        String key = "blog:liked:" + blog.getId();
        Boolean isMember = stringRedisTemplate.opsForSet().isMember(key, userId.toString());
        blog.setIsLike(Boolean.TRUE.equals(isMember));
    }

    /**
     * 查询博客作者信息并设置到博客对象中
     */
    private void queryBlogUser(Blog blog) {
        Long userId = blog.getUserId();
        User user = userService.getById(userId);
        blog.setName(user.getNickName());
        blog.setIcon(user.getIcon());
    }
}
