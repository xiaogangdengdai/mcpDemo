package com.example.mcp.tools;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

/**
 * 系统日志工具集
 * 提供氧屋系统问题日志查询、保存和更新功能
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

        String issueLogSql = "SELECT id, type, create_table_sql, before_transformation, " +
                             "transformation, business_context, status, new_requirement " +
                             "FROM system_issue_log WHERE status = 1 ORDER BY created_at ASC LIMIT 1";

        try {
            return jdbcTemplate.query(issueLogSql, rs -> {
                if (rs.next()) {
                    String id = rs.getString("id");
                    String type = rs.getString("type");
                    String createTableSql = rs.getString("create_table_sql");
                    String beforeTransformation = rs.getString("before_transformation");
                    String transformation = rs.getString("transformation");
                    String businessContext = rs.getString("business_context");
                    String status = rs.getString("status");
                    String newRequirement = rs.getString("new_requirement");

                    // 查询关联的附件
                    List<String> attachmentPaths = queryAttachmentPaths(id);

                    // 构建返回结果
                    return buildResultXml(id, type, createTableSql, beforeTransformation,
                                          transformation, businessContext, status, newRequirement, attachmentPaths);
                }
                return "未找到待处理的问题日志。";
            });
        } catch (Exception e) {
            log.error("查询问题日志失败", e);
            return "查询失败：" + e.getMessage();
        }
    }

    @Tool(description = "保存系统问题日志到数据库")
    public String saveSystemLog(
        @ToolParam(description = "类型：1.bug修复 2.新功能开发 3.原有功能改造 4.页面原型快速实现") Integer type,
        @ToolParam(description = "问题详细描述") String description,
        @ToolParam(description = "备注") String remark,
        @ToolParam(description = "SQL建表语句") String createTableSql,
        @ToolParam(description = "新需求描述") String newRequirement,
        @ToolParam(description = "改造前功能描述") String beforeTransformation,
        @ToolParam(description = "改造后的目标") String transformation,
        @ToolParam(description = "业务介绍") String businessContext,
        @ToolParam(description = "创建人") String creator
    ) {
        log.info("保存系统问题日志, type={}, creator={}", type, creator);

        // 生成UUID作为主键
        String id = UUID.randomUUID().toString();

        String sql = "INSERT INTO system_issue_log " +
                     "(id, type, description, remark, create_table_sql, new_requirement, " +
                     "before_transformation, transformation, business_context, status, creator) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, 1, ?)";

        try {
            int rows = jdbcTemplate.update(sql,
                id,
                type,
                description,
                remark,
                createTableSql,
                newRequirement,
                beforeTransformation,
                transformation,
                businessContext,
                creator
            );

            if (rows > 0) {
                return "保存成功，ID: " + id;
            } else {
                return "保存失败：未插入任何记录";
            }
        } catch (Exception e) {
            log.error("保存问题日志失败", e);
            return "保存失败：" + e.getMessage();
        }
    }

    @Tool(description = "更新系统问题日志的状态和描述")
    public String updateSystemLog(
        @ToolParam(description = "问题日志ID") String id,
        @ToolParam(description = "状态：1.待处理 2.处理中 3.已完成 4.处理失败") Integer status,
        @ToolParam(description = "问题详细描述（非必填，不传则不更新）") String description
    ) {
        log.info("更新系统问题日志, id={}, status={}", id, status);

        try {
            int rows;
            if (description != null) {
                // 同时更新 status 和 description
                String sql = "UPDATE system_issue_log SET status = ?, description = ? WHERE id = ?";
                rows = jdbcTemplate.update(sql, status, description, id);
            } else {
                // 只更新 status
                String sql = "UPDATE system_issue_log SET status = ? WHERE id = ?";
                rows = jdbcTemplate.update(sql, status, id);
            }

            if (rows > 0) {
                return "更新成功，影响记录数: " + rows;
            } else {
                return "更新失败：未找到ID为 " + id + " 的记录";
            }
        } catch (Exception e) {
            log.error("更新问题日志失败", e);
            return "更新失败：" + e.getMessage();
        }
    }

    @Tool(description = "保存系统附件信息到数据库")
    public String saveSystemAttachment(
        @ToolParam(description = "关联的目标ID") String targetId,
        @ToolParam(description = "文件路径") String filePath,
        @ToolParam(description = "文件名称") String fileName,
        @ToolParam(description = "排序顺序，默认为0") Integer sortOrder
    ) {
        log.info("保存系统附件, targetId={}, fileName={}", targetId, fileName);

        // 生成UUID作为主键
        String id = UUID.randomUUID().toString();

        // type 固定为 2
        Integer type = 2;

        // 如果 sortOrder 为 null，默认为 0
        if (sortOrder == null) {
            sortOrder = 0;
        }

        String sql = "INSERT INTO sys_attachment " +
                     "(id, target_id, file_path, file_name, type, sort_order, created_at) " +
                     "VALUES (?, ?, ?, ?, ?, ?, NOW())";

        try {
            int rows = jdbcTemplate.update(sql,
                id,
                targetId,
                filePath,
                fileName,
                type,
                sortOrder
            );

            if (rows > 0) {
                return "保存成功，附件ID: " + id;
            } else {
                return "保存失败：未插入任何记录";
            }
        } catch (Exception e) {
            log.error("保存系统附件失败", e);
            return "保存失败：" + e.getMessage();
        }
    }

    /**
     * 查询关联的附件路径列表
     * @param targetId 关联的目标ID (system_issue_log的id)
     * @return 附件路径列表
     */
    private List<String> queryAttachmentPaths(String targetId) {
        String attachmentSql = "SELECT file_path FROM sys_attachment " +
                               "WHERE target_id = ? AND type = 1 " +
                               "ORDER BY sort_order ASC";

        return jdbcTemplate.query(attachmentSql,
            ps -> ps.setString(1, targetId),
            (rs, rowNum) -> rs.getString("file_path"));
    }

    /**
     * 构建 XML 格式的返回结果
     */
    private String buildResultXml(String id, String type, String createTableSql, String beforeTransformation,
                                  String transformation, String businessContext, String status,
                                  String newRequirement, List<String> attachmentPaths) {
        StringBuilder sb = new StringBuilder();

        sb.append("<referenceInfo>\n");

        sb.append("    <id>\n");
        sb.append("    ").append(nullToEmpty(id)).append("\n");
        sb.append("    </id>\n");

        sb.append("    <type>\n");
        sb.append("    ").append(nullToEmpty(type)).append("\n");
        sb.append("    </type>\n");

        sb.append("    <createTableSql>\n");
        sb.append("    ").append(nullToEmpty(createTableSql)).append("\n");
        sb.append("    </createTableSql>\n");

        sb.append("    <newRequirement>\n");
        sb.append("    ").append(nullToEmpty(newRequirement)).append("\n");
        sb.append("    </newRequirement>\n");

        sb.append("    <beforeTransformation>\n");
        sb.append("    ").append(nullToEmpty(beforeTransformation)).append("\n");
        sb.append("    </beforeTransformation>\n");

        sb.append("    <transformation>\n");
        sb.append("    ").append(nullToEmpty(transformation)).append("\n");
        sb.append("    </transformation>\n");

        sb.append("    <businessContext>\n");
        sb.append("    ").append(nullToEmpty(businessContext)).append("\n");
        sb.append("    </businessContext>\n");

        sb.append("    <status>\n");
        sb.append("    ").append(nullToEmpty(status)).append("\n");
        sb.append("    </status>\n");

        sb.append("    <attachmentPaths>\n");
        for (String path : attachmentPaths) {
            sb.append("     <attachmentPath>\n");
            sb.append("     ").append(nullToEmpty(path)).append("\n");
            sb.append("     </attachmentPath>\n");
        }
        sb.append("    </attachmentPaths>\n");

        sb.append("</referenceInfo>");

        return sb.toString();
    }

    /**
     * 空值转换为空字符串
     */
    private String nullToEmpty(String value) {
        return value == null ? "" : value;
    }
}
