package com.resume.airesume.service;

import com.resume.airesume.dto.InterviewGenerateDTO;
import com.resume.airesume.dto.InterviewQuestionVO;
import java.util.List;
import java.util.Map;

/**
 * 面试题服务接口
 */
public interface InterviewQuestionService {

    /**
     * 生成面试题（调用DeepSeek AI）
     * @param userId 用户ID
     * @param dto    生成请求
     * @return 生成的面试题列表
     */
    List<InterviewQuestionVO> generateQuestions(Long userId, InterviewGenerateDTO dto);

    /**
     * 查询单条面试题详情
     * @param id     题目ID
     * @param userId 用户ID（权限校验）
     * @return 面试题详情
     */
    InterviewQuestionVO getQuestion(Long id, Long userId);

    /**
     * 查询用户的面试题列表（支持按类型和状态筛选）
     * @param userId       用户ID
     * @param questionType 题目类型（可选）
     * @param prepStatus   准备状态（可选）
     * @return 面试题列表
     */
    List<InterviewQuestionVO> getQuestionList(Long userId, String questionType, String prepStatus);

    /**
     * 更新答题草稿
     * @param id          题目ID
     * @param userId      用户ID
     * @param answerDraft 答题草稿内容
     */
    void updateAnswerDraft(Long id, Long userId, String answerDraft);

    /**
     * 更新准备状态
     * @param id         题目ID
     * @param userId     用户ID
     * @param prepStatus 新状态
     */
    void updatePrepStatus(Long id, Long userId, String prepStatus);

    /**
     * 保存/取消保存面试题
     * @param id      题目ID
     * @param userId  用户ID
     * @param isSaved 0取消保存 1保存
     */
    void toggleSave(Long id, Long userId, Integer isSaved);

    /**
     * 删除面试题（逻辑删除）
     * @param id     题目ID
     * @param userId 用户ID
     */
    void deleteQuestion(Long id, Long userId);

    /**
     * 获取面试准备统计（按状态分组计数）
     * @param userId 用户ID
     * @return 统计数据
     */
    Map<String, Object> getStats(Long userId);
}
