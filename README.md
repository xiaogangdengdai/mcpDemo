# MCP Server Demo

基于 Spring Boot 的 MCP（Model Context Protocol）服务器示例项目。

## 项目结构

```
mcpDemo/
├── src/main/java/com/example/mcp/
│   ├── McpServerApplication.java      # 主应用类
│   ├── config/
│   │   └── McpServerConfig.java       # MCP 配置
│   └── tools/
│       ├── CalculatorTools.java       # 计算器工具
│       ├── TimeTools.java             # 时间工具
│       ├── UserTools.java             # 用户管理工具
│       ├── StringTools.java           # 字符串工具
│       └── CryptoTools.java           # 加密工具
├── src/main/resources/
│   └── application.yml                # 应用配置
└── pom.xml                            # Maven 配置
```

## 已实现的工具

### 1. 计算器工具 (CalculatorTools)
- `add` - 加法运算
- `subtract` - 减法运算
- `multiply` - 乘法运算
- `divide` - 除法运算
- `sqrt` - 平方根
- `power` - 幂运算

### 2. 时间工具 (TimeTools)
- `getCurrentTime` - 获取当前时间
- `getTimeByTimezone` - 获取指定时区时间
- `getDayOfWeek` - 获取星期几
- `daysBetween` - 计算日期差

### 3. 用户工具 (UserTools)
- `getUserById` - 根据 ID 查询用户
- `listAllUsers` - 列出所有用户
- `createUser` - 创建新用户
- `searchUserByName` - 按姓名搜索用户

### 4. 字符串工具 (StringTools)
- `toUpperCase` - 转大写
- `toLowerCase` - 转小写
- `countCharacters` - 统计字符数
- `reverseText` - 反转文本
- `generateRandomString` - 生成随机字符串
- `checkPalindrome` - 检查是否为回文

### 5. 加密工具 (CryptoTools)
- `base64Encode` - Base64 编码
- `base64Decode` - Base64 解码
- `md5Hash` - MD5 哈希
- `sha256Hash` - SHA-256 哈希
- `analyzePasswordStrength` - 密码强度分析

## 环境要求

- JDK 17+
- Maven 3.6+
- Spring Boot 3.2+

## 构建与运行

### 构建
```bash
cd /home/wzh/codes/mcpDemo
./mvnw clean package
```

### 运行
```bash
java -jar target/mcp-server-demo-1.0.0.jar
```

### 开发模式
```bash
./mvnw spring-boot:run
```

## 连接到 Claude Code

### 方法一：SSE 模式（推荐）

启动服务后，在 Claude Code 配置中添加：

```json
{
  "mcpServers": {
    "mcp-demo": {
      "url": "http://localhost:8080/sse"
    }
  }
}
```

### 方法二：本地命令模式

```json
{
  "mcpServers": {
    "mcp-demo": {
      "command": "java",
      "args": ["-jar", "/home/wzh/codes/mcpDemo/target/mcp-server-demo-1.0.0.jar"]
    }
  }
}
```

## Claude Code 配置文件位置

- **macOS**: `~/Library/Application Support/Claude/claude_desktop_config.json`
- **Linux**: `~/.config/claude/config.json`
- **Windows**: `%APPDATA%\Claude\claude_desktop_config.json`

## 测试工具调用

启动服务后，可以通过以下方式测试：

1. 在 Claude Code 中询问：
   - "帮我计算 123 加 456"
   - "现在几点了？"
   - "列出所有用户"
   - "把 'Hello World' 转成大写"

## 日志

日志级别配置在 `application.yml` 中，可以通过修改配置调整日志输出级别。

## 扩展工具

要添加新工具：

1. 在 `tools` 包下创建新的工具类
2. 使用 `@Component` 注解
3. 使用 `@Tool` 注解标记方法
4. 在 `McpServerConfig.java` 中注册新的 `ToolCallbackProvider`
