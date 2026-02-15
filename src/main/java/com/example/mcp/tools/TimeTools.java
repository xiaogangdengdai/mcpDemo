package com.example.mcp.tools;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.ZoneId;

/**
 * 时间工具集
 * 提供时间相关的功能
 */
@Component
public class TimeTools {

    private static final Logger log = LoggerFactory.getLogger(TimeTools.class);
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Tool(description = "获取当前日期和时间")
    public String getCurrentTime() {
        log.info("获取当前时间");
        LocalDateTime now = LocalDateTime.now();
        return "当前时间: " + now.format(FORMATTER);
    }

    @Tool(description = "获取指定时区的当前时间")
    public String getTimeByTimezone(
            @ToolParam(description = "时区ID，例如: Asia/Shanghai, America/New_York, Europe/London") String timezone) {
        log.info("获取时区时间: {}", timezone);
        try {
            ZoneId zoneId = ZoneId.of(timezone);
            LocalDateTime now = LocalDateTime.now(zoneId);
            return String.format("时区 %s 当前时间: %s", timezone, now.format(FORMATTER));
        } catch (Exception e) {
            return "错误：无效的时区ID '" + timezone + "'。示例有效时区: Asia/Shanghai, America/New_York, Europe/London";
        }
    }

    @Tool(description = "获取当前是星期几")
    public String getDayOfWeek() {
        log.info("获取星期几");
        LocalDateTime now = LocalDateTime.now();
        String[] weekDays = {"星期一", "星期二", "星期三", "星期四", "星期五", "星期六", "星期日"};
        int dayIndex = now.getDayOfWeek().getValue() - 1;
        return "今天是: " + weekDays[dayIndex] + " (" + now.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) + ")";
    }

    @Tool(description = "计算两个日期之间的天数差")
    public String daysBetween(
            @ToolParam(description = "开始日期，格式: yyyy-MM-dd") String startDate,
            @ToolParam(description = "结束日期，格式: yyyy-MM-dd") String endDate) {
        log.info("计算天数差: {} 到 {}", startDate, endDate);
        try {
            java.time.LocalDate start = java.time.LocalDate.parse(startDate);
            java.time.LocalDate end = java.time.LocalDate.parse(endDate);
            long days = java.time.temporal.ChronoUnit.DAYS.between(start, end);
            return String.format("从 %s 到 %s 相隔 %d 天", startDate, endDate, Math.abs(days));
        } catch (Exception e) {
            return "错误：日期格式不正确，请使用 yyyy-MM-dd 格式，例如: 2024-01-15";
        }
    }
}
