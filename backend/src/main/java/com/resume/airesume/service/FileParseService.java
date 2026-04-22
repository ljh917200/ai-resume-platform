package com.resume.airesume.service;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

/**
 * 文件解析服务
 * 作用：解析 PDF 和 Word 文件，提取文字内容
 */
@Service
public class FileParseService {

    /**
     * 解析文件，自动识别格式
     *
     * @param file 上传的文件
     * @return 提取的文字内容
     */
    public String parseFile(MultipartFile file) throws IOException {
        // 获取文件名
        String fileName = file.getOriginalFilename();

        if (fileName == null) {
            throw new IOException("文件名不能为空");
        }

        // 转小写，方便判断后缀
        String lowerName = fileName.toLowerCase();

        // 根据后缀选择解析方法
        if (lowerName.endsWith(".pdf")) {
            return parsePdf(file.getInputStream());
        } else if (lowerName.endsWith(".docx")) {
            return parseDocx(file.getInputStream());
        } else if (lowerName.endsWith(".doc")) {
            throw new IOException("暂不支持 .doc 格式，请转换为 .docx 格式");
        } else {
            throw new IOException("不支持的文件格式，仅支持 PDF 和 DOCX");
        }
    }

    /**
     * 解析 PDF 文件
     *
     * @param inputStream 文件输入流
     * @return 提取的文字内容
     */
    private String parsePdf(InputStream inputStream) throws IOException {
        // 先把 InputStream 转成 byte[]
        byte[] bytes = inputStream.readAllBytes();

        // PDFBox 3.0 使用 Loader.loadPDF() 加载文档
        try (PDDocument document = Loader.loadPDF(bytes)) {

            // PDFTextStripper 用于提取 PDF 中的文字
            PDFTextStripper stripper = new PDFTextStripper();

            // 设置提取范围（从第1页到最后一页）
            stripper.setStartPage(1);
            stripper.setEndPage(document.getNumberOfPages());

            // 提取文字
            String text = stripper.getText(document);

            // 清理多余的空白字符
            return text.trim();
        }
    }

    /**
     * 解析 DOCX 文件（Word 2007+）
     *
     * @param inputStream 文件输入流
     * @return 提取的文字内容
     */
    private String parseDocx(InputStream inputStream) throws IOException {
        // XWPFDocument 是 POI 的核心类，代表一个 Word 文档
        try (XWPFDocument document = new XWPFDocument(inputStream)) {

            // 获取所有段落
            List<XWPFParagraph> paragraphs = document.getParagraphs();

            // 拼接所有段落的文字
            StringBuilder sb = new StringBuilder();
            for (XWPFParagraph paragraph : paragraphs) {
                String text = paragraph.getText();
                if (text != null && !text.trim().isEmpty()) {
                    sb.append(text).append("\n");
                }
            }

            return sb.toString().trim();
        }
        // try-with-resources 会自动关闭 document
    }
}