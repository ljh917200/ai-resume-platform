package com.resume.airesume.service;

import com.resume.airesume.entity.User;

public interface UserService {

    // ==================== 认证相关方法 ====================

    /**
     * 用户注册
     * @param username 用户名
     * @param email 邮箱
     * @param password 密码（明文）
     * @return 注册后的用户对象
     */
    User register(String username, String email, String password);

    /**
     * 用户名+密码登录
     * @param username 用户名
     * @param password 密码（明文）
     * @return JWT Token
     */
    String loginByUsername(String username, String password);

    /**
     * 邮箱+密码登录
     * @param email 邮箱
     * @param password 密码（明文）
     * @return JWT Token
     */
    String loginByEmail(String email, String password);

    // ==================== 基础查询方法 ====================

    /**
     * 根据用户名查询用户
     * @param username 用户名
     * @return 用户对象
     */
    User getByUsername(String username);

    /**
     * 根据邮箱查询用户
     * @param email 邮箱
     * @return 用户对象
     */
    User getByEmail(String email);

    /**
     * 根据ID查询用户
     * @param id 用户ID
     * @return 用户对象
     */
    User getById(Long id);

    // ==================== 个人中心专用方法 ====================

    /**
     * 修改用户名
     * @param id 用户ID
     * @param newUsername 新用户名
     */
    void updateUsername(Long id, String newUsername);

    /**
     * 修改用户邮箱
     * @param id 用户ID
     * @param newEmail 新邮箱
     */
    void updateEmail(Long id, String newEmail);

    /**
     * 修改用户密码
     * @param id 用户ID
     * @param oldPassword 原密码（明文）
     * @param newPassword 新密码（明文）
     */
    void updatePassword(Long id, String oldPassword, String newPassword);


    /**
     * 更新用户头像
     *
     * @param userId    用户ID
     * @param avatarUrl 头像路径
     */
    void updateAvatar(Long userId, String avatarUrl);

    /**
     * 获取用户简历数量
     * @param userId 用户ID
     * @return 简历数量
     */
    int getResumeCount(Long userId);

    /**
     * 增加配额使用量
     * @param userId 用户ID
     * @param increment 增加量
     */
    void incrementQuotaUsed(Long userId, Integer increment);




}