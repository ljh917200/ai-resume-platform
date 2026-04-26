package com.resume.airesume.entity;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 优化历史记录实体类
 * 保存每次简历优化的完整记录，便于用户追溯和对比
 */
@Data
public class OptimizeHistory {

    /**
     * 主键ID
     */
    private Long id;

    /**
     * 简历ID
     */
    private Long resumeId;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 目标岗位（用户指定的优化方向）
     */
    private String targetRole;

    /**
     * 优化前的原始文本
     */
    private String originalText;

    /**
     * 优化后的文本
     */
    private String optimizedText;

    /**
     * 优化前的结构化数据（JSON格式）
     */
    private String originalStructuredData;

    /**
     * 优化后的结构化数据（JSON格式）
     */
    private String optimizedStructuredData;

    /**
     * 优化时间
     */
    private LocalDateTime createdAt;
}