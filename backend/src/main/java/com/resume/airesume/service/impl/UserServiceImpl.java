package com.resume.airesume.service.impl;

import com.resume.airesume.entity.User;
import com.resume.airesume.mapper.UserMapper;
import com.resume.airesume.service.UserService;
import com.resume.airesume.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.util.Base64;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserMapper userMapper;

    @Override
    public User register(String username, String email, String password) {
        // 检查用户名是否已存在
        User existByUsername = userMapper.findByUsername(username);
        if (existByUsername != null) {
            throw new RuntimeException("用户名已被注册");
        }

        // 检查邮箱是否已存在
        User existByEmail = userMapper.findByEmail(email);
        if (existByEmail != null) {
            throw new RuntimeException("邮箱已被注册");
        }

        // 创建用户
        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        user.setPasswordHash(sha256(password));
        user.setQuotaUsed(0);
        user.setCreatedAt(LocalDateTime.now());

        userMapper.insert(user);
        return user;
    }

    @Override
    public String loginByUsername(String username, String password) {
        // 查询用户
        User user = userMapper.findByUsername(username);
        if (user == null) {
            throw new RuntimeException("用户名不存在");
        }

        // 验证密码
        if (!user.getPasswordHash().equals(sha256(password))) {
            throw new RuntimeException("密码错误");
        }

        // 生成JWT token
        return JwtUtil.generateToken(user.getId(), user.getUsername(), user.getEmail());
    }

    @Override
    public String loginByEmail(String email, String password) {
        // 查询用户
        User user = userMapper.findByEmail(email);
        if (user == null) {
            throw new RuntimeException("邮箱不存在");
        }

        // 验证密码
        if (!user.getPasswordHash().equals(sha256(password))) {
            throw new RuntimeException("密码错误");
        }

        // 生成JWT token
        return JwtUtil.generateToken(user.getId(), user.getUsername(), user.getEmail());
    }

    @Override
    public User getByUsername(String username) {
        return userMapper.findByUsername(username);
    }

    @Override
    public User getByEmail(String email) {
        return userMapper.findByEmail(email);
    }

    @Override
    public User getById(Long id) {
        return userMapper.findById(id);
    }

    // SHA256加密
    private String sha256(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (Exception e) {
            throw new RuntimeException("加密失败", e);
        }
    }
}