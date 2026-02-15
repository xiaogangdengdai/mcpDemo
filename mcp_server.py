#!/usr/bin/env python3
"""
MCP Server Wrapper
通过 stdio 与 Claude 通信的 MCP 服务器包装器
"""

import sys
import subprocess
import json

# MCP 服务器 JAR 路径
JAR_PATH = "/home/wzh/codes/mcpDemo/target/mcp-server-demo-1.0.0.jar"

def main():
    # 启动 Java 进程
    proc = subprocess.Popen(
        ["java", "-Dmcp.server.enabled=true", "-jar", JAR_PATH],
        stdin=subprocess.PIPE,
        stdout=subprocess.PIPE,
        stderr=subprocess.PIPE,
        text=True
    )
    
    # 转发 stdin/stdout
    import threading
    import select
    
    def forward_stdout():
        for line in proc.stdout:
            print(line, end='')
            sys.stdout.flush()
    
    def forward_stderr():
        for line in proc.stderr:
            print(line, end='', file=sys.stderr)
            sys.stderr.flush()
    
    # 启动输出转发线程
    stdout_thread = threading.Thread(target=forward_stdout, daemon=True)
    stderr_thread = threading.Thread(target=forward_stderr, daemon=True)
    stdout_thread.start()
    stderr_thread.start()
    
    # 转发 stdin
    try:
        for line in sys.stdin:
            proc.stdin.write(line)
            proc.stdin.flush()
    except:
        pass
    
    proc.wait()

if __name__ == "__main__":
    main()
