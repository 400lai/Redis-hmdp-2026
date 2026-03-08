package com.hmdp.config;

import com.hmdp.dto.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 全局异常处理器 - 捕获并处理 Controller 层抛出的运行时异常，统一返回错误信息
 */
@Slf4j
@RestControllerAdvice
public class WebExceptionAdvice {

    /**
     * 处理运行时异常，记录错误日志并返回统一的错误响应
     * @param e 运行时异常对象，包含异常详细信息
     * @return 操作结果，返回"服务器异常"错误信息
     */
    @ExceptionHandler(RuntimeException.class)
    public Result handleRuntimeException(RuntimeException e) {
        log.error(e.toString(), e);
        return Result.fail("服务器异常");
    }
}
