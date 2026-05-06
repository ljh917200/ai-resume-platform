package com.resume.airesume.mapper;

import com.resume.airesume.entity.JobApplication;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;
import java.util.Map;

/**
 * 投递记录Mapper接口
 * SQL映射在 resources/mapper/ApplicationMapper.xml 中
 */
@Mapper
public interface ApplicationMapper {

    /**
     * 新增投递记录
     * @param application 投递记录实体
     * @return 影响行数
     */
    int insert(JobApplication application);

    /**
     * 根据ID更新投递记录（只更新非空字段）
     * @param application 投递记录实体
     * @return 影响行数
     */
    int updateById(JobApplication application);

    /**
     * 根据ID查询投递记录（过滤已删除）
     * @param id 主键ID
     * @return 投递记录
     */
    JobApplication selectById(@Param("id") Long id);

    /**
     * 查询用户的投递记录列表（支持状态过滤+分页）
     * @param userId 用户ID
     * @param status 投递状态（可选，为null查全部）
     * @param offset 偏移量
     * @param limit 每页数量
     * @return 投递记录列表
     */
    List<JobApplication> selectByUserId(@Param("userId") Long userId,
                                        @Param("status") String status,
                                        @Param("offset") Integer offset,
                                        @Param("limit") Integer limit);

    /**
     * 统计用户的投递记录数量（支持状态过滤）
     * @param userId 用户ID
     * @param status 投递状态（可选）
     * @return 数量
     */
    int countByUserId(@Param("userId") Long userId, @Param("status") String status);

    /**
     * 查询用户投递记录列表（支持关键词+状态+渠道+分页）
     * keyword 同时匹配公司名称和职位名称（模糊查询）
     * @param userId   用户ID
     * @param status   投递状态（可选）
     * @param keyword  搜索关键词，匹配公司名称或职位名称（可选）
     * @param source   投递渠道（可选）
     * @param offset   偏移量
     * @param limit    每页数量
     * @return 投递记录列表
     */
    List<JobApplication> selectByUserIdWithFilter(@Param("userId") Long userId,
                                                  @Param("status") String status,
                                                  @Param("keyword") String keyword,
                                                  @Param("source") String source,
                                                  @Param("offset") Integer offset,
                                                  @Param("limit") Integer limit);

    /**
     * 统计用户投递记录数量（支持关键词+状态+渠道）
     * 与 selectByUserIdWithFilter 配合使用，用于分页总数计算
     * @param userId   用户ID
     * @param status   投递状态（可选）
     * @param keyword  搜索关键词（可选）
     * @param source   投递渠道（可选）
     * @return 符合条件的记录数
     */
    int countByUserIdWithFilter(@Param("userId") Long userId,
                                @Param("status") String status,
                                @Param("keyword") String keyword,
                                @Param("source") String source);

    /**
     * 看板数据：按状态分组统计数量
     * @param userId 用户ID
     * @return [{status: "applied", count: 5}, ...]
     */
    List<Map<String, Object>> selectBoardData(@Param("userId") Long userId);

    /**
     * 更新投递状态
     * @param id 主键ID
     * @param status 新状态
     * @return 影响行数
     */
    int updateStatus(@Param("id") Long id, @Param("status") String status);

    /**
     * 逻辑删除投递记录（将deleted字段设为1）
     * @param id 主键ID
     * @return 影响行数
     */
    int deleteById(@Param("id") Long id);

    /**
     * 批量逻辑删除投递记录
     * SQL中通过 user_id 条件校验归属，防止越权删除别人的记录
     * @param userId 用户ID，用于归属校验
     * @param ids    要删除的记录ID列表
     * @return 影响行数
     */
    int batchDeleteByIds(@Param("userId") Long userId, @Param("ids") List<Long> ids);

    /**
     * 统计用户本周新增投递数量
     * @param userId 用户ID
     * @return 数量
     */
    int countThisWeek(@Param("userId") Long userId);
}
