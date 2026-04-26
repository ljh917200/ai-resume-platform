package com.resume.airesume.mapper;

import com.resume.airesume.entity.OptimizeHistory;
import org.apache.ibatis.annotations.*;
import java.util.List;

/**
 * 优化历史记录Mapper接口
 * 提供优化历史的增删查操作
 */
@Mapper
public interface OptimizeHistoryMapper {

    /**
     * 插入一条优化历史记录
     *
     * @param history 优化历史对象
     * @return 影响行数
     */
    @Insert("INSERT INTO optimize_history (resume_id, user_id, target_role, original_text, optimized_text, " +
            "original_structured_data, optimized_structured_data, created_at) " +
            "VALUES (#{resumeId}, #{userId}, #{targetRole}, #{originalText}, #{optimizedText}, " +
            "#{originalStructuredData}, #{optimizedStructuredData}, #{createdAt})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(OptimizeHistory history);

    /**
     * 根据简历ID查询优化历史列表（按时间倒序）
     *
     * @param resumeId 简历ID
     * @return 优化历史列表
     */
    @Select("SELECT id, resume_id, user_id, target_role, original_text, optimized_text, " +
            "original_structured_data, optimized_structured_data, created_at " +
            "FROM optimize_history WHERE resume_id = #{resumeId} ORDER BY created_at DESC")
    List<OptimizeHistory> findByResumeId(Long resumeId);

    /**
     * 根据ID查询优化历史详情
     *
     * @param id 历史记录ID
     * @return 优化历史对象
     */
    @Select("SELECT id, resume_id, user_id, target_role, original_text, optimized_text, " +
            "original_structured_data, optimized_structured_data, created_at " +
            "FROM optimize_history WHERE id = #{id}")
    OptimizeHistory findById(Long id);

    /**
     * 根据简历ID删除所有优化历史
     *
     * @param resumeId 简历ID
     * @return 影响行数
     */
    @Delete("DELETE FROM optimize_history WHERE resume_id = #{resumeId}")
    int deleteByResumeId(Long resumeId);
}