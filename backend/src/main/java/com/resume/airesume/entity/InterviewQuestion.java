package com.resume.airesume.entity;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * AI面试题实体类
 * 每道面试题单独一条记录，支持答题草稿和准备状态追踪
 */
@Data
public class InterviewQuestion {

    /** 主键ID */
    private Long id;

    /** 用户ID */
    private Long userId;

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

    /** 题目类型：technical/behavioral/situational/hr */
    private String questionType;

    /** 题目内容 */
    private String questionText;

    /** 难度：easy/medium/hard */
    private String difficulty;

    /** 答题提示 */
    private String hint;

    /** 关键得分点（JSON数组格式） */
    private String keyPoints;

    /** 用户答题草稿 */
    private String answerDraft;

    /** 准备状态：unprepared/preparing/prepared */
    private String prepStatus;

    /** 是否保存：0否1是 */
    private Integer isSaved;

    /** 逻辑删除：0未删除1已删除 */
    private Integer deleted;

    /** 创建时间 */
    private LocalDateTime createTime;

    /** 更新时间 */
    private LocalDateTime updateTime;
}
