package com.resume.airesume.mapper;

import com.resume.airesume.entity.CoverLetter;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

/**
 * 求职信Mapper接口
 */
@Mapper
public interface CoverLetterMapper {

    int insert(CoverLetter coverLetter);

    CoverLetter selectById(@Param("id") Long id);

    List<CoverLetter> selectByUserId(@Param("userId") Long userId);

    int updateById(CoverLetter coverLetter);

    int deleteById(@Param("id") Long id);
}
