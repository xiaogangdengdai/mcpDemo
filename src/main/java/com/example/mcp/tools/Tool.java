package com.example.mcp.tools;

import java.lang.annotation.*;

/**
 * 工具注解 - 用于标记可被 AI 调用的方法
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Tool {
    String description() default "";
}
