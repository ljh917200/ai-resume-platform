package com.resume.airesume.util;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Base64;

/**
 * 头像注入工具类
 * 作用：在AI生成的简历HTML中，手动注入头像img标签
 *
 * 为什么不让AI生成img标签？
 * - AI生成的img标签经常不符合XHTML规范，导致PDF导出失败
 * - 自己注入可以100%控制格式，确保PDF兼容
 * - 使用base64嵌入图片，PDF导出时不依赖外部URL
 */
@Component
public class AvatarUtil {

    @Value("${file.upload.path:./uploads}")
    private String uploadPath;

    /**
     * 在简历HTML中注入头像
     * 策略：使用 float:right 将头像放在右上角，姓名和联系方式自然在左侧
     * 这是最经典的简历头像布局，所有模板都兼容
     *
     * 效果：
     * ┌─────────────────────────────────────┐
     * │  赖俊煌                    [头像]    │
     * │  电话：138xxxx │ 邮箱：xxx          │
     * │  求职意向：前端开发工程师           │
     * │─────────────────────────────────────│
     * │  教育经历                           │
     * │  ...                                │
     * └─────────────────────────────────────┘
     *
     * @param html AI生成的简历HTML
     * @param avatarUrl 用户头像的相对路径（如 /uploads/avatars/user_1_xxx.jpg）
     * @param structuredDataJson 结构化数据JSON（本方法暂不使用，保留参数兼容）
     * @return 注入头像后的HTML
     */
    public String injectAvatar(String html, String avatarUrl, String structuredDataJson) {
        if (html == null || avatarUrl == null || avatarUrl.isEmpty()) {
            return html;
        }

        try {
            // 1. 将头像文件转为base64
            String base64Data = convertAvatarToBase64(avatarUrl);
            if (base64Data == null) {
                System.err.println("[头像注入] 头像文件不存在，跳过注入");
                return html;
            }

            // 2. 构建头像HTML（float:right，姓名自然在左侧）
            String avatarHtml = "<img src=\"data:image/jpeg;base64," + base64Data + "\" " +
                    "width=\"90\" height=\"90\" " +
                    "style=\"float:right; margin:0 0 10px 15px; border-radius:90%;\" " +
                    "alt=\"头像\"/>";

            // 3. 找到container div，在内容开头插入头像
            int insertPos = findContainerInsertPosition(html);
            if (insertPos == -1) {
                System.err.println("[头像注入] 未找到插入位置，跳过");
                return html;
            }

            // 4. 插入头像 + 清除浮动（确保头像下方的模块正常显示）
            String clearDiv = "<div style=\"clear:both; height:0; overflow:hidden;\"></div>";

            // 找到第一个模块标题（通常以 border-bottom 或特定样式标识），
            // 在其前面插入清除浮动，这样头像只占据头部区域
            int clearPos = findClearPosition(html, insertPos);

            String result;
            if (clearPos > insertPos) {
                // 在模块标题前插入清除浮动
                result = html.substring(0, insertPos) + avatarHtml +
                        html.substring(insertPos, clearPos) + clearDiv +
                        html.substring(clearPos);
            } else {
                // 没找到合适位置，直接插入头像（不加清除浮动也OK）
                result = html.substring(0, insertPos) + avatarHtml + html.substring(insertPos);
            }

            System.out.println("[头像注入] 头像注入成功（float:right布局）");
            return result;

        } catch (Exception e) {
            System.err.println("[头像注入] 注入失败：" + e.getMessage());
            e.printStackTrace();
            return html;
        }
    }

    /**
     * 找到container div内容开始的位置
     */
    private int findContainerInsertPosition(String html) {
        // 方式1：找 class="container"
        int containerIndex = html.indexOf("class=\"container\"");
        if (containerIndex != -1) {
            int closePos = html.indexOf(">", containerIndex);
            if (closePos != -1) {
                return closePos + 1;
            }
        }

        // 方式2：找 <body> 标签
        int bodyIndex = html.indexOf("<body>");
        if (bodyIndex != -1) {
            return bodyIndex + "<body>".length();
        }

        return -1;
    }

