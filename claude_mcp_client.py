#!/usr/bin/env python3
"""
Claude MCP 客户端模拟器
模拟 Claude 调用 MCP 服务器的完整流程
"""

import subprocess
import json
import sys
import os
import time
import threading
import queue

class ClaudeMCPClient:
    """MCP 客户端 - 与 Claude Code 类似的方式连接 MCP 服务器"""
    
    def __init__(self, server_command):
        self.server_command = server_command
        self.process = None
        self.request_id = 0
        self.reader_thread = None
        self.response_queue = queue.Queue()
        
    def start(self):
        """启动 MCP 服务器进程"""
        print(f"🔌 连接 MCP 服务器: {self.server_command}")
        
        self.process = subprocess.Popen(
            self.server_command,
            stdin=subprocess.PIPE,
            stdout=subprocess.PIPE,
            stderr=subprocess.PIPE,
            text=True,
            bufsize=1  # 行缓冲
        )
        
        # 启动后台线程读取响应
        self.reader_thread = threading.Thread(target=self._read_responses, daemon=True)
        self.reader_thread.start()
        
        # 等待进程启动
        time.sleep(0.5)
        
        if self.process.poll() is not None:
            stderr = self.process.stderr.read()
            raise Exception(f"MCP 服务器启动失败: {stderr}")
        
        print("✅ 已连接\n")
        
    def _read_responses(self):
        """后台线程读取响应"""
        try:
            for line in self.process.stdout:
                if line.strip():
                    try:
                        response = json.loads(line)
                        self.response_queue.put(response)
                    except json.JSONDecodeError:
                        pass
        except:
            pass
            
    def _send_request(self, method, params=None):
        """发送请求并等待响应"""
        self.request_id += 1
        
        request = {
            "jsonrpc": "2.0",
            "id": self.request_id,
            "method": method,
            "params": params or {}
        }
        
        # 发送请求
        request_line = json.dumps(request) + "\n"
        self.process.stdin.write(request_line)
        self.process.stdin.flush()
        
        # 等待响应 (超时 10 秒)
        try:
            response = self.response_queue.get(timeout=10)
            return response
        except queue.Empty:
            return {"error": "timeout"}
            
    def initialize(self):
        """初始化连接"""
        print("📋 发送初始化请求...")
        response = self._send_request("initialize", {
            "protocolVersion": "2024-11-05",
            "capabilities": {},
            "clientInfo": {
                "name": "claude-mcp-test",
                "version": "1.0.0"
            }
        })
        
        if "result" in response:
            server_info = response["result"].get("serverInfo", {})
            print(f"   ✅ 已连接到: {server_info.get('name')} v{server_info.get('version')}")
            print(f"   📦 协议版本: {response['result'].get('protocolVersion')}")
            return True
        else:
            print(f"   ❌ 初始化失败: {response}")
            return False
            
    def list_tools(self):
        """获取工具列表"""
        print("\n📋 获取工具列表...")
        response = self._send_request("tools/list", {})
        
        if "result" in response:
            tools = response["result"].get("tools", [])
            print(f"   ✅ 找到 {len(tools)} 个工具")
            return tools
        else:
            print(f"   ❌ 获取失败: {response}")
            return []
            
    def call_tool(self, name, arguments=None):
        """调用工具"""
        print(f"\n🔧 调用工具: {name}")
        if arguments:
            print(f"   参数: {json.dumps(arguments, ensure_ascii=False)}")
        
        response = self._send_request("tools/call", {
            "name": name,
            "arguments": arguments or {}
        })
        
        if "result" in response:
            content = response["result"].get("content", [])
            if content:
                text = content[0].get("text", "")
                print(f"   ✅ 结果: {text}")
                return text
        else:
            error = response.get("error", "未知错误")
            print(f"   ❌ 调用失败: {error}")
            return None
            
    def close(self):
        """关闭连接"""
        if self.process:
            self.process.terminate()
            self.process.wait()
            print("\n🔌 已断开连接")


def main():
    print("=" * 60)
    print("    Claude MCP 客户端测试")
    print("=" * 60)
    print()
    
    # MCP 服务器命令
    server_cmd = ["/home/wzh/.local/bin/mcp-server-demo"]
    
    client = ClaudeMCPClient(server_cmd)
    
    try:
        # 启动并初始化
        client.start()
        
        if not client.initialize():
            return 1
            
        # 获取工具列表
        tools = client.list_tools()
        
        # 交互式测试
        print("\n" + "=" * 60)
        print("    交互测试模式")
        print("=" * 60)
        print("\n可用工具:")
        for i, tool in enumerate(tools[:10], 1):
            print(f"  {i:2d}. {tool['name']}: {tool['description']}")
        if len(tools) > 10:
            print(f"  ... 还有 {len(tools) - 10} 个工具")
        
        print("\n输入命令 (输入数字选择工具，或输入 'q' 退出):")
        
        while True:
            try:
                cmd = input("\n> ").strip()
                
                if cmd.lower() == 'q':
                    break
                    
                # 选择工具
                try:
                    idx = int(cmd) - 1
                    if 0 <= idx < len(tools):
                        tool = tools[idx]
                        tool_name = tool['name']
                        
                        # 获取参数
                        properties = tool.get('inputSchema', {}).get('properties', {})
                        args = {}
                        
                        for param_name, param_info in properties.items():
                            value = input(f"   {param_info.get('description', param_name)}: ")
                            
                            # 类型转换
                            param_type = param_info.get('type', 'string')
                            if param_type == 'number':
                                try:
                                    if '.' in value:
                                        args[param_name] = float(value)
                                    else:
                                        args[param_name] = int(value)
                                except:
                                    args[param_name] = 0
                            else:
                                args[param_name] = value
                        
                        client.call_tool(tool_name, args)
                    else:
                        print("无效的工具编号")
                except ValueError:
                    print("请输入数字或 'q'")
                    
            except KeyboardInterrupt:
                break
            except EOFError:
                break
                
    except Exception as e:
        print(f"\n❌ 错误: {e}")
        import traceback
        traceback.print_exc()
        return 1
        
    finally:
        client.close()
        
    print("\n👋 再见!")
    return 0


if __name__ == "__main__":
    sys.exit(main())
