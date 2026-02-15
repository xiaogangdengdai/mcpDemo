#!/usr/bin/env python3
"""
MCP Server 测试脚本
用于测试 MCP 服务器是否正常工作
"""

import subprocess
import json
import sys
import time
import os

# MCP 服务器配置
MCP_COMMAND = "/home/wzh/.local/bin/mcp-server-demo"
# 或者直接使用 java 命令
MCP_JAVA_CMD = [
    "java",
    "-Dmcp.server.enabled=true",
    "-Dspring.main.web-application-type=none", 
    "-Dspring.main.banner-mode=off",
    "-Dlogging.level.root=OFF",
    "-jar",
    "/home/wzh/codes/mcpDemo/target/mcp-server-demo-1.0.0.jar"
]

class MCPClient:
    def __init__(self, use_script=True):
        self.process = None
        self.use_script = use_script
        
    def start(self):
        """启动 MCP 服务器"""
        print("🚀 启动 MCP 服务器...")
        
        if self.use_script and os.path.exists(MCP_COMMAND):
            cmd = [MCP_COMMAND]
            print(f"   使用脚本: {MCP_COMMAND}")
        else:
            cmd = MCP_JAVA_CMD
            print(f"   使用 Java: java -jar ...")
        
        self.process = subprocess.Popen(
            cmd,
            stdin=subprocess.PIPE,
            stdout=subprocess.PIPE,
            stderr=subprocess.PIPE,
            text=True,
            bufsize=1
        )
        time.sleep(1)  # 等待服务器启动
        
        if self.process.poll() is not None:
            stderr = self.process.stderr.read()
            raise Exception(f"MCP 服务器启动失败: {stderr}")
        
        print("✅ MCP 服务器已启动\n")
        
    def send_request(self, method, params=None, request_id=1):
        """发送 JSON-RPC 请求"""
        request = {
            "jsonrpc": "2.0",
            "id": request_id,
            "method": method,
            "params": params or {}
        }
        
        request_str = json.dumps(request) + "\n"
        print(f"📤 发送: {method}")
        print(f"   {json.dumps(request, ensure_ascii=False, indent=2)}")
        
        self.process.stdin.write(request_str)
        self.process.stdin.flush()
        
        # 读取响应
        response_str = self.process.stdout.readline()
        if not response_str:
            raise Exception("未收到响应")
        
        response = json.loads(response_str)
        print(f"📥 响应:")
        print(f"   {json.dumps(response, ensure_ascii=False, indent=2)}\n")
        
        return response
    
    def close(self):
        """关闭 MCP 服务器"""
        if self.process:
            self.process.terminate()
            self.process.wait()
            print("🛑 MCP 服务器已关闭")


def test_mcp_server():
    """测试 MCP 服务器功能"""
    print("=" * 60)
    print("       MCP Server 测试脚本")
    print("=" * 60 + "\n")
    
    client = MCPClient(use_script=True)
    
    try:
        # 启动服务器
        client.start()
        
        # 测试 1: 初始化
        print("📋 测试 1: 初始化 (initialize)")
        print("-" * 40)
        response = client.send_request("initialize", {
            "protocolVersion": "2024-11-05",
            "capabilities": {},
            "clientInfo": {
                "name": "test-client",
                "version": "1.0.0"
            }
        })
        
        if "result" in response:
            print("✅ 初始化成功!")
            server_info = response["result"].get("serverInfo", {})
            print(f"   服务器: {server_info.get('name')} v{server_info.get('version')}")
        else:
            print("❌ 初始化失败!")
            return False
        
        print()
        
        # 测试 2: 获取工具列表
        print("📋 测试 2: 获取工具列表 (tools/list)")
        print("-" * 40)
        response = client.send_request("tools/list", {})
        
        if "result" in response:
            tools = response["result"].get("tools", [])
            print(f"✅ 获取到 {len(tools)} 个工具:")
            for tool in tools[:10]:  # 只显示前10个
                print(f"   - {tool['name']}: {tool['description']}")
            if len(tools) > 10:
                print(f"   ... 还有 {len(tools) - 10} 个工具")
        else:
            print("❌ 获取工具列表失败!")
            return False
        
        print()
        
        # 测试 3: 调用计算器工具
        print("📋 测试 3: 调用工具 (tools/call)")
        print("-" * 40)
        response = client.send_request("tools/call", {
            "name": "calculator_add",
            "arguments": {
                "a": 100,
                "b": 200
            }
        })
        
        if "result" in response:
            content = response["result"].get("content", [])
            if content:
                text = content[0].get("text", "")
                print(f"✅ 计算结果: 100 + 200 = {text}")
            else:
                print("❌ 响应内容为空")
        else:
            print("❌ 工具调用失败!")
            return False
        
        print()
        
        # 测试 4: 调用时间工具
        print("📋 测试 4: 获取当前时间")
        print("-" * 40)
        response = client.send_request("tools/call", {
            "name": "time_getcurrenttime",
            "arguments": {}
        })
        
        if "result" in response:
            content = response["result"].get("content", [])
            if content:
                text = content[0].get("text", "")
                print(f"✅ {text}")
        
        print()
        
        # 测试 5: 调用字符串工具
        print("📋 测试 5: 字符串转大写")
        print("-" * 40)
        response = client.send_request("tools/call", {
            "name": "string_touppercase",
            "arguments": {
                "text": "hello world"
            }
        })
        
        if "result" in response:
            content = response["result"].get("content", [])
            if content:
                text = content[0].get("text", "")
                print(f"✅ 转换结果: {text}")
        
        print()
        print("=" * 60)
        print("       ✅ 所有测试通过!")
        print("=" * 60)
        
        return True
        
    except Exception as e:
        print(f"\n❌ 测试失败: {e}")
        import traceback
        traceback.print_exc()
        return False
        
    finally:
        client.close()


def interactive_mode():
    """交互模式 - 手动测试"""
    print("=" * 60)
    print("       MCP Server 交互测试模式")
    print("=" * 60 + "\n")
    
    client = MCPClient(use_script=True)
    
    try:
        client.start()
        
        # 初始化
        client.send_request("initialize", {
            "protocolVersion": "2024-11-05",
            "capabilities": {},
            "clientInfo": {"name": "interactive-test", "version": "1.0.0"}
        })
        
        print("输入 JSON-RPC 请求 (Ctrl+D 退出):")
        print("示例: {\"jsonrpc\":\"2.0\",\"id\":1,\"method\":\"tools/list\",\"params\":{}}")
        print()
        
        while True:
            try:
                line = input("> ")
                if not line.strip():
                    continue
                    
                request = json.loads(line)
                response = client.send_request(
                    request.get("method"),
                    request.get("params", {}),
                    request.get("id", 1)
                )
                
            except EOFError:
                break
            except json.JSONDecodeError as e:
                print(f"JSON 解析错误: {e}")
            except Exception as e:
                print(f"错误: {e}")
                
    finally:
        client.close()


if __name__ == "__main__":
    if len(sys.argv) > 1 and sys.argv[1] == "-i":
        interactive_mode()
    else:
        success = test_mcp_server()
        sys.exit(0 if success else 1)
