package com.resume.airesume.entity;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * AI求职信实体类
 */
@Data
public class CoverLetter {

    private Long id;
    private Long userId;
    private Long resumeId;
    private Long applicationId;
    private String jobTitle;
    private String companyName;
    private String jobDescription;
    private String letterContent;
    private String letterStyle;   // formal/casual/creative
    private String language;      // zh/en
    private Integer isSaved;      // 0否1是
    private Integer deleted;      // 0未删除1已删除
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
