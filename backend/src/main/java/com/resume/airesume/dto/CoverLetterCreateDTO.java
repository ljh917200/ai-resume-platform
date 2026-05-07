package com.resume.airesume.dto;

import lombok.Data;

/**
 * 求职信生成请求DTO
 * 用户提交目标岗位信息，AI生成求职信
 */
@Data
public class CoverLetterCreateDTO {

    /** 关联简历ID（必填，从哪份简历生成） */
    private Long resumeId;

    /** 关联投递记录ID（选填，从投递面板跳转时传入） */
    private Long applicationId;

    /** 目标岗位名称（必填） */
    private String jobTitle;

    /** 公司名称（选填） */
    private String companyName;

    /** 岗位描述JD（选填，有JD生成质量更高） */
    private String jobDescription;

    /** 求职信风格：formal正式/casual轻松/creative创意，默认formal */
    private String letterStyle;

    /** 语言：zh中文/en英文，默认zh */
    private String language;
}
