package com.example.mcp.server;

import com.example.mcp.tools.*;
import com.fasterxml.jackson.databind.*;
import org.slf4j.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.*;

import java.io.*;
import java.lang.reflect.*;
import java.util.*;
import java.util.concurrent.*;

/**
 * MCP 协议服务器
 * 通过 stdio 与 Claude 通信.
 */
@Component
@ConditionalOnProperty(name = "mcp.server.enabled", havingValue = "true")
public class McpStdioServer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(McpStdioServer.class);
    private final ObjectMapper mapper = new ObjectMapper();
    private final Map<String, ToolInfo> tools = new ConcurrentHashMap<>();
    
    private final CalculatorTools calculatorTools;
    private final StringTools stringTools;
    private final TimeTools timeTools;
    private final UserTools userTools;
    private final CryptoTools cryptoTools;

    public McpStdioServer(CalculatorTools calculatorTools, StringTools stringTools,
                          TimeTools timeTools, UserTools userTools, CryptoTools cryptoTools) {
        this.calculatorTools = calculatorTools;
        this.stringTools = stringTools;
        this.timeTools = timeTools;
        this.userTools = userTools;
        this.cryptoTools = cryptoTools;
        registerTools();
    }

    private void registerTools() {
        registerToolObject(calculatorTools);
        registerToolObject(stringTools);
        registerToolObject(timeTools);
        registerToolObject(userTools);
        registerToolObject(cryptoTools);
    }

    private void registerToolObject(Object toolObj) {
        for (Method method : toolObj.getClass().getDeclaredMethods()) {
            Tool toolAnnotation = method.getAnnotation(Tool.class);
            if (toolAnnotation != null) {
                String toolName = toolObj.getClass().getSimpleName().replace("Tools", "") + "_" + method.getName();
                tools.put(toolName.toLowerCase(), new ToolInfo(toolObj, method, toolAnnotation.description()));
            }
        }
    }

    @Override
    public void run(String... args) {
        log.info("MCP Stdio Server 启动，共注册 {} 个工具", tools.size());
        
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
             PrintWriter writer = new PrintWriter(new OutputStreamWriter(System.out), true)) {
            
            String line;
            while ((line = reader.readLine()) != null) {
                try {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> request = mapper.readValue(line, Map.class);
                    Map<String, Object> response = handleRequest(request);
                    if (response != null) {
                        writer.println(mapper.writeValueAsString(response));
                        writer.flush();
                    }
                } catch (Exception e) {
                    log.error("处理请求失败", e);
                    Map<String, Object> error = new LinkedHashMap<>();
                    error.put("jsonrpc", "2.0");
                    error.put("error", Map.of("code", -32700, "message", e.getMessage()));
                    writer.println(mapper.writeValueAsString(error));
                    writer.flush();
                }
            }
        } catch (IOException e) {
            log.error("MCP 服务器错误", e);
        }
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> handleRequest(Map<String, Object> request) {
        String method = (String) request.get("method");
        Object id = request.get("id");
        
        if ("initialize".equals(method)) {
            Map<String, Object> result = new LinkedHashMap<>();
            result.put("protocolVersion", "2024-11-05");
            result.put("capabilities", Map.of("tools", Map.of()));
            result.put("serverInfo", Map.of("name", "mcp-server-demo", "version", "1.0.0"));
            return createResponse(id, result);
        }
        
        if ("tools/list".equals(method)) {
            List<Map<String, Object>> toolList = new ArrayList<>();
            for (Map.Entry<String, ToolInfo> entry : tools.entrySet()) {
                Map<String, Object> tool = new LinkedHashMap<>();
                tool.put("name", entry.getKey());
                tool.put("description", entry.getValue().description());
                tool.put("inputSchema", createInputSchema(entry.getValue()));
                toolList.add(tool);
            }
            return createResponse(id, Map.of("tools", toolList));
        }
        
        if ("tools/call".equals(method)) {
            Map<String, Object> params = (Map<String, Object>) request.get("params");
            String toolName = (String) params.get("name");
            Map<String, Object> arguments = params.containsKey("arguments") 
                ? (Map<String, Object>) params.get("arguments") 
                : Collections.emptyMap();
            
            try {
                Object result = callTool(toolName, arguments);
                return createResponse(id, Map.of("content", 
                    List.of(Map.of("type", "text", "text", String.valueOf(result)))));
            } catch (Exception e) {
                return createResponse(id, Map.of("isError", true, 
                    "content", List.of(Map.of("type", "text", "text", "错误: " + e.getMessage()))));
            }
        }
        
        if ("notifications/initialized".equals(method)) {
            return null; // 无需响应
        }
        
        return createErrorResponse(id, -32601, "Method not found: " + method);
    }

    private Map<String, Object> createInputSchema(ToolInfo toolInfo) {
        Map<String, Object> schema = new LinkedHashMap<>();
        schema.put("type", "object");
        
        Map<String, Object> properties = new LinkedHashMap<>();
        List<String> required = new ArrayList<>();
        
        for (Parameter param : toolInfo.method().getParameters()) {
            ToolParam tp = param.getAnnotation(ToolParam.class);
            String paramName = param.getName();
            
            Map<String, Object> prop = new LinkedHashMap<>();
            prop.put("type", getType(param.getType()));
            prop.put("description", tp != null ? tp.description() : paramName);
            properties.put(paramName, prop);
            required.add(paramName);
        }
        
        schema.put("properties", properties);
        if (!required.isEmpty()) {
            schema.put("required", required);
        }
        return schema;
    }

    private String getType(Class<?> clazz) {
        if (clazz == int.class || clazz == Integer.class || 
            clazz == long.class || clazz == Long.class || 
            clazz == double.class || clazz == Double.class) {
            return "number";
        } else if (clazz == boolean.class || clazz == Boolean.class) {
            return "boolean";
        }
        return "string";
    }

    private Object callTool(String toolName, Map<String, Object> args) throws Exception {
        ToolInfo info = tools.get(toolName.toLowerCase());
        if (info == null) {
            throw new IllegalArgumentException("Unknown tool: " + toolName);
        }
        
        Parameter[] params = info.method().getParameters();
        Object[] argsArray = new Object[params.length];
        
        for (int i = 0; i < params.length; i++) {
            String paramName = params[i].getName();
            Object value = args.get(paramName);
            
            if (value == null) {
                argsArray[i] = null;
            } else if (params[i].getType() == int.class || params[i].getType() == Integer.class) {
                argsArray[i] = ((Number) value).intValue();
            } else if (params[i].getType() == double.class || params[i].getType() == Double.class) {
                argsArray[i] = ((Number) value).doubleValue();
            } else if (params[i].getType() == long.class || params[i].getType() == Long.class) {
                argsArray[i] = ((Number) value).longValue();
            } else {
                argsArray[i] = value;
            }
        }
        
        return info.method().invoke(info.instance(), argsArray);
    }

    private Map<String, Object> createResponse(Object id, Object result) {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("jsonrpc", "2.0");
        response.put("id", id);
        response.put("result", result);
        return response;
    }

    private Map<String, Object> createErrorResponse(Object id, int code, String message) {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("jsonrpc", "2.0");
        response.put("id", id);
        response.put("error", Map.of("code", code, "message", message));
        return response;
    }

    private record ToolInfo(Object instance, Method method, String description) {}
}
