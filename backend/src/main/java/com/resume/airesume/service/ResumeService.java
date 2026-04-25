package com.resume.airesume.service;

import com.resume.airesume.entity.Resume;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface ResumeService {

    // 上传简历
    Resume upload(Long userId, MultipartFile file);

    // 获取用户的简历列表
    List<Resume> getListByUserId(Long userId);

    // 获取简历详情
    Resume getById(Long id, Long userId);

    // 删除简历
    boolean delete(Long id, Long userId);

    // 更新优化内容
    void updateOptimizedText(Long resumeId, String optimizedText,String optimizedStructuredData);
}