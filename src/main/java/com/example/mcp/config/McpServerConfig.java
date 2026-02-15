package com.example.mcp.config;

import com.example.mcp.tools.CalculatorTools;
import com.example.mcp.tools.TimeTools;
import com.example.mcp.tools.UserTools;
import com.example.mcp.tools.StringTools;
import com.example.mcp.tools.CryptoTools;
import com.example.mcp.tools.SystemLogTools;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;
import java.util.List;

/**
 * MCP 服务器配置类
 * 注册所有工具类到 MCP 服务器
 */
@Configuration
public class McpServerConfig {

    private static final Logger log = LoggerFactory.getLogger(McpServerConfig.class);

    public McpServerConfig() {
        log.info("初始化 MCP 服务器配置");
    }

    /**
     * 注册所有工具类
     */
    @Bean
    public List<Object> allTools(
            CalculatorTools calculatorTools,
            TimeTools timeTools,
            UserTools userTools,
            StringTools stringTools,
            CryptoTools cryptoTools,
            SystemLogTools systemLogTools) {
        log.info("注册所有工具类");
        return Arrays.asList(
            calculatorTools,
            timeTools,
            userTools,
            stringTools,
            cryptoTools,
            systemLogTools
        );
    }
}
