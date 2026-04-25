package com.resume.airesume.controller;

import com.resume.airesume.dto.Result;
import com.resume.airesume.entity.Resume;
import com.resume.airesume.service.DeepSeekService;
import com.resume.airesume.service.ResumeService;
import com.resume.airesume.util.PdfExportUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

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
            return Result.error("上传失败: " + e.getMessage());
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
            return Result.error(e.getMessage());
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

        // 4. 调用 AI 优化
        String optimizedText = deepSeekService.optimizeResume(resume.getOriginalText(), targetRole);

        // 5. 保存优化内容到数据库
        resumeService.updateOptimizedText(id, optimizedText);

        // 6. 构建返回结果
        Map<String, Object> data = new HashMap<>();
        data.put("resumeId", id);
        data.put("fileName", resume.getFileName());
        data.put("originalText", resume.getOriginalText());
        data.put("optimizedText", optimizedText);
        if (targetRole != null) {
            data.put("targetRole", targetRole);
        }

        return Result.success("优化成功", data);
    }

    /**
     * 导出简历PDF
     *
     * @param id      简历ID
     * @param type    导出类型：original（原始内容）/ optimized（优化后内容）
     * @param request HTTP请求对象
     * @param response HTTP响应对象
     */
    @GetMapping("/export/{id}")
    public void exportResume(
            @PathVariable("id") Long id,
            @RequestParam(value = "type", defaultValue = "optimized") String type,
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

            // 3. 根据类型选择内容
            String content;
            String title;
            if ("original".equals(type)) {
                content = resume.getOriginalText();
                title = "简历（原始版本）";
            } else {
                content = resume.getOptimizedText();
                if (content == null || content.isEmpty()) {
                    response.setContentType("application/json;charset=UTF-8");
                    response.getWriter().write("{\"code\":400,\"message\":\"该简历尚未优化\"}");
                    return;
                }
                title = "简历（优化版本）";
            }

            // 4. 生成PDF
            byte[] pdfBytes = PdfExportUtil.generateResumePdf(content, title);

            // 5. 生成文件名
            String date = java.time.LocalDate.now().toString().replace("-", "");
            String fileName = "简历_" + date + ".pdf";

            // 6. 设置响应头
            response.setContentType("application/pdf");
            response.setHeader("Content-Disposition", "attachment; filename=" +
                    java.net.URLEncoder.encode(fileName, "UTF-8"));
            response.setContentLength(pdfBytes.length);

            // 7. 写入响应流
            response.getOutputStream().write(pdfBytes);
            response.getOutputStream().flush();

        } catch (Exception e) {
            e.printStackTrace();
            try {
                response.setContentType("application/json;charset=UTF-8");
                response.getWriter().write("{\"code\":500,\"message\":\"导出失败: " + e.getMessage() + "\"}");
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

}