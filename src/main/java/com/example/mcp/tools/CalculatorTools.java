package com.example.mcp.tools;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * 计算器工具集
 * 提供基础数学运算功能
 */
@Component
public class CalculatorTools {

    private static final Logger log = LoggerFactory.getLogger(CalculatorTools.class);

    @Tool(description = "计算两个整数的加法运算")
    public int add(
            @ToolParam(description = "第一个加数") int a,
            @ToolParam(description = "第二个加数") int b) {
        log.info("执行加法: {} + {}", a, b);
        return a + b;
    }

    @Tool(description = "计算两个整数的减法运算")
    public int subtract(
            @ToolParam(description = "被减数") int a,
            @ToolParam(description = "减数") int b) {
        log.info("执行减法: {} - {}", a, b);
        return a - b;
    }

    @Tool(description = "计算两个整数的乘法运算")
    public int multiply(
            @ToolParam(description = "第一个乘数") int a,
            @ToolParam(description = "第二个乘数") int b) {
        log.info("执行乘法: {} * {}", a, b);
        return a * b;
    }

    @Tool(description = "计算两个数的除法运算，返回精确结果")
    public String divide(
            @ToolParam(description = "被除数") double a,
            @ToolParam(description = "除数，不能为零") double b) {
        log.info("执行除法: {} / {}", a, b);
        if (b == 0) {
            return "错误：除数不能为零";
        }
        double result = a / b;
        return String.format("%.4f", result);
    }

    @Tool(description = "计算一个数的平方根")
    public String sqrt(@ToolParam(description = "要计算平方根的数，必须非负") double number) {
        log.info("执行平方根: sqrt({})", number);
        if (number < 0) {
            return "错误：不能对负数求平方根";
        }
        return String.format("%.4f", Math.sqrt(number));
    }

    @Tool(description = "计算一个数的幂运算")
    public String power(
            @ToolParam(description = "底数") double base,
            @ToolParam(description = "指数") double exponent) {
        log.info("执行幂运算: {} ^ {}", base, exponent);
        return String.format("%.4f", Math.pow(base, exponent));
    }
}
