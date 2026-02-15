package com.example.mcp.tools;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * 用户工具集
 * 模拟用户数据操作
 */
@Component
public class UserTools {

    private static final Logger log = LoggerFactory.getLogger(UserTools.class);
    
    // 模拟用户数据库
    private static final Map<String, User> USER_DATABASE = new HashMap<>();

    static {
        // 初始化一些测试用户
        USER_DATABASE.put("001", new User("001", "张三", "zhangsan@example.com", "工程师"));
        USER_DATABASE.put("002", new User("002", "李四", "lisi@example.com", "设计师"));
        USER_DATABASE.put("003", new User("003", "王五", "wangwu@example.com", "产品经理"));
    }

    @Tool(description = "根据用户ID查询用户信息")
    public String getUserById(@ToolParam(description = "用户ID") String userId) {
        log.info("查询用户: {}", userId);
        User user = USER_DATABASE.get(userId);
        if (user != null) {
            return String.format("用户信息 - ID: %s, 姓名: %s, 邮箱: %s, 职位: %s",
                    user.id, user.name, user.email, user.position);
        }
        return "未找到用户ID为 " + userId + " 的用户。现有用户ID: 001, 002, 003";
    }

    @Tool(description = "列出所有用户")
    public String listAllUsers() {
        log.info("列出所有用户");
        StringBuilder sb = new StringBuilder("用户列表:\n");
        for (User user : USER_DATABASE.values()) {
            sb.append(String.format("- ID: %s, 姓名: %s, 职位: %s\n", user.id, user.name, user.position));
        }
        return sb.toString();
    }

    @Tool(description = "创建新用户")
    public String createUser(
            @ToolParam(description = "用户姓名") String name,
            @ToolParam(description = "用户邮箱") String email,
            @ToolParam(description = "用户职位") String position) {
        log.info("创建用户: {}, {}, {}", name, email, position);
        String newId = UUID.randomUUID().toString().substring(0, 3);
        User newUser = new User(newId, name, email, position);
        USER_DATABASE.put(newId, newUser);
        return String.format("用户创建成功 - ID: %s, 姓名: %s, 邮箱: %s, 职位: %s",
                newId, name, email, position);
    }

    @Tool(description = "根据姓名搜索用户")
    public String searchUserByName(@ToolParam(description = "用户姓名（支持模糊匹配）") String name) {
        log.info("搜索用户: {}", name);
        StringBuilder sb = new StringBuilder("搜索结果:\n");
        boolean found = false;
        for (User user : USER_DATABASE.values()) {
            if (user.name.contains(name)) {
                sb.append(String.format("- ID: %s, 姓名: %s, 职位: %s\n", user.id, user.name, user.position));
                found = true;
            }
        }
        if (!found) {
            return "未找到姓名包含 \"" + name + "\" 的用户";
        }
        return sb.toString();
    }

    // 内部用户类
    private static class User {
        String id;
        String name;
        String email;
        String position;

        User(String id, String name, String email, String position) {
            this.id = id;
            this.name = name;
            this.email = email;
            this.position = position;
        }
    }
}
