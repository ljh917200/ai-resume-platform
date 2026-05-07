package com.resume.airesume.service.impl;

import com.resume.airesume.dto.InterviewGenerateDTO;
import com.resume.airesume.dto.InterviewQuestionVO;
import com.resume.airesume.entity.InterviewQuestion;
import com.resume.airesume.entity.Resume;
import com.resume.airesume.mapper.InterviewQuestionMapper;
import com.resume.airesume.mapper.ResumeMapper;
import com.resume.airesume.service.DeepSeekService;
import com.resume.airesume.service.InterviewQuestionService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 面试题服务实现类
 * 核心逻辑：构建Prompt → 调用DeepSeek → 解析题目列表 → 逐条持久化
 */
@Service
public class InterviewQuestionServiceImpl implements InterviewQuestionService {

    @Autowired
    private InterviewQuestionMapper questionMapper;

    @Autowired
    private ResumeMapper resumeMapper;

    @Autowired
    private DeepSeekService deepSeekService;

    /** JSON解析器 */
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public List<InterviewQuestionVO> generateQuestions(Long userId, InterviewGenerateDTO dto) {
        // 1. 获取简历内容
        Resume resume = resumeMapper.findById(dto.getResumeId());
        if (resume == null) {
            throw new RuntimeException("简历不存在");
        }

        // 2. 确定要生成的题目类型
        int countPerType = dto.getCountPerType() != null ? dto.getCountPerType() : 3;
        List<String> types;
        if (dto.getQuestionType() != null && !dto.getQuestionType().isEmpty()) {
            // 只生成指定类型
            types = Collections.singletonList(dto.getQuestionType());
        } else {
            // 生成全部4种类型
            types = Arrays.asList("technical", "behavioral", "situational", "hr");
        }

        // 3. 逐类型调用AI生成面试题
        List<InterviewQuestion> allQuestions = new ArrayList<>();
        for (String type : types) {
            String systemPrompt = buildSystemPrompt(type);
            String userPrompt = buildUserPrompt(resume, dto);

            // 调用DeepSeek，temperature适中，保证题目有变化但不出格
            String aiResult = deepSeekService.chat(systemPrompt, userPrompt, 0.6);

            // 解析AI返回的题目列表
            List<InterviewQuestion> questions = parseQuestions(aiResult, type, countPerType);

            // 填充公共字段并持久化
            for (InterviewQuestion q : questions) {
                q.setUserId(userId);
                q.setResumeId(dto.getResumeId());
                q.setApplicationId(dto.getApplicationId());
                q.setJobTitle(dto.getJobTitle());
                q.setCompanyName(dto.getCompanyName());
                q.setJobDescription(dto.getJobDescription());
                q.setPrepStatus("unprepared");
                q.setIsSaved(0);
                q.setDeleted(0);
                questionMapper.insert(q);
                allQuestions.add(q);
            }
        }

        // 4. 转换为VO返回
        return allQuestions.stream().map(this::toVO).collect(Collectors.toList());
    }

    @Override
    public InterviewQuestionVO getQuestion(Long id, Long userId) {
        InterviewQuestion question = questionMapper.selectById(id);
        if (question == null || !question.getUserId().equals(userId)) {
            throw new RuntimeException("面试题不存在或无权访问");
        }
        return toVO(question);
    }

    @Override
    public List<InterviewQuestionVO> getQuestionList(Long userId, String questionType, String prepStatus) {
        List<InterviewQuestion> list;
        // 优先按类型筛选，其次按状态筛选，都没有则查全部
        if (questionType != null && !questionType.isEmpty()) {
            list = questionMapper.selectByUserIdAndType(userId, questionType);
        } else if (prepStatus != null && !prepStatus.isEmpty()) {
            list = questionMapper.selectByUserIdAndStatus(userId, prepStatus);
        } else {
            list = questionMapper.selectByUserId(userId);
        }
        return list.stream().map(this::toVO).collect(Collectors.toList());
    }

    @Override
    public void updateAnswerDraft(Long id, Long userId, String answerDraft) {
        InterviewQuestion question = questionMapper.selectById(id);
        if (question == null || !question.getUserId().equals(userId)) {
            throw new RuntimeException("面试题不存在或无权访问");
        }
        question.setAnswerDraft(answerDraft);
        questionMapper.updateById(question);
    }

