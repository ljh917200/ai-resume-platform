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

    /**
     * 通用AI对话方法
     * 用于岗位匹配分析等场景，支持自定义system prompt
     *
     * @param systemPrompt 系统提示词
     * @param userContent  用户消息内容
     * @param temperature  温度参数
     * @return AI返回的文本内容
     */
    public String chat(String systemPrompt, String userContent, double temperature) {
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", "deepseek-chat");

        List<Map<String, String>> messages = new ArrayList<>();

        Map<String, String> systemMessage = new HashMap<>();
        systemMessage.put("role", "system");
        systemMessage.put("content", systemPrompt);
        messages.add(systemMessage);

        Map<String, String> userMessage = new HashMap<>();
        userMessage.put("role", "user");
        userMessage.put("content", userContent);
        messages.add(userMessage);

        requestBody.put("messages", messages);
        requestBody.put("temperature", temperature);
        requestBody.put("max_tokens", 3000);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(deepSeekConfig.getKey());

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

        try {
            ResponseEntity<Map> response = restTemplate.exchange(
                    deepSeekConfig.getUrl(),
                    HttpMethod.POST,
                    entity,
                    Map.class
            );

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                Map<String, Object> body = response.getBody();
                List<Map<String, Object>> choices = (List<Map<String, Object>>) body.get("choices");
                if (choices != null && !choices.isEmpty()) {
                    Map<String, Object> firstChoice = choices.get(0);
                    Map<String, String> message = (Map<String, String>) firstChoice.get("message");
                    return message.get("content");
                }
            }
            throw new RuntimeException("AI响应异常");
        } catch (Exception e) {
            throw new RuntimeException("AI调用失败：" + e.getMessage());
        }
    }




    /**
     * 生成简历HTML（v1.7.0新增）
     *
     * 功能说明：
     * - 根据结构化简历数据，调用 DeepSeek 生成 XHTML 格式的简历
     * - 生成的 HTML 包含内嵌 CSS 样式，可以直接预览和转 PDF
     * - 根据 templateId 使用不同的配色方案（简约蓝/商务灰/创意橙）
     *
     * 使用场景：
     * - 用户点击"预览简历"或"导出PDF"时，先调用此方法生成 HTML
     * - 生成的 HTML 会存储到数据库，后续预览直接使用缓存
     *
     * @param structuredDataJson 结构化简历数据（JSON格式），包含姓名、教育、工作经历等
     * @param templateId 模板ID：1-简约蓝 2-商务灰 3-创意橙
     * @return XHTML 格式的简历 HTML 字符串，失败返回 null
     */
    public String generateResumeHtml(String structuredDataJson, Integer templateId) {
        // ========== 第一步：构建请求体 ==========
        Map<String, Object> requestBody = new HashMap<>();

        // 指定使用 deepseek-chat 模型
        requestBody.put("model", "deepseek-chat");

        // 构建消息列表
        List<Map<String, String>> messages = new ArrayList<>();

        // 系统消息：包含详细的 HTML 生成提示词
        Map<String, String> systemMessage = new HashMap<>();
        systemMessage.put("role", "system");
        systemMessage.put("content", buildHtmlGenerationPrompt(templateId));
        messages.add(systemMessage);

        // 用户消息：提供结构化数据
        Map<String, String> userMessage = new HashMap<>();
        userMessage.put("role", "user");
        userMessage.put("content", "请根据以下结构化数据生成简历HTML：\n" + structuredDataJson);
        messages.add(userMessage);

        requestBody.put("messages", messages);

        // 温度参数：0.3（结构化任务需要稳定性，不要太随机）
        requestBody.put("temperature", 0.3);

        // 最大输出 token 数：3000（HTML 内容较多，需要充足空间）
        requestBody.put("max_tokens", 3000);

        // ========== 第二步：设置请求头 ==========
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(deepSeekConfig.getKey());

        // ========== 第三步：发送请求 ==========
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

        try {
            // 发送 POST 请求到 DeepSeek API
            ResponseEntity<Map> response = restTemplate.exchange(
                    deepSeekConfig.getUrl(),
                    HttpMethod.POST,
                    entity,
                    Map.class
            );

            // ========== 第四步：解析响应 ==========
            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                Map<String, Object> body = response.getBody();
                List<Map<String, Object>> choices = (List<Map<String, Object>>) body.get("choices");

                if (choices != null && !choices.isEmpty()) {
                    Map<String, Object> firstChoice = choices.get(0);
                    Map<String, String> message = (Map<String, String>) firstChoice.get("message");
                    String htmlContent = message.get("content");

                    // 提取纯净的 HTML 内容（去除 markdown 代码块标记）
                    return extractHtml(htmlContent);
                }
            }
            return null;

        } catch (Exception e) {
            System.err.println("生成简历HTML失败：" + e.getMessage());
            return null;
        }
    }

    /**
     * 构建HTML生成提示词（v1.7.0修复版）
     * ★ 修复：body只设置margin: 0，不设置padding
     */
    private String buildHtmlGenerationPrompt(Integer templateId) {
        if (templateId == null) templateId = 1;

        String styleGuide = getTemplateStyleGuide(templateId);

        StringBuilder prompt = new StringBuilder();

        prompt.append("你是一位专业的简历设计师，精通XHTML和CSS。\n\n");

        prompt.append("【输出要求 - 必须严格遵守】\n");
        prompt.append("1. 只输出完整的XHTML代码，不要包含任何解释、说明或注释\n");
        prompt.append("2. 第一行必须是DOCTYPE声明，前面不能有任何内容（包括空格和换行）\n");
        prompt.append("3. XHTML必须严格符合XML规范：\n");
        prompt.append("   - 所有标签必须正确闭合，如 <br/> 而不是 <br>\n");
        prompt.append("   - 所有属性值必须用双引号包裹\n");
        prompt.append("   - 特殊字符必须转义：&amp; &lt; &gt; &quot;\n");
        prompt.append("   - 标签必须正确嵌套\n");
        prompt.append("4. 所有样式必须内嵌在<style>标签中\n");
        prompt.append("5. 必须使用中文字体：Microsoft YaHei, SimHei, PingFang SC\n");
        prompt.append("6. 禁止使用JavaScript\n");
        prompt.append("7. 禁止使用CSS3特性（flex、grid、linear-gradient等）\n\n");

        // ★ 修复：body只设置margin: 0
        prompt.append("【样式强制要求 - 必须遵守】\n");
        prompt.append("1. body样式必须包含：margin: 0; （不要设置padding）\n");
        prompt.append("2. 内容宽度：700px，居中显示\n");
        prompt.append("3. 禁止设置任何背景色！包括body、div、table、td等的background-color\n");
        prompt.append("4. 禁止body或外层容器设置overflow: hidden\n\n");

        prompt.append("【禁止事项 - 违反将导致失败】\n");
        prompt.append("1. 禁止生成没有内容的空div、空table、空容器\n");
        prompt.append("2. 禁止给任何容器设置背景色\n");
        prompt.append("3. 禁止使用占位符文字如\"待填写\"、\"无\"等\n");
        prompt.append("4. 如果某个模块没有数据，直接跳过该模块，不要生成空容器\n\n");

        prompt.append("【XHTML模板 - 必须严格按此格式】\n");
        prompt.append("<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Strict//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd\">\n");
        prompt.append("<html xmlns=\"http://www.w3.org/1999/xhtml\">\n");
        prompt.append("<head>\n");
        prompt.append("<meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\"/>\n");
        prompt.append("<title>简历</title>\n");
        prompt.append("<style type=\"text/css\">\n");
        prompt.append("body { font-family: 'Microsoft YaHei', 'SimHei', sans-serif; margin: 0; }\n");
        prompt.append(".container { width: 690px; margin: 0 auto; }\n");
        prompt.append("/* 其他样式：不要设置任何background-color */\n");
        prompt.append("</style>\n");
        prompt.append("</head>\n");
        prompt.append("<body>\n");
        prompt.append("<div class=\"container\">\n");
        prompt.append("/* 在这里写简历内容，不要设置背景色 */\n");
        prompt.append("</div>\n");
        prompt.append("</body>\n");
        prompt.append("</html>\n\n");

        prompt.append("【设计风格】\n");
        prompt.append(styleGuide);
        prompt.append("\n\n");

        prompt.append("【内容结构】\n");
        prompt.append("根据JSON数据生成以下模块（有数据才生成，无数据跳过）：\n");
        prompt.append("- 姓名 + 联系方式\n");
        prompt.append("- 教育经历\n");
        prompt.append("- 工作/实习经历\n");
        prompt.append("- 项目经历\n");
        prompt.append("- 专业技能\n");
        prompt.append("- 荣誉奖项\n");
        prompt.append("- 证书资质\n");
        prompt.append("- 自我评价\n\n");

        prompt.append("【重要：样式限制】\n");
        prompt.append("- 只能使用color属性设置文字颜色\n");
        prompt.append("- 可以使用border设置边框\n");
        prompt.append("- 不要使用任何背景色\n");
        prompt.append("请严格按照上述XHTML模板格式生成简历代码，确保XML结构正确：");

        return prompt.toString();
    }

    /**
     * 从AI响应中提取HTML内容并清理
     */
    private String extractHtml(String content) {
        if (content == null) return null;

        content = content.trim();

        // 去除 markdown 代码块标记
        if (content.startsWith("```html")) {
            content = content.substring(7);
        } else if (content.startsWith("```xml")) {
            content = content.substring(7);
        } else if (content.startsWith("```")) {
            content = content.substring(3);
        }
        if (content.endsWith("```")) {
            content = content.substring(0, content.length() - 3);
        }

        content = content.trim();

        // 清理DOCTYPE前的所有内容
        int doctypeIndex = content.indexOf("<!DOCTYPE");
        if (doctypeIndex > 0) {
            content = content.substring(doctypeIndex);
        }

        // 修复常见的XHTML格式问题
        content = content.replaceAll("<br>", "<br/>");
        content = content.replaceAll("<br />", "<br/>");
        content = content.replaceAll("<hr>", "<hr/>");
        content = content.replaceAll("<hr />", "<hr/>");
        content = content.replaceAll("<img([^>]*)>", "<img$1/>");
        content = content.replaceAll("<input([^>]*)>", "<input$1/>");

        return content;
    }

    /**
     * 根据模板ID获取风格指南
     * ★ 修复：去掉所有padding和背景色要求
     */
    private String getTemplateStyleGuide(Integer templateId) {
        return switch (templateId) {
            case 2 -> """
            === 商务灰风格（模板2）===
            
            【重要：CSS兼容性要求】
            - 必须使用CSS2.1兼容布局，禁止使用flex、grid、linear-gradient
            - 双栏布局必须使用<table>或float实现
            - 禁止使用任何背景色
            - 只能使用边框和文字颜色区分区域
            
            【定位】传统企业、国企、事业单位、银行
            【气质】稳重、专业、正式
            
            【布局设计 - 使用table实现双栏】
            使用<table>布局：
            - 外层表格：宽度690px，两列布局
            - 左列：宽度210px，右侧有分隔线（border-right）
            - 右列：宽度480px，无背景色
            - 无任何背景色！
            
            左列内容：
            - 姓名区域（18pt，加粗，深灰色#2C3E50）
            - 联系方式（紧凑排列）
            - 技能区域
            
            右列内容：
            - 工作经历（公司名加粗，时间右对齐）
            - 教育经历
            - 项目经历
            
            【配色方案 - 只有文字和边框】
            - 主色调：#5A6A7A
            - 深色：#2C3E50
            - 辅助色：#7F8C8D
            - 分隔线：1px solid #E0E0E0
            - 无任何背景色！
            
            【排版细节】
            - 姓名：18pt，加粗，#2C3E50
            - 模块标题：12pt，加粗，#5A6A7A
            - 正文：10pt，行高1.6，#333333
            - 时间：9pt，右对齐，#666666
            
            【严格禁止】
            1. 禁止生成空容器
            2. 禁止设置任何background-color
            3. 如果某个模块没有数据，直接跳过
            """;

            case 3 -> """
            === 创意橙风格（模板3）===
            
            【重要：CSS兼容性要求】
            - 必须使用CSS2.1兼容布局，禁止使用flex、grid、linear-gradient
            - 单栏布局使用普通div即可
            - 禁止使用任何背景色
            - 只能用边框和文字颜色区分模块
            
            【定位】互联网、创意行业、应届生
            【气质】活力、创新、个性
            
            【布局设计 - 单栏布局】
            外层容器：
            - 宽度690px，居中
            - 无背景色
            
            顶部区域：
            - 姓名：24pt，粗体，#F76B1C，居中
            - 求职意向：12pt，居中
            - 联系方式：小标签形式
            
            主体区域：
            - 各模块垂直排列
            - 模块间距：16px
            - 模块之间用分隔线区分
            
            【配色方案 - 只有文字和边框】
            - 主色调：#F76B1C
            - 辅助色：#FF9A56
            - 正文色：#2D2D2D
            - 模块分隔：1px solid #FFE8D6
            - 无任何背景色！
            
            【排版细节】
            - 姓名：24pt，粗体，#F76B1C
            - 模块标题：12pt，粗体，#F76B1C，底部边框
            - 正文：10pt，行高1.6，#333333
            - 时间：9pt，灰色
            
            【装饰元素】
            - 模块标题前：4px宽的#F76B1C竖线（用border-left实现）
            
            【严格禁止】
            1. 禁止生成空容器
            2. 禁止设置任何background-color
            3. 如果某个模块没有数据，直接跳过
            """;

            default -> """
            === 简约蓝风格（模板1）===
            
            【重要：CSS兼容性要求】
            - 必须使用CSS2.1兼容布局，禁止使用flex、grid
            - 单栏布局使用普通div即可
            - 禁止使用任何背景色
            
            【定位】科技公司、外企、通用
            【气质】简洁、现代、专业
            
            【布局设计 - 单栏布局】
            外层容器：
            - 宽度690px，居中
            - 无背景色
            
            顶部区域：
            - 姓名：22pt，粗体，#4A90E2，左对齐
            - 求职意向：11pt，#666666
            - 联系方式：10pt，#888888
            
            主体区域：
            - 各模块垂直排列
            - 模块间距：20px
            
            【配色方案 - 只有文字和边框】
            - 主色调：#4A90E2
            - 深色：#357ABD
            - 正文色：#333333
            - 分隔线：1px solid #E8F4FF
            - 无任何背景色！
            
            【排版细节】
            - 姓名：22pt，粗体，#4A90E2
            - 模块标题：13pt，粗体，#4A90E2，底部边框
            - 正文：10pt，行高1.6，#333333
            - 时间：9pt，#888888
            
            【装饰元素】
            - 模块标题：底部1px边框，#4A90E2
            
            【严格禁止】
            1. 禁止生成空容器
            2. 禁止设置任何background-color
            3. 如果某个模块没有数据，直接跳过
            """;
        };
    }
}
