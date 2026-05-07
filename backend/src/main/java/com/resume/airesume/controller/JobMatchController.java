package com.resume.airesume.controller;

import com.resume.airesume.dto.JobMatchAnalyzeDTO;
import com.resume.airesume.dto.JobMatchResultVO;
import com.resume.airesume.dto.Result;
import com.resume.airesume.service.JobMatchService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * AI岗位匹配分析控制器
 */
@RestController
@RequestMapping("/api/job-match")
public class JobMatchController {

    @Autowired
    private JobMatchService jobMatchService;

    /**
     * 发起岗位匹配分析
     */
    @PostMapping("/analyze")
    public Result<JobMatchResultVO> analyze(@RequestBody JobMatchAnalyzeDTO dto, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        if (userId == null) {
            return Result.error("用户未登录");
        }
        JobMatchResultVO result = jobMatchService.analyzeMatch(userId, dto);
        return Result.success(result);
    }

    /**
     * 查询单条分析结果
     */
    @GetMapping("/result/{id}")
    public Result<JobMatchResultVO> getResult(@PathVariable Long id, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        if (userId == null) {
            return Result.error("用户未登录");
        }
        JobMatchResultVO result = jobMatchService.getResult(id, userId);
        return Result.success(result);
    }

    /**
     * 查询当前用户的匹配分析历史
     */
    @GetMapping("/history")
    public Result<List<JobMatchResultVO>> history(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        if (userId == null) {
            return Result.error("用户未登录");
        }
        List<JobMatchResultVO> list = jobMatchService.getHistory(userId);
        return Result.success(list);
    }

    /**
     * 保存/取消保存分析记录
     */
    @PutMapping("/{id}/save")
    public Result<Void> toggleSave(@PathVariable Long id, @RequestParam Integer isSaved, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        if (userId == null) {
            return Result.error("用户未登录");
        }
        jobMatchService.toggleSave(id, userId, isSaved);
        return Result.success(null);
    }

    /**
     * 删除分析记录
     */
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        if (userId == null) {
            return Result.error("用户未登录");
        }
        jobMatchService.deleteAnalysis(id, userId);
        return Result.success(null);
    }
}
