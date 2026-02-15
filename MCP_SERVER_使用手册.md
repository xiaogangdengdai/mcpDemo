# MCP Server Demo 使用手册

> 📅 创建时间：2026年2月15日
> 👤 作者：Claude Assistant
> 📦 版本：1.0.0

---

## 目录

1. [项目概述](#1-项目概述)
2. [快速开始](#2-快速开始)
3. [启动 MCP 服务器](#3-启动-mcp-服务器)
4. [停止 MCP 服务器](#4-停止-mcp-服务器)
5. [配置 Claude Code](#5-配置-claude-code)
6. [使用工具](#6-使用工具)
7. [故障排除](#7-故障排除)
8. [附录](#8-附录)

---

## 1. 项目概述

### 1.1 什么是 MCP？

MCP (Model Context Protocol) 是 Anthropic 开发的协议，允许 Claude 与外部工具进行交互。通过 MCP，Claude 可以：

- 调用自定义工具
- 获取实时数据
- 执行系统操作

### 1.2 项目结构

```
/home/wzh/codes/mcpDemo/
├── target/
│   └── mcp-server-demo-1.0.0.jar    # 编译后的 JAR 文件
├── src/main/java/com/example/mcp/
│   ├── McpServerApplication.java    # 主程序
│   ├── server/
│   │   └── McpStdioServer.java      # MCP 协议服务器
│   └── tools/                       # 工具类
│       ├── CalculatorTools.java     # 计算器
│       ├── StringTools.java         # 字符串处理
│       ├── TimeTools.java           # 时间工具
│       ├── UserTools.java           # 用户管理
│       └── CryptoTools.java         # 加密工具
├── test_mcp_server.py               # 测试脚本
├── diagnose_mcp.py                  # 诊断脚本
└── claude_mcp_client.py             # 交互客户端
```

### 1.3 已注册工具 (25个)

| 类别 | 工具名称 | 功能描述 |
|------|---------|---------|
| **计算器** | calculator_add | 加法运算 |
| | calculator_subtract | 减法运算 |
| | calculator_multiply | 乘法运算 |
| | calculator_divide | 除法运算 |
| | calculator_sqrt | 平方根 |
| | calculator_power | 幂运算 |
| **字符串** | string_touppercase | 转大写 |
| | string_tolowercase | 转小写 |
| | string_reversetext | 反转文本 |
| | string_countcharacters | 统计字符 |
| | string_checkpalindrome | 检查回文 |
| | string_generaterandomstring | 生成随机字符串 |
| **时间** | time_getcurrenttime | 获取当前时间 |
| | time_gettimebytimezone | 获取时区时间 |
| | time_getdayofweek | 获取星期几 |
| | time_daysbetween | 计算日期差 |
| **用户** | user_getuserbyid | 根据ID查询用户 |
| | user_listallusers | 列出所有用户 |
| | user_createuser | 创建用户 |
| | user_searchuserbyname | 搜索用户 |
| **加密** | crypto_base64encode | Base64 编码 |
| | crypto_base64decode | Base64 解码 |
| | crypto_md5hash | MD5 哈希 |
| | crypto_sha256hash | SHA-256 哈希 |
| | crypto_analyzepasswordstrength | 密码强度分析 |

---

## 2. 快速开始

### 2.1 系统要求

- **操作系统**: Linux (Ubuntu 22.04+)
- **Java**: JDK 21+
- **Python**: 3.8+ (可选，用于测试脚本)

### 2.2 检查环境

```bash
# 检查 Java 版本
java -version
# 应显示: openjdk version "21.0.x"

# 检查 JAR 文件是否存在
ls -la /home/wzh/codes/mcpDemo/target/mcp-server-demo-1.0.0.jar

# 检查 MCP 服务器脚本
ls -la /home/wzh/.local/bin/mcp-server-demo
```

### 2.3 一键测试

```bash
# 运行诊断脚本
python3 /home/wzh/codes/mcpDemo/diagnose_mcp.py

# 如果所有检查通过，显示 ✅
```

---

## 3. 启动 MCP 服务器

### 3.1 方式一：使用脚本启动（推荐）

MCP 服务器通过 **stdin/stdout** 与 Claude 通信，**不需要手动启动**。Claude Code 会自动调用配置的脚本。

脚本位置：`/home/wzh/.local/bin/mcp-server-demo`

脚本内容：
```bash
#!/bin/bash
cd /home/wzh/codes/mcpDemo 2>/dev/null

exec java \
  -Dmcp.server.enabled=true \
  -Dspring.main.web-application-type=none \
  -Dspring.main.banner-mode=off \
  -Dlogging.level.root=OFF \
  -jar target/mcp-server-demo-1.0.0.jar \
  2>/dev/null
```

### 3.2 方式二：手动测试启动

```bash
# 发送初始化请求测试服务器
echo '{"jsonrpc":"2.0","id":1,"method":"initialize","params":{}}' | /home/wzh/.local/bin/mcp-server-demo

# 预期输出：
# {"jsonrpc":"2.0","id":1,"result":{"protocolVersion":"2024-11-05","capabilities":{"tools":{}},"serverInfo":{"name":"mcp-server-demo","version":"1.0.0"}}}
```

### 3.3 方式三：运行完整测试

```bash
# 运行自动化测试
python3 /home/wzh/codes/mcpDemo/test_mcp_server.py

# 预期输出：
# ✅ 所有测试通过!
```

### 3.4 方式四：交互式测试

```bash
# 运行交互客户端
python3 /home/wzh/codes/mcpDemo/claude_mcp_client.py

# 可以选择工具并输入参数进行测试
```

---

## 4. 停止 MCP 服务器

### 4.1 正常情况

MCP 服务器是**按需启动**的进程：
- Claude Code 调用工具时启动
- 工具执行完毕后自动退出
- **不需要手动停止**

### 4.2 如果有进程残留

```bash
# 查看是否有残留进程
ps aux | grep mcp-server-demo

# 如果有残留，强制终止
pkill -f mcp-server-demo

# 或者按 PID 终止
kill -9 <PID>
```

### 4.3 检查端口占用（如果有）

```bash
# 检查 8080 端口（HTTP 模式）
lsof -i :8080

# 如果有占用，终止
fuser -k 8080/tcp
```

---

## 5. 配置 Claude Code

### 5.1 配置文件位置

```
/home/wzh/.claude/settings.json
```

### 5.2 完整配置内容

```json
{
  "env": {
    "ANTHROPIC_AUTH_TOKEN": "your-token-here",
    "ANTHROPIC_BASE_URL": "https://open.bigmodel.cn/api/anthropic",
    "API_TIMEOUT_MS": "3000000",
    "CLAUDE_CODE_DISABLE_NONESSENTIAL_TRAFFIC": "1",
    "ANTHROPIC_DEFAULT_HAIKU_MODEL": "glm-5",
    "ANTHROPIC_DEFAULT_SONNET_MODEL": "glm-5",
    "ANTHROPIC_DEFAULT_OPUS_MODEL": "glm-5"
  },
  "enabledPlugins": {
    "glm-plan-usage@zai-coding-plugins": true,
    "glm-plan-bug@zai-coding-plugins": true
  },
  "mcpServers": {
    "mcp-server-demo": {
      "command": "/home/wzh/.local/bin/mcp-server-demo",
      "args": [],
      "env": {}
    }
  }
}
```

### 5.3 关键配置说明

| 字段 | 说明 |
|------|------|
| `mcpServers` | MCP 服务器配置块 |
| `mcp-server-demo` | 服务器名称（自定义） |
| `command` | 启动命令（脚本路径） |
| `args` | 命令行参数（通常为空） |
| `env` | 环境变量（可选） |

### 5.4 添加更多 MCP 服务器

```json
{
  "mcpServers": {
    "mcp-server-demo": {
      "command": "/home/wzh/.local/bin/mcp-server-demo",
      "args": [],
      "env": {}
    },
    "filesystem": {
      "command": "npx",
      "args": ["-y", "@modelcontextprotocol/server-filesystem", "/home/wzh"],
      "env": {}
    }
  }
}
```

### 5.5 重新加载配置

修改配置后，需要**重启 Claude Code**：

```bash
# 退出 Claude Code
# Ctrl+C 或输入 exit

# 重新启动 Claude Code
claude
```

---

## 6. 使用工具

### 6.1 在 Claude Code 中使用

启动 Claude Code 后，可以直接用自然语言调用工具：

```
# 计算器
帮我计算 123 加 456 等于多少
计算 100 除以 3 的结果
100 的平方根是多少

# 字符串
把 "hello world" 转成大写
反转字符串 "你好世界"
统计 "Hello World" 有多少个字符

# 时间
现在几点了？
今天是星期几？
纽约现在几点？

# 用户
列出所有用户
查询 ID 为 001 的用户信息
搜索姓张的用户

# 加密
把 "hello" 进行 Base64 编码
计算 "password123" 的 MD5 值
分析密码 "MyP@ssw0rd" 的强度
```

### 6.2 通过 REST API 使用（HTTP 模式）

如果启动了 HTTP 服务器（端口 8080）：

```bash
# 主页
curl http://localhost:8080/api/

# 计算器
curl "http://localhost:8080/api/calc/add?a=100&b=200"

# 时间
curl http://localhost:8080/api/time/now

# 用户
curl http://localhost:8080/api/user/list

# 加密
curl "http://localhost:8080/api/crypto/md5?text=hello"
```

---

## 7. 故障排除

### 7.1 常见问题

#### 问题 1：连接失败

**症状**：Claude Code 提示 "MCP 服务器连接失败"

**解决方案**：
```bash
# 1. 检查脚本是否存在
ls -la /home/wzh/.local/bin/mcp-server-demo

# 2. 检查脚本是否可执行
chmod +x /home/wzh/.local/bin/mcp-server-demo

# 3. 测试服务器
echo '{"jsonrpc":"2.0","id":1,"method":"initialize","params":{}}' | /home/wzh/.local/bin/mcp-server-demo

# 4. 运行诊断
python3 /home/wzh/codes/mcpDemo/diagnose_mcp.py
```

#### 问题 2：端口 8080 被占用

**症状**：启动时报错 "Port 8080 was already in use"

**解决方案**：
```bash
# 查找占用进程
lsof -i :8080

# 终止占用进程
fuser -k 8080/tcp

# 或者终止所有 Java 进程
pkill -f mcp-server-demo
```

#### 问题 3：Java 版本不正确

**症状**：启动时报错 "Unsupported class file major version"

**解决方案**：
```bash
# 检查 Java 版本
java -version

# 应该显示 21.x.x
# 如果不是，安装 JDK 21
sudo apt-get install openjdk-21-jdk-headless
```

#### 问题 4：配置文件格式错误

**症状**：Claude Code 无法启动

**解决方案**：
```bash
# 验证 JSON 格式
python3 -c "import json; json.load(open('/home/wzh/.claude/settings.json')); print('OK')"

# 如果报错，检查 JSON 语法
```

### 7.2 查看日志

```bash
# MCP 服务器日志
cat /home/wzh/.claude/mcp-server-demo.log

# Claude Code 日志
# 通常在 ~/.claude/debug/ 目录
```

### 7.3 重新编译项目

```bash
cd /home/wzh/codes/mcpDemo

# 清理并重新编译
mvn clean package -DskipTests

# 检查生成的 JAR
ls -la target/mcp-server-demo-1.0.0.jar
```

---

## 8. 附录

### 8.1 命令速查表

| 操作 | 命令 |
|------|------|
| 测试 MCP 服务器 | `echo '{"jsonrpc":"2.0","id":1,"method":"initialize","params":{}}' \| /home/wzh/.local/bin/mcp-server-demo` |
| 运行诊断 | `python3 /home/wzh/codes/mcpDemo/diagnose_mcp.py` |
| 运行测试 | `python3 /home/wzh/codes/mcpDemo/test_mcp_server.py` |
| 终止残留进程 | `pkill -f mcp-server-demo` |
| 释放端口 | `fuser -k 8080/tcp` |
| 查看 Java 版本 | `java -version` |
| 重新编译 | `cd /home/wzh/codes/mcpDemo && mvn clean package -DskipTests` |

### 8.2 文件路径汇总

| 文件 | 路径 |
|------|------|
| JAR 文件 | `/home/wzh/codes/mcpDemo/target/mcp-server-demo-1.0.0.jar` |
| 启动脚本 | `/home/wzh/.local/bin/mcp-server-demo` |
| Claude 配置 | `/home/wzh/.claude/settings.json` |
| 测试脚本 | `/home/wzh/codes/mcpDemo/test_mcp_server.py` |
| 诊断脚本 | `/home/wzh/codes/mcpDemo/diagnose_mcp.py` |
| MCP 日志 | `/home/wzh/.claude/mcp-server-demo.log` |
| 项目目录 | `/home/wzh/codes/mcpDemo/` |

### 8.3 开机自启动（可选）

如果需要开机自动启动 HTTP 服务器：

```bash
# 创建 systemd 服务文件
sudo tee /etc/systemd/system/mcp-server.service << 'EOF'
[Unit]
Description=MCP Server Demo
After=network.target

[Service]
Type=simple
User=wzh
WorkingDirectory=/home/wzh/codes/mcpDemo
ExecStart=/usr/bin/java -jar target/mcp-server-demo-1.0.0.jar
Restart=on-failure

[Install]
WantedBy=multi-user.target
EOF

# 启用服务
sudo systemctl daemon-reload
sudo systemctl enable mcp-server
sudo systemctl start mcp-server

# 查看状态
sudo systemctl status mcp-server
```

### 8.4 联系支持

如有问题，请：
1. 运行诊断脚本：`python3 /home/wzh/codes/mcpDemo/diagnose_mcp.py`
2. 查看日志文件：`cat /home/wzh/.claude/mcp-server-demo.log`
3. 重新编译项目：`cd /home/wzh/codes/mcpDemo && mvn clean package -DskipTests`

---

> 📝 **文档结束**
> 
> 如有疑问，请参考故障排除章节或运行诊断脚本获取帮助。