    /**
     * 找到清除浮动的位置
     * 策略：在第一个模块标题前清除浮动，这样头像只占据姓名区域
     * 模块标题的特征：包含 border-bottom 的样式，或者 class 含 section/header/title
     */
    private int findClearPosition(String html, int startPos) {
        // 在container内容中查找模块分隔标志
        // 优先找 border-bottom（模块标题通常有下边框线）
        String[] markers = {
                "border-bottom",
                "section-title",
                "section_header",
                "module-title",
                "header-title"
        };

        int bestPos = -1;

        for (String marker : markers) {
            int pos = html.indexOf(marker, startPos + 100); // 跳过头部区域
            if (pos != -1 && (bestPos == -1 || pos < bestPos)) {
                // 找到标记所在的标签开头
                int tagStart = html.lastIndexOf("<", pos);
                if (tagStart > startPos) {
                    bestPos = tagStart;
                }
            }
        }

        return bestPos;
    }

    /**
     * 将头像文件转为base64字符串
     * 会尝试多种路径查找文件
     */
    private String convertAvatarToBase64(String avatarUrl) {
        try {
            // 从URL提取相对路径部分
            String relativePath = avatarUrl;
            if (relativePath.startsWith("/uploads/")) {
                relativePath = relativePath.substring("/uploads/".length());
            } else if (relativePath.startsWith("uploads/")) {
                relativePath = relativePath.substring("uploads/".length());
            }

            // 尝试多种路径
            Path[] possiblePaths = {
                    Paths.get(uploadPath, relativePath),
                    Paths.get(uploadPath, avatarUrl.startsWith("/") ? avatarUrl.substring(1) : avatarUrl),
                    Paths.get(System.getProperty("user.dir"), avatarUrl.startsWith("/") ? avatarUrl.substring(1) : avatarUrl),
                    Paths.get(System.getProperty("user.dir"), "uploads", relativePath)
            };

            for (int i = 0; i < possiblePaths.length; i++) {
                Path path = possiblePaths[i];
                if (Files.exists(path)) {
                    byte[] imageBytes = Files.readAllBytes(path);
                    System.out.println("[头像注入] 找到头像：" + path.toAbsolutePath() + "，大小：" + imageBytes.length + "字节");
                    return Base64.getEncoder().encodeToString(imageBytes);
                }
            }

            System.err.println("[头像注入] 所有路径均未找到头像文件");
            System.err.println("[头像注入] uploadPath=" + uploadPath + ", avatarUrl=" + avatarUrl);
            return null;
        } catch (Exception e) {
            System.err.println("[头像注入] base64转换失败：" + e.getMessage());
            return null;
        }
    }
}





/*
package com.resume.airesume.util;

import com.resume.airesume.entity.User;
import com.resume.airesume.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Base64;

*/
/**
 * 头像注入工具类（v2.0 表格布局版）
 *
 * 作用：在AI生成的简历HTML中，手动注入头像
 *
 * v2.0 改进：
 * - 使用表格布局代替 float:right，头像独占右侧列，绝不遮挡任何内容
 * - 方法签名改为传入 userId，内部自动查找头像，Controller 不用操心
 * - 多重回退策略：表格布局 → float:right → 放弃注入
 *
 * 为什么不让AI生成img标签？
 * - AI生成的img标签经常不符合XHTML规范，导致PDF导出失败
 * - 自己注入可以100%控制格式，确保PDF兼容
 * - 使用base64嵌入图片，PDF导出时不依赖外部URL
 *//*

@Component
public class AvatarUtil {

    @Value("${file.upload.path:./uploads}")
    private String uploadPath;

    @Autowired
    private UserMapper userMapper;

    // 简历常见模块标题关键词（用于检测"头部区域"的结束位置）
    private static final String[] SECTION_KEYWORDS = {
            "教育", "工作经历", "实习", "项目经历", "专业技能", "自我评价",
            "荣誉", "证书", "校园经历", "社会实践", "研究", "语言能力"
    };

    */
