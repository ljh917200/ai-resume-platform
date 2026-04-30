package com.resume.airesume.service.impl;

import com.resume.airesume.entity.User;
import com.resume.airesume.mapper.ResumeMapper;
import com.resume.airesume.mapper.UserMapper;
import com.resume.airesume.service.UserService;
import com.resume.airesume.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDateTime;

/**
 * 用户服务实现类
 * 实现用户注册、登录、个人信息管理等业务逻辑
 */
@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private ResumeMapper resumeMapper;

    // ==================== 认证相关方法实现 ====================

    @Override
    public User register(String username, String email, String password) {
        // 1. 检查用户名是否已存在
        User existByUsername = userMapper.findByUsername(username);
        if (existByUsername != null) {
            throw new RuntimeException("用户名已被注册");
        }

        // 2. 检查邮箱是否已存在
        User existByEmail = userMapper.findByEmail(email);
        if (existByEmail != null) {
            throw new RuntimeException("邮箱已被注册");
        }

        // 3. 创建用户对象
        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        user.setPasswordHash(sha256(password));  // 使用SHA256加密密码
        user.setQuotaUsed(0);                     // 初始配额为0
        user.setCreatedAt(LocalDateTime.now());  // 设置创建时间

        // 4. 保存到数据库
        userMapper.insert(user);
        return user;
    }

    @Override
    public String loginByUsername(String username, String password) {
        // 1. 查询用户
        User user = userMapper.findByUsername(username);
        if (user == null) {
            throw new RuntimeException("用户名不存在");
        }

        // 2. 验证密码
        if (!user.getPasswordHash().equals(sha256(password))) {
            throw new RuntimeException("密码错误");
        }

        // 3. 生成JWT token并返回
        return JwtUtil.generateToken(user.getId(), user.getUsername(), user.getEmail());
    }

    @Override
    public String loginByEmail(String email, String password) {
        // 1. 查询用户
        User user = userMapper.findByEmail(email);
        if (user == null) {
            throw new RuntimeException("邮箱不存在");
        }

        // 2. 验证密码
        if (!user.getPasswordHash().equals(sha256(password))) {
            throw new RuntimeException("密码错误");
        }

        // 3. 生成JWT token并返回
        return JwtUtil.generateToken(user.getId(), user.getUsername(), user.getEmail());
    }

    // ==================== 基础查询方法实现 ====================

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

    // ==================== 个人中心专用方法实现 ====================

    /**
     * 修改用户名
     * @param id 用户ID
     * @param newUsername 新用户名
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateUsername(Long id, String newUsername) {
        // 1. 查询当前用户
        User user = userMapper.findById(id);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }

        // 2. 检查新用户名是否与原用户名相同（无需更新）
        if (newUsername.equals(user.getUsername())) {
            return;
        }

        // 3. 检查新用户名是否已被其他用户使用
        User existUser = userMapper.findByUsername(newUsername);
        if (existUser != null && !existUser.getId().equals(id)) {
            throw new RuntimeException("用户名已被其他用户使用");
        }

        // 4. 更新用户名
        int rows = userMapper.updateUsername(id, newUsername);
        if (rows == 0) {
            throw new RuntimeException("用户名修改失败");
        }
    }

    /**
     * 修改用户邮箱
     * @param id 用户ID
     * @param newEmail 新邮箱
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateEmail(Long id, String newEmail) {
        // 1. 查询当前用户
        User user = userMapper.findById(id);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }

        // 2. 检查新邮箱是否与原邮箱相同（无需更新）
        if (newEmail.equalsIgnoreCase(user.getEmail())) {
            return;
        }

        // 3. 检查新邮箱是否已被其他用户使用
        User existUser = userMapper.findByEmail(newEmail);
        if (existUser != null && !existUser.getId().equals(id)) {
            throw new RuntimeException("邮箱已被其他用户使用");
        }

        // 4. 更新邮箱
        int rows = userMapper.updateEmail(id, newEmail);
        if (rows == 0) {
            throw new RuntimeException("邮箱修改失败");
        }
    }

    /**
     * 修改用户密码
     * @param id 用户ID
     * @param oldPassword 原密码（明文）
     * @param newPassword 新密码（明文）
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updatePassword(Long id, String oldPassword, String newPassword) {
        // 1. 查询当前用户
        User user = userMapper.findById(id);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }

        // 2. 验证原密码是否正确
        String oldPasswordHash = sha256(oldPassword);
        if (!oldPasswordHash.equals(user.getPasswordHash())) {
            throw new RuntimeException("原密码错误");
        }

        // 3. 加密新密码
        String newPasswordHash = sha256(newPassword);

        // 4. 更新密码
        int rows = userMapper.updatePassword(id, newPasswordHash);
        if (rows == 0) {
            throw new RuntimeException("密码修改失败");
        }
    }

    /**
     * 更新用户头像
     * @param userId 用户ID
     * @param avatarUrl 新头像URL
     */
    @Override
    public void updateAvatar(Long userId, String avatarUrl) {
        userMapper.updateAvatar(userId, avatarUrl);
    }

    /**
     * 获取用户简历数量
     * @param userId 用户ID
     * @return 简历数量
     */
    @Override
    public int getResumeCount(Long userId) {
        // 通过ResumeMapper查询该用户的简历数量
        return resumeMapper.findByUserId(userId).size();
    }

    /**
     * 增加配额使用量
     * @param userId 用户ID
     * @param increment 增加量（可以为负数）
     */
    @Override
    public void incrementQuotaUsed(Long userId, Integer increment) {
        // 使用原子操作更新配额
        int rows = userMapper.incrementQuotaUsed(userId, increment);
        if (rows == 0) {
            throw new RuntimeException("配额更新失败，用户可能不存在");
        }
    }

    // ==================== 私有工具方法 ====================

    /**
     * SHA256加密工具方法
     * 用于密码加密
     *
     * @param input 明文输入
     * @return SHA256加密后的十六进制字符串
     */
    private String sha256(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (Exception e) {
            throw new RuntimeException("密码加密失败", e);
        }
    }



}