package com.resume.airesume.mapper;

import com.resume.airesume.entity.JobMatchAnalysis;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

/**
 * 岗位匹配分析 Mapper
 */
@Mapper
public interface JobMatchAnalysisMapper {

    /**
     * 插入分析记录
     */
    int insert(JobMatchAnalysis analysis);

    /**
     * 根据ID查询
     */
    JobMatchAnalysis selectById(@Param("id") Long id);

    /**
     * 查询用户的分析历史（分页）
     */
    List<JobMatchAnalysis> selectByUserId(@Param("userId") Long userId);

    /**
     * 查询用户已保存的分析记录
     */
    List<JobMatchAnalysis> selectSavedByUserId(@Param("userId") Long userId);

    /**
     * 更新分析记录
     */
    int update(JobMatchAnalysis analysis);

    /**
     * 逻辑删除
     */
    int deleteById(@Param("id") Long id);

    /**
     * 统计用户分析次数
     */
    int countByUserId(@Param("userId") Long userId);
}
