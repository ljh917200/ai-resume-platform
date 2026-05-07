package com.resume.airesume.service.impl;

import com.resume.airesume.dto.CoverLetterCreateDTO;
import com.resume.airesume.dto.CoverLetterVO;
import com.resume.airesume.entity.CoverLetter;
import com.resume.airesume.entity.Resume;
import com.resume.airesume.mapper.CoverLetterMapper;
import com.resume.airesume.mapper.ResumeMapper;
import com.resume.airesume.service.CoverLetterService;
import com.resume.airesume.service.DeepSeekService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 求职信服务实现类
 * 核心逻辑：构建Prompt → 调用DeepSeek → 解析结果 → 持久化
 */
@Service
public class CoverLetterServiceImpl implements CoverLetterService {

    @Autowired
    private CoverLetterMapper coverLetterMapper;

    @Autowired
    private ResumeMapper resumeMapper;

    @Autowired
    private DeepSeekService deepSeekService;

    /** JSON解析器 */
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public CoverLetterVO generateLetter(Long userId, CoverLetterCreateDTO dto) {
        // 1. 根据resumeId获取简历内容
        Resume resume = resumeMapper.findById(dto.getResumeId());
        if (resume == null) {
            throw new RuntimeException("简历不存在");
        }

        // 2. 构建系统提示词
        String systemPrompt = buildSystemPrompt(dto.getLetterStyle(), dto.getLanguage());

        // 3. 构建用户提示词（简历内容 + 岗位信息）
        String userPrompt = buildUserPrompt(resume, dto);

        // 4. 调用DeepSeek生成求职信，temperature稍高让文案更有创意
        String aiResult = deepSeekService.chat(systemPrompt, userPrompt, 0.7);

        // 5. 提取求职信内容（AI可能返回JSON或纯文本，兼容处理）
        String letterContent = parseLetterContent(aiResult);

        // 6. 持久化到数据库
        CoverLetter letter = new CoverLetter();
        letter.setUserId(userId);
        letter.setResumeId(dto.getResumeId());
        letter.setApplicationId(dto.getApplicationId());
        letter.setJobTitle(dto.getJobTitle());
        letter.setCompanyName(dto.getCompanyName());
        letter.setJobDescription(dto.getJobDescription());
        letter.setLetterContent(letterContent);
        letter.setLetterStyle(dto.getLetterStyle() != null ? dto.getLetterStyle() : "formal");
        letter.setLanguage(dto.getLanguage() != null ? dto.getLanguage() : "zh");
        letter.setIsSaved(0);
        letter.setDeleted(0);
        coverLetterMapper.insert(letter);

        // 7. 转换为VO返回
        return toVO(letter);
    }

    @Override
    public CoverLetterVO getLetter(Long id, Long userId) {
        CoverLetter letter = coverLetterMapper.selectById(id);
        // 权限校验：记录必须存在且属于当前用户
        if (letter == null || !letter.getUserId().equals(userId)) {
            throw new RuntimeException("求职信不存在或无权访问");
        }
        return toVO(letter);
    }

    @Override
    public List<CoverLetterVO> getHistory(Long userId) {
        List<CoverLetter> list = coverLetterMapper.selectByUserId(userId);
        return list.stream().map(this::toVO).collect(Collectors.toList());
    }

    @Override
    public CoverLetterVO regenerateLetter(Long id, Long userId, String style, String lang) {
        // 1. 查询原记录并校验权限
        CoverLetter letter = coverLetterMapper.selectById(id);
        if (letter == null || !letter.getUserId().equals(userId)) {
            throw new RuntimeException("求职信不存在或无权访问");
        }

        // 2. 获取关联的简历
        Resume resume = resumeMapper.findById(letter.getResumeId());
        if (resume == null) {
            throw new RuntimeException("关联简历不存在");
        }

        // 3. 使用新风格/语言，若未传则用原来的
        String newStyle = style != null ? style : letter.getLetterStyle();
        String newLang = lang != null ? lang : letter.getLanguage();

        // 4. 构建Prompt并调用AI重新生成
        String systemPrompt = buildSystemPrompt(newStyle, newLang);
        CoverLetterCreateDTO dto = new CoverLetterCreateDTO();
        dto.setJobTitle(letter.getJobTitle());
        dto.setCompanyName(letter.getCompanyName());
        dto.setJobDescription(letter.getJobDescription());
        String userPrompt = buildUserPrompt(resume, dto);

        String aiResult = deepSeekService.chat(systemPrompt, userPrompt, 0.7);
        String letterContent = parseLetterContent(aiResult);

        // 5. 更新记录
        letter.setLetterContent(letterContent);
        letter.setLetterStyle(newStyle);
        letter.setLanguage(newLang);
        coverLetterMapper.updateById(letter);

        return toVO(letter);
    }

