package com.resume.airesume.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class Resume {
    private Long id;              // 主键
    private Long userId;          // 用户ID
    private String originalText;  // 原始文本（解析后的）
    private String structuredData;// 结构化数据（JSON格式）
    private String fileName;      // 原始文件名
    private String fileFormat;    // 文件格式（PDF/DOCX）
    private LocalDateTime createdAt; // 创建时间
    private String optimizedText;       // 优化后的内容
    private LocalDateTime lastOptimizedAt; // 最后优化时间
    private String optimizedStructuredData; //优化后的结构化数据(JSON格式)
    /**
     * 用户自定义简历名称（可选）
     * 为空时显示file_name
     */
    private String displayName;
    /**
     * 简历模板ID
     * 1-简约蓝 2-商务灰 3-创意橙
     * 默认为1
     */
    private Integer templateId = 1;
    /**
     * AI生成的原始简历HTML（v1.7.0新增）
     * DeepSeek 根据结构化数据生成的 XHTML 格式简历
     */
    private String generatedHtml;

    /**
     * AI生成的优化后简历HTML（v1.7.0新增）
     * DeepSeek 根据优化后结构化数据生成的 XHTML 格式简历
     */
    private String optimizedHtml;
}