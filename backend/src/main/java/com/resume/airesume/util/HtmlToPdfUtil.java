package com.resume.airesume.util;

import org.springframework.stereotype.Component;
import org.xhtmlrenderer.pdf.ITextRenderer;
import com.itextpdf.text.pdf.BaseFont;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;

@Component
public class HtmlToPdfUtil {

    public byte[] convertToPdf(String xhtmlContent) throws Exception {
        if (xhtmlContent == null || xhtmlContent.trim().isEmpty()) {
            throw new IllegalArgumentException("HTML内容不能为空");
        }

        // 清理 HTML
        xhtmlContent = cleanHtml(xhtmlContent);

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        ITextRenderer renderer = new ITextRenderer();

        // 加载中文字体
        String fontPath = "/fonts/SimSun.ttf";
        InputStream fontStream = getClass().getResourceAsStream(fontPath);

        if (fontStream != null) {
            renderer.getFontResolver().addFont(
                    fontPath,
                    BaseFont.IDENTITY_H,
                    BaseFont.NOT_EMBEDDED
            );
            fontStream.close();

            // ★ 只注入字体，不注入其他样式，避免覆盖HTML原有样式
            if (!xhtmlContent.contains("@font-face")) {
                String fontStyle =
                        "<style type=\"text/css\">\n" +
                                "  * { font-family: 'SimSun', '宋体', serif !important; }\n" +
                                "</style>\n";
                xhtmlContent = xhtmlContent.replace("</head>", fontStyle + "</head>");
            }
        }

        try {
            renderer.setDocumentFromString(xhtmlContent);
            renderer.layout();
            renderer.createPDF(outputStream);
            renderer.finishPDF();
        } catch (Exception e) {
            System.err.println("PDF转换失败，HTML内容前500字符：");
            System.err.println(xhtmlContent.substring(0, Math.min(500, xhtmlContent.length())));
            throw new Exception("PDF转换失败：" + e.getMessage(), e);
        }

        return outputStream.toByteArray();
    }

    /**
     * 清理HTML，确保符合XHTML标准
     */
    private String cleanHtml(String html) {
        // 确保 DOCTYPE 在最前面
        int doctypeIndex = html.indexOf("<!DOCTYPE");
        if (doctypeIndex > 0) {
            html = html.substring(doctypeIndex);
        }

        // 修复自闭合标签
        html = html.replaceAll("<br\\s*>", "<br/>");
        html = html.replaceAll("<br\\s+/>", "<br/>");
        html = html.replaceAll("<hr\\s*>", "<hr/>");
        html = html.replaceAll("<hr\\s+/>", "<hr/>");
        html = html.replaceAll("<img([^>]*)(?<!/)>", "<img$1/>");
        html = html.replaceAll("<input([^>]*)(?<!/)>", "<input$1/>");

        // 删除空容器
        html = html.replaceAll("<div[^>]*>\\s*</div>", "");
        html = html.replaceAll("<table[^>]*>\\s*</table>", "");
        html = html.replaceAll("<td[^>]*>\\s*</td>", "<td></td>");

        // ★ 不再注入任何padding样式！HTML本身已经包含了所有样式

        return html;
    }
}
