package com.resume.airesume.entity;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 岗位匹配分析记录实体类
 * 存储AI对简历与JD的匹配度分析结果
 */
@Data
public class JobMatchAnalysis {

    /** 主键ID */
    private Long id;

    /** 用户ID */
    private Long userId;

    /** 关联的简历ID */
    private Long resumeId;

    /** 关联的投递记录ID */
    private Long applicationId;

    // ===== JD信息 =====

    /** 职位名称 */
    private String jobTitle;

    /** 公司名称 */
    private String companyName;

    /** 职位描述原文 */
    private String jobDescription;

    /** 职位要求原文 */
    private String jobRequirement;

    // ===== AI分析结果 =====

    /** 匹配度评分（0-100） */
    private Integer matchScore;

    /** 匹配等级：high/middle/low */
    private String matchLevel;

    /** 优势分析（JSON格式） */
    private String strengths;

    /** 劣势分析（JSON格式） */
    private String weaknesses;

    /** 缺少技能（JSON格式） */
    private String missingSkills;

    /** 简历调整建议（JSON格式） */
    private String suggestions;

    /** 完整分析结果 */
    private String analysisResult;

    // ===== 统计 =====

    /** 关键词匹配数 */
    private Integer keywordsMatch;

    /** 关键词总数 */
    private Integer keywordsTotal;

    /** 经验匹配度：0-不匹配 1-匹配 2-超出要求 */
    private Integer experienceMatch;

    // ===== 状态 =====

    /** 是否保存：0-临时分析 1-已保存 */
    private Integer isSaved;

    // ===== 审计字段 =====

    /** 创建时间 */
    private LocalDateTime createTime;

    /** 更新时间 */
    private LocalDateTime updateTime;

    /** 逻辑删除标志：0-未删除 1-已删除 */
    private Integer deleted;
}
