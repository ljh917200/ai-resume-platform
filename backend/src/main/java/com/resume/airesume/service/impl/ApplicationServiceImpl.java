package com.resume.airesume.service.impl;

import com.resume.airesume.dto.ApplicationCreateDTO;
import com.resume.airesume.dto.ApplicationUpdateDTO;
import com.resume.airesume.entity.JobApplication;
import com.resume.airesume.mapper.ApplicationMapper;
import com.resume.airesume.service.ApplicationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 投递记录服务实现类
 * 处理投递记录的业务逻辑，包括CRUD、状态流转、看板数据、统计等
 */
@Service
public class ApplicationServiceImpl implements ApplicationService {

    @Autowired
    private ApplicationMapper applicationMapper;

    /**
     * 创建投递记录
     * 将DTO转为实体，设置默认状态为interested，写入数据库
     */
    @Override
    public JobApplication createApplication(Long userId, ApplicationCreateDTO dto) {
        JobApplication application = new JobApplication();
        application.setUserId(userId);
        application.setCompanyName(dto.getCompanyName());
        application.setCompanyIndustry(dto.getCompanyIndustry());
        application.setCompanySize(dto.getCompanySize());
        application.setCompanyLocation(dto.getCompanyLocation());
        application.setJobTitle(dto.getJobTitle());
        application.setJobCategory(dto.getJobCategory());
        application.setJobLevel(dto.getJobLevel());
        application.setSalaryMin(dto.getSalaryMin());
        application.setSalaryMax(dto.getSalaryMax());
        // 投递渠道，默认"其他"
        application.setSource(dto.getSource() != null ? dto.getSource() : "其他");
        application.setSourceUrl(dto.getSourceUrl());
        // 状态默认为 interested（感兴趣）
        application.setStatus(dto.getStatus() != null ? dto.getStatus() : "interested");
        application.setApplyDate(dto.getApplyDate());
        application.setInterviewDate(dto.getInterviewDate());
        application.setHrName(dto.getHrName());
        application.setNotes(dto.getNotes());
        application.setJobDescription(dto.getJobDescription());
        application.setResumeId(dto.getResumeId());

        applicationMapper.insert(application);
        return application;
    }

    /**
     * 更新投递记录
     * 先验证记录归属当前用户，再只更新传入的非空字段
     */
    @Override
    public JobApplication updateApplication(Long userId, Long id, ApplicationUpdateDTO dto) {
        // 先查询，验证归属
        JobApplication existing = applicationMapper.selectById(id);
        if (existing == null || !existing.getUserId().equals(userId)) {
            throw new RuntimeException("投递记录不存在或无权操作");
        }

        // 构建更新实体，只设置非空字段
        JobApplication update = new JobApplication();
        update.setId(id);
        update.setCompanyName(dto.getCompanyName());
        update.setCompanyIndustry(dto.getCompanyIndustry());
        update.setCompanySize(dto.getCompanySize());
        update.setCompanyLocation(dto.getCompanyLocation());
        update.setJobTitle(dto.getJobTitle());
        update.setJobCategory(dto.getJobCategory());
        update.setJobLevel(dto.getJobLevel());
        update.setSalaryMin(dto.getSalaryMin());
        update.setSalaryMax(dto.getSalaryMax());
        update.setSource(dto.getSource());
        update.setSourceUrl(dto.getSourceUrl());
        update.setStatus(dto.getStatus());
        update.setApplyDate(dto.getApplyDate());
        update.setInterviewDate(dto.getInterviewDate());
        update.setHrName(dto.getHrName());
        update.setNotes(dto.getNotes());
        update.setJobDescription(dto.getJobDescription());
        update.setResumeId(dto.getResumeId());

        applicationMapper.updateById(update);
        // 更新后重新查询返回最新数据
        return applicationMapper.selectById(id);
    }

