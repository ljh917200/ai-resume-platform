package com.resume.airesume.controller;

import com.resume.airesume.dto.Result;
import com.resume.airesume.entity.OptimizeHistory;
import com.resume.airesume.service.OptimizeHistoryService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 优化历史记录控制器
 * 提供优化历史的查询接口
 */
@RestController
@RequestMapping("/api/history")
public class OptimizeHistoryController {

    @Autowired
    private OptimizeHistoryService optimizeHistoryService;

    /**
     * 获取某份简历的优化历史列表
     *
     * @param resumeId 简历ID
     * @param request HTTP请求对象（获取当前用户ID）
     * @return 优化历史列表
     */
    @GetMapping("/list/{resumeId}")
    public Result<List<OptimizeHistory>> getHistoryList(
            @PathVariable("resumeId") Long resumeId,
            HttpServletRequest request) {

        try {
            // 1. 获取当前登录用户ID
            Long userId = (Long) request.getAttribute("userId");
            if (userId == null) {
                return Result.error("用户未登录");
            }

            // 2. 查询优化历史列表
            List<OptimizeHistory> historyList = optimizeHistoryService.getHistoryList(resumeId, userId);

            return Result.success("查询成功", historyList);

        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 获取优化历史详情
     *
     * @param id 历史记录ID
     * @param request HTTP请求对象（获取当前用户ID）
     * @return 优化历史详情
     */
    @GetMapping("/{id}")
    public Result<OptimizeHistory> getHistoryDetail(
            @PathVariable("id") Long id,
            HttpServletRequest request) {

        try {
            // 1. 获取当前登录用户ID
            Long userId = (Long) request.getAttribute("userId");
            if (userId == null) {
                return Result.error("用户未登录");
            }

            // 2. 查询历史详情
            OptimizeHistory history = optimizeHistoryService.getHistoryDetail(id, userId);

            return Result.success("查询成功", history);

        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
}