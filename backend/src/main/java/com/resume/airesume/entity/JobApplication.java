package com.resume.airesume.entity;

import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 投递记录实体类
 * 对应数据库表 job_application
 * 记录用户的职位投递信息，支持看板式状态管理
 */
@Data
public class JobApplication {

    /** 主键ID */
    private Long id;

    /** 用户ID，关联user表 */
    private Long userId;

    /** 公司名称 */
    private String companyName;

    /** 公司行业 */
    private String companyIndustry;

    /** 公司规模：初创/中小型/大型/上市公司 */
    private String companySize;

    /** 公司地址 */
    private String companyLocation;

    /** 职位名称 */
    private String jobTitle;

    /** 职位类别：技术/产品/运营/市场/职能等 */
    private String jobCategory;

    /** 职级：初级/中级/高级/专家 */
    private String jobLevel;

    /** 最低薪资（月薪，单位：元） */
    private Integer salaryMin;

    /** 最高薪资（月薪，单位：元） */
    private Integer salaryMax;

    /** 投递渠道：boss直聘/猎聘/拉勾/官网/内推/其他 */
    private String source;

    /** 职位来源链接 */
    private String sourceUrl;

    /**
     * 投递状态
     * interested → applied → screening → test → first_interview → second_interview → hr_interview
     * 终态：offer_received / offer_rejected / rejected / withdrawn
     */
    private String status;

    /** 投递日期 */
    private LocalDate applyDate;

    /** 面试时间 */
    private LocalDateTime interviewDate;

    /** HR姓名 */
    private String hrName;

    /** 备注信息 */
    private String notes;

    /** 职位描述原文（用于后续AI分析） */
    private String jobDescription;

    /** 关联的简历ID */
    private Long resumeId;

    /** 创建时间 */
    private LocalDateTime createTime;

    /** 更新时间 */
    private LocalDateTime updateTime;

    /** 逻辑删除标志：0-未删除 1-已删除 */
    private Integer deleted;
}