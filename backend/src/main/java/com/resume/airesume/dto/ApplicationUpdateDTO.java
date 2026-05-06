package com.resume.airesume.dto;

import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 更新投递记录的请求DTO
 * 所有字段可选，只更新传入的字段
 */
@Data
public class ApplicationUpdateDTO {

    /** 公司名称 */
    private String companyName;

    /** 公司行业 */
    private String companyIndustry;

    /** 公司规模 */
    private String companySize;

    /** 公司地址 */
    private String companyLocation;

    /** 职位名称 */
    private String jobTitle;

    /** 职位类别 */
    private String jobCategory;

    /** 职级 */
    private String jobLevel;

    /** 最低薪资 */
    private Integer salaryMin;

    /** 最高薪资 */
    private Integer salaryMax;

    /** 投递渠道 */
    private String source;

    /** 职位来源链接 */
    private String sourceUrl;

    /** 投递状态 */
    private String status;

    /** 投递日期 */
    private LocalDate applyDate;

    /** 面试时间 */
    private LocalDateTime interviewDate;

    /** HR姓名 */
    private String hrName;

    /** 备注 */
    private String notes;

    /** 职位描述原文 */
    private String jobDescription;

    /** 关联简历ID */
    private Long resumeId;
}