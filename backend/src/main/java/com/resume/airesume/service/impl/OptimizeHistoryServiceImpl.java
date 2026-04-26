package com.resume.airesume.service.impl;

import com.resume.airesume.entity.OptimizeHistory;
import com.resume.airesume.entity.Resume;
import com.resume.airesume.mapper.OptimizeHistoryMapper;
import com.resume.airesume.mapper.ResumeMapper;
import com.resume.airesume.service.OptimizeHistoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 优化历史记录服务实现类
 */
@Service
public class OptimizeHistoryServiceImpl implements OptimizeHistoryService {

    @Autowired
    private OptimizeHistoryMapper optimizeHistoryMapper;

    @Autowired
    private ResumeMapper resumeMapper;

    /**
     * 保存优化历史记录
     */
    @Override
    public boolean saveHistory(Long resumeId, Long userId, String targetRole,
                               String originalText, String optimizedText,
                               String originalStructuredData, String optimizedStructuredData) {
        // 创建历史记录对象
        OptimizeHistory history = new OptimizeHistory();
        history.setResumeId(resumeId);
        history.setUserId(userId);
        history.setTargetRole(targetRole);
        history.setOriginalText(originalText);
        history.setOptimizedText(optimizedText);
        history.setOriginalStructuredData(originalStructuredData);
        history.setOptimizedStructuredData(optimizedStructuredData);
        history.setCreatedAt(LocalDateTime.now());

        // 插入数据库
        return optimizeHistoryMapper.insert(history) > 0;
    }

    /**
     * 获取某份简历的优化历史列表
     */
    @Override
    public List<OptimizeHistory> getHistoryList(Long resumeId, Long userId) {
        // 验证简历归属
        Resume resume = resumeMapper.findById(resumeId);
        if (resume == null) {
            throw new RuntimeException("简历不存在");
        }
        if (!resume.getUserId().equals(userId)) {
            throw new RuntimeException("无权访问此简历的历史记录");
        }

        // 查询历史列表
        return optimizeHistoryMapper.findByResumeId(resumeId);
    }

    /**
     * 获取优化历史详情
     */
    @Override
    public OptimizeHistory getHistoryDetail(Long id, Long userId) {
        // 查询历史记录
        OptimizeHistory history = optimizeHistoryMapper.findById(id);
        if (history == null) {
            throw new RuntimeException("历史记录不存在");
        }

        // 验证归属
        if (!history.getUserId().equals(userId)) {
            throw new RuntimeException("无权访问此历史记录");
        }

        return history;
    }
}