#!/bin/bash
# MCP Server 启动脚本
cd /home/wzh/codes/mcpDemo
java -Dmcp.server.enabled=true -jar target/mcp-server-demo-1.0.0.jar