    @Override
    public void toggleSave(Long id, Long userId, Integer isSaved) {
        CoverLetter letter = coverLetterMapper.selectById(id);
        if (letter == null || !letter.getUserId().equals(userId)) {
            throw new RuntimeException("求职信不存在或无权访问");
        }
        letter.setIsSaved(isSaved);
        coverLetterMapper.updateById(letter);
    }

    @Override
    public void deleteLetter(Long id, Long userId) {
        CoverLetter letter = coverLetterMapper.selectById(id);
        if (letter == null || !letter.getUserId().equals(userId)) {
            throw new RuntimeException("求职信不存在或无权访问");
        }
        coverLetterMapper.deleteById(id);
    }

    // ==================== 私有方法 ====================

    /**
     * 构建系统提示词（根据风格和语言调整）
     */
    private String buildSystemPrompt(String style, String language) {
        String styleDesc;
        switch (style != null ? style : "formal") {
            case "casual":
                styleDesc = "轻松活泼，像朋友推荐一样自然，可以适当使用口语化表达";
                break;
            case "creative":
                styleDesc = "富有创意，开头吸引眼球，用独特的角度展现自己，避免模板化";
                break;
            default:
                styleDesc = "正式专业，逻辑清晰，用词严谨，符合商务信函规范";
        }

        String langDesc = "en".equals(language) ? "英文" : "中文";

        return "你是一位资深的求职顾问，擅长撰写高质量求职信。" +
                "请根据用户的简历和目标岗位信息，撰写一封" + styleDesc + "的" + langDesc + "求职信。" +
                "要求：\n" +
                "1. 开头简洁有力，说明求职意向和来源\n" +
                "2. 主体突出2-3个核心优势，结合岗位需求展开，用具体经历佐证\n" +
                "3. 结尾表达期待，主动请求面试机会\n" +
                "4. 控制在300-500字，不重复简历内容，展现个人价值\n" +
                "5. 请直接输出求职信正文，不要加标题、不要加\"尊敬的HR\"等称呼开头" +
                (langDesc.equals("英文") ? "，结尾不要用Sincerely等签名" : "，结尾不要加\"此致敬礼\"等套话");
    }

    /**
     * 构建用户提示词（拼接简历内容和岗位信息）
     */
    private String buildUserPrompt(Resume resume, CoverLetterCreateDTO dto) {
        StringBuilder sb = new StringBuilder();

        // 简历内容
        sb.append("【我的简历】\n");
        if (resume.getStructuredData() != null) {
            sb.append(resume.getStructuredData());
        } else if (resume.getOriginalText() != null) {
            sb.append(resume.getOriginalText());
        } else {
            sb.append("（无简历内容）");
        }

        // 目标岗位
        sb.append("\n\n【目标岗位】\n");
        sb.append("岗位名称：").append(dto.getJobTitle());
        if (dto.getCompanyName() != null && !dto.getCompanyName().isEmpty()) {
            sb.append("\n公司名称：").append(dto.getCompanyName());
        }
        if (dto.getJobDescription() != null && !dto.getJobDescription().isEmpty()) {
            sb.append("\n岗位描述：\n").append(dto.getJobDescription());
        }

        return sb.toString();
    }

    /**
     * 解析AI返回的求职信内容
     * 兼容两种返回格式：纯文本 或 JSON包装
     */
    private String parseLetterContent(String aiResult) {
        if (aiResult == null || aiResult.trim().isEmpty()) {
            throw new RuntimeException("AI生成内容为空");
        }

        String content = aiResult.trim();

        // 尝试按JSON解析（AI可能返回 {"content": "..."} 格式）
        if (content.startsWith("{")) {
            try {
                var node = objectMapper.readTree(content);
                if (node.has("content")) {
                    return node.get("content").asText();
                }
                if (node.has("letter")) {
                    return node.get("letter").asText();
                }
                // JSON格式但没找到字段，取第一个字符串值
                var fields = node.fields();
                while (fields.hasNext()) {
                    var entry = fields.next();
                    if (entry.getValue().isTextual()) {
                        return entry.getValue().asText();
                    }
                }
            } catch (Exception e) {
                // JSON解析失败，当纯文本处理
            }
        }

        // 去掉可能的markdown代码块包裹
        content = content.replaceAll("^```[\\s\\S]*?\\n", "");
        content = content.replaceAll("\\n```$", "");

        return content;
    }

    /**
     * Entity转VO
     */
    private CoverLetterVO toVO(CoverLetter letter) {
        CoverLetterVO vo = new CoverLetterVO();
        vo.setId(letter.getId());
        vo.setResumeId(letter.getResumeId());
        vo.setApplicationId(letter.getApplicationId());
        vo.setJobTitle(letter.getJobTitle());
        vo.setCompanyName(letter.getCompanyName());
        vo.setJobDescription(letter.getJobDescription());
        vo.setLetterContent(letter.getLetterContent());
        vo.setLetterStyle(letter.getLetterStyle());
        vo.setLanguage(letter.getLanguage());
        vo.setIsSaved(letter.getIsSaved());
        vo.setCreateTime(letter.getCreateTime());
        return vo;
    }
}
