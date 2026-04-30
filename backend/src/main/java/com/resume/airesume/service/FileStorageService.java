package com.resume.airesume.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

/**
 * 文件存储服务
 * 负责处理文件的上传、删除等操作
 *
 * 存储位置：D:/Study/Work/ai-resume-platform/backend/uploads
 * 访问方式：http://localhost:8080/uploads/avatars/user_1_xxx.jpg
 */
@Service
public class FileStorageService {

    /**
     * 文件存储根路径
     * 从 application.properties 读取：file.upload.path
     * 默认值为 ./uploads（项目根目录下的uploads文件夹）
     */
    @Value("${file.upload.path}")
    private String uploadPath;

    /**
     * 上传头像
     *
     * 流程：
     * 1. 检查文件是否为空
     * 2. 检查文件类型（只允许图片）
     * 3. 检查文件大小（限制2MB）
     * 4. 创建存储目录
     * 5. 生成唯一文件名
     * 6. 保存文件到磁盘
     * 7. 返回访问路径
     *
     * @param file   用户上传的头像文件
     * @param userId 用户ID，用于生成文件名
     * @return 文件访问路径，如 /uploads/avatars/user_1_abc123.jpg
     * @throws IOException 文件操作异常
     */
    public String uploadAvatar(MultipartFile file, Long userId) throws IOException {

        // ========== 第一步：检查文件是否为空 ==========
        if (file.isEmpty()) {
            throw new IOException("文件不能为空");
        }

        // ========== 第二步：检查文件类型 ==========
        // 获取文件的MIME类型，如 image/jpeg、image/png
        String contentType = file.getContentType();

        // 只允许图片类型（image/开头）
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new IOException("只支持图片文件，请上传 jpg、png 格式");
        }

        // ========== 第三步：检查文件大小 ==========
        // 限制最大2MB（2 * 1024 * 1024 字节）
        long maxSize = 2 * 1024 * 1024;
        if (file.getSize() > maxSize) {
            throw new IOException("图片大小不能超过2MB");
        }

        // ========== 第四步：创建存储目录 ==========
        // 头像存储路径：./uploads/avatars/
        String avatarDir = uploadPath + "/avatars";
        File dir = new File(avatarDir);

        // 如果目录不存在，自动创建（包括父目录）
        if (!dir.exists()) {
            dir.mkdirs();
        }

        // ========== 第五步：生成唯一文件名 ==========
        // 获取原始文件名
        String originalFilename = file.getOriginalFilename();

        // 提取文件扩展名（如 .jpg、.png）
        String extension = ".jpg"; // 默认扩展名
        if (originalFilename != null && originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }

        // 生成文件名格式：user_{userId}_{随机8位}.jpg
        // 例如：user_1_abc12345.jpg
        String randomStr = UUID.randomUUID().toString().substring(0, 8);
        String fileName = "user_" + userId + "_" + randomStr + extension;

        // ========== 第六步：保存文件到磁盘 ==========
        // 创建目标文件对象
        File destFile = new File(avatarDir + "/" + fileName);

        // 将上传的文件保存到目标位置
        // transferTo 方法会自动关闭输入流
        file.transferTo(destFile);

        // ========== 第七步：返回访问路径 ==========
        // 返回相对路径，前端可以通过 http://localhost:8080/uploads/avatars/xxx.jpg 访问
        return "/uploads/avatars/" + fileName;
    }

    /**
     * 删除头像文件
     *
     * 作用：用户更换头像时，删除旧头像文件
     *
     * @param avatarUrl 头像路径，如 /uploads/avatars/user_1_abc123.jpg
     */
    public void deleteAvatar(String avatarUrl) {
        // 空值检查
        if (avatarUrl == null || avatarUrl.isEmpty()) {
            return;
        }

        // 从URL提取文件物理路径
        // /uploads/avatars/xxx.jpg -> ./uploads/avatars/xxx.jpg
        String filePath = uploadPath + avatarUrl.replace("/uploads", "");
        File file = new File(filePath);

        // 如果文件存在，删除它
        if (file.exists()) {
            file.delete();
            System.out.println("[文件存储] 已删除头像：" + filePath);
        }
    }
}