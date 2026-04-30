package com.resume.airesume.service;

import com.resume.airesume.entity.Resume;
import com.resume.airesume.mapper.ResumeMapper;
import com.resume.airesume.service.DeepSeekService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * 异步预生成服务
 * 作用：在简历上传或优化后，后台静默生成三个模板的HTML
 * 用户点预览时可以直接从缓存读取，秒返回
 *
 * 优化策略：只缓存纯HTML（不含头像），头像在请求时实时注入
 * 这样切换头像不需要清缓存和重新生成，加载速度大幅提升
 */
@Service
public class PreGenerateService {

    @Autowired
    private ResumeMapper resumeMapper;

    @Autowired
    private DeepSeekService deepSeekService;

    // 不再需要 UserMapper 和 AvatarUtil
    // 头像由 ResumeController 在返回前实时注入，预生成只管纯HTML

    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 异步预生成三个模板的HTML（纯HTML，不含头像）
     *
     * @param resumeId 简历ID
     * @param userId 用户ID
     * @param structuredData 结构化数据
     * @param type 类型：original 或 optimized
     */
    @Async
    public void preGenerateHtmlTemplates(Long resumeId, Long userId, String structuredData, String type) {
        System.out.println("[预生成] 开始为简历 " + resumeId + " 生成HTML，类型：" + type);

        for (int templateId = 1; templateId <= 3; templateId++) {
            try {
                System.out.println("[预生成] 正在生成模板 " + templateId + "...");

                // 只调用AI生成纯HTML，不注入头像
                String htmlContent = deepSeekService.generateResumeHtml(structuredData, templateId);

                if (htmlContent != null && !htmlContent.isEmpty()) {
                    saveSingleTemplate(resumeId, userId, templateId, htmlContent, type);
                    System.out.println("[预生成] 模板 " + templateId + " 生成并保存成功");
                } else {
                    System.out.println("[预生成] 模板 " + templateId + " 生成失败");
                }

            } catch (Exception e) {
                System.out.println("[预生成] 模板 " + templateId + " 生成异常：" + e.getMessage());
                e.printStackTrace();
            }
        }

        System.out.println("[预生成] 简历 " + resumeId + " 全部完成");
    }

    /**
     * 保存单个模板HTML到数据库（增量更新）
     */
    private void saveSingleTemplate(Long resumeId, Long userId, Integer templateId, String htmlContent, String type) {
        try {
            // 1. 先从数据库读取已有的HTML JSON
            Resume resume = resumeMapper.findById(resumeId);
            String existingJson = "optimized".equals(type) ? resume.getOptimizedHtml() : resume.getGeneratedHtml();

            // 2. 解析已有的Map
            Map<String, String> htmlMap = new HashMap<>();
            if (existingJson != null && !existingJson.isEmpty()) {
                try {
                    Map<String, String> parsedMap = objectMapper.readValue(existingJson, Map.class);
                    htmlMap.putAll(parsedMap);
                } catch (Exception e) {
                    // 解析失败，忽略
                }
            }

            // 3. 添加新生成的模板（纯HTML，不含头像）
            htmlMap.put(String.valueOf(templateId), htmlContent);

            // 4. 保存回数据库
            String newJson = objectMapper.writeValueAsString(htmlMap);

            if ("optimized".equals(type)) {
                resumeMapper.updateOptimizedHtml(resumeId, userId, newJson);
            } else {
                resumeMapper.updateGeneratedHtml(resumeId, userId, newJson);
            }

        } catch (Exception e) {
            System.out.println("[预生成] 保存模板" + templateId + "失败：" + e.getMessage());
        }
    }
}