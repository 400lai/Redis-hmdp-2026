package com.hmdp.controller;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import com.hmdp.dto.Result;
import com.hmdp.utils.SystemConstants;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

/**
 * 文件上传控制器 - 处理博客图片的上传和删除操作
 */
@Slf4j
@Tag(name = "文件上传", description = "图片上传、删除等接口")
@RestController
@RequestMapping("upload")
public class UploadController {

    @Operation(summary = "上传图片", description = "上传博客图片")
    @PostMapping("blog")
    public Result uploadImage(
            @Parameter(description = "上传的图片文件", required = true) @RequestParam("file") MultipartFile image) {
        try {
            String originalFilename = image.getOriginalFilename();
            String fileName = createNewFileName(originalFilename);
            image.transferTo(new File(SystemConstants.IMAGE_UPLOAD_DIR, fileName));
            log.debug("文件上传成功，{}", fileName);
            return Result.ok(fileName);
        } catch (IOException e) {
            throw new RuntimeException("文件上传失败", e);
        }
    }

    @Operation(summary = "删除图片", description = "删除博客图片")
    @GetMapping("/blog/delete")
    public Result deleteBlogImg(
            @Parameter(description = "要删除的文件名", required = true) @RequestParam("name") String filename) {
        File file = new File(SystemConstants.IMAGE_UPLOAD_DIR, filename);
        if (file.isDirectory()) {
            return Result.fail("错误的文件名称");
        }
        FileUtil.del(file);
        return Result.ok();
    }

    /**
     * 生成新的文件名（使用 UUID 避免重名，并通过哈希值分目录存储）
     * @param originalFilename 原始文件名，用于提取文件后缀
     * @return 生成的新文件名路径，格式为 /blogs/{d1}/{d2}/{uuid}.{suffix}
     */
    private String createNewFileName(String originalFilename) {
        // 获取后缀
        String suffix = StrUtil.subAfter(originalFilename, ".", true);
        // 生成目录
        String name = UUID.randomUUID().toString();
        int hash = name.hashCode();
        int d1 = hash & 0xF;
        int d2 = (hash >> 4) & 0xF;
        // 判断目录是否存在
        File dir = new File(SystemConstants.IMAGE_UPLOAD_DIR, StrUtil.format("/blogs/{}/{}", d1, d2));
        if (!dir.exists()) {
            dir.mkdirs();
        }
        // 生成文件名
        return StrUtil.format("/blogs/{}/{}/{}.{}", d1, d2, name, suffix);
    }
}
