package com.resume.airesume.controller;

import com.resume.airesume.dto.ApplicationCreateDTO;
import com.resume.airesume.dto.ApplicationUpdateDTO;
import com.resume.airesume.entity.JobApplication;
import com.resume.airesume.service.ApplicationService;
import com.resume.airesume.util.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 投递记录控制器
 * 提供投递记录的CRUD、看板数据、统计等接口
 */
@RestController
@RequestMapping("/api/applications")
public class ApplicationController {

    @Autowired
    private ApplicationService applicationService;

    @Autowired
    private JwtUtil jwtUtil;

    /**
     * 从请求中获取当前用户ID
     */
    private Long getCurrentUserId(HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
        }
        return jwtUtil.getUserIdFromToken(token);
    }

    /**
     * 创建投递记录
     * POST /api/applications
     */
    @PostMapping
    public ResponseEntity<?> createApplication(
            @Validated @RequestBody ApplicationCreateDTO dto,
            HttpServletRequest request) {
        Long userId = getCurrentUserId(request);
        JobApplication application = applicationService.createApplication(userId, dto);
        return ResponseEntity.ok(Result.success(application));
    }

    /**
     * 查询投递记录列表（分页+状态+关键词+渠道过滤）
     * GET /api/applications?status=applied&keyword=腾讯&source=boss直聘&page=1&size=10
     */
    @GetMapping
    public ResponseEntity<?> listApplications(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String source,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size,
            HttpServletRequest request) {
        Long userId = getCurrentUserId(request);
        Map<String, Object> result = applicationService.listApplications(userId, status, keyword, source, page, size);
        return ResponseEntity.ok(Result.success(result));
    }

    /**
     * 获取投递记录详情
     * GET /api/applications/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> getApplication(
            @PathVariable Long id,
            HttpServletRequest request) {
        Long userId = getCurrentUserId(request);
        JobApplication application = applicationService.getApplication(userId, id);
        return ResponseEntity.ok(Result.success(application));
    }

    /**
     * 更新投递记录
     * PUT /api/applications/{id}
     */
    @PutMapping("/{id}")
    public ResponseEntity<?> updateApplication(
            @PathVariable Long id,
            @RequestBody ApplicationUpdateDTO dto,
            HttpServletRequest request) {
        Long userId = getCurrentUserId(request);
        JobApplication application = applicationService.updateApplication(userId, id, dto);
        return ResponseEntity.ok(Result.success(application));
    }

    /**
     * 批量删除投递记录
     * DELETE /api/applications/batch
     * 请求体：{"ids": [1, 2, 3]}
     */
    @DeleteMapping("/batch")
    public ResponseEntity<?> batchDeleteApplications(
            @RequestBody Map<String, List<Long>> body,
            HttpServletRequest request) {
        Long userId = getCurrentUserId(request);
        List<Long> ids = body.get("ids");
        if (ids == null || ids.isEmpty()) {
            return ResponseEntity.badRequest().body(Result.error("请选择要删除的记录"));
        }
        applicationService.batchDeleteApplications(userId, ids);
        return ResponseEntity.ok(Result.success("批量删除成功"));
    }

    /**
     * 删除投递记录（逻辑删除）
     * DELETE /api/applications/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteApplication(
            @PathVariable Long id,
            HttpServletRequest request) {
        Long userId = getCurrentUserId(request);
        applicationService.deleteApplication(userId, id);
        return ResponseEntity.ok(Result.success("删除成功"));
    }

    /**
     * 更新投递状态
     * PUT /api/applications/{id}/status
     * 请求体：{"status": "first_interview"}
     */
    @PutMapping("/{id}/status")
    public ResponseEntity<?> updateStatus(
            @PathVariable Long id,
            @RequestBody Map<String, String> body,
            HttpServletRequest request) {
        Long userId = getCurrentUserId(request);
        String status = body.get("status");
        if (status == null || status.isEmpty()) {
            return ResponseEntity.badRequest().body(Result.error("状态不能为空"));
        }
        applicationService.updateStatus(userId, id, status);
        return ResponseEntity.ok(Result.success("状态更新成功"));
    }

    /**
     * 获取看板数据（按状态分组统计）
     * GET /api/applications/board
     */
    @GetMapping("/board")
    public ResponseEntity<?> getBoardData(HttpServletRequest request) {
        Long userId = getCurrentUserId(request);
        List<Map<String, Object>> boardData = applicationService.getBoardData(userId);
        return ResponseEntity.ok(Result.success(boardData));
    }

    /**
     * 获取统计数据
     * GET /api/applications/stats
     */
    @GetMapping("/stats")
    public ResponseEntity<?> getStats(HttpServletRequest request) {
        Long userId = getCurrentUserId(request);
        Map<String, Object> stats = applicationService.getStats(userId);
        return ResponseEntity.ok(Result.success(stats));
    }

    /**
     * 统一返回格式
     */
    static class Result {
        public int code;
        public String message;
        public Object data;

        public Result(int code, String message, Object data) {
            this.code = code;
            this.message = message;
            this.data = data;
        }

        public static Result success(Object data) {
            return new Result(200, "success", data);
        }

        public static Result error(String message) {
            return new Result(500, message, null);
        }
    }
}
