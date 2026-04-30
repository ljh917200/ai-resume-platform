package com.resume.airesume.mapper;

import com.resume.airesume.entity.Resume;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface ResumeMapper {

    // 根据ID查询简历
    @Select("SELECT * FROM resume WHERE id = #{id}")
    Resume findById(Long id);

    // 根据用户ID查询简历列表
    @Select("SELECT * FROM resume WHERE user_id = #{userId} ORDER BY created_at DESC")
    List<Resume> findByUserId(Long userId);

    // 插入简历
    @Insert("INSERT INTO resume(user_id, original_text, structured_data, file_name, file_format, created_at) " +
            "VALUES(#{userId}, #{originalText}, #{structuredData}, #{fileName}, #{fileFormat}, #{createdAt})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insert(Resume resume);

    // 删除简历
    @Delete("DELETE FROM resume WHERE id = #{id}")
    int deleteById(Long id);

    // 更新简历  更新简历内容（文本、结构化数据等），在优化简历时用
    @Update("UPDATE resume SET original_text=#{originalText}, structured_data=#{structuredData}, optimized_structured_data=#{optimizedStructuredData}, template_id=#{templateId} WHERE id=#{id}")
    int update(Resume resume);

    // 更新简历模板  只更新模板ID，在用户切换模板时用
    @Update("UPDATE resume SET template_id = #{templateId} WHERE id = #{id} AND user_id = #{userId}")
    int updateTemplateId(@Param("id") Long id, @Param("userId") Long userId, @Param("templateId") Integer templateId);

    // 更新优化内容
    @Update("UPDATE resume SET optimized_text = #{optimizedText},optimized_structured_data=#{optimizedStructuredData}, last_optimized_at = #{lastOptimizedAt} WHERE id = #{id}")
    void updateOptimizedText(Resume resume);

    /**
     * 更新简历显示名称
     *
     * @param id 简历ID
     * @param userId 用户ID（验证归属）
     * @param displayName 新名称
     * @return 影响行数
     */
    @Update("UPDATE resume SET display_name = #{displayName} WHERE id = #{id} AND user_id = #{userId}")
    int updateDisplayName(@Param("id") Long id, @Param("userId") Long userId, @Param("displayName") String displayName);


    /**
     * 批量删除简历
     *
     * @param ids 简历ID列表
     * @param userId 用户ID（验证归属）
     * @return 影响行数
     */
    @Delete("<script>" +
            "DELETE FROM resume WHERE id IN " +
            "<foreach collection='ids' item='id' open='(' separator=',' close=')'>" +
            "#{id}" +
            "</foreach>" +
            " AND user_id = #{userId}" +
            "</script>")
    int batchDelete(@Param("ids") List<Long> ids, @Param("userId") Long userId);

    /**
     * 更新原始简历HTML（v1.7.0新增）
     * <p>
     * 功能说明：
     * - 将 DeepSeek 生成的原始简历 HTML 存储到数据库
     * - 避免重复调用 AI，节省成本
     *
     * @param id            简历ID
     * @param userId        用户ID（用于验证归属）
     * @param generatedHtml AI生成的原始简历HTML内容
     */
    @Update("UPDATE resume SET generated_html = #{generatedHtml} WHERE id = #{id} AND user_id = #{userId}")
    void updateGeneratedHtml(@Param("id") Long id, @Param("userId") Long userId, @Param("generatedHtml") String generatedHtml);

    /**
     * 更新优化后简历HTML（v1.7.0新增）
     * <p>
     * 功能说明：
     * - 将 DeepSeek 生成的优化后简历 HTML 存储到数据库
     * - 用户优化简历后，会生成优化版的 HTML
     *
     * @param id            简历ID
     * @param userId        用户ID（用于验证归属）
     * @param optimizedHtml AI生成的优化后简历HTML内容
     */
    @Update("UPDATE resume SET optimized_html = #{optimizedHtml} WHERE id = #{id} AND user_id = #{userId}")
    void updateOptimizedHtml(@Param("id") Long id, @Param("userId") Long userId, @Param("optimizedHtml") String optimizedHtml);


    /**
     * 更新HTML内容和模板ID（v1.7.0新增）
     * <p>
     * 功能说明：
     * - 同时更新 HTML 内容和模板 ID
     * - 用于切换模板后重新生成 HTML
     *
     * @param id          简历ID
     * @param userId      用户ID（验证归属）
     * @param htmlField   要更新的HTML字段名：generated_html 或 optimized_html
     * @param htmlContent HTML内容
     * @param templateId  模板ID
     */
    @Update("<script>" +
            "UPDATE resume SET " +
            "<if test='htmlField == \"optimizedHtml\"'>" +
            "optimized_html = #{htmlContent}, " +
            "</if>" +
            "<if test='htmlField == \"generatedHtml\"'>" +
            "generated_html = #{htmlContent}, " +
            "</if>" +
            "template_id = #{templateId} " +
            "WHERE id = #{id} AND user_id = #{userId}" +
            "</script>")
    void updateHtmlAndTemplate(@Param("id") Long id,
                              @Param("userId") Long userId,
                              @Param("htmlField") String htmlField,
                              @Param("htmlContent") String htmlContent,
                              @Param("templateId") Integer templateId);


    /**
     * 更新简历是否显示头像
     * @param id 简历ID
     * @param showAvatar 0-不显示 1-显示
     */
    @Update("UPDATE resume SET show_avatar = #{showAvatar} WHERE id = #{id}")
    void updateShowAvatar(@Param("id") Long id, @Param("showAvatar") Integer showAvatar);


}