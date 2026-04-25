package com.resume.airesume.mapper;

import com.resume.airesume.entity.Resume;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface ResumeMapper {

    // 根据ID查询简历
    @Select("SELECT * FROM resume WHERE id = #{id}")
    Resume findById(Long id);

    // 根据用户ID查询简历列表
    @Select("SELECT * FROM resume WHERE user_id = #{userId} ORDER BY created_at DESC")
    List<Resume> findByUserId(Long userId);

    // 插入简历
    @Insert("INSERT INTO resume(user_id, original_text, structured_data, file_name, file_format, created_at) " +
            "VALUES(#{userId}, #{originalText}, #{structuredData}, #{fileName}, #{fileFormat}, #{createdAt})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insert(Resume resume);

    // 删除简历
    @Delete("DELETE FROM resume WHERE id = #{id}")
    int deleteById(Long id);

    // 更新简历
    @Update("UPDATE resume SET original_text=#{originalText}, structured_data=#{structuredData}, optimized_structured_data=#{optimizedStructuredData} WHERE id=#{id}")
    int update(Resume resume);

    // 更新优化内容
    @Update("UPDATE resume SET optimized_text = #{optimizedText},optimized_structured_data=#{optimizedStructuredData}, last_optimized_at = #{lastOptimizedAt} WHERE id = #{id}")
    void updateOptimizedText(Resume resume);
}