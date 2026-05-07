package com.resume.airesume.service.impl;

import com.resume.airesume.dto.JobMatchAnalyzeDTO;
import com.resume.airesume.dto.JobMatchResultVO;
import com.resume.airesume.entity.JobMatchAnalysis;
import com.resume.airesume.entity.Resume;
import com.resume.airesume.mapper.JobMatchAnalysisMapper;
import com.resume.airesume.mapper.ResumeMapper;
import com.resume.airesume.service.JobMatchService;
import com.resume.airesume.service.DeepSeekService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * 岗位匹配分析服务实现
 */
@Service
public class JobMatchServiceImpl implements JobMatchService {

    @Autowired
    private JobMatchAnalysisMapper jobMatchMapper;

    @Autowired
    private ResumeMapper resumeMapper;

    @Autowired
    private DeepSeekService deepSeekService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public JobMatchResultVO analyzeMatch(Long userId, JobMatchAnalyzeDTO dto) {
        // 1. 获取简历内容
        String resumeContent = getResumeContent(dto.getResumeId(), userId);

        // 2. 获取投递记录中的JD（如果关联了投递记录）
        if (dto.getApplicationId() != null && dto.getJobDescription() == null) {
            // 这里暂时不实现，后续投递模块有JD字段时再补
        }

        // 3. 构建Prompt并调用DeepSeek
        String prompt = buildAnalyzePrompt(resumeContent, dto.getJobDescription(), dto.getJobRequirement());
        String systemPrompt = "你是一位资深的HR分析师，擅长评估简历与岗位的匹配度。请严格按照要求的JSON格式返回分析结果。";
        String aiResponse = deepSeekService.chat(systemPrompt, prompt, 0.3);

        // 4. 解析AI返回的JSON
        Map<String, Object> result = parseAiResponse(aiResponse);

        // 5. 构建实体并保存
        JobMatchAnalysis analysis = new JobMatchAnalysis();
        analysis.setUserId(userId);
        analysis.setResumeId(dto.getResumeId());
        analysis.setApplicationId(dto.getApplicationId());
        analysis.setJobTitle(dto.getJobTitle());
        analysis.setCompanyName(dto.getCompanyName());
        analysis.setJobDescription(dto.getJobDescription());
        analysis.setJobRequirement(dto.getJobRequirement());
        analysis.setAnalysisResult(aiResponse);
        analysis.setIsSaved(dto.getIsSaved() != null ? dto.getIsSaved() : 0);

        // 填充分析结果
        fillAnalysisResult(analysis, result);

        jobMatchMapper.insert(analysis);

        // 6. 转VO返回
        return convertToVO(analysis);
    }

    @Override
    public JobMatchResultVO getResult(Long id, Long userId) {
        JobMatchAnalysis analysis = jobMatchMapper.selectById(id);
        if (analysis == null || !analysis.getUserId().equals(userId)) {
            throw new RuntimeException("分析记录不存在或无权访问");
        }
        return convertToVO(analysis);
    }

    @Override
    public List<JobMatchResultVO> getHistory(Long userId) {
        List<JobMatchAnalysis> list = jobMatchMapper.selectByUserId(userId);
        List<JobMatchResultVO> voList = new ArrayList<>();
        for (JobMatchAnalysis a : list) {
            voList.add(convertToVO(a));
        }
        return voList;
    }

    @Override
    public void toggleSave(Long id, Long userId, Integer isSaved) {
        JobMatchAnalysis analysis = jobMatchMapper.selectById(id);
        if (analysis == null || !analysis.getUserId().equals(userId)) {
            throw new RuntimeException("分析记录不存在或无权访问");
        }
        JobMatchAnalysis update = new JobMatchAnalysis();
        update.setId(id);
        update.setIsSaved(isSaved);
        jobMatchMapper.update(update);
    }

    @Override
    public void deleteAnalysis(Long id, Long userId) {
        JobMatchAnalysis analysis = jobMatchMapper.selectById(id);
        if (analysis == null || !analysis.getUserId().equals(userId)) {
            throw new RuntimeException("分析记录不存在或无权访问");
        }
        jobMatchMapper.deleteById(id);
    }

    // ===== 私有方法 =====

