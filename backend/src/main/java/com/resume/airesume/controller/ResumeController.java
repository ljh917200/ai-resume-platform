package com.resume.airesume.controller;

import com.resume.airesume.dto.Result;
import com.resume.airesume.entity.Resume;
import com.resume.airesume.service.DeepSeekService;
import com.resume.airesume.service.OptimizeHistoryService;
import com.resume.airesume.service.ResumeService;
import com.resume.airesume.util.PdfExportUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

/**
 * 简历管理控制器
 * 处理简历的上传、查询、删除等操作
 */
@RestController
@RequestMapping("/api/resume")
public class ResumeController {

    @Autowired
    private ResumeService resumeService;

    @Autowired
    private DeepSeekService deepSeekService;

    @Autowired
    private PdfExportUtil pdfExportUtil;

    //注入优化历史服务
    @Autowired
    private OptimizeHistoryService optimizeHistoryService;

    /**
     * 上传简历
     *
     * @param file 简历文件（PDF/DOCX格式）
     * @param request HTTP请求对象（用于从拦截器获取用户信息）
     * @return 上传成功后返回简历信息
     */
    @PostMapping("/upload")
    public Result<Map<String, Object>> upload(
            @RequestParam("file") MultipartFile file,      // 接收上传的文件
            HttpServletRequest request) {                  // 注入 request，从拦截器获取用户信息

        try {
            // 1. 从拦截器存入的数据中获取当前登录用户ID
            Long userId = (Long) request.getAttribute("userId");
            if (userId == null) {
                return Result.error("用户未登录");
            }

            // 2. 校验文件是否为空
            if (file.isEmpty()) {
                return Result.error("请选择要上传的文件");
            }

            // 3. 校验文件格式（目前支持PDF和DOCX）
            String fileName = file.getOriginalFilename();
            if (fileName == null) {
                return Result.error("文件名不能为空");
            }

            String fileFormat = fileName.substring(fileName.lastIndexOf(".") + 1).toLowerCase();
            if (!fileFormat.equals("pdf") && !fileFormat.equals("docx") && !fileFormat.equals("doc")) {
                return Result.error("仅支持PDF和Word文档格式");
            }

            // 4. 校验文件大小（限制5MB）
            long maxSize = 5 * 1024 * 1024; // 5MB
            if (file.getSize() > maxSize) {
                return Result.error("文件大小不能超过5MB");
            }

            // 5. 调用Service上传简历
            Resume resume = resumeService.upload(userId, file);

            // 6. 构建返回数据（不返回完整文本，只返回基本信息）
            Map<String, Object> data = new HashMap<>();
            data.put("id", resume.getId());
            data.put("fileName", resume.getFileName());
            data.put("fileFormat", resume.getFileFormat());
            data.put("createdAt", resume.getCreatedAt());

            return Result.success("上传成功", data);

        } catch (Exception e) {
            e.printStackTrace();  // 后端记录详细日志
            return Result.error("上传失败，请检查文件格式后重试");
        }
    }

    /**
     * 获取当前用户的简历列表
     *
     * @param request HTTP请求对象（用于获取当前用户ID）
     * @return 简历列表
     */
    @GetMapping("/list")
    public Result<List<Resume>> list(HttpServletRequest request) {
        try {
            // 从拦截器获取当前用户ID
            Long userId = (Long) request.getAttribute("userId");
            if (userId == null) {
                return Result.error("用户未登录");
            }

            // 查询该用户的所有简历
            List<Resume> resumes = resumeService.getListByUserId(userId);
            return Result.success("查询成功", resumes);
        } catch (Exception e) {
            return Result.error("查询失败: " + e.getMessage());
        }
    }

