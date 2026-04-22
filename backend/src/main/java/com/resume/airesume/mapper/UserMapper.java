package com.resume.airesume.mapper;

import com.resume.airesume.entity.User;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface UserMapper {

    // 根据ID查询用户
    @Select("SELECT * FROM user WHERE id = #{id}")
    User findById(Long id);

    // 根据用户名查询用户
    @Select("SELECT * FROM user WHERE username = #{username}")
    User findByUsername(String username);

    // 根据邮箱查询用户
    @Select("SELECT * FROM user WHERE email = #{email}")
    User findByEmail(String email);

    // 插入用户
    @Insert("INSERT INTO user(username, email, password_hash, avatar_url, quota_used, created_at) " +
            "VALUES(#{username}, #{email}, #{passwordHash}, #{avatarUrl}, #{quotaUsed}, #{createdAt})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insert(User user);

    // 更新用户
    @Update("UPDATE user SET username=#{username}, email=#{email}, password_hash=#{passwordHash}, " +
            "avatar_url=#{avatarUrl}, quota_used=#{quotaUsed} WHERE id=#{id}")
    int update(User user);

    // 查询所有用户
    @Select("SELECT * FROM user")
    List<User> findAll();
}