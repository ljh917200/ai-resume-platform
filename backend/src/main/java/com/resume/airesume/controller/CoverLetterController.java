package com.resume.airesume.controller;

import com.resume.airesume.dto.CoverLetterCreateDTO;
import com.resume.airesume.dto.CoverLetterVO;
import com.resume.airesume.dto.Result;
import com.resume.airesume.service.CoverLetterService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * AI求职信控制器
 * 提供求职信生成、查询、重新生成、保存、删除等接口
 */
@RestController
@RequestMapping("/api/cover-letter")
public class CoverLetterController {

    @Autowired
    private CoverLetterService coverLetterService;

    /**
     * 生成求职信
     * @param dto     生成请求（简历ID、岗位信息、风格、语言）
     * @param request 用于获取当前登录用户ID
     */
    @PostMapping("/generate")
    public Result<CoverLetterVO> generate(@RequestBody CoverLetterCreateDTO dto, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        if (userId == null) {
            return Result.error("用户未登录");
        }
        CoverLetterVO result = coverLetterService.generateLetter(userId, dto);
        return Result.success(result);
    }

    /**
     * 查询单条求职信详情
     * @param id      记录ID
     * @param request 用于获取当前登录用户ID（权限校验）
     */
    @GetMapping("/{id}")
    public Result<CoverLetterVO> getLetter(@PathVariable Long id, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        if (userId == null) {
            return Result.error("用户未登录");
        }
        CoverLetterVO result = coverLetterService.getLetter(id, userId);
        return Result.success(result);
    }

    /**
     * 查询当前用户的求职信历史列表
     * @param request 用于获取当前登录用户ID
     */
    @GetMapping("/history")
    public Result<List<CoverLetterVO>> history(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        if (userId == null) {
            return Result.error("用户未登录");
        }
        List<CoverLetterVO> list = coverLetterService.getHistory(userId);
        return Result.success(list);
    }

    /**
     * 重新生成求职信（换风格或换语言）
     * @param id      记录ID
     * @param style   新风格（可选，不传则保持原风格）
     * @param lang    新语言（可选，不传则保持原语言）
     * @param request 用于获取当前登录用户ID（权限校验）
     */
    @PostMapping("/{id}/regenerate")
    public Result<CoverLetterVO> regenerate(
            @PathVariable Long id,
            @RequestParam(required = false) String style,
            @RequestParam(required = false) String lang,
            HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        if (userId == null) {
            return Result.error("用户未登录");
        }
        CoverLetterVO result = coverLetterService.regenerateLetter(id, userId, style, lang);
        return Result.success(result);
    }

    /**
     * 保存/取消保存求职信
     * @param id      记录ID
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
        coverLetterService.toggleSave(id, userId, isSaved);
        return Result.success(null);
    }

    /**
     * 删除求职信（逻辑删除）
     * @param id      记录ID
     * @param request 用于获取当前登录用户ID（权限校验）
     */
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        if (userId == null) {
            return Result.error("用户未登录");
        }
        coverLetterService.deleteLetter(id, userId);
        return Result.success(null);
    }
}