    /**
     * 获取简历详情
     *
     * @param id 简历ID
     * @param request HTTP请求对象（用于获取当前用户ID，验证权限）
     * @return 简历详细信息
     */
    @GetMapping("/{id}")
    public Result<Resume> detail(
            @PathVariable("id") Long id,              // 从URL路径获取简历ID
            HttpServletRequest request) {             // 从拦截器获取用户ID

        try {
            // 从拦截器获取当前用户ID
            Long userId = (Long) request.getAttribute("userId");
            if (userId == null) {
                return Result.error("用户未登录");
            }

            // 查询简历详情（会验证是否属于该用户）
            Resume resume = resumeService.getById(id, userId);
            return Result.success("查询成功", resume);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 删除简历
     *
     * @param id 简历ID
     * @param request HTTP请求对象（用于获取当前用户ID，验证权限）
     * @return 删除结果
     */
    @DeleteMapping("/{id}")
    public Result<String> delete(
            @PathVariable("id") Long id,              // 从URL路径获取简历ID
            HttpServletRequest request) {             // 从拦截器获取用户ID

        try {
            // 从拦截器获取当前用户ID
            Long userId = (Long) request.getAttribute("userId");
            if (userId == null) {
                return Result.error("用户未登录");
            }

            // 删除简历（会验证是否属于该用户）
            boolean success = resumeService.delete(id, userId);
            if (success) {
                return Result.success("删除成功", null);
            } else {
                return Result.error("删除失败");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("删除失败，请稍后重试");
        }
    }

    /**
     * 优化指定简历
     * 用户上传简历后，直接点击优化，无需手动复制文字
     *
     * @param id         简历ID
     * @param targetRole 目标岗位（可选）
     * @param request    HTTP请求（获取当前用户）
     */
    @PostMapping("/optimize/{id}")
    public Result<Map<String, Object>> optimizeResume(
            @PathVariable("id") Long id,
            @RequestParam(value = "targetRole", required = false) String targetRole,
            HttpServletRequest request) {

        // 1. 获取当前登录用户ID
        Long userId = (Long) request.getAttribute("userId");

        // 2. 查询简历（同时验证归属）
        Resume resume = resumeService.getById(id, userId);

        // 验证简历是否存在（getById 已经验证了用户归属，这里只需检查是否为空）
        if (resume == null) {
            return Result.error("简历不存在或无权访问");
        }

        // 3. 检查简历内容是否为空
        if (resume.getOriginalText() == null || resume.getOriginalText().trim().isEmpty()) {
            return Result.error("简历内容为空，无法优化");
        }

        // 4. 调用 AI 优化纯文本
        String optimizedText = deepSeekService.optimizeResume(resume.getOriginalText(), targetRole);

        // 5. ★ 对优化后的文本重新做结构化提取（关键补充）
        String optimizedStructuredData = null;
        try {
            optimizedStructuredData = deepSeekService.structureResume(optimizedText);
        } catch (Exception e) {
            System.err.println("优化版结构化失败：" + e.getMessage());
        }

        // 6. 保存优化内容到数据库
        resumeService.updateOptimizedText(id, optimizedText,optimizedStructuredData);

        // ★ 新增：保存优化历史记录
        try {
            optimizeHistoryService.saveHistory(
                    id,
                    userId,
                    targetRole,
                    resume.getOriginalText(),           // 优化前原始文本
                    optimizedText,                      // 优化后文本
                    resume.getStructuredData(),         // 优化前结构化数据
                    optimizedStructuredData             // 优化后结构化数据
            );
        } catch (Exception e) {
            // 保存历史失败不影响主流程，记录日志即可
            System.err.println("保存优化历史失败：" + e.getMessage());
        }

        // 7. 构建返回结果
        Map<String, Object> data = new HashMap<>();
        data.put("resumeId", id);
        data.put("fileName", resume.getFileName());
        data.put("originalText", resume.getOriginalText());
        data.put("optimizedText", optimizedText);
        data.put("optimizedStructuredData", optimizedStructuredData);

        if (targetRole != null) {
            data.put("targetRole", targetRole);
        }
        return Result.success("优化成功", data);
    }

    /**
     * 导出简历PDF
     *
     * @param id         简历ID
     * @param type       导出类型：original（原始内容）/ optimized（优化后内容）
     * @param templateId 模板ID：1-简约蓝 2-商务灰 3-创意橙（可选，不传则用简历保存的模板）
     * @param request    HTTP请求对象
     * @param response   HTTP响应对象
     */
    @GetMapping("/export/{id}")
    public void exportResume(
            @PathVariable("id") Long id,
            @RequestParam(value = "type", defaultValue = "optimized") String type,
            @RequestParam(value = "templateId", required = false) Integer templateId,
            HttpServletRequest request,
            HttpServletResponse response) {
        try {
            // 1. 获取当前登录用户ID
            Long userId = (Long) request.getAttribute("userId");
            if (userId == null) {
                response.setContentType("application/json;charset=UTF-8");
                response.getWriter().write("{\"code\":401,\"message\":\"用户未登录\"}");
                return;
            }

            // 2. 查询简历（同时验证归属）
            Resume resume = resumeService.getById(id, userId);
            if (resume == null) {
                response.setContentType("application/json;charset=UTF-8");
                response.getWriter().write("{\"code\":404,\"message\":\"简历不存在或无权访问\"}");
                return;
            }

            // 3. 如果没传templateId，使用简历保存的模板（默认为1）
            if (templateId == null) {
                templateId = resume.getTemplateId() != null ? resume.getTemplateId() : 1;
            }

            // 4. 根据类型选择内容
            String content;
            String structuredDataForExport;
            String title;
            if ("original".equals(type)) {
                content = resume.getOriginalText();
                structuredDataForExport = resume.getStructuredData();
                title = "简历（原始版本）";
            } else {
                content = resume.getOptimizedText();
                structuredDataForExport = resume.getOptimizedStructuredData();
                if (content == null || content.isEmpty()) {
                    response.setContentType("application/json;charset=UTF-8");
                    response.getWriter().write("{\"code\":400,\"message\":\"该简历尚未优化\"}");
                    return;
                }
                title = "简历（优化版本）";
            }

            // 5. 生成PDF（传入三个参数：结构化数据、原始文本、模板ID）
            byte[] pdfBytes = pdfExportUtil.generatePdfFromStructuredData(structuredDataForExport, content, templateId);

            // 6. 生成文件名
            String date = java.time.LocalDate.now().toString().replace("-", "");
            String fileName = "简历_" + date + ".pdf";

            // 7. 设置响应头
            response.setContentType("application/pdf");
            response.setCharacterEncoding("UTF-8");
            response.setHeader("Content-Disposition", "attachment; filename=" +
                    new String(fileName.getBytes(StandardCharsets.UTF_8), StandardCharsets.ISO_8859_1));
            response.setContentLength(pdfBytes.length);

            // 8. 写入响应流
            try (java.io.OutputStream outputStream = response.getOutputStream()) {
                outputStream.write(pdfBytes);
                outputStream.flush();
            }

        } catch (Exception e) {
            e.printStackTrace();
            try {
                response.setContentType("application/json;charset=UTF-8");
                response.getWriter().write("{\"code\":500,\"message\":\"导出失败: " + e.getMessage() + "\"}");
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
        }
    }

    /**
     * 重命名简历
     *
     * @param id 简历ID
     * @param requestBody 请求体，包含新名称
     * @param request HTTP请求对象（获取当前用户ID）
     * @return 操作结果
     */
    @PutMapping("/rename/{id}")
    public Result<String> renameResume(
            @PathVariable("id") Long id,
            @RequestBody Map<String, String> requestBody,
            HttpServletRequest request) {

        try {
            // 1. 获取当前登录用户ID
            Long userId = (Long) request.getAttribute("userId");
            if (userId == null) {
                return Result.error("用户未登录");
            }

            // 2. 获取新名称
            String displayName = requestBody.get("displayName");
            if (displayName == null || displayName.trim().isEmpty()) {
                return Result.error("简历名称不能为空");
            }

            // 3. 限制名称长度
            if (displayName.length() > 50) {
                return Result.error("简历名称不能超过50个字符");
            }

            // 4. 调用服务层重命名
            boolean success = resumeService.renameResume(id, userId, displayName.trim());
            if (success) {
                return Result.success("重命名成功", null);
            } else {
                return Result.error("重命名失败");
            }

        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 批量删除简历
     *
     * @param requestBody 请求体，包含简历ID列表
     * @param request HTTP请求对象（获取当前用户ID）
     * @return 操作结果
     */
    @DeleteMapping("/batch")
    public Result<Map<String, Object>> batchDelete(
            @RequestBody Map<String, List<Long>> requestBody,
            HttpServletRequest request) {

        try {
            // 1. 获取当前登录用户ID
            Long userId = (Long) request.getAttribute("userId");
            if (userId == null) {
                return Result.error("用户未登录");
            }

            // 2. 获取简历ID列表
            List<Long> ids = requestBody.get("ids");
            if (ids == null || ids.isEmpty()) {
                return Result.error("请选择要删除的简历");
            }

            // 3. 调用服务层批量删除
            int deletedCount = resumeService.batchDelete(ids, userId);

            // 4. 返回删除数量
            Map<String, Object> data = new HashMap<>();
            data.put("deletedCount", deletedCount);
            return Result.success("成功删除 " + deletedCount + " 份简历", data);

        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 切换简历模板
     */
    @PutMapping("/{id}/template")
    public Result switchTemplate(
            @PathVariable Long id,
            @RequestParam Integer templateId,
            HttpServletRequest request) {

        // 1. 获取当前登录用户ID
        Long userId = (Long) request.getAttribute("userId");
        if (userId == null) {
            return Result.error("用户未登录");
        }

        try {
            boolean success = resumeService.switchTemplate(id, userId, templateId);
            if (success) {
                return Result.success("模板切换成功");
            } else {
                return Result.error("模板切换失败");
            }
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }


}