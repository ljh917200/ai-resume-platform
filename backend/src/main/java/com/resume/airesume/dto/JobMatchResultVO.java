package com.resume.airesume.dto;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 岗位匹配分析结果VO（返回给前端）
 */
@Data
public class JobMatchResultVO {

    /** 分析记录ID */
    private Long id;

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

    // ===== 匹配结果 =====

    /** 匹配度评分（0-100） */
    private Integer matchScore;

    /** 匹配等级：high/middle/low */
    private String matchLevel;

    /** 匹配等级中文描述 */
    private String matchLevelText;

    /** 优势列表 */
    private List<String> strengths;

    /** 劣势列表 */
    private List<String> weaknesses;

    /** 缺少技能列表 */
    private List<String> missingSkills;

    /** 简历调整建议列表 */
    private List<String> suggestions;

    /** 完整分析结果（原文） */
    private String analysisResult;

    // ===== 统计 =====

    /** 关键词匹配数 */
    private Integer keywordsMatch;

    /** 关键词总数 */
    private Integer keywordsTotal;

    /** 经验匹配度：0-不匹配 1-匹配 2-超出要求 */
    private Integer experienceMatch;

    /** 是否已保存 */
    private Integer isSaved;

    /** 分析时间 */
    private LocalDateTime createTime;
}