/**
     * 在简历HTML中注入头像（主入口）
     *
     * 策略：将头部区域（姓名+联系方式）包裹在2列表格中
     * 左列=原始头部内容（78%宽度），右列=头像（22%宽度）
     * 头像独占右侧列，不会遮挡左侧任何内容
     *
     * 效果：
     * ┌─────────────────────────────────────┐
     * │ 赖俊煌                    [头像]    │
     * │ 电话：138xxxx  邮箱：xxx            │
     * │ 求职意向：前端开发工程师             │
     * ├─────────────────────────────────────┤
     * │ 教育经历                            │
     * │ ...                                 │
     * └─────────────────────────────────────┘
     *
     * @param html AI生成的简历HTML（纯HTML，不含头像）
     * @param userId 用户ID（内部自动查找头像）
     * @param structuredDataJson 结构化数据JSON（本方法暂不使用，保留参数兼容）
     * @return 注入头像后的HTML
     *//*

    public String injectAvatar(String html, Long userId, String structuredDataJson) {
        if (html == null || userId == null) {
            return html;
        }

        try {
            // 1. 查找用户头像信息
            User user = userMapper.findById(userId);
            if (user == null || user.getAvatarUrl() == null || user.getAvatarUrl().isEmpty()) {
                System.out.println("[头像注入] 用户无头像，跳过");
                return html;
            }

            // 2. 将头像文件转为base64
            String base64Data = convertAvatarToBase64(user.getAvatarUrl());
            if (base64Data == null) {
                System.err.println("[头像注入] 头像文件不存在，跳过注入");
                return html;
            }

            // 3. 检测图片格式（用于构建正确的data URI）
            String mimeType = detectMimeType(user.getAvatarUrl());

            // 4. 尝试方案A：表格布局注入（推荐，不会遮挡任何内容）
            String result = tryTableLayoutInjection(html, base64Data, mimeType);
            if (result != null) {
                System.out.println("[头像注入] 表格布局注入成功");
                return result;
            }

            // 5. 方案A失败，回退到方案B：float:right注入
            result = tryFloatRightInjection(html, base64Data, mimeType);
            if (result != null) {
                System.out.println("[头像注入] float:right布局注入成功（回退方案）");
                return result;
            }

            System.err.println("[头像注入] 所有注入方式均失败，返回原始HTML");
            return html;

        } catch (Exception e) {
            System.err.println("[头像注入] 注入失败：" + e.getMessage());
            e.printStackTrace();
            return html;
        }
    }

    // ==================== 方案A：表格布局（推荐） ====================

    */
/**
     * 表格布局注入
     * 将头部区域包裹在2列表格中：左列=原始内容，右列=头像
     * 这是PDF最稳定的布局方式，头像绝不会遮挡任何内容
     *//*

    private String tryTableLayoutInjection(String html, String base64Data, String mimeType) {
        try {
            // 1. 找到container内容的起始位置
            int contentStart = findContainerContentStart(html);
            if (contentStart == -1) {
                System.err.println("[头像注入-表格] 未找到container，跳过");
                return null;
            }

            // 2. 找到第一个模块标题的位置（= 头部区域的结束位置）
            int sectionStart = findFirstSectionPosition(html, contentStart);
            if (sectionStart == -1 || sectionStart <= contentStart) {
                System.err.println("[头像注入-表格] 未找到模块分隔位置，跳过");
                return null;
            }

            // 3. 提取头部内容（姓名 + 联系方式 + 求职意向等）
            String headerContent = html.substring(contentStart, sectionStart).trim();

            // 4. 安全检查：头部内容不能太长（超过50%的HTML可能是检测有误）
            if (headerContent.length() > html.length() * 0.5) {
                System.err.println("[头像注入-表格] 头部内容过长，可能检测有误，跳过");
                return null;
            }

            // 5. 构建头像img标签（XHTML规范：必须自闭合）
            String avatarImg = "<img src=\"data:" + mimeType + ";base64," + base64Data + "\" " +
                    "width=\"80\" height=\"80\" " +
                    "style=\"border-radius:50%;\" " +
                    "alt=\"头像\"/>";

            // 6. 构建2列表格（左列=原始头部，右列=头像）
            String tableHtml = "<table width=\"100%\" cellpadding=\"0\" cellspacing=\"0\" border=\"0\" " +
                    "style=\"margin-bottom:10px;\">" +
                    "<tr>" +
                    "<td valign=\"top\" style=\"width:78%;\">" +
                    headerContent +
                    "</td>" +
                    "<td valign=\"top\" style=\"width:22%; text-align:right; padding-left:10px;\">" +
                    avatarImg +
                    "</td>" +
                    "</tr>" +
                    "</table>";

            // 7. 替换原始头部内容为表格布局
            String result = html.substring(0, contentStart) + tableHtml + html.substring(sectionStart);
            return result;

        } catch (Exception e) {
            System.err.println("[头像注入-表格] 表格布局注入失败：" + e.getMessage());
            return null;
        }
    }

    // ==================== 方案B：float:right（回退方案） ====================

    */
