package com.resume.airesume.controller;

import com.resume.airesume.dto.Result;
import com.resume.airesume.service.DeepSeekService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * AI 优化接口
 */
@RestController
@RequestMapping("/api/ai")
public class AiController {

    @Autowired
    private DeepSeekService deepSeekService;

    /**
     * 文本优化接口
     *
     * @param request    HTTP 请求（用于获取当前用户）
     * @param text       需要优化的文本
     * @param targetRole 目标岗位（可选）
     * @return 优化后的文本
     */
    @PostMapping("/optimize")
    public Result<Map<String, Object>> optimizeText(
            HttpServletRequest request,
            @RequestParam("text") String text,
            @RequestParam(value = "targetRole", required = false) String targetRole) {

        // 从拦截器获取当前登录用户（可选：记录谁使用了 AI 功能）
        Long userId = (Long) request.getAttribute("userId");

        // 调用 AI 服务优化文本
        String optimizedText = deepSeekService.optimizeResume(text, targetRole);

        // 构建返回结果
        Map<String, Object> data = new HashMap<>();
        data.put("originalText", text);
        data.put("optimizedText", optimizedText);
        if (targetRole != null) {
            data.put("targetRole", targetRole);
        }

        return Result.success("优化成功", data);
    }
}