package com.resume.airesume.mapper;

import com.resume.airesume.entity.InterviewQuestion;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;
import java.util.Map;

/**
 * 面试题Mapper接口
 */
@Mapper
public interface InterviewQuestionMapper {

    /** 新增面试题 */
    int insert(InterviewQuestion question);

    /** 根据ID查询面试题（过滤已删除） */
    InterviewQuestion selectById(@Param("id") Long id);

    /** 查询用户的面试题列表（按创建时间倒序，过滤已删除） */
    List<InterviewQuestion> selectByUserId(@Param("userId") Long userId);

    /** 按题目类型查询用户的面试题列表 */
    List<InterviewQuestion> selectByUserIdAndType(
            @Param("userId") Long userId,
            @Param("questionType") String questionType
    );

    /** 按准备状态查询用户的面试题列表 */
    List<InterviewQuestion> selectByUserIdAndStatus(
            @Param("userId") Long userId,
            @Param("prepStatus") String prepStatus
    );

    /** 根据ID更新面试题（只更新非空字段） */
    int updateById(InterviewQuestion question);

    /** 逻辑删除面试题 */
    int deleteById(@Param("id") Long id);

    /** 按准备状态分组统计用户的面试题数量 */
    List<Map<String, Object>> selectStatsByStatus(@Param("userId") Long userId);
}