    /**
     * 获取简历内容
     */
    private String getResumeContent(Long resumeId, Long userId) {
        if (resumeId == null) {
            throw new RuntimeException("请选择要匹配的简历");
        }
        Resume resume = resumeMapper.findById(resumeId);
        if (resume == null || !resume.getUserId().equals(userId)) {
            throw new RuntimeException("简历不存在或无权访问");
        }
        // 优先用结构化数据，没有就用原始文本
        if (resume.getStructuredData() != null && !resume.getStructuredData().isEmpty()) {
            return resume.getStructuredData();
        }
        return resume.getOriginalText();
    }

    /**
     * 构建岗位匹配分析Prompt
     */
    private String buildAnalyzePrompt(String resumeContent, String jobDescription, String jobRequirement) {
        StringBuilder prompt = new StringBuilder();

        prompt.append("你是一位资深的HR分析师，擅长评估简历与岗位的匹配度。请仔细分析以下简历和岗位信息。\n\n");

        prompt.append("## 简历内容\n");
        prompt.append(resumeContent);
        prompt.append("\n\n");

        prompt.append("## 职位描述\n");
        prompt.append(jobDescription);
        prompt.append("\n\n");

        if (jobRequirement != null && !jobRequirement.isEmpty()) {
            prompt.append("## 职位要求\n");
            prompt.append(jobRequirement);
            prompt.append("\n\n");
        }

        prompt.append("## 分析要求\n");
        prompt.append("请从技能匹配、经验匹配、学历匹配、关键词匹配等维度进行深入分析，返回严格的JSON格式结果：\n\n");

        prompt.append("{\n");
        prompt.append("  \"matchScore\": 75,\n");
        prompt.append("  \"matchLevel\": \"middle\",\n");
        prompt.append("  \"strengths\": [\"优势1\", \"优势2\"],\n");
        prompt.append("  \"weaknesses\": [\"劣势1\", \"劣势2\"],\n");
        prompt.append("  \"missingSkills\": [\"缺少技能1\", \"缺少技能2\"],\n");
        prompt.append("  \"suggestions\": [\"建议1\", \"建议2\"],\n");
        prompt.append("  \"keywordsMatch\": 5,\n");
        prompt.append("  \"keywordsTotal\": 8,\n");
        prompt.append("  \"experienceMatch\": 1\n");
        prompt.append("}\n\n");

        prompt.append("字段说明：\n");
        prompt.append("- matchScore: 总体匹配度0-100，80以上为high，50-79为middle，50以下为low\n");
        prompt.append("- matchLevel: high/middle/low\n");
        prompt.append("- strengths: 简历中与岗位匹配的优势（3-5条）\n");
        prompt.append("- weaknesses: 简历中的不足（2-4条）\n");
        prompt.append("- missingSkills: JD要求但简历中缺失的技能（列出具体技能名）\n");
        prompt.append("- suggestions: 针对性简历修改建议（3-5条，要具体可执行）\n");
        prompt.append("- keywordsMatch: 简历中命中的JD关键词数量\n");
        prompt.append("- keywordsTotal: JD中的核心关键词总数\n");
        prompt.append("- experienceMatch: 经验匹配度，0-不满足 1-基本满足 2-超出要求\n\n");

        prompt.append("只返回JSON，不要有其他文字说明。\n");

        return prompt.toString();
    }

    /**
     * 解析AI返回的JSON
     */
    private Map<String, Object> parseAiResponse(String aiResponse) {
        try {
            // 去除markdown代码块标记
            String json = aiResponse.trim();
            if (json.startsWith("```json")) {
                json = json.substring(7);
            } else if (json.startsWith("```")) {
                json = json.substring(3);
            }
            if (json.endsWith("```")) {
                json = json.substring(0, json.length() - 3);
            }
            json = json.trim();

            return objectMapper.readValue(json, new TypeReference<Map<String, Object>>() {});
        } catch (Exception e) {
            throw new RuntimeException("AI返回结果解析失败：" + e.getMessage());
        }
    }

