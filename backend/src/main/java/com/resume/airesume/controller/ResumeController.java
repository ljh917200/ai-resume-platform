package com.resume.airesume.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.resume.airesume.dto.Result;
import com.resume.airesume.entity.Resume;
import com.resume.airesume.mapper.ResumeMapper;
import com.resume.airesume.mapper.UserMapper;
import com.resume.airesume.service.DeepSeekService;
import com.resume.airesume.service.OptimizeHistoryService;
import com.resume.airesume.service.PreGenerateService;
import com.resume.airesume.service.ResumeService;
import com.resume.airesume.util.HtmlToPdfUtil;
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

    // 注入HTML转PDF工具（v1.7.0新增）
    @Autowired
    private HtmlToPdfUtil htmlToPdfUtil;

    //注入优化历史服务
    @Autowired
    private OptimizeHistoryService optimizeHistoryService;

    @Autowired
    private PreGenerateService preGenerateService;  // 新增注入

    @Autowired
    private ResumeMapper resumeMapper;

    @Autowired
    private UserMapper userMapper;

    // 服务器基础地址（用于拼接头像完整URL）
    @org.springframework.beans.factory.annotation.Value("${server.base-url:http://localhost:8080}")
    private String serverBaseUrl;

    @Autowired
    private com.resume.airesume.util.AvatarUtil avatarUtil;


    private final ObjectMapper objectMapper = new ObjectMapper();

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

        // ========== 新增：启动异步预生成 ==========
        preGenerateService.preGenerateHtmlTemplates(
                id,
                userId,
                optimizedStructuredData,
                "optimized"
        );
        // ==========================================

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


    /**
     * 生成简历HTML（v1.8.0优化版）
     *
     * 功能说明：
     * - 调用 DeepSeek 根据结构化数据生成 XHTML 格式的简历
     * - 使用JSON存储所有模板的HTML，切换模板时缓存命中秒返回
     * - 懒加载：只有用户选择的模板才生成，节省AI调用成本
     * - ★ v1.8.0优化：缓存只存纯HTML（不含头像），头像在返回前实时注入
     *   这样切换头像不需要清缓存和重新生成，加载速度大幅提升
     *
     * @param id 简历ID
     * @param type 类型：original-原始版，optimized-优化版
     * @param templateId 模板ID
     * @param request HTTP请求对象（获取用户ID）
     * @return 包含 HTML 内容的响应
     */
    @PostMapping("/generate-html")
    public Result<Map<String, Object>> generateHtml(
            @RequestParam("id") Long id,
            @RequestParam(value = "type", defaultValue = "original") String type,
            @RequestParam(value = "templateId", required = false) Integer templateId,
            HttpServletRequest request) {

        // 1. 获取当前登录用户ID
        Long userId = (Long) request.getAttribute("userId");
        if (userId == null) {
            return Result.error("用户未登录");
        }

        try {
            // 2. 查询简历并验证归属
            Resume resume = resumeService.getById(id, userId);
            if (resume == null) {
                return Result.error("简历不存在");
            }

            // 3. 确定模板ID（没传就用简历保存的模板，默认1）
            if (templateId == null) {
                templateId = resume.getTemplateId() != null ? resume.getTemplateId() : 1;
            }

            // 4. 选择对应的结构化数据和HTML字段
            String structuredData;
            String htmlJson; // 存储所有模板HTML的JSON字符串

            if ("optimized".equals(type)) {
                // 优化版
                if (resume.getOptimizedStructuredData() == null || resume.getOptimizedStructuredData().isEmpty()) {
                    return Result.error("该简历尚未优化，无法生成优化版HTML");
                }
                structuredData = resume.getOptimizedStructuredData();
                htmlJson = resume.getOptimizedHtml();
            } else {
                // 原始版
                if (resume.getStructuredData() == null || resume.getStructuredData().isEmpty()) {
                    return Result.error("简历结构化数据为空，请重新上传");
                }
                structuredData = resume.getStructuredData();
                htmlJson = resume.getGeneratedHtml();
            }

            // 5. 解析JSON，获取所有已缓存的模板HTML
            Map<String, String> htmlMap = new HashMap<>();
            if (htmlJson != null && !htmlJson.isEmpty()) {
                try {
                    Map<String, String> parsedMap = objectMapper.readValue(htmlJson, Map.class);
                    htmlMap.putAll(parsedMap);
                } catch (Exception e) {
                    // JSON解析失败，可能是旧数据格式（单个HTML字符串）
                    if (htmlJson.trim().startsWith("<!DOCTYPE")) {
                        htmlMap.put("1", htmlJson);  // 默认存为模板1
                        System.out.println("[缓存] 检测到旧格式HTML，已迁移到模板1");
                    } else {
                        System.out.println("[缓存] HTML格式异常，将重新生成：" + e.getMessage());
                    }
                }
            }

            // 6. 检查目标模板是否已缓存
            String targetKey = String.valueOf(templateId);
            String htmlContent = htmlMap.get(targetKey);

            boolean needGenerate = (htmlContent == null || htmlContent.isEmpty());

            if (needGenerate) {
                // 需要生成：调用 DeepSeek（2个参数，不含头像）
                // ★ 缓存只存纯HTML，头像在返回前实时注入
                htmlContent = deepSeekService.generateResumeHtml(structuredData, templateId);
                if (htmlContent == null || htmlContent.isEmpty()) {
                    return Result.error("HTML生成失败，请稍后重试");
                }

                // ★ 不在这里注入头像！缓存存纯HTML
                // 更新缓存Map
                htmlMap.put(targetKey, htmlContent);

                // 转为JSON保存到数据库
                String newHtmlJson = objectMapper.writeValueAsString(htmlMap);

                // 保存到数据库
                if ("optimized".equals(type)) {
                    resumeService.updateOptimizedHtml(id, userId, newHtmlJson);
                } else {
                    resumeService.updateGeneratedHtml(id, userId, newHtmlJson);
                }

                // 更新当前选中的模板ID
                resumeService.switchTemplate(id, userId, templateId);
            }

            // 7. ★ 返回前实时注入头像（只是字符串操作，毫秒级）
            String htmlToReturn = htmlContent;
            if (resume.getShowAvatar() != null && resume.getShowAvatar() == 1) {
                // 先查询用户头像URL
                com.resume.airesume.entity.User user = userMapper.findById(userId);
                if (user != null && user.getAvatarUrl() != null && !user.getAvatarUrl().isEmpty()) {
                    htmlToReturn = avatarUtil.injectAvatar(htmlContent, user.getAvatarUrl(), structuredData);
                }
            }

            // 8. 返回结果
            Map<String, Object> data = new HashMap<>();
            data.put("id", id);
            data.put("htmlContent", htmlToReturn);  // 返回的是注入头像后的HTML
            data.put("type", type);
            data.put("templateId", templateId);
            data.put("fromCache", !needGenerate);

            return Result.success("HTML生成成功", data);

        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("生成失败：" + e.getMessage());
        }
    }

    /**
     * 从HTML导出PDF（v1.8.0优化版）
     * ★ 优化：从缓存读取纯HTML，导出前实时注入头像，再转PDF
     */
    @PostMapping("/export-from-html")
    public void exportFromHtml(
            @RequestParam("id") Long id,
            @RequestParam(value = "type", defaultValue = "original") String type,
            @RequestParam(value = "templateId", required = false) Integer templateId,
            HttpServletRequest request,
            HttpServletResponse response) {

        // 1. 获取当前登录用户ID
        Long userId = (Long) request.getAttribute("userId");
        if (userId == null) {
            try {
                response.setContentType("application/json;charset=UTF-8");
                response.getWriter().write("{\"code\":401,\"message\":\"用户未登录\"}");
            } catch (IOException e) {
                e.printStackTrace();
            }
            return;
        }

        try {
            // 2. 查询简历并验证归属
            Resume resume = resumeService.getById(id, userId);
            if (resume == null) {
                response.setContentType("application/json;charset=UTF-8");
                response.getWriter().write("{\"code\":404,\"message\":\"简历不存在\"}");
                return;
            }

            // 3. 确定模板ID
            if (templateId == null) {
                templateId = resume.getTemplateId() != null ? resume.getTemplateId() : 1;
            }

            // 4. 获取对应的 HTML JSON（纯HTML缓存，不含头像）
            String htmlJson;
            String fileTitle;

            if ("optimized".equals(type)) {
                htmlJson = resume.getOptimizedHtml();
                fileTitle = "简历_优化版";
                if (htmlJson == null || htmlJson.isEmpty()) {
                    response.setContentType("application/json;charset=UTF-8");
                    response.getWriter().write("{\"code\":400,\"message\":\"请先生成优化版HTML\"}");
                    return;
                }
            } else {
                htmlJson = resume.getGeneratedHtml();
                fileTitle = "简历_原始版";
                if (htmlJson == null || htmlJson.isEmpty()) {
                    response.setContentType("application/json;charset=UTF-8");
                    response.getWriter().write("{\"code\":400,\"message\":\"请先生成原始版HTML\"}");
                    return;
                }
            }

            // 5. 从JSON中提取目标模板的纯HTML
            String htmlContent;
            try {
                Map<String, String> htmlMap = objectMapper.readValue(htmlJson, Map.class);
                htmlContent = htmlMap.get(String.valueOf(templateId));
                if (htmlContent == null || htmlContent.isEmpty()) {
                    response.setContentType("application/json;charset=UTF-8");
                    response.getWriter().write("{\"code\":400,\"message\":\"该模板HTML未生成，请先在预览页面选择该模板\"}");
                    return;
                }
            } catch (Exception e) {
                // 旧数据格式，直接使用
                htmlContent = htmlJson;
            }

            // 6. ★ 导出前实时注入头像（不影响缓存，只是字符串操作）
            if (resume.getShowAvatar() != null && resume.getShowAvatar() == 1) {
                // 先查询用户头像URL
                com.resume.airesume.entity.User user = userMapper.findById(userId);
                if (user != null && user.getAvatarUrl() != null && !user.getAvatarUrl().isEmpty()) {
                    String structuredData = "optimized".equals(type)
                            ? resume.getOptimizedStructuredData()
                            : resume.getStructuredData();
                    htmlContent = avatarUtil.injectAvatar(htmlContent, user.getAvatarUrl(), structuredData);
                }
            }

            // 7. 使用 Flying Saucer 将 HTML 转换为 PDF
            byte[] pdfBytes = htmlToPdfUtil.convertToPdf(htmlContent);

            // 8. 生成文件名
            String date = java.time.LocalDate.now().toString().replace("-", "");
            String templateName = getTemplateName(templateId);
            String fileName = fileTitle + "_" + templateName + "_" + date + ".pdf";

            // 9. 设置响应头
            response.setContentType("application/pdf");
            response.setCharacterEncoding("UTF-8");
            response.setHeader("Content-Disposition", "attachment; filename=" + new String(fileName.getBytes(StandardCharsets.UTF_8), StandardCharsets.ISO_8859_1));
            response.setContentLength(pdfBytes.length);

            // 10. 将 PDF 写入响应流
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
     * 根据模板ID获取模板名称
     */
    private String getTemplateName(Integer templateId) {
        if (templateId == null) return "简约蓝";
        switch (templateId) {
            case 2: return "商务灰";
            case 3: return "创意橙";
            default: return "简约蓝";
        }
    }

    /**
     * 切换简历是否显示头像（v1.8.0优化版）
     * ★ 优化策略：只更新数据库字段，不需要清缓存和重新预生成
     * 因为缓存存的是纯HTML（不含头像），头像在请求时实时注入
     *
     * @param id 简历ID
     * @param params 包含 showAvatar 字段（0-不显示 1-显示）
     * @param request HTTP请求对象
     * @return 设置结果
     */
    @PutMapping("/{id}/show-avatar")
    public Result<String> toggleShowAvatar(
            @PathVariable Long id,
            @RequestBody Map<String, Integer> params,
            HttpServletRequest request) {
        try {
            // 1. 获取当前登录用户ID
            Long userId = (Long) request.getAttribute("userId");
            if (userId == null) {
                return Result.error("用户未登录");
            }

            // 2. 获取参数
            Integer showAvatar = params.get("showAvatar");
            if (showAvatar == null || (showAvatar != 0 && showAvatar != 1)) {
                return Result.error("参数错误，showAvatar只能为0或1");
            }

            // 3. 验证简历归属
            Resume resume = resumeService.getById(id, userId);
            if (resume == null) {
                return Result.error("简历不存在");
            }

            // 4. 只更新数据库字段（不需要清缓存，不需要重新预生成）
            resumeMapper.updateShowAvatar(id, showAvatar);

            return Result.success("设置成功", null);
        } catch (Exception e) {
            return Result.error("设置失败: " + e.getMessage());
        }
    }


}