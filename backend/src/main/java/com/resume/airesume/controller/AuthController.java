package com.resume.airesume.controller;

import com.resume.airesume.dto.Result;
import com.resume.airesume.entity.User;
import com.resume.airesume.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private UserService userService;

    // 用户注册
    @PostMapping("/register")
    public Result<Map<String, Object>> register(@RequestBody Map<String, String> params) {
        try {
            String username = params.get("username");
            String email = params.get("email");
            String password = params.get("password");

            User user = userService.register(username, email, password);

            Map<String, Object> data = new HashMap<>();
            data.put("id", user.getId());
            data.put("username", user.getUsername());
            data.put("email", user.getEmail());

            return Result.success("注册成功", data);
        } catch (Exception e) {
            return Result.error("注册失败: " + e.getMessage());
        }
    }

    // 用户名+密码登录
    @PostMapping("/login/username")
    public Result<Map<String, Object>> loginByUsername(@RequestBody Map<String, String> params) {
        try {
            String username = params.get("username");
            String password = params.get("password");

            String token = userService.loginByUsername(username, password);

            Map<String, Object> data = new HashMap<>();
            data.put("token", token);

            return Result.success("登录成功", data);
        } catch (Exception e) {
            return Result.error("登录失败: " + e.getMessage());
        }
    }

    // 邮箱+密码登录
    @PostMapping("/login/email")
    public Result<Map<String, Object>> loginByEmail(@RequestBody Map<String, String> params) {
        try {
            String email = params.get("email");
            String password = params.get("password");

            String token = userService.loginByEmail(email, password);

            Map<String, Object> data = new HashMap<>();
            data.put("token", token);

            return Result.success("登录成功", data);
        } catch (Exception e) {
            return Result.error("登录失败: " + e.getMessage());
        }
    }
}