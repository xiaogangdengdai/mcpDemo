package com.example.mcp.tools;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

/**
 * 系统日志工具集
 * 提供氧屋系统问题日志查询功能
 */
@Component
public class SystemLogTools {

    private static final Logger log = LoggerFactory.getLogger(SystemLogTools.class);
    private final JdbcTemplate jdbcTemplate;

    public SystemLogTools(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Tool(description = "获取氧屋系统最早的1条问题日志")
    public String getSystemLog() {
        log.info("查询最早的待处理问题日志");
        String sql = "SELECT description, remark FROM system_issue_log " +
                     "WHERE status = 1 ORDER BY created_at ASC LIMIT 1";
        try {
            return jdbcTemplate.query(sql, rs -> {
                if (rs.next()) {
                    String description = rs.getString("description");
                    String remark = rs.getString("remark");
                    return String.format("描述信息为：%s，备注信息为%s。", description, remark);
                }
                return "未找到待处理的问题日志。";
            });
        } catch (Exception e) {
            log.error("查询问题日志失败", e);
            return "查询失败：" + e.getMessage();
        }
    }
}
