package com.resume.airesume.service;

import com.resume.airesume.config.DeepSeekConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;

/**
 * DeepSeek AI 服务
 * 作用：调用 DeepSeek API 进行文本优化
 */
@Service
public class DeepSeekService {

    @Autowired
    private DeepSeekConfig deepSeekConfig;

    // RestTemplate 用于发送 HTTP 请求
    private final RestTemplate restTemplate = new RestTemplate();

    /**
     * 优化简历文本
     *
     * @param originalText 原始简历文本
     * @param targetRole   目标岗位（可选，用于针对性优化）
     * @return 优化后的文本
     */
    public String optimizeResume(String originalText, String targetRole) {
        // ============================================
        // 第一步：构建请求体
        // ============================================
        // DeepSeek API 使用 OpenAI 兼容格式
        Map<String, Object> requestBody = new HashMap<>();

        // 指定模型
        requestBody.put("model", "deepseek-chat");

        // 构建消息列表
        List<Map<String, String>> messages = new ArrayList<>();

        // 系统提示词：定义 AI 的角色和行为
        Map<String, String> systemMessage = new HashMap<>();
        systemMessage.put("role", "system");
        systemMessage.put("content", buildSystemPrompt(targetRole));
        messages.add(systemMessage);

        // 用户消息：需要优化的文本
        Map<String, String> userMessage = new HashMap<>();
        userMessage.put("role", "user");
        userMessage.put("content", originalText);
        messages.add(userMessage);

        requestBody.put("messages", messages);

        // 温度参数：0.7 适合创意性任务
        requestBody.put("temperature", 0.7);

        // 最大输出 token 数
        requestBody.put("max_tokens", 2000);

        // ============================================
        // 第二步：设置请求头
        // ============================================
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        // Authorization: Bearer sk-xxxxxx
        headers.setBearerAuth(deepSeekConfig.getKey());

        // ============================================
        // 第三步：发送请求
        // ============================================
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

        try {
            // 发送 POST 请求
            ResponseEntity<Map> response = restTemplate.exchange(
                    deepSeekConfig.getUrl(),
                    HttpMethod.POST,
                    entity,
                    Map.class
            );

            // ============================================
            // 第四步：解析响应
            // ============================================
            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                // 响应格式：
                // {
                //   "choices": [
                //     {
                //       "message": {
                //         "content": "优化后的文本..."
                //       }
                //     }
                //   ]
                // }
                Map<String, Object> body = response.getBody();
                List<Map<String, Object>> choices = (List<Map<String, Object>>) body.get("choices");

                if (choices != null && !choices.isEmpty()) {
                    Map<String, Object> firstChoice = choices.get(0);
                    Map<String, String> message = (Map<String, String>) firstChoice.get("message");
                    return message.get("content");
                }
            }

            return "优化失败：API 响应异常";

        } catch (Exception e) {
            // 捕获异常，返回错误信息
            return "优化失败：" + e.getMessage();
        }
    }

    /**
     * 构建系统提示词（灵活优化版：仅锁定客观信息，描述自由发挥）
     */
    private String buildSystemPrompt(String targetRole) {
        StringBuilder prompt = new StringBuilder();

        prompt.append("你是专业的简历优化师，优化风格灵活自然，只做精准提升，不破坏简历完整性。\n\n");
        prompt.append("【保留规则（仅约束客观信息）】\n");
        prompt.append("1. 以下客观信息**必须完整保留，不可删除、不可篡改、不可省略**，仅可轻微理顺措辞：\n");
        prompt.append("   - 个人信息：姓名、电话、邮箱、所在城市、联系方式\n");
        prompt.append("   - 教育信息：学校名称、专业、学历、就读时间段\n");
        prompt.append("   - 经历核心：公司/项目名称、担任角色、起止时间、奖项/证书名称\n");
        prompt.append("2. 简历所有模块**必须全部保留**，不合并、不删减、不改变原有结构顺序\n\n");

        prompt.append("【优化规则（描述内容自由发挥）】\n");
        prompt.append("1. 对自我评价、项目描述、工作内容、经历细节等描述性文字，自由优化润色\n");
        prompt.append("2. 用STAR法则梳理逻辑，用数据化成果提升质感，不虚构、不夸大\n");
        prompt.append("3. 用动作动词开头，强化专业度，精简冗余表述，提升可读性\n");
        prompt.append("4. 突出技术栈、业务价值与个人能力，贴合求职场景\n\n");

        if (targetRole != null && !targetRole.isEmpty()) {
            prompt.append("目标岗位：").append(targetRole).append("\n");
            prompt.append("请围绕岗位匹配度优化描述，客观信息依然完整保留。\n\n");
        }

        prompt.append("直接输出完整优化后的简历，无需额外解释。");
        return prompt.toString();
    }


    /**
     * 结构化简历内容
     * 将简历文本提取为结构化JSON数据
     *
     * @param originalText 原始简历文本
     * @return 结构化JSON字符串
     */
    public String structureResume(String originalText) {
        // ============================================
        // 第一步：构建请求体
        // ============================================
        Map<String, Object> requestBody = new HashMap<>();

        // 指定模型
        requestBody.put("model", "deepseek-chat");

        // 构建消息列表
        List<Map<String, String>> messages = new ArrayList<>();

        // 系统提示词：定义结构化提取规则
        Map<String, String> systemMessage = new HashMap<>();
        systemMessage.put("role", "system");
        systemMessage.put("content", buildStructurePrompt());
        messages.add(systemMessage);

        // 用户消息：需要结构化的文本
        Map<String, String> userMessage = new HashMap<>();
        userMessage.put("role", "user");
        userMessage.put("content", originalText);
        messages.add(userMessage);

        requestBody.put("messages", messages);

        // 温度参数：0.3 适合结构化任务（更稳定）
        requestBody.put("temperature", 0.3);

        // 最大输出 token 数
        requestBody.put("max_tokens", 2000);

        // ============================================
        // 第二步：设置请求头
        // ============================================
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(deepSeekConfig.getKey());

        // ============================================
        // 第三步：发送请求
        // ============================================
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

        try {
            // 发送 POST 请求
            ResponseEntity<Map> response = restTemplate.exchange(
                    deepSeekConfig.getUrl(),
                    HttpMethod.POST,
                    entity,
                    Map.class
            );

            // ============================================
            // 第四步：解析响应
            // ============================================
            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                Map<String, Object> body = response.getBody();
                List<Map<String, Object>> choices = (List<Map<String, Object>>) body.get("choices");

                if (choices != null && !choices.isEmpty()) {
                    Map<String, Object> firstChoice = choices.get(0);
                    Map<String, String> message = (Map<String, String>) firstChoice.get("message");
                    String content = message.get("content");


                    // 提取JSON部分（去除markdown代码块标记）
                    return extractJson(content);
                }
            }

            return null;

        } catch (Exception e) {
            // 结构化失败不影响主流程，返回null
            System.err.println("简历结构化失败：" + e.getMessage());
            return null;
        }
    }


    /**
     * 构建结构化提示词
     * 扩展版：支持更多简历字段动态提取
     */
    private String buildStructurePrompt() {
        StringBuilder prompt = new StringBuilder();

        prompt.append("你是一个专业的简历信息提取专家，负责将用户提供的简历文本提取为标准化的JSON格式。\n\n");
        prompt.append("【输出要求】\n");
        prompt.append("1. 只输出JSON格式数据，不要包含任何解释、说明或markdown标记\n");
        prompt.append("2. JSON必须符合标准格式，键名使用双引号，字符串值使用双引号\n");
        prompt.append("3. 所有字段根据简历实际内容提取，没有相关经历则使用空数组[]\n");
        prompt.append("4. 时间统一格式：2023.09 - 2025.06（使用中文点号分隔年月）\n\n");

        prompt.append("【字段说明】\n");
        prompt.append("- name: 姓名（字符串）\n");
        prompt.append("- phone: 手机号（字符串）\n");
        prompt.append("- email: 电子邮箱（字符串）\n");
        prompt.append("- location: 所在城市（字符串，如：北京、上海）\n");
        prompt.append("- github: GitHub主页地址（字符串，可为空）\n");
        prompt.append("- blog: 个人博客地址（字符串，可为空）\n\n");

        prompt.append("- education: 教育经历（数组，每个元素包含）\n");
        prompt.append("  - school: 学校名称（字符串）\n");
        prompt.append("  - major: 专业（字符串）\n");
        prompt.append("  - degree: 学历（字符串，如：本科、硕士、博士）\n");
        prompt.append("  - period: 就读时间（字符串，如：2020.09 - 2024.06）\n");
        prompt.append("  - gpa: GPA或排名（字符串，如：3.8/4.0 或 前5%，可为空）\n\n");

        prompt.append("- experience: 工作/实习经历（数组，每个元素包含）\n");
        prompt.append("  - company: 公司名称（字符串）\n");
        prompt.append("  - position: 职位（字符串）\n");
        prompt.append("  - period: 工作时间（字符串）\n");
        prompt.append("  - description: 工作内容描述（字符串，每条内容用换行分隔）\n\n");

        prompt.append("- projects: 项目经历（数组，每个元素包含）\n");
        prompt.append("  - name: 项目名称（字符串）\n");
        prompt.append("  - role: 担任角色（字符串）\n");
        prompt.append("  - period: 项目时间（字符串）\n");
        prompt.append("  - description: 项目描述和技术栈（字符串，涵盖项目背景、职责、技术栈、成果等）\n\n");

        prompt.append("- skills: 专业技能（字符串数组，如：[\"Java\", \"Spring Boot\", \"MySQL\"]）\n\n");

        prompt.append("- awards: 荣誉奖励（数组，每个元素包含）\n");
        prompt.append("  - name: 奖项名称（字符串）\n");
        prompt.append("  - level: 奖项级别（字符串，如：国家级、省级、校级、院级）\n");
        prompt.append("  - year: 获奖年份（字符串）\n\n");

        prompt.append("- competitions: 学科竞赛（数组，每个元素包含）\n");
        prompt.append("  - name: 比赛名称（字符串）\n");
        prompt.append("  - result: 获奖结果（字符串，如：一等奖、全国前三名）\n");
        prompt.append("  - year: 参赛年份（字符串）\n\n");

        prompt.append("- certifications: 证书资质（字符串数组，如：[\"英语六级\", \"教师资格证\"]）\n\n");

        prompt.append("- campusActivities: 校园经历（数组，每个元素包含）\n");
        prompt.append("  - name: 组织/活动名称（字符串）\n");
        prompt.append("  - role: 担任角色（字符串）\n");
        prompt.append("  - period: 参与时间（字符串）\n\n");

        prompt.append("- selfEvaluation: 自我评价（字符串，50-100字左右）\n\n");

        prompt.append("【JSON示例】\n");
        prompt.append("{\n");
        prompt.append("  \"name\": \"张三\",\n");
        prompt.append("  \"phone\": \"13800138000\",\n");
        prompt.append("  \"email\": \"zhangsan@example.com\",\n");
        prompt.append("  \"location\": \"北京\",\n");
        prompt.append("  \"github\": \"https://github.com/zhangsan\",\n");
        prompt.append("  \"blog\": null,\n");
        prompt.append("  \"education\": [\n");
        prompt.append("    {\n");
        prompt.append("      \"school\": \"清华大学\",\n");
        prompt.append("      \"major\": \"计算机科学与技术\",\n");
        prompt.append("      \"degree\": \"本科\",\n");
        prompt.append("      \"period\": \"2020.09 - 2024.06\",\n");
        prompt.append("      \"gpa\": \"3.8/4.0\"\n");
        prompt.append("    }\n");
        prompt.append("  ],\n");
        prompt.append("  \"experience\": [],\n");
        prompt.append("  \"projects\": [\n");
        prompt.append("    {\n");
        prompt.append("      \"name\": \"在线教育平台\",\n");
        prompt.append("      \"role\": \"后端开发\",\n");
        prompt.append("      \"period\": \"2023.03 - 2023.08\",\n");
        prompt.append("      \"description\": \"负责平台后端架构设计与开发\\n使用Spring Boot构建RESTful API\\n实现用户认证、课程管理等功能\\n日均支撑10000+并发请求\"\n");
        prompt.append("    }\n");
        prompt.append("  ],\n");
        prompt.append("  \"skills\": [\"Java\", \"Spring Boot\", \"MySQL\", \"Redis\", \"Docker\"],\n");
        prompt.append("  \"awards\": [\n");
        prompt.append("    {\n");
        prompt.append("      \"name\": \"国家奖学金\",\n");
        prompt.append("      \"level\": \"国家级\",\n");
        prompt.append("      \"year\": \"2023\"\n");
        prompt.append("    }\n");
        prompt.append("  ],\n");
        prompt.append("  \"competitions\": [],\n");
        prompt.append("  \"certifications\": [\"英语六级\", \"阿里云认证\"],\n");
        prompt.append("  \"campusActivities\": [],\n");
        prompt.append("  \"selfEvaluation\": \"热爱技术，具有良好的编程习惯和代码风格。\"\n");
        prompt.append("}\n");

        return prompt.toString();
    }

    /**
     * 从AI响应中提取JSON
     * AI可能返回 ```json ... ``` 格式，需要去除markdown标记
     */
    private String extractJson(String content) {
        if (content == null) {
            return null;
        }

        // 去除markdown代码块标记
        content = content.trim();
        if (content.startsWith("```json")) {
            content = content.substring(7);
        } else if (content.startsWith("```")) {
            content = content.substring(3);
        }
        if (content.endsWith("```")) {
            content = content.substring(0, content.length() - 3);
        }

        return content.trim();
    }
}