    @Override
    public void updatePrepStatus(Long id, Long userId, String prepStatus) {
        InterviewQuestion question = questionMapper.selectById(id);
        if (question == null || !question.getUserId().equals(userId)) {
            throw new RuntimeException("面试题不存在或无权访问");
        }
        question.setPrepStatus(prepStatus);
        questionMapper.updateById(question);
    }

    @Override
    public void toggleSave(Long id, Long userId, Integer isSaved) {
        InterviewQuestion question = questionMapper.selectById(id);
        if (question == null || !question.getUserId().equals(userId)) {
            throw new RuntimeException("面试题不存在或无权访问");
        }
        question.setIsSaved(isSaved);
        questionMapper.updateById(question);
    }

    @Override
    public void deleteQuestion(Long id, Long userId) {
        InterviewQuestion question = questionMapper.selectById(id);
        if (question == null || !question.getUserId().equals(userId)) {
            throw new RuntimeException("面试题不存在或无权访问");
        }
        questionMapper.deleteById(id);
    }

    @Override
    public Map<String, Object> getStats(Long userId) {
        Map<String, Object> stats = new HashMap<>();
        // 按状态分组统计
        List<Map<String, Object>> statusStats = questionMapper.selectStatsByStatus(userId);
        int total = 0, prepared = 0, preparing = 0, unprepared = 0;
        for (Map<String, Object> item : statusStats) {
            String status = (String) item.get("status");
            int count = ((Number) item.get("count")).intValue();
            total += count;
            switch (status) {
                case "prepared": prepared = count; break;
                case "preparing": preparing = count; break;
                case "unprepared": unprepared = count; break;
            }
        }
        stats.put("total", total);
        stats.put("prepared", prepared);
        stats.put("preparing", preparing);
        stats.put("unprepared", unprepared);
        // 准备完成率
        stats.put("completionRate", total > 0 ? Math.round(prepared * 100.0 / total) : 0);
        return stats;
    }

    // ==================== 私有方法 ====================

    /**
     * 根据题目类型构建系统提示词
     */
    private String buildSystemPrompt(String questionType) {
        String typeDesc;
        switch (questionType) {
            case "technical":
                typeDesc = "技术面试题，考察编程能力、技术原理、系统设计等技术硬实力";
                break;
            case "behavioral":
                typeDesc = "行为面试题，考察过往经历中的协作、沟通、领导力等软实力（STAR法则）";
                break;
            case "situational":
                typeDesc = "情景面试题，给出假设场景考察应变能力和决策思路";
                break;
            case "hr":
                typeDesc = "HR面试题，考察求职动机、职业规划、薪资期望、文化匹配度";
                break;
            default:
                typeDesc = "综合面试题";
        }

        return "你是一位资深面试官，擅长出高质量的" + typeDesc + "。\n" +
                "请根据候选人的简历和目标岗位，生成面试题。\n" +
                "要求：\n" +
                "1. 题目要有针对性，结合简历中的技术栈和项目经历\n" +
                "2. 难度分布合理：约30%简单、50%中等、20%困难\n" +
                "3. 每道题必须包含：题目文本、难度、答题提示、关键得分点（3-5个）\n" +
                "4. 严格按以下JSON格式返回，不要返回其他内容：\n" +
                "[\n" +
                "  {\n" +
                "    \"question\": \"题目内容\",\n" +
                "    \"difficulty\": \"easy/medium/hard\",\n" +
                "    \"hint\": \"答题思路提示\",\n" +
                "    \"keyPoints\": [\"得分点1\", \"得分点2\", \"得分点3\"]\n" +
                "  }\n" +
                "]";
    }

