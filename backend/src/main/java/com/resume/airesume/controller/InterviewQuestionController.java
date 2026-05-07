package com.resume.airesume.controller;

import com.resume.airesume.dto.InterviewGenerateDTO;
import com.resume.airesume.dto.InterviewQuestionVO;
import com.resume.airesume.dto.Result;
import com.resume.airesume.service.InterviewQuestionService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * AI面试题控制器
 * 提供面试题生成、查询、答题草稿、准备状态管理等接口
 */
@RestController
@RequestMapping("/api/interview")
public class InterviewQuestionController {

    @Autowired
    private InterviewQuestionService interviewQuestionService;

    /**
     * 生成面试题
     * @param dto     生成请求（简历ID、岗位信息、题目类型、每类数量）
     * @param request 用于获取当前登录用户ID
     */
    @PostMapping("/generate")
    public Result<List<InterviewQuestionVO>> generate(
            @RequestBody InterviewGenerateDTO dto, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        if (userId == null) {
            return Result.error("用户未登录");
        }
        List<InterviewQuestionVO> list = interviewQuestionService.generateQuestions(userId, dto);
        return Result.success(list);
    }

    /**
     * 查询单条面试题详情
     * @param id      题目ID
     * @param request 用于获取当前登录用户ID（权限校验）
     */
    @GetMapping("/{id}")
    public Result<InterviewQuestionVO> getQuestion(
            @PathVariable Long id, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        if (userId == null) {
            return Result.error("用户未登录");
        }
        InterviewQuestionVO result = interviewQuestionService.getQuestion(id, userId);
        return Result.success(result);
    }

    /**
     * 查询面试题列表（支持按类型和状态筛选）
     * @param questionType 题目类型（可选）
     * @param prepStatus   准备状态（可选）
     * @param request      用于获取当前登录用户ID
     */
    @GetMapping("/list")
    public Result<List<InterviewQuestionVO>> list(
            @RequestParam(required = false) String questionType,
            @RequestParam(required = false) String prepStatus,
            HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        if (userId == null) {
            return Result.error("用户未登录");
        }
        List<InterviewQuestionVO> list = interviewQuestionService.getQuestionList(userId, questionType, prepStatus);
        return Result.success(list);
    }

    /**
     * 更新答题草稿
     * @param id          题目ID
     * @param body 答题草稿内容
     * @param request     用于获取当前登录用户ID（权限校验）
     */
    @PutMapping("/{id}/draft")
    public Result<Void> updateDraft(
            @PathVariable Long id,
            @RequestBody Map<String, String> body,
            HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        if (userId == null) {
            return Result.error("用户未登录");
        }
        interviewQuestionService.updateAnswerDraft(id, userId, body.get("answerDraft"));
        return Result.success(null);
    }

    /**
     * 更新准备状态
     * @param id         题目ID
     * @param prepStatus 新状态（unprepared/preparing/prepared）
     * @param request    用于获取当前登录用户ID（权限校验）
     */
    @PutMapping("/{id}/status")
    public Result<Void> updateStatus(
            @PathVariable Long id,
            @RequestParam String prepStatus,
            HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        if (userId == null) {
            return Result.error("用户未登录");
        }
        interviewQuestionService.updatePrepStatus(id, userId, prepStatus);
        return Result.success(null);
    }

    /**
     * 保存/取消保存面试题
     * @param id      题目ID
     * @param isSaved 0取消保存 1保存
     * @param request 用于获取当前登录用户ID（权限校验）
     */
    @PutMapping("/{id}/save")
    public Result<Void> toggleSave(
            @PathVariable Long id,
            @RequestParam Integer isSaved,
            HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        if (userId == null) {
            return Result.error("用户未登录");
        }
        interviewQuestionService.toggleSave(id, userId, isSaved);
        return Result.success(null);
    }

    /**
     * 删除面试题（逻辑删除）
     * @param id      题目ID
     * @param request 用于获取当前登录用户ID（权限校验）
     */
    @DeleteMapping("/{id}")
    public Result<Void> delete(
            @PathVariable Long id, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        if (userId == null) {
            return Result.error("用户未登录");
        }
        interviewQuestionService.deleteQuestion(id, userId);
        return Result.success(null);
    }

    /**
     * 获取面试准备统计（按状态分组计数+完成率）
     * @param request 用于获取当前登录用户ID
     */
    @GetMapping("/stats")
    public Result<Map<String, Object>> stats(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        if (userId == null) {
            return Result.error("用户未登录");
        }
        Map<String, Object> stats = interviewQuestionService.getStats(userId);
        return Result.success(stats);
    }
}
