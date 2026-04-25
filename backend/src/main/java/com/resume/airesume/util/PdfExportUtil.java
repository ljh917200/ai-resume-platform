package com.resume.airesume.util;

import com.itextpdf.io.font.PdfEncodings;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Text;
import com.itextpdf.layout.properties.TextAlignment;

import java.io.ByteArrayOutputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * PDF导出工具类
 * 功能：将简历内容导出为PDF格式
 * 依赖：iText 7
 */
public class PdfExportUtil {

    // 中文字体路径（使用iText内置的亚洲字体）
    private static final String CHINESE_FONT = "STSong-Light";

    /**
     * 生成简历PDF
     *
     * @param content 简历内容
     * @param title 简历标题
     * @return PDF字节数组
     * @throws Exception 生成失败时抛出异常
     */
    public static byte[] generateResumePdf(String content, String title) throws Exception {
        // 创建输出流
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        // 创建PDF写入器
        PdfWriter writer = new PdfWriter(outputStream);

        // 创建PDF文档
        PdfDocument pdfDoc = new PdfDocument(writer);

        // 创建文档对象
        Document document = new Document(pdfDoc);

        try {
            // 创建中文字体
            PdfFont font = PdfFontFactory.createFont(CHINESE_FONT, "UniGB-UCS2-H");

            // 设置文档默认字体
            document.setFont(font);

            // 添加标题
            Paragraph titleParagraph = new Paragraph(title)
                    .setFontSize(20)
                    .setBold()
                    .setTextAlignment(TextAlignment.CENTER)
                    .setMarginBottom(20);
            document.add(titleParagraph);

            // 添加分隔线
            Paragraph separator = new Paragraph("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
                    .setTextAlignment(TextAlignment.CENTER)
                    .setMarginBottom(20)
                    .setFontSize(10);
            document.add(separator);

            // 处理内容：按段落分割
            String[] paragraphs = content.split("\n");
            for (String para : paragraphs) {
                if (para.trim().isEmpty()) {
                    // 空行
                    document.add(new Paragraph(" ").setMarginBottom(5));
                } else {
                    // 普通段落
                    Paragraph paragraph = new Paragraph(para.trim())
                            .setFontSize(11)
                            .setMarginBottom(8)
                            .setTextAlignment(TextAlignment.JUSTIFIED);
                    document.add(paragraph);
                }
            }

            // 添加页脚：导出时间
            Paragraph footer = new Paragraph()
                    .setMarginTop(30)
                    .setFontSize(9);
            footer.add(new Text("导出时间：" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")))
                    .setItalic());
            document.add(footer);

            // 关闭文档
            document.close();

            return outputStream.toByteArray();

        } catch (Exception e) {
            document.close();
            throw new Exception("PDF生成失败: " + e.getMessage(), e);
        }
    }

    /**
     * 生成文件名
     * 格式：简历_姓名_日期.pdf
     *
     * @param username 用户名
     * @return 文件名
     */
    public static String generateFileName(String username) {
        String date = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        return String.format("简历_%s_%s.pdf", username, date);
    }
}