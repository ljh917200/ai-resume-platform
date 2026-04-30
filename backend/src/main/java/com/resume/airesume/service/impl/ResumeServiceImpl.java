package com.resume.airesume.service.impl;

import com.resume.airesume.entity.Resume;
import com.resume.airesume.mapper.ResumeMapper;
import com.resume.airesume.service.DeepSeekService;
import com.resume.airesume.service.FileParseService;
import com.resume.airesume.service.PreGenerateService;
import com.resume.airesume.service.ResumeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class ResumeServiceImpl implements ResumeService {

    @Autowired
    private ResumeMapper resumeMapper;

    @Autowired
    private FileParseService fileParseService;  // 注入文件解析服务

    @Autowired
    private DeepSeekService deepSeekService;

    @Autowired
    private PreGenerateService preGenerateService;  // 新增注入

    @Override
    public Resume upload(Long userId, MultipartFile file) {
        try {
            // ============================================
            // 第一步：解析文件，提取文字
            // ============================================
            String originalText = fileParseService.parseFile(file);

            // 获取文件名和格式
            String fileName = file.getOriginalFilename();
            String fileFormat = fileName.substring(fileName.lastIndexOf(".") + 1).toUpperCase();

            // ============================================
            // 第二步：调用AI进行结构化提取（新增）
            // ============================================
            String structuredData = null;
            try {
                structuredData = deepSeekService.structureResume(originalText);
            } catch (Exception e) {
                // 结构化失败不影响主流程，记录日志即可
                System.err.println("简历结构化失败，将使用空值：" + e.getMessage());
            }

            // ============================================
            // 第三步：创建简历对象并保存
            // ============================================
            Resume resume = new Resume();
            resume.setUserId(userId);
            resume.setOriginalText(originalText);
            resume.setStructuredData(structuredData);  // 存储结构化数据
            resume.setFileName(fileName);
            resume.setFileFormat(fileFormat);
            resume.setCreatedAt(LocalDateTime.now());

            // 保存到数据库
            resumeMapper.insert(resume);

            // ========== 新增：启动异步预生成 ==========
            preGenerateService.preGenerateHtmlTemplates(
                    resume.getId(),
                    userId,
                    structuredData,
                    "original"
            );
            // ==========================================

            return resume;

        } catch (Exception e) {
            throw new RuntimeException("文件解析失败：" + e.getMessage());
        }
    }

    @Override
    public List<Resume> getListByUserId(Long userId) {
        return resumeMapper.findByUserId(userId);
    }

    @Override
    public Resume getById(Long id, Long userId) {
        Resume resume = resumeMapper.findById(id);
        // 验证是否是该用户的简历
        if (resume == null) {
            throw new RuntimeException("简历不存在");
        }
        if (!resume.getUserId().equals(userId)) {
            throw new RuntimeException("无权访问此简历");
        }
        return resume;
    }

    @Override
    public boolean delete(Long id, Long userId) {
        // 先验证是否是该用户的简历
        Resume resume = resumeMapper.findById(id);
        if (resume == null) {
            throw new RuntimeException("简历不存在");
        }
        if (!resume.getUserId().equals(userId)) {
            throw new RuntimeException("无权删除此简历");
        }
        return resumeMapper.deleteById(id) > 0;
    }


    // 更新优化内容
    public void updateOptimizedText(Long resumeId, String optimizedText,String optimizedStructuredData) {
        Resume resume = new Resume();
        resume.setId(resumeId);
        resume.setOptimizedText(optimizedText);
        resume.setOptimizedStructuredData(optimizedStructuredData);  // 新增
        resume.setLastOptimizedAt(LocalDateTime.now());
        resumeMapper.updateOptimizedText(resume);
    }


    @Override
    public boolean renameResume(Long id, Long userId, String displayName) {
        // 验证简历是否存在且属于该用户
        Resume resume = resumeMapper.findById(id);
        if (resume == null) {
            throw new RuntimeException("简历不存在");
        }
        if (!resume.getUserId().equals(userId)) {
            throw new RuntimeException("无权修改此简历");
        }

        // 更新显示名称
        return resumeMapper.updateDisplayName(id, userId, displayName) > 0;
    }

    @Override
    public int batchDelete(List<Long> ids, Long userId) {
        if (ids == null || ids.isEmpty()) {
            throw new RuntimeException("请选择要删除的简历");
        }

        // 批量删除（SQL中会验证用户归属）
        return resumeMapper.batchDelete(ids, userId);
    }

    @Override
    public boolean switchTemplate(Long id, Long userId, Integer templateId) {
        // 验证模板ID
        if (templateId < 1 || templateId > 3) {
            throw new RuntimeException("模板ID无效，请选择1-3");
        }

        // 验证简历是否存在且属于该用户
        Resume resume = resumeMapper.findById(id);
        if (resume == null) {
            throw new RuntimeException("简历不存在");
        }
        if (!resume.getUserId().equals(userId)) {
            throw new RuntimeException("无权修改此简历");
        }

        return resumeMapper.updateTemplateId(id, userId, templateId) > 0;
    }

    /**
     * 更新原始简历HTML（v1.7.0新增）
     *
     * 实现说明：
     * - 直接调用 Mapper 的 updateGeneratedHtml 方法
     * - 参数校验在 Controller 层已做，这里只执行存储
     *
     * @param resumeId 简历ID
     * @param userId 用户ID
     * @param generatedHtml 原始简历HTML内容
     */
    @Override
    public void updateGeneratedHtml(Long resumeId, Long userId, String generatedHtml) {
        resumeMapper.updateGeneratedHtml(resumeId, userId, generatedHtml);
    }

    /**
     * 更新优化后简历HTML（v1.7.0新增）
     *
     * 实现说明：
     * - 直接调用 Mapper 的 updateOptimizedHtml 方法
     * - 参数校验在 Controller 层已做，这里只执行存储
     *
     * @param resumeId 简历ID
     * @param userId 用户ID
     * @param optimizedHtml 优化后简历HTML内容
     */
    @Override
    public void updateOptimizedHtml(Long resumeId, Long userId, String optimizedHtml) {
        resumeMapper.updateOptimizedHtml(resumeId, userId, optimizedHtml);
    }

    /**
     * 更新HTML内容和模板ID
     */
    @Override
    public void updateHtmlAndTemplate(Long id, Long userId, String htmlField, String htmlContent, Integer templateId) {
        resumeMapper.updateHtmlAndTemplate(id, userId, htmlField, htmlContent, templateId);
    }



}