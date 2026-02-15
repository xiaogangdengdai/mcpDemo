package com.example.mcp.tools;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Random;

/**
 * 字符串工具集
 * 提供字符串处理功能
 */
@Component
public class StringTools {

    private static final Logger log = LoggerFactory.getLogger(StringTools.class);

    @Tool(description = "将文本转换为大写")
    public String toUpperCase(@ToolParam(description = "要转换的文本") String text) {
        log.info("转大写: {}", text);
        return text.toUpperCase();
    }

    @Tool(description = "将文本转换为小写")
    public String toLowerCase(@ToolParam(description = "要转换的文本") String text) {
        log.info("转小写: {}", text);
        return text.toLowerCase();
    }

    @Tool(description = "计算文本的字符数量")
    public String countCharacters(@ToolParam(description = "要统计的文本") String text) {
        log.info("统计字符数: {}", text);
        int charCount = text.length();
        int charCountNoSpace = text.replace(" ", "").length();
        int wordCount = text.isEmpty() ? 0 : text.trim().split("\\s+").length;
        return String.format("字符统计 - 总字符数: %d, 不含空格: %d, 单词数: %d",
                charCount, charCountNoSpace, wordCount);
    }

    @Tool(description = "反转文本")
    public String reverseText(@ToolParam(description = "要反转的文本") String text) {
        log.info("反转文本: {}", text);
        return new StringBuilder(text).reverse().toString();
    }

    @Tool(description = "生成随机字符串")
    public String generateRandomString(
            @ToolParam(description = "字符串长度，默认16") int length) {
        log.info("生成随机字符串，长度: {}", length);
        if (length <= 0 || length > 100) {
            return "错误：长度必须在 1-100 之间";
        }
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        Random random = new Random();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < length; i++) {
            sb.append(chars.charAt(random.nextInt(chars.length())));
        }
        return "随机字符串: " + sb.toString();
    }

    @Tool(description = "检查字符串是否为回文")
    public String checkPalindrome(@ToolParam(description = "要检查的文本") String text) {
        log.info("检查回文: {}", text);
        String cleaned = text.toLowerCase().replaceAll("[^a-z0-9]", "");
        String reversed = new StringBuilder(cleaned).reverse().toString();
        boolean isPalindrome = cleaned.equals(reversed);
        return String.format("文本 \"%s\" %s回文", text, isPalindrome ? "是" : "不是");
    }
}
