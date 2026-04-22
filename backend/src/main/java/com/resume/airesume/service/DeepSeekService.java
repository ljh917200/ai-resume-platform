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
     * 构建系统提示词
     * 这是 Prompt Engineering 的核心！
     */
    private String buildSystemPrompt(String targetRole) {
        StringBuilder prompt = new StringBuilder();

        prompt.append("你是一位专业的简历优化专家，擅长帮助求职者优化简历内容。\n\n");
        prompt.append("优化原则：\n");
        prompt.append("1. 使用 STAR 法则（情境-任务-行动-结果）重写经历\n");
        prompt.append("2. 用具体的数据和成果替换模糊的描述\n");
        prompt.append("3. 使用动作动词开头，如：负责、主导、开发、优化、提升等\n");
        prompt.append("4. 突出技术亮点和业务价值\n");
        prompt.append("5. 保持简洁，去除冗余信息\n\n");

        if (targetRole != null && !targetRole.isEmpty()) {
            prompt.append("目标岗位：").append(targetRole).append("\n");
            prompt.append("请针对该岗位要求，重点突出相关技能和经验。\n\n");
        }

        prompt.append("请直接输出优化后的内容，不需要解释优化过程。");

        return prompt.toString();
    }
}