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

    /**
     * 更新简历显示名称
     *
     * @param id 简历ID
     * @param userId 用户ID
     * @param displayName 新名称
     * @return 是否成功
     */
    boolean renameResume(Long id, Long userId, String displayName);

    /**
     * 批量删除简历
     *
     * @param ids 简历ID列表
     * @param userId 用户ID
     * @return 删除数量
     */
    int batchDelete(List<Long> ids, Long userId);

    boolean switchTemplate(Long id, Long userId, Integer templateId);

}