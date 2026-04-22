package com.resume.airesume.service.impl;

import com.resume.airesume.entity.Resume;
import com.resume.airesume.mapper.ResumeMapper;
import com.resume.airesume.service.FileParseService;
import com.resume.airesume.service.ResumeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class ResumeServiceImpl implements ResumeService {

    @Autowired
    private ResumeMapper resumeMapper;

    @Autowired
    private FileParseService fileParseService;  // 注入文件解析服务

    @Override
    public Resume upload(Long userId, MultipartFile file) {
        try {
            // 解析文件，提取文字
            String originalText = fileParseService.parseFile(file);

            // 获取文件名和格式
            String fileName = file.getOriginalFilename();
            String fileFormat = fileName.substring(fileName.lastIndexOf(".") + 1).toUpperCase();

            // 创建简历对象
            Resume resume = new Resume();
            resume.setUserId(userId);
            resume.setOriginalText(originalText);  // 存储解析后的文字
            resume.setFileName(fileName);
            resume.setFileFormat(fileFormat);
            resume.setCreatedAt(LocalDateTime.now());

            // 保存到数据库
            resumeMapper.insert(resume);

            return resume;

        } catch (Exception e) {
            throw new RuntimeException("文件解析失败：" + e.getMessage());
        }
    }

    @Override
    public List<Resume> getListByUserId(Long userId) {
        return resumeMapper.findByUserId(userId);
    }

    @Override
    public Resume getById(Long id, Long userId) {
        Resume resume = resumeMapper.findById(id);
        // 验证是否是该用户的简历
        if (resume == null) {
            throw new RuntimeException("简历不存在");
        }
        if (!resume.getUserId().equals(userId)) {
            throw new RuntimeException("无权访问此简历");
        }
        return resume;
    }

    @Override
    public boolean delete(Long id, Long userId) {
        // 先验证是否是该用户的简历
        Resume resume = resumeMapper.findById(id);
        if (resume == null) {
            throw new RuntimeException("简历不存在");
        }
        if (!resume.getUserId().equals(userId)) {
            throw new RuntimeException("无权删除此简历");
        }
        return resumeMapper.deleteById(id) > 0;
    }
}