package com.resume.airesume.dto;

import lombok.Data;

/**
 * 岗位匹配分析请求DTO
 */
@Data
public class JobMatchAnalyzeDTO {

    /** 关联的简历ID（必填，用哪份简历去匹配） */
    private Long resumeId;

    /** 关联的投递记录ID（可选，从投递记录直接分析） */
    private Long applicationId;

    /** 职位名称（必填） */
    private String jobTitle;

    /** 公司名称（可选） */
    private String companyName;

    /** 职位描述原文（必填） */
    private String jobDescription;

    /** 职位要求原文（可选，有些JD描述和要求是分开的） */
    private String jobRequirement;

    /** 是否保存分析结果：0-临时分析 1-保存 */
    private Integer isSaved;
}
