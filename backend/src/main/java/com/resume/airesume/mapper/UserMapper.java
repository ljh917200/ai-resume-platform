package com.resume.airesume.mapper;

import com.resume.airesume.entity.User;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface UserMapper {

    // ==================== 基础查询方法 ====================

    /**
     * 根据ID查询用户
     * @param id 用户ID
     * @return 用户对象
     */
    @Select("SELECT * FROM user WHERE id = #{id}")
    User findById(Long id);

    /**
     * 根据用户名查询用户
     * @param username 用户名
     * @return 用户对象
     */
    @Select("SELECT * FROM user WHERE username = #{username}")
    User findByUsername(String username);

    /**
     * 根据邮箱查询用户
     * @param email 邮箱
     * @return 用户对象
     */
    @Select("SELECT * FROM user WHERE email = #{email}")
    User findByEmail(String email);

    // ==================== 基础增删改方法 ====================

    /**
     * 插入新用户
     * @param user 用户对象
     */
    @Insert("INSERT INTO user(username, email, password_hash, avatar_url, quota_used, created_at) " +
            "VALUES(#{username}, #{email}, #{passwordHash}, #{avatarUrl}, #{quotaUsed}, #{createdAt})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insert(User user);

    /**
     * 更新用户信息（通用方法）
     * @param user 用户对象
     * @return 影响行数
     */
    @Update("UPDATE user SET username=#{username}, email=#{email}, password_hash=#{passwordHash}, " +
            "avatar_url=#{avatarUrl}, quota_used=#{quotaUsed} WHERE id=#{id}")
    int update(User user);

    /**
     * 查询所有用户
     * @return 用户列表
     */
    @Select("SELECT * FROM user")
    List<User> findAll();

    // ==================== 个人中心专用方法 ====================

    /**
     * 修改用户名
     * @param id 用户ID
     * @param username 新用户名
     * @return 影响行数
     */
    @Update("UPDATE user SET username = #{username} WHERE id = #{id}")
    int updateUsername(@Param("id") Long id, @Param("username") String username);

    /**
     * 修改用户邮箱
     * @param id 用户ID
     * @param email 新邮箱
     * @return 影响行数
     */
    @Update("UPDATE user SET email = #{email} WHERE id = #{id}")
    int updateEmail(@Param("id") Long id, @Param("email") String email);

    /**
     * 修改用户密码
     * @param id 用户ID
     * @param passwordHash 新密码哈希
     * @return 影响行数
     */
    @Update("UPDATE user SET password_hash = #{passwordHash} WHERE id = #{id}")
    int updatePassword(@Param("id") Long id, @Param("passwordHash") String passwordHash);

    /**
     * 更新用户头像
     *
     * @param userId    用户ID
     * @param avatarUrl 头像路径
     */
    @Update("UPDATE user SET avatar_url = #{avatarUrl} WHERE id = #{userId}")
    void updateAvatar(@Param("userId") Long userId, @Param("avatarUrl") String avatarUrl);

    /**
     * 更新用户配额使用量
     * @param id 用户ID
     * @param quotaUsed 新的配额使用量
     * @return 影响行数
     */
    @Update("UPDATE user SET quota_used = #{quotaUsed} WHERE id = #{id}")
    int updateQuotaUsed(@Param("id") Long id, @Param("quotaUsed") Integer quotaUsed);

    /**
     * 增加配额使用量（原子操作）
     * @param id 用户ID
     * @param increment 增加量（可以为负数）
     * @return 影响行数
     */
    @Update("UPDATE user SET quota_used = quota_used + #{increment} WHERE id = #{id}")
    int incrementQuotaUsed(@Param("id") Long id, @Param("increment") Integer increment);



}