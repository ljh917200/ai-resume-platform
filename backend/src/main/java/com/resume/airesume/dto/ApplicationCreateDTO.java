package com.resume.airesume.dto;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
/**
 * 创建投递记录的请求DTO
 */
@Data
public class ApplicationCreateDTO {

    /** 公司名称（必填） */
    @NotBlank(message = "公司名称不能为空")
    private String companyName;

    /** 公司行业 */
    private String companyIndustry;

    /** 公司规模 */
    private String companySize;

    /** 公司地址 */
    private String companyLocation;

    /** 职位名称（必填） */
    @NotBlank(message = "职位名称不能为空")
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

    /** 投递状态，默认interested */
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