    /**
     * 构建用户提示词（拼接简历内容和岗位信息）
     */
    private String buildUserPrompt(Resume resume, InterviewGenerateDTO dto) {
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
     * 解析AI返回的面试题列表
     * 兼容JSON数组、markdown代码块包裹、纯文本等格式
     */
    private List<InterviewQuestion> parseQuestions(String aiResult, String questionType, int countPerType) {
        List<InterviewQuestion> questions = new ArrayList<>();
        if (aiResult == null || aiResult.trim().isEmpty()) {
            return questions;
        }

        String content = aiResult.trim();

        // 1. 去掉markdown代码块包裹（可能在任意位置，不只是在首尾）
        content = content.replaceAll("```[a-zA-Z]*\\s*", "");
        content = content.replaceAll("```", "");

        // 2. 尝试提取JSON数组（找第一个 [ 和最后一个 ] 之间的内容）
        int startIdx = content.indexOf('[');
        int endIdx = content.lastIndexOf(']');
        if (startIdx != -1 && endIdx > startIdx) {
            String jsonStr = content.substring(startIdx, endIdx + 1);
            try {
                List<Map<String, Object>> items = objectMapper.readValue(
                        jsonStr, new TypeReference<List<Map<String, Object>>>() {});

                for (Map<String, Object> item : items) {
                    InterviewQuestion q = new InterviewQuestion();
                    q.setQuestionType(questionType);
                    q.setQuestionText(getStringValue(item, "question"));
                    q.setDifficulty(getStringValue(item, "difficulty"));
                    q.setHint(getStringValue(item, "hint"));

                    // keyPoints转JSON字符串存储
                    Object keyPoints = item.get("keyPoints");
                    if (keyPoints != null) {
                        q.setKeyPoints(objectMapper.writeValueAsString(keyPoints));
                    }

                    // 只取指定数量
                    if (questions.size() < countPerType) {
                        questions.add(q);
                    }
                }

                // JSON解析成功，直接返回
                return questions;
            } catch (Exception e) {
                // JSON解析失败，继续尝试纯文本方式
            }
        }

        // 3. 纯文本降级：按行解析，跳过JSON语法行
        String[] lines = content.split("\n");
        for (String line : lines) {
            line = line.trim();
            // 跳过空行、标题行、JSON语法行
            if (line.isEmpty()) continue;
            if (line.startsWith("#")) continue;
            if (line.equals("[") || line.equals("]") || line.equals("{") || line.equals("}")) continue;
            if (line.startsWith("\"question\"") || line.startsWith("\"difficulty\"")) continue;
            if (line.startsWith("\"hint\"") || line.startsWith("\"keyPoints\"")) continue;
            // 去掉序号前缀
            line = line.replaceFirst("^\\d+[.、)）]\\s*", "");
            // 去掉开头的引号和连字符
            line = line.replaceFirst("^[-*]\\s*", "");
            if (!line.isEmpty() && questions.size() < countPerType) {
                InterviewQuestion q = new InterviewQuestion();
                q.setQuestionType(questionType);
                q.setQuestionText(line);
                q.setDifficulty("medium");
                questions.add(q);
            }
        }

        return questions;
    }

    /** 从Map中安全获取字符串值 */
    private String getStringValue(Map<String, Object> map, String key) {
        Object value = map.get(key);
        return value != null ? value.toString() : null;
    }

    /**
     * Entity转VO
     * keyPoints从JSON字符串解析为List
     */
    private InterviewQuestionVO toVO(InterviewQuestion question) {
        InterviewQuestionVO vo = new InterviewQuestionVO();
        vo.setId(question.getId());
        vo.setResumeId(question.getResumeId());
        vo.setApplicationId(question.getApplicationId());
        vo.setJobTitle(question.getJobTitle());
        vo.setCompanyName(question.getCompanyName());
        vo.setQuestionType(question.getQuestionType());
        vo.setQuestionText(question.getQuestionText());
        vo.setDifficulty(question.getDifficulty());
        vo.setHint(question.getHint());
        vo.setAnswerDraft(question.getAnswerDraft());
        vo.setPrepStatus(question.getPrepStatus());
        vo.setIsSaved(question.getIsSaved());
        vo.setCreateTime(question.getCreateTime());

        // keyPoints: JSON字符串 → List<String>
        if (question.getKeyPoints() != null && !question.getKeyPoints().isEmpty()) {
            try {
                List<String> points = objectMapper.readValue(
                        question.getKeyPoints(), new TypeReference<List<String>>() {});
                vo.setKeyPoints(points);
            } catch (Exception e) {
                // 解析失败，当普通字符串处理
                vo.setKeyPoints(Collections.singletonList(question.getKeyPoints()));
            }
        } else {
            vo.setKeyPoints(Collections.emptyList());
        }

        return vo;
    }
}