/**
     * float:right注入（回退方案）
     * 当表格布局检测失败时使用，可能在某些模板上与右侧内容有轻微重叠
     *//*

    private String tryFloatRightInjection(String html, String base64Data, String mimeType) {
        try {
            int contentStart = findContainerContentStart(html);
            if (contentStart == -1) {
                return null;
            }

            // 构建头像img标签（float:right布局）
            String avatarHtml = "<img src=\"data:" + mimeType + ";base64," + base64Data + "\" " +
                    "width=\"80\" height=\"80\" " +
                    "style=\"float:right; margin:0 0 10px 15px; border-radius:50%;\" " +
                    "alt=\"头像\"/>";

            // 在container内容开头插入头像
            String clearDiv = "<div style=\"clear:both;\"></div>";

            // 找到清除浮动的位置（在第一个模块标题前）
            int clearPos = findClearPosition(html, contentStart);

            String result;
            if (clearPos > contentStart) {
                result = html.substring(0, contentStart) + avatarHtml +
                        html.substring(contentStart, clearPos) + clearDiv + html.substring(clearPos);
            } else {
                result = html.substring(0, contentStart) + avatarHtml + html.substring(contentStart);
            }
            return result;

        } catch (Exception e) {
            System.err.println("[头像注入-float] float:right注入失败：" + e.getMessage());
            return null;
        }
    }

    // ==================== 头部区域检测工具方法 ====================

    */
/**
     * 找到container div内容开始的位置
     * 即container标签的">"之后的第一个字符位置
     *//*

    private int findContainerContentStart(String html) {
        // 方式1：找 class="container"
        int containerIndex = html.indexOf("class=\"container\"");
        if (containerIndex != -1) {
            int closePos = html.indexOf(">", containerIndex);
            if (closePos != -1) {
                return closePos + 1;
            }
        }

        // 方式2：找 <body> 标签
        int bodyIndex = html.indexOf("<body");
        if (bodyIndex != -1) {
            int closePos = html.indexOf(">", bodyIndex);
            if (closePos != -1) {
                return closePos + 1;
            }
        }

        return -1;
    }

    */
/**
     * 找到第一个模块标题的位置（头部区域的结束边界）
     *
     * 检测策略（按优先级）：
     * 1. 找h2/h3/h4标签中包含模块关键词的（最可靠）
     * 2. 找任意h2/h3标签（可能是模块标题）
     * 3. 找class含section/title的div
     *//*

    private int findFirstSectionPosition(String html, int startPos) {
        int bestPos = -1;

        // ===== 策略1：找包含模块关键词的h2/h3/h4标签 =====
        String[] headingTags = {"h2", "h3", "h4"};
        for (String tag : headingTags) {
            String openTag = "<" + tag;
            int searchFrom = startPos;

            while (searchFrom < html.length()) {
                int tagPos = html.indexOf(openTag, searchFrom);
                if (tagPos == -1) break;

                // 找到闭合标签
                String closeTag = "</" + tag + ">";
                int closePos = html.indexOf(closeTag, tagPos);
                if (closePos == -1) break;

                // 提取标题文本内容
                int contentStart = html.indexOf(">", tagPos) + 1;
                if (contentStart > closePos) {
                    searchFrom = closePos + closeTag.length();
                    continue;
                }
                String headingText = html.substring(contentStart, closePos);

                // 检查是否包含模块关键词
                for (String keyword : SECTION_KEYWORDS) {
                    if (headingText.contains(keyword)) {
                        if (bestPos == -1 || tagPos < bestPos) {
                            bestPos = tagPos;
                        }
                        break;
                    }
                }

                searchFrom = closePos + closeTag.length();
            }
        }

        if (bestPos != -1) return bestPos;

        // ===== 策略2：找任意h2/h3标签 =====
        for (String tag : headingTags) {
            int pos = html.indexOf("<" + tag, startPos + 50); // 跳过头部一小段避免匹配到名字
            if (pos != -1 && (bestPos == -1 || pos < bestPos)) {
                bestPos = pos;
            }
        }

        if (bestPos != -1) return bestPos;

        // ===== 策略3：找含section/title的class =====
        String[] sectionClasses = {"section-title", "section_header", "module-title", "section-title"};
        for (String cls : sectionClasses) {
            int pos = html.indexOf(cls, startPos);
            if (pos != -1) {
                // 找到包含这个class的标签开头
                int tagStart = html.lastIndexOf("<", pos);
                if (tagStart >= startPos && (bestPos == -1 || tagStart < bestPos)) {
                    bestPos = tagStart;
                }
            }
        }

        return bestPos;
    }

    */
