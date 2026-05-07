package com.resume.airesume.dto;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 面试题响应VO
 */
@Data
public class InterviewQuestionVO {

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

    /** 题目类型：technical/behavioral/situational/hr */
    private String questionType;

    /** 题目内容 */
    private String questionText;

    /** 难度：easy/medium/hard */
    private String difficulty;

    /** 答题提示 */
    private String hint;

    /** 关键得分点（已解析为List） */
    private List<String> keyPoints;

    /** 用户答题草稿 */
    private String answerDraft;

    /** 准备状态：unprepared/preparing/prepared */
    private String prepStatus;

    /** 是否保存：0否1是 */
    private Integer isSaved;

    /** 创建时间 */
    private LocalDateTime createTime;
}
