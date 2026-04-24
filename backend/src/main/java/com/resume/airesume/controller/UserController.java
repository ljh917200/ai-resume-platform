
package com.resume.airesume.controller;

import com.resume.airesume.dto.Result;
import com.resume.airesume.entity.User;
import com.resume.airesume.mapper.ResumeMapper;
import com.resume.airesume.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;

/**
 * 用户中心控制器
 * 处理用户个人信息管理相关接口
 * 包括：获取用户信息、修改用户名、绑定/修改邮箱、修改密码、获取统计数据
 */
@RestController
@RequestMapping("/api/user")
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private ResumeMapper resumeMapper;

    /**
     * 获取当前登录用户的基本信息
     *
     * @param request HTTP请求对象（用于从拦截器获取用户ID）
     * @return 用户基本信息（不包含密码）
     */
    @GetMapping("/profile")
    public Result<Map<String, Object>> getProfile(HttpServletRequest request) {
        try {
            // 1. 从拦截器获取当前登录用户ID
            Long userId = (Long) request.getAttribute("userId");
            if (userId == null) {
                return Result.error("用户未登录");
            }

            // 2. 查询用户信息
            User user = userService.getById(userId);
            if (user == null) {
                return Result.error("用户不存在");
            }

            // 3. 构建返回数据（不返回密码哈希）
            Map<String, Object> data = new HashMap<>();
            data.put("id", user.getId());
            data.put("username", user.getUsername());
            data.put("email", user.getEmail());
            data.put("avatarUrl", user.getAvatarUrl());
            data.put("quotaUsed", user.getQuotaUsed());
            data.put("createdAt", user.getCreatedAt());

            return Result.success("获取成功", data);
        } catch (Exception e) {
            return Result.error("获取用户信息失败: " + e.getMessage());
        }
    }

    /**
     * 修改用户显示名称
     *
     * @param params 包含新用户名的参数Map
     * @param request HTTP请求对象（用于从拦截器获取用户ID）
     * @return 修改结果
     */
    @PutMapping("/username")
    public Result<String> updateUsername(
            @RequestBody Map<String, String> params,
            HttpServletRequest request) {
        try {
            // 1. 获取当前登录用户ID
            Long userId = (Long) request.getAttribute("userId");
            if (userId == null) {
                return Result.error("用户未登录");
            }

            // 2. 获取新用户名
            String newUsername = params.get("username");
            if (newUsername == null || newUsername.trim().isEmpty()) {
                return Result.error("用户名不能为空");
            }
            newUsername = newUsername.trim();

            // 3. 校验用户名长度（3-20个字符）
            if (newUsername.length() < 3 || newUsername.length() > 20) {
                return Result.error("用户名长度需要在3-20个字符之间");
            }

            // 4. 检查新用户名是否已被其他用户使用
            User existUser = userService.getByUsername(newUsername);
            if (existUser != null && !existUser.getId().equals(userId)) {
                return Result.error("用户名已被其他用户使用");
            }

            // 5. 调用Service层更新用户名
            userService.updateUsername(userId, newUsername);

            return Result.success("用户名修改成功", null);
        } catch (Exception e) {
            return Result.error("修改用户名失败: " + e.getMessage());
        }
    }

    /**
     * 绑定或修改用户邮箱
     *
     * @param params 包含新邮箱的参数Map
     * @param request HTTP请求对象（用于从拦截器获取用户ID）
     * @return 修改结果
     */
    @PutMapping("/email")
    public Result<String> updateEmail(
            @RequestBody Map<String, String> params,
            HttpServletRequest request) {
        try {
            // 1. 获取当前登录用户ID
            Long userId = (Long) request.getAttribute("userId");
            if (userId == null) {
                return Result.error("用户未登录");
            }

            // 2. 获取新邮箱
            String newEmail = params.get("email");
            if (newEmail == null || newEmail.trim().isEmpty()) {
                return Result.error("邮箱不能为空");
            }
            newEmail = newEmail.trim().toLowerCase();

            // 3. 校验邮箱格式（简单验证）
            if (!newEmail.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")) {
                return Result.error("邮箱格式不正确");
            }

            // 4. 检查新邮箱是否已被其他用户使用
            User existUser = userService.getByEmail(newEmail);
            if (existUser != null && !existUser.getId().equals(userId)) {
                return Result.error("邮箱已被其他用户使用");
            }

            // 5. 调用Service层更新邮箱
            userService.updateEmail(userId, newEmail);

            return Result.success("邮箱修改成功", null);
        } catch (Exception e) {
            return Result.error("修改邮箱失败: " + e.getMessage());
        }
    }

    /**
     * 修改用户密码
     * 需要验证原密码才能修改
     *
     * @param params 包含原密码和新密码的参数Map
     * @param request HTTP请求对象（用于从拦截器获取用户ID）
     * @return 修改结果
     */
    @PutMapping("/password")
    public Result<String> updatePassword(
            @RequestBody Map<String, String> params,
            HttpServletRequest request) {
        try {
            // 1. 获取当前登录用户ID
            Long userId = (Long) request.getAttribute("userId");
            if (userId == null) {
                return Result.error("用户未登录");
            }

            // 2. 获取原密码和新密码
            String oldPassword = params.get("oldPassword");
            String newPassword = params.get("newPassword");

            // 3. 校验原密码是否为空
            if (oldPassword == null || oldPassword.trim().isEmpty()) {
                return Result.error("原密码不能为空");
            }

            // 4. 校验新密码是否为空
            if (newPassword == null || newPassword.trim().isEmpty()) {
                return Result.error("新密码不能为空");
            }

            // 5. 校验新密码长度（6-20个字符）
            if (newPassword.length() < 6 || newPassword.length() > 20) {
                return Result.error("新密码长度需要在6-20个字符之间");
            }

            // 6. 校验新密码和原密码不能相同
            if (oldPassword.equals(newPassword)) {
                return Result.error("新密码不能与原密码相同");
            }

            // 7. 调用Service层验证原密码并修改密码
            userService.updatePassword(userId, oldPassword, newPassword);

            return Result.success("密码修改成功", null);
        } catch (Exception e) {
            return Result.error("修改密码失败: " + e.getMessage());
        }
    }

    /**
     * 获取用户统计数据
     * 包括：简历数量、优化次数、已用额度、加入天数
     *
     * @param request HTTP请求对象（用于从拦截器获取用户ID）
     * @return 统计数据
     */
    @GetMapping("/statistics")
    public Result<Map<String, Object>> getStatistics(HttpServletRequest request) {
        try {
            // 1. 从拦截器获取当前登录用户ID
            Long userId = (Long) request.getAttribute("userId");
            if (userId == null) {
                return Result.error("用户未登录");
            }

            // 2. 查询用户信息获取quotaUsed和createdAt
            User user = userService.getById(userId);
            if (user == null) {
                return Result.error("用户不存在");
            }

            // 3. 查询简历数量
            int resumeCount = userService.getResumeCount(userId);

            // 4. 计算加入天数
            long joinDays = 0;
            if (user.getCreatedAt() != null) {
                joinDays = ChronoUnit.DAYS.between(user.getCreatedAt(), LocalDateTime.now()) + 1;
            }

            // 5. 构建统计数据
            Map<String, Object> data = new HashMap<>();
            data.put("resumeCount", resumeCount);           // 简历数量
            data.put("optimizeCount", user.getQuotaUsed()); // 优化次数（已用额度）
            data.put("quotaUsed", user.getQuotaUsed());     // 已用额度
            data.put("joinDays", joinDays);                 // 加入天数

            return Result.success("获取成功", data);
        } catch (Exception e) {
            return Result.error("获取统计数据失败: " + e.getMessage());
        }
    }
}
