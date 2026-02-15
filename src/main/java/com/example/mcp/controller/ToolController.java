package com.example.mcp.controller;

import com.example.mcp.tools.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * 工具测试控制器
 * 提供 HTTP 端点来测试各个工具
 */
@RestController
@RequestMapping("/api")
public class ToolController {

    @Autowired
    private CalculatorTools calculatorTools;

    @Autowired
    private StringTools stringTools;

    @Autowired
    private TimeTools timeTools;

    @Autowired
    private UserTools userTools;

    @Autowired
    private CryptoTools cryptoTools;

    @GetMapping("/")
    public Map<String, Object> index() {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("name", "MCP Server Demo");
        result.put("version", "1.0.0");
        result.put("status", "running");
        result.put("endpoints", Arrays.asList(
            "/api/calc/add?a=1&b=2",
            "/api/calc/subtract?a=5&b=3",
            "/api/calc/multiply?a=4&b=3",
            "/api/calc/divide?a=10&b=2",
            "/api/string/upper?text=hello",
            "/api/string/lower?text=HELLO",
            "/api/string/reverse?text=hello",
            "/api/time/now",
            "/api/time/day",
            "/api/user/list",
            "/api/user/get?id=001",
            "/api/crypto/base64/encode?text=hello",
            "/api/crypto/md5?text=hello"
        ));
        return result;
    }

    // ============= 计算器工具 =============

    @GetMapping("/calc/add")
    public Map<String, Object> add(@RequestParam int a, @RequestParam int b) {
        return result("add", a + " + " + b, calculatorTools.add(a, b));
    }

    @GetMapping("/calc/subtract")
    public Map<String, Object> subtract(@RequestParam int a, @RequestParam int b) {
        return result("subtract", a + " - " + b, calculatorTools.subtract(a, b));
    }

    @GetMapping("/calc/multiply")
    public Map<String, Object> multiply(@RequestParam int a, @RequestParam int b) {
        return result("multiply", a + " * " + b, calculatorTools.multiply(a, b));
    }

    @GetMapping("/calc/divide")
    public Map<String, Object> divide(@RequestParam double a, @RequestParam double b) {
        return result("divide", a + " / " + b, calculatorTools.divide(a, b));
    }

    @GetMapping("/calc/sqrt")
    public Map<String, Object> sqrt(@RequestParam double n) {
        return result("sqrt", "sqrt(" + n + ")", calculatorTools.sqrt(n));
    }

    @GetMapping("/calc/power")
    public Map<String, Object> power(@RequestParam double base, @RequestParam double exp) {
        return result("power", base + " ^ " + exp, calculatorTools.power(base, exp));
    }

    // ============= 字符串工具 =============

    @GetMapping("/string/upper")
    public Map<String, Object> toUpper(@RequestParam String text) {
        return result("toUpperCase", text, stringTools.toUpperCase(text));
    }

    @GetMapping("/string/lower")
    public Map<String, Object> toLower(@RequestParam String text) {
        return result("toLowerCase", text, stringTools.toLowerCase(text));
    }

    @GetMapping("/string/reverse")
    public Map<String, Object> reverse(@RequestParam String text) {
        return result("reverseText", text, stringTools.reverseText(text));
    }

    @GetMapping("/string/count")
    public Map<String, Object> count(@RequestParam String text) {
        return result("countCharacters", text, stringTools.countCharacters(text));
    }

    @GetMapping("/string/palindrome")
    public Map<String, Object> palindrome(@RequestParam String text) {
        return result("checkPalindrome", text, stringTools.checkPalindrome(text));
    }

    // ============= 时间工具 =============

    @GetMapping("/time/now")
    public Map<String, Object> now() {
        return result("getCurrentTime", "now", timeTools.getCurrentTime());
    }

    @GetMapping("/time/day")
    public Map<String, Object> day() {
        return result("getDayOfWeek", "today", timeTools.getDayOfWeek());
    }

    @GetMapping("/time/timezone")
    public Map<String, Object> timezone(@RequestParam String tz) {
        return result("getTimeByTimezone", tz, timeTools.getTimeByTimezone(tz));
    }

    // ============= 用户工具 =============

    @GetMapping("/user/list")
    public Map<String, Object> listUsers() {
        return result("listAllUsers", "all", userTools.listAllUsers());
    }

    @GetMapping("/user/get")
    public Map<String, Object> getUser(@RequestParam String id) {
        return result("getUserById", id, userTools.getUserById(id));
    }

    @GetMapping("/user/search")
    public Map<String, Object> searchUser(@RequestParam String name) {
        return result("searchUserByName", name, userTools.searchUserByName(name));
    }

    @GetMapping("/user/create")
    public Map<String, Object> createUser(
            @RequestParam String name,
            @RequestParam String email,
            @RequestParam String position) {
        return result("createUser", name, userTools.createUser(name, email, position));
    }

    // ============= 加密工具 =============

    @GetMapping("/crypto/base64/encode")
    public Map<String, Object> base64Encode(@RequestParam String text) {
        return result("base64Encode", text, cryptoTools.base64Encode(text));
    }

    @GetMapping("/crypto/base64/decode")
    public Map<String, Object> base64Decode(@RequestParam String text) {
        return result("base64Decode", text, cryptoTools.base64Decode(text));
    }

    @GetMapping("/crypto/md5")
    public Map<String, Object> md5(@RequestParam String text) {
        return result("md5Hash", text, cryptoTools.md5Hash(text));
    }

    @GetMapping("/crypto/sha256")
    public Map<String, Object> sha256(@RequestParam String text) {
        return result("sha256Hash", text, cryptoTools.sha256Hash(text));
    }

    @GetMapping("/crypto/password")
    public Map<String, Object> analyzePassword(@RequestParam String password) {
        return result("analyzePasswordStrength", "***", cryptoTools.analyzePasswordStrength(password));
    }

    // 辅助方法
    private Map<String, Object> result(String tool, String input, Object output) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("tool", tool);
        map.put("input", input);
        map.put("output", output);
        map.put("timestamp", new Date());
        return map;
    }
}
