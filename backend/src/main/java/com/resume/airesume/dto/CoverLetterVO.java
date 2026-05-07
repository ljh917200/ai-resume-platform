package com.resume.airesume.dto;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 求职信响应VO
 * 返回给前端的求职信数据
 */
@Data
public class CoverLetterVO {

    /** 记录ID */
    private Long id;

    /** 关联简历ID */
    private Long resumeId;

    /** 关联投递记录ID */
    private Long applicationId;

    /** 目标岗位名称 */
    private String jobTitle;

    /** 公司名称 */
    private String companyName;

    /** 岗位描述JD */
    private String jobDescription;

    /** 求职信内容 */
    private String letterContent;

    /** 风格：formal/casual/creative */
    private String letterStyle;

    /** 语言：zh/en */
    private String language;

    /** 是否保存：0否1是 */
    private Integer isSaved;

    /** 创建时间 */
    private LocalDateTime createTime;
}