    /**
     * 获取投递记录详情
     * 验证记录归属当前用户后才返回
     */
    @Override
    public JobApplication getApplication(Long userId, Long id) {
        JobApplication application = applicationMapper.selectById(id);
        if (application == null || !application.getUserId().equals(userId)) {
            throw new RuntimeException("投递记录不存在或无权操作");
        }
        return application;
    }

    /**
     * 查询投递记录列表（支持关键词+状态+渠道+分页）
     * keyword 会同时匹配公司名称和职位名称（模糊查询）
     * 所有过滤条件均为可选，不传则不限制
     */
    @Override
    public Map<String, Object> listApplications(Long userId, String status, String keyword, String source, Integer page, Integer size) {
        // 分页参数默认值
        if (page == null || page < 1) page = 1;
        if (size == null || size < 1) size = 10;
        int offset = (page - 1) * size;

        // 使用带过滤条件的查询方法
        List<JobApplication> list = applicationMapper.selectByUserIdWithFilter(userId, status, keyword, source, offset, size);
        int total = applicationMapper.countByUserIdWithFilter(userId, status, keyword, source);

        // 返回分页数据
        Map<String, Object> result = new HashMap<>();
        result.put("list", list);
        result.put("total", total);
        result.put("page", page);
        result.put("size", size);
        return result;
    }

    /**
     * 获取看板数据（按状态分组统计）
     * 返回每种状态对应的记录数量，用于看板列头展示
     */
    @Override
    public List<Map<String, Object>> getBoardData(Long userId) {
        return applicationMapper.selectBoardData(userId);
    }

    /**
     * 获取统计数据
     * 包含：投递总数、面试中数量、已Offer数量、已拒绝数量、本周新增
     * 面试中数量 = 筛选+笔试+一面+二面+HR面 各状态数量之和
     */
    @Override
    public Map<String, Object> getStats(Long userId) {
        Map<String, Object> stats = new HashMap<>();
        // 投递总数
        stats.put("total", applicationMapper.countByUserId(userId, null));
        // 面试中（筛选、笔试、一面、二面、HR面）
        int interviewing = 0;
        String[] interviewStatuses = {"screening", "test", "first_interview", "second_interview", "hr_interview"};
        for (String s : interviewStatuses) {
            interviewing += applicationMapper.countByUserId(userId, s);
        }
        stats.put("interviewing", interviewing);
        // 已收到offer
        stats.put("offered", applicationMapper.countByUserId(userId, "offer_received"));
        // 已拒绝
        stats.put("rejected", applicationMapper.countByUserId(userId, "rejected"));
        // 本周新增
        stats.put("thisWeekAdded", applicationMapper.countThisWeek(userId));
        return stats;
    }

    /**
     * 更新投递状态
     * 验证记录归属当前用户后才允许修改状态
     */
    @Override
    public void updateStatus(Long userId, Long id, String status) {
        JobApplication existing = applicationMapper.selectById(id);
        if (existing == null || !existing.getUserId().equals(userId)) {
            throw new RuntimeException("投递记录不存在或无权操作");
        }
        applicationMapper.updateStatus(id, status);
    }

    /**
     * 删除投递记录（逻辑删除）
     * 验证记录归属当前用户后才允许删除
     * 实际操作是将 deleted 字段设为1，不是物理删除
     */
    @Override
    public void deleteApplication(Long userId, Long id) {
        JobApplication existing = applicationMapper.selectById(id);
        if (existing == null || !existing.getUserId().equals(userId)) {
            throw new RuntimeException("投递记录不存在或无权操作");
        }
        applicationMapper.deleteById(id);
    }

    /**
     * 批量删除投递记录（逻辑删除）
     * SQL层面通过 user_id 条件校验归属，防止越权删除别人的记录
     * @param userId 用户ID，用于归属校验
     * @param ids    要删除的记录ID列表
     */
    @Override
    public void batchDeleteApplications(Long userId, List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            throw new RuntimeException("请选择要删除的记录");
        }
        // 直接调用批量删除SQL，SQL中通过 user_id 条件防止越权
        applicationMapper.batchDeleteByIds(userId, ids);
    }
}
