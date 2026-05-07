package com.resume.airesume.dto;

import lombok.Data;

/**
 * 面试题生成请求DTO
 */
@Data
public class InterviewGenerateDTO {

    /** 关联简历ID（必填） */
    private Long resumeId;

    /** 关联投递记录ID（选填，从投递面板跳转时传入） */
    private Long applicationId;

    /** 目标岗位名称（必填） */
    private String jobTitle;

    /** 公司名称（选填） */
    private String companyName;

    /** 岗位描述JD（选填，有JD生成质量更高） */
    private String jobDescription;

    /** 题目类型（选填，不传则生成全部类型：technical/behavioral/situational/hr） */
    private String questionType;

    /** 每种类型生成几道题，默认3 */
    private Integer countPerType;
}