    /**
     * 将AI结果填充到实体
     */
    @SuppressWarnings("unchecked")
    private void fillAnalysisResult(JobMatchAnalysis analysis, Map<String, Object> result) {
        // 匹配分数
        Object score = result.get("matchScore");
        analysis.setMatchScore(score != null ? ((Number) score).intValue() : 0);

        // 匹配等级
        String level = (String) result.getOrDefault("matchLevel", "low");
        analysis.setMatchLevel(level);

        // 列表字段转JSON存储
        try {
            List<String> strengths = (List<String>) result.get("strengths");
            if (strengths != null) analysis.setStrengths(objectMapper.writeValueAsString(strengths));

            List<String> weaknesses = (List<String>) result.get("weaknesses");
            if (weaknesses != null) analysis.setWeaknesses(objectMapper.writeValueAsString(weaknesses));

            List<String> missingSkills = (List<String>) result.get("missingSkills");
            if (missingSkills != null) analysis.setMissingSkills(objectMapper.writeValueAsString(missingSkills));

            List<String> suggestions = (List<String>) result.get("suggestions");
            if (suggestions != null) analysis.setSuggestions(objectMapper.writeValueAsString(suggestions));
        } catch (Exception e) {
            // JSON序列化失败，存原始字符串
        }

        // 统计字段
        Object km = result.get("keywordsMatch");
        analysis.setKeywordsMatch(km != null ? ((Number) km).intValue() : 0);

        Object kt = result.get("keywordsTotal");
        analysis.setKeywordsTotal(kt != null ? ((Number) kt).intValue() : 0);

        Object em = result.get("experienceMatch");
        analysis.setExperienceMatch(em != null ? ((Number) em).intValue() : 0);
    }

    /**
     * Entity转VO
     */
    private JobMatchResultVO convertToVO(JobMatchAnalysis analysis) {
        JobMatchResultVO vo = new JobMatchResultVO();
        vo.setId(analysis.getId());
        vo.setResumeId(analysis.getResumeId());
        vo.setApplicationId(analysis.getApplicationId());
        vo.setJobTitle(analysis.getJobTitle());
        vo.setCompanyName(analysis.getCompanyName());
        vo.setJobDescription(analysis.getJobDescription());
        vo.setMatchScore(analysis.getMatchScore());
        vo.setMatchLevel(analysis.getMatchLevel());
        vo.setMatchLevelText(getMatchLevelText(analysis.getMatchLevel()));
        vo.setKeywordsMatch(analysis.getKeywordsMatch());
        vo.setKeywordsTotal(analysis.getKeywordsTotal());
        vo.setExperienceMatch(analysis.getExperienceMatch());
        vo.setIsSaved(analysis.getIsSaved());
        vo.setAnalysisResult(analysis.getAnalysisResult());
        vo.setCreateTime(analysis.getCreateTime());

        // JSON列表字段解析
        try {
            if (analysis.getStrengths() != null) {
                vo.setStrengths(objectMapper.readValue(analysis.getStrengths(), new TypeReference<List<String>>() {}));
            }
            if (analysis.getWeaknesses() != null) {
                vo.setWeaknesses(objectMapper.readValue(analysis.getWeaknesses(), new TypeReference<List<String>>() {}));
            }
            if (analysis.getMissingSkills() != null) {
                vo.setMissingSkills(objectMapper.readValue(analysis.getMissingSkills(), new TypeReference<List<String>>() {}));
            }
            if (analysis.getSuggestions() != null) {
                vo.setSuggestions(objectMapper.readValue(analysis.getSuggestions(), new TypeReference<List<String>>() {}));
            }
        } catch (Exception e) {
            // 解析失败给默认空列表
        }

        if (vo.getStrengths() == null) vo.setStrengths(new ArrayList<>());
        if (vo.getWeaknesses() == null) vo.setWeaknesses(new ArrayList<>());
        if (vo.getMissingSkills() == null) vo.setMissingSkills(new ArrayList<>());
        if (vo.getSuggestions() == null) vo.setSuggestions(new ArrayList<>());

        return vo;
    }

    /**
     * 匹配等级中文
     */
    private String getMatchLevelText(String level) {
        if ("high".equals(level)) return "高度匹配";
        if ("middle".equals(level)) return "中等匹配";
        return "匹配度较低";
    }
}
