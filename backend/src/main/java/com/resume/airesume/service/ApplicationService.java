package com.resume.airesume.service;

import com.resume.airesume.dto.ApplicationCreateDTO;
import com.resume.airesume.dto.ApplicationUpdateDTO;
import com.resume.airesume.entity.JobApplication;
import java.util.List;
import java.util.Map;

/**
 * 投递记录服务接口
 * 定义投递记录的业务操作方法
 */
public interface ApplicationService {

    /**
     * 创建投递记录
     * @param userId 用户ID
     * @param dto    创建请求DTO
     * @return 创建后的投递记录
     */
    JobApplication createApplication(Long userId, ApplicationCreateDTO dto);

    /**
     * 更新投递记录
     * @param userId 用户ID
     * @param id     投递记录ID
     * @param dto    更新请求DTO
     * @return 更新后的投递记录
     */
    JobApplication updateApplication(Long userId, Long id, ApplicationUpdateDTO dto);

    /**
     * 获取投递记录详情
     * @param userId 用户ID
     * @param id     投递记录ID
     * @return 投递记录
     */
    JobApplication getApplication(Long userId, Long id);

    /**
     * 查询投递记录列表（支持关键词+状态+渠道+分页）
     * @param userId  用户ID
     * @param status  投递状态（可选）
     * @param keyword 搜索关键词，匹配公司名称或职位（可选）
     * @param source  投递渠道（可选）
     * @param page    页码
     * @param size    每页数量
     * @return 分页结果 {list, total, page, size}
     */
    Map<String, Object> listApplications(Long userId, String status, String keyword, String source, Integer page, Integer size);

    /**
     * 获取看板数据（按状态分组统计）
     * @param userId 用户ID
     * @return [{status: "applied", count: 5}, ...]
     */
    List<Map<String, Object>> getBoardData(Long userId);

    /**
     * 获取统计数据
     * 包含投递总数、面试中、已Offer、已拒绝、本周新增
     * @param userId 用户ID
     * @return 统计数据Map
     */
    Map<String, Object> getStats(Long userId);

    /**
     * 更新投递状态
     * @param userId 用户ID
     * @param id     投递记录ID
     * @param status 新状态
     */
    void updateStatus(Long userId, Long id, String status);

    /**
     * 删除投递记录（逻辑删除）
     * @param userId 用户ID
     * @param id     投递记录ID
     */
    void deleteApplication(Long userId, Long id);

    /**
     * 批量删除投递记录（逻辑删除）
     * 校验用户归属，防止越权删除
     * @param userId 用户ID
     * @param ids    要删除的记录ID列表
     */
    void batchDeleteApplications(Long userId, List<Long> ids);
}
