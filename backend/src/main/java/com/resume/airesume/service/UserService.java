package com.resume.airesume.service;

import com.resume.airesume.entity.User;

public interface UserService {

    // 用户注册
    User register(String username, String email, String password);

    // 用户名+密码登录
    String loginByUsername(String username, String password);

    // 邮箱+密码登录
    String loginByEmail(String email, String password);

    // 根据用户名查询用户
    User getByUsername(String username);

    // 根据邮箱查询用户
    User getByEmail(String email);

    // 根据ID查询用户
    User getById(Long id);
}