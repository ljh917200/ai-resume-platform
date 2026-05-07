package com.resume.airesume.service;

import com.resume.airesume.dto.JobMatchAnalyzeDTO;
import com.resume.airesume.dto.JobMatchResultVO;
import java.util.List;

/**
 * 岗位匹配分析服务接口
 */
public interface JobMatchService {

    /**
     * 分析JD与简历的匹配度（核心方法，调用DeepSeek）
     * @param userId 用户ID
     * @param dto 分析请求
     * @return 分析结果
     */
    JobMatchResultVO analyzeMatch(Long userId, JobMatchAnalyzeDTO dto);

    /**
     * 获取分析结果详情
     * @param id 分析记录ID
     * @param userId 用户ID（权限校验）
     * @return 分析结果
     */
    JobMatchResultVO getResult(Long id, Long userId);

    /**
     * 查询用户分析历史
     * @param userId 用户ID
     * @return 分析记录列表
     */
    List<JobMatchResultVO> getHistory(Long userId);

    /**
     * 保存/取消保存分析记录
     * @param id 分析记录ID
     * @param userId 用户ID
     * @param isSaved 0-取消保存 1-保存
     */
    void toggleSave(Long id, Long userId, Integer isSaved);

    /**
     * 删除分析记录（逻辑删除）
     * @param id 分析记录ID
     * @param userId 用户ID
     */
    void deleteAnalysis(Long id, Long userId);
}
