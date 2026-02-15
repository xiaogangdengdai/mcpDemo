package com.example.mcp.tools;

import java.lang.annotation.*;

/**
 * 工具参数注解 - 用于标记方法参数的描述
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ToolParam {
    String description() default "";
}