/**
     * 找到清除浮动的位置（float:right回退方案使用）
     * 在第一个模块标题前清除浮动，头像只占据头部区域
     *//*

    private int findClearPosition(String html, int startPos) {
        String[] markers = {
                "border-bottom", "section-title", "section_header",
                "module-title", "header-title"
        };

        int bestPos = -1;
        for (String marker : markers) {
            int pos = html.indexOf(marker, startPos + 100);
            if (pos != -1 && (bestPos == -1 || pos < bestPos)) {
                int tagStart = html.lastIndexOf("<", pos);
                if (tagStart > startPos) {
                    bestPos = tagStart;
                }
            }
        }
        return bestPos;
    }

    // ==================== 文件处理工具方法 ====================

    */
/**
     * 将头像文件转为base64字符串
     * 会尝试多种路径查找文件
     *//*

    private String convertAvatarToBase64(String avatarUrl) {
        try {
            // 从URL提取相对路径部分
            String relativePath = avatarUrl;
            if (relativePath.startsWith("/uploads/")) {
                relativePath = relativePath.substring("/uploads/".length());
            } else if (relativePath.startsWith("uploads/")) {
                relativePath = relativePath.substring("uploads/".length());
            }

            // 尝试多种路径组合
            Path[] possiblePaths = {
                    // 优先：uploadPath + 相对路径（最常见）
                    Paths.get(uploadPath, relativePath),
                    // 备选：uploadPath + 去掉前导斜杠的avatarUrl
                    Paths.get(uploadPath, avatarUrl.startsWith("/") ? avatarUrl.substring(1) : avatarUrl),
                    // 备选：项目工作目录 + 完整路径
                    Paths.get(System.getProperty("user.dir"), avatarUrl.startsWith("/") ? avatarUrl.substring(1) : avatarUrl),
                    // 备选：项目工作目录 + uploads + 相对路径
                    Paths.get(System.getProperty("user.dir"), "uploads", relativePath)
            };

            for (Path path : possiblePaths) {
                if (Files.exists(path)) {
                    byte[] imageBytes = Files.readAllBytes(path);
                    System.out.println("[头像注入] 找到头像文件：" + path.toAbsolutePath() +
                            "，大小：" + imageBytes.length + "字节");
                    return Base64.getEncoder().encodeToString(imageBytes);
                }
            }

            System.err.println("[头像注入] 所有路径均未找到头像文件");
            System.err.println("[头像注入] uploadPath=" + uploadPath + ", avatarUrl=" + avatarUrl);
            return null;

        } catch (Exception e) {
            System.err.println("[头像注入] base64转换失败：" + e.getMessage());
            return null;
        }
    }

    */
/**
     * 根据文件扩展名检测图片MIME类型
     *//*

    private String detectMimeType(String avatarUrl) {
        if (avatarUrl == null) return "image/jpeg";
        String lower = avatarUrl.toLowerCase();
        if (lower.endsWith(".png")) return "image/png";
        if (lower.endsWith(".gif")) return "image/gif";
        if (lower.endsWith(".webp")) return "image/webp";
        // 默认jpeg（包括.jpg和.jpeg）
        return "image/jpeg";
    }
}*/
