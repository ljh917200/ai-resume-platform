package com.resume.airesume.service;

import com.resume.airesume.entity.OptimizeHistory;
import java.util.List;

/**
 * 优化历史记录服务接口
 */
public interface OptimizeHistoryService {

    /**
     * 保存优化历史记录
     *
     * @param resumeId 简历ID
     * @param userId 用户ID
     * @param targetRole 目标岗位
     * @param originalText 优化前原始文本
     * @param optimizedText 优化后文本
     * @param originalStructuredData 优化前结构化数据
     * @param optimizedStructuredData 优化后结构化数据
     * @return 保存是否成功
     */
    boolean saveHistory(Long resumeId, Long userId, String targetRole,
                        String originalText, String optimizedText,
                        String originalStructuredData, String optimizedStructuredData);

    /**
     * 获取某份简历的优化历史列表
     *
     * @param resumeId 简历ID
     * @param userId 用户ID（验证权限）
     * @return 优化历史列表
     */
    List<OptimizeHistory> getHistoryList(Long resumeId, Long userId);

    /**
     * 获取优化历史详情
     *
     * @param id 历史记录ID
     * @param userId 用户ID（验证权限）
     * @return 优化历史详情
     */
    OptimizeHistory getHistoryDetail(Long id, Long userId);
}