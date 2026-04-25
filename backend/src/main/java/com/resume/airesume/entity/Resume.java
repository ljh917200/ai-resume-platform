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
}