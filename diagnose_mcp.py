#!/usr/bin/env python3
"""
MCP 连接诊断工具
帮助诊断 Claude Code 与 MCP 服务器的连接问题
"""

import subprocess
import json
import os
import sys
import time

def check_file_exists(path, desc):
    """检查文件是否存在"""
    if os.path.exists(path):
        print(f"✅ {desc}: {path}")
        return True
    else:
        print(f"❌ {desc}: {path} (不存在)")
        return False

def check_executable(path, desc):
    """检查文件是否可执行"""
    if os.access(path, os.X_OK):
        print(f"✅ {desc}: 可执行")
        return True
    else:
        print(f"❌ {desc}: 不可执行")
        return False

def test_mcp_server(command):
    """测试 MCP 服务器"""
    print(f"\n=== 测试 MCP 服务器 ===")
    print(f"命令: {command}")
    
    try:
        # 发送初始化请求
        request = '{"jsonrpc":"2.0","id":1,"method":"initialize","params":{"protocolVersion":"2024-11-05","capabilities":{},"clientInfo":{"name":"test","version":"1.0.0"}}}'
        
        proc = subprocess.Popen(
            command,
            shell=True,
            stdin=subprocess.PIPE,
            stdout=subprocess.PIPE,
            stderr=subprocess.PIPE,
            text=True
        )
        
        stdout, stderr = proc.communicate(input=request + "\n", timeout=10)
        
        if proc.returncode != 0:
            print(f"❌ 进程返回码: {proc.returncode}")
            if stderr:
                print(f"   错误输出: {stderr[:500]}")
            return False
        
        # 解析响应
        for line in stdout.strip().split("\n"):
            if line.startswith("{"):
                try:
                    response = json.loads(line)
                    if "result" in response:
                        server_info = response["result"].get("serverInfo", {})
                        print(f"✅ 连接成功!")
                        print(f"   服务器: {server_info.get('name')} v{server_info.get('version')}")
                        return True
                    elif "error" in response:
                        print(f"❌ 服务器返回错误: {response['error']}")
                        return False
                except json.JSONDecodeError:
                    pass
        
        print(f"❌ 未收到有效响应")
        print(f"   stdout: {stdout[:500]}")
        if stderr:
            print(f"   stderr: {stderr[:500]}")
        return False
        
    except subprocess.TimeoutExpired:
        print(f"❌ 超时")
        proc.kill()
        return False
    except Exception as e:
        print(f"❌ 异常: {e}")
        return False

def main():
    print("=" * 60)
    print("       MCP 连接诊断工具")
    print("=" * 60)
    
    all_passed = True
    
    # 1. 检查文件
    print("\n=== 检查文件 ===")
    all_passed &= check_file_exists("/home/wzh/.claude/settings.json", "Claude 配置文件")
    all_passed &= check_file_exists("/home/wzh/.local/bin/mcp-server-demo", "MCP 服务器脚本")
    all_passed &= check_file_exists("/home/wzh/codes/mcpDemo/target/mcp-server-demo-1.0.0.jar", "MCP JAR 文件")
    
    # 2. 检查权限
    print("\n=== 检查权限 ===")
    all_passed &= check_executable("/home/wzh/.local/bin/mcp-server-demo", "MCP 服务器脚本")
    
    # 3. 检查配置
    print("\n=== 检查配置 ===")
    try:
        with open("/home/wzh/.claude/settings.json") as f:
            config = json.load(f)
        
        mcp_servers = config.get("mcpServers", {})
        if mcp_servers:
            print(f"✅ 找到 {len(mcp_servers)} 个 MCP 服务器配置")
            for name, server_config in mcp_servers.items():
                print(f"\n   服务器: {name}")
                print(f"   命令: {server_config.get('command')}")
                print(f"   参数: {server_config.get('args', [])}")
        else:
            print("❌ 没有找到 MCP 服务器配置")
            all_passed = False
    except Exception as e:
        print(f"❌ 配置文件错误: {e}")
        all_passed = False
    
    # 4. 测试 MCP 服务器
    print("\n" + "=" * 60)
    
    # 测试直接调用脚本
    if not test_mcp_server("/home/wzh/.local/bin/mcp-server-demo"):
        all_passed = False
    
    # 5. 测试 Java 直接调用
    print("\n=== 测试 Java 直接调用 ===")
    java_cmd = "java -Dmcp.server.enabled=true -Dspring.main.web-application-type=none -Dspring.main.banner-mode=off -Dlogging.level.root=OFF -jar /home/wzh/codes/mcpDemo/target/mcp-server-demo-1.0.0.jar"
    if not test_mcp_server(java_cmd):
        all_passed = False
    
    # 6. 最终结果
    print("\n" + "=" * 60)
    if all_passed:
        print("       ✅ 所有检查通过!")
        print("\n请尝试重启 Claude Code 来加载 MCP 服务器。")
    else:
        print("       ❌ 存在问题，请检查上面的错误信息")
    print("=" * 60)
    
    return 0 if all_passed else 1

if __name__ == "__main__":
    sys.exit(main())
