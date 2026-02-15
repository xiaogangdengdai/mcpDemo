package com.example.mcp.tools;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Base64;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * 加密工具集
 * 提供常用的加密和编码功能
 */
@Component
public class CryptoTools {

    private static final Logger log = LoggerFactory.getLogger(CryptoTools.class);

    @Tool(description = "将文本进行 Base64 编码")
    public String base64Encode(@ToolParam(description = "要编码的文本") String text) {
        log.info("Base64 编码");
        String encoded = Base64.getEncoder().encodeToString(text.getBytes());
        return "Base64 编码结果: " + encoded;
    }

    @Tool(description = "将 Base64 文本解码")
    public String base64Decode(@ToolParam(description = "要解码的 Base64 文本") String encodedText) {
        log.info("Base64 解码");
        try {
            byte[] decoded = Base64.getDecoder().decode(encodedText);
            return "Base64 解码结果: " + new String(decoded);
        } catch (Exception e) {
            return "错误：无效的 Base64 编码";
        }
    }

    @Tool(description = "计算文本的 MD5 哈希值")
    public String md5Hash(@ToolParam(description = "要计算哈希的文本") String text) {
        log.info("计算 MD5");
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] digest = md.digest(text.getBytes());
            StringBuilder sb = new StringBuilder();
            for (byte b : digest) {
                sb.append(String.format("%02x", b));
            }
            return "MD5 哈希: " + sb.toString();
        } catch (NoSuchAlgorithmException e) {
            return "错误：MD5 算法不可用";
        }
    }

    @Tool(description = "计算文本的 SHA-256 哈希值")
    public String sha256Hash(@ToolParam(description = "要计算哈希的文本") String text) {
        log.info("计算 SHA-256");
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] digest = md.digest(text.getBytes());
            StringBuilder sb = new StringBuilder();
            for (byte b : digest) {
                sb.append(String.format("%02x", b));
            }
            return "SHA-256 哈希: " + sb.toString();
        } catch (NoSuchAlgorithmException e) {
            return "错误：SHA-256 算法不可用";
        }
    }

    @Tool(description = "生成简单的密码强度分析")
    public String analyzePasswordStrength(@ToolParam(description = "要分析的密码") String password) {
        log.info("分析密码强度");
        int score = 0;
        StringBuilder analysis = new StringBuilder("密码分析:\n");

        if (password.length() >= 8) {
            score += 1;
            analysis.append("✓ 长度 >= 8 位\n");
        } else {
            analysis.append("✗ 长度不足 8 位\n");
        }

        if (password.length() >= 12) {
            score += 1;
            analysis.append("✓ 长度 >= 12 位\n");
        }

        if (password.matches(".*[a-z].*")) {
            score += 1;
            analysis.append("✓ 包含小写字母\n");
        }

        if (password.matches(".*[A-Z].*")) {
            score += 1;
            analysis.append("✓ 包含大写字母\n");
        }

        if (password.matches(".*[0-9].*")) {
            score += 1;
            analysis.append("✓ 包含数字\n");
        }

        if (password.matches(".*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>/?].*")) {
            score += 1;
            analysis.append("✓ 包含特殊字符\n");
        }

        String strength;
        if (score <= 2) {
            strength = "弱";
        } else if (score <= 4) {
            strength = "中等";
        } else if (score <= 5) {
            strength = "强";
        } else {
            strength = "非常强";
        }

        analysis.append(String.format("\n综合评分: %d/7 - 强度: %s", score, strength));
        return analysis.toString();
    }
}
