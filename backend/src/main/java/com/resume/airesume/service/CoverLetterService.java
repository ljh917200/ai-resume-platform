package com.resume.airesume.service;

import com.resume.airesume.dto.CoverLetterCreateDTO;
import com.resume.airesume.dto.CoverLetterVO;
import java.util.List;

/**
 * 求职信服务接口
 */
public interface CoverLetterService {

    /**
     * 生成求职信（调用DeepSeek AI）
     * @param userId 用户ID
     * @param dto    生成请求
     * @return 生成结果
     */
    CoverLetterVO generateLetter(Long userId, CoverLetterCreateDTO dto);

    /**
     * 查询求职信详情
     * @param id     记录ID
     * @param userId 用户ID（权限校验）
     * @return 求职信详情
     */
    CoverLetterVO getLetter(Long id, Long userId);

    /**
     * 查询用户的求职信历史列表
     * @param userId 用户ID
     * @return 求职信列表（按时间倒序）
     */
    List<CoverLetterVO> getHistory(Long userId);

    /**
     * 重新生成求职信（换风格或换语言）
     * @param id     记录ID
     * @param userId 用户ID
     * @param style  新风格（可选）
     * @param lang   新语言（可选）
     * @return 重新生成结果
     */
    CoverLetterVO regenerateLetter(Long id, Long userId, String style, String lang);

    /**
     * 保存/取消保存求职信
     * @param id      记录ID
     * @param userId  用户ID
     * @param isSaved 0取消保存 1保存
     */
    void toggleSave(Long id, Long userId, Integer isSaved);

    /**
     * 删除求职信（逻辑删除）
     * @param id     记录ID
     * @param userId 用户ID
     */
    void deleteLetter(Long id, Long userId);
}
