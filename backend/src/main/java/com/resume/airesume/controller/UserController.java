
package com.resume.airesume.controller;

import com.resume.airesume.dto.Result;
import com.resume.airesume.entity.User;
import com.resume.airesume.mapper.ResumeMapper;
import com.resume.airesume.service.FileStorageService;
import com.resume.airesume.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

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

    @Autowired
    private FileStorageService fileStorageService;

    // 服务器基础地址（用于拼接头像完整URL）
    @org.springframework.beans.factory.annotation.Value("${server.base-url:http://localhost:8080}")
    private String serverBaseUrl;

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

            // 头像URL处理：如果是相对路径，拼接完整URL
            String avatarUrl = user.getAvatarUrl();
            if (avatarUrl != null && !avatarUrl.isEmpty() && !avatarUrl.startsWith("http")) {
                avatarUrl = serverBaseUrl + avatarUrl;
            }


            // 3. 构建返回数据（不返回密码哈希）
            Map<String, Object> data = new HashMap<>();
            data.put("id", user.getId());
            data.put("username", user.getUsername());
            data.put("email", user.getEmail());
            data.put("avatarUrl", avatarUrl);
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



    /**
     * 上传头像
     *
     * 功能说明：
     * - 用户上传头像图片
     * - 图片存储到本地磁盘
     * - 图片路径保存到 user.avatar_url 字段
     *
     * @param file    头像图片文件
     * @param request HTTP请求（获取当前用户ID）
     * @return 上传结果，包含头像访问路径
     */
    @PostMapping("/avatar")
    public Result<String> uploadAvatar(
            @RequestParam("file") MultipartFile file,
            HttpServletRequest request) {

        try {
            // ========== 第一步：获取当前登录用户ID ==========
            Long userId = (Long) request.getAttribute("userId");
            if (userId == null) {
                return Result.error("用户未登录");
            }

            // ========== 第二步：上传文件 ==========
            // 调用文件存储服务，返回访问路径
            String avatarUrl = fileStorageService.uploadAvatar(file, userId);

            // ========== 第三步：删除旧头像（如果有） ==========
            // 先查询用户当前头像
            User user = userService.getById(userId);
            if (user != null && user.getAvatarUrl() != null) {
                // 删除旧头像文件
                fileStorageService.deleteAvatar(user.getAvatarUrl());
            }

            // ========== 第四步：更新数据库 ==========
            // 将新头像路径保存到数据库
            userService.updateAvatar(userId, avatarUrl);

            // ========== 第五步：返回结果 ==========
            return Result.success("头像上传成功", avatarUrl);

        } catch (Exception e) {
            return Result.error("头像上传失败：" + e.getMessage());
        }
    }

    /**
     * 删除头像
     *
     * @param request HTTP请求（获取当前用户ID）
     * @return 操作结果
     */
    @DeleteMapping("/avatar")
    public Result<String> deleteAvatar(HttpServletRequest request) {
        try {
            // 获取当前用户ID
            Long userId = (Long) request.getAttribute("userId");
            if (userId == null) {
                return Result.error("用户未登录");
            }

            // 查询用户当前头像
            User user = userService.getById(userId);
            if (user == null || user.getAvatarUrl() == null) {
                return Result.error("用户没有头像");
            }

            // 删除文件
            fileStorageService.deleteAvatar(user.getAvatarUrl());

            // 清空数据库中的头像路径
            userService.updateAvatar(userId, null);

            return Result.success("头像删除成功");

        } catch (Exception e) {
            return Result.error("删除失败：" + e.getMessage());
        }
    }
}
