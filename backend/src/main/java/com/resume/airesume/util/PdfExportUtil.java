package com.resume.airesume.util;

import com.itextpdf.kernel.colors.Color;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.kernel.pdf.canvas.draw.SolidLine;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.borders.Border;
import com.itextpdf.layout.element.*;
import com.itextpdf.layout.properties.Leading;
import com.itextpdf.layout.properties.Property;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;

/**
 * PDF导出工具类
 * 支持动态排版，根据结构化数据自动生成简历PDF
 * 使用iText 7版本
 *
 * @author AI Resume Platform
 * @version 1.3.0
 */
@Component
public class PdfExportUtil {

    // 主色调（深蓝色，用于标题）
    private static final Color PRIMARY_COLOR = new DeviceRgb(64, 158, 255);

    // 次要颜色（深灰色，用于日期）
    private static final Color GRAY_COLOR = new DeviceRgb(100, 100, 100);

    // 浅灰色（用于分隔线）
    private static final Color LIGHT_GRAY = new DeviceRgb(220, 220, 220);

    public byte[] generatePdfFromStructuredData(String structuredData, String originalText) throws Exception {


        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        PdfWriter writer = new PdfWriter(outputStream);
        PdfDocument pdfDoc = new PdfDocument(writer);
        Document document = new Document(pdfDoc);

        document.setMargins(50, 45, 50, 45);

        PdfFont chineseFont = PdfFontFactory.createFont(
                "STSong-Light", "UniGB-UCS2-H", PdfFontFactory.EmbeddingStrategy.PREFER_EMBEDDED
        );
        document.setFont(chineseFont);

        if (structuredData == null || structuredData.trim().isEmpty() || "null".equals(structuredData)) {
            generateSimplePdf(document, originalText);
        } else {
            JSONObject json = new JSONObject(structuredData);
            generateStructuredPdf(document, json);
        }

        document.close();
        return outputStream.toByteArray();
    }

    private void generateStructuredPdf(Document document, JSONObject json) throws Exception {

        // 1. 姓名（标题，居中显示）
        String name = json.optString("name", null);
        if (name != null && !name.isEmpty()) {
            Paragraph titleParagraph = new Paragraph(name)
                    .setFontSize(26)
                    .setBold()
                    .setFontColor(PRIMARY_COLOR)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setMarginBottom(8);
            document.add(titleParagraph);
        }

        // 2. 联系信息
        addContactInfo(document, json);

        // 3. 分隔线
        addSeparator(document);

        String selfEvaluation = json.optString("selfEvaluation", null);
        if (selfEvaluation != null && !selfEvaluation.isEmpty()) {
            addSection(document, "自我评价");
            Paragraph evalParagraph = new Paragraph(selfEvaluation)
                    .setFontSize(11)
                    .setMarginBottom(15);
            document.add(evalParagraph);
        }

        JSONArray education = json.optJSONArray("education");
        if (education != null && education.length() > 0) {
            addSection(document, "教育经历");
            addEducationItems(document, education);
        }

        JSONArray experience = json.optJSONArray("experience");
        if (experience != null && experience.length() > 0) {
            addSection(document, "工作经历");
            addExperienceItems(document, experience);
        }

        JSONArray projects = json.optJSONArray("projects");
        if (projects != null && projects.length() > 0) {
            addSection(document, "项目经历");
            addProjectItems(document, projects);
        }

        JSONArray skills = json.optJSONArray("skills");
        if (skills != null && skills.length() > 0) {
            addSection(document, "技能特长");
            addSkillsItems(document, skills);
        }

        JSONArray awards = json.optJSONArray("awards");
        if (awards != null && awards.length() > 0) {
            addSection(document, "获奖情况");
            addAwardItems(document, awards);
        }

        JSONArray competitions = json.optJSONArray("competitions");
        if (competitions != null && competitions.length() > 0) {
            addSection(document, "比赛经历");
            addCompetitionItems(document, competitions);
        }

        JSONArray certifications = json.optJSONArray("certifications");
        if (certifications != null && certifications.length() > 0) {
            addSection(document, "证书资质");
            addCertificationItems(document, certifications);
        }

        JSONArray campusActivities = json.optJSONArray("campusActivities");
        if (campusActivities != null && campusActivities.length() > 0) {
            addSection(document, "校园活动");
            addCampusActivityItems(document, campusActivities);
        }
    }

    private void addContactInfo(Document document, JSONObject json) throws Exception {
        String phone = json.optString("phone", null);
        String email = json.optString("email", null);
        String location = json.optString("location", null);

        StringBuilder contactBuilder = new StringBuilder();
        if (phone != null && !phone.isEmpty()) {
            contactBuilder.append(phone);
        }
        if (email != null && !email.isEmpty()) {
            if (!contactBuilder.isEmpty()) contactBuilder.append("  |  ");
            contactBuilder.append(email);
        }
        if (location != null && !location.isEmpty()) {
            if (!contactBuilder.isEmpty()) contactBuilder.append("  |  ");
            contactBuilder.append(location);
        }

        if (!contactBuilder.isEmpty()) {
            Paragraph contactParagraph = new Paragraph(contactBuilder.toString())
                    .setFontSize(9)
                    .setFontColor(GRAY_COLOR)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setMarginBottom(10);
            document.add(contactParagraph);
        }
    }

    private void addSeparator(Document document) throws Exception {
        SolidLine line = new SolidLine(0.3f);
        line.setColor(LIGHT_GRAY);
        LineSeparator separator = new LineSeparator(line);
        separator.setWidth(UnitValue.createPercentValue(100));
        document.add(separator);
        document.add(new Paragraph("\n"));
    }

    private void addSection(Document document, String title) throws Exception {
        Paragraph sectionParagraph = new Paragraph(title)
                .setFontSize(15)
                .setBold()
                .setFontColor(PRIMARY_COLOR)
                .setMarginTop(12)
                .setMarginBottom(4);
        document.add(sectionParagraph);

        // 全宽极细浅灰色下划线
        SolidLine underline = new SolidLine(0.3f);
        underline.setColor(LIGHT_GRAY);
        LineSeparator separator = new LineSeparator(underline);
        separator.setWidth(UnitValue.createPercentValue(100));
        document.add(separator);
    }

    private void addEducationItems(Document document, JSONArray education) throws Exception {
        for (int i = 0; i < education.length(); i++) {
            JSONObject edu = education.getJSONObject(i);
            String school = edu.optString("school", "");
            String degree = edu.optString("degree", "");
            String major = edu.optString("major", "");
            String period = edu.optString("period", "");

            // 创建双栏表格（学校+学位在左，时间在右）
            Table table = new Table(UnitValue.createPercentArray(new float[]{75, 25}))
                    .setWidth(UnitValue.createPercentValue(100))
                    .setMarginBottom(8);

            // 左侧：学校 | 学位 | 专业
            StringBuilder leftText = new StringBuilder(school);
            if (!degree.isEmpty()) {
                leftText.append("  |  ").append(degree);
            }
            if (!major.isEmpty()) {
                leftText.append("  |  ").append(major);
            }

            Cell leftCell = new Cell()
                    .add(new Paragraph(leftText.toString())
                            .setFontSize(12)
                            .setBold())
                    .setBorder(Border.NO_BORDER)
                    .setPadding(0);

            // 右侧：时间段（右对齐）
            Cell rightCell = new Cell()
                    .add(new Paragraph(period)
                            .setFontSize(10)
                            .setFontColor(GRAY_COLOR)
                            .setTextAlignment(TextAlignment.RIGHT))
                    .setBorder(Border.NO_BORDER)
                    .setPadding(0);

            table.addCell(leftCell);
            table.addCell(rightCell);
            document.add(table);
        }
    }

    private void addExperienceItems(Document document, JSONArray experience) throws Exception {
        for (int i = 0; i < experience.length(); i++) {
            JSONObject exp = experience.getJSONObject(i);
            String company = exp.optString("company", "");
            String position = exp.optString("position", "");
            String period = exp.optString("period", "");
            String description = exp.optString("description", "");

            // 双栏表格
            Table table = new Table(UnitValue.createPercentArray(new float[]{75, 25}))
                    .setWidth(UnitValue.createPercentValue(100))
                    .setMarginBottom(5);

            StringBuilder leftText = new StringBuilder(company);
            if (!position.isEmpty()) {
                leftText.append("  |  ").append(position);
            }

            Cell leftCell = new Cell()
                    .add(new Paragraph(leftText.toString())
                            .setFontSize(12)
                            .setBold())
                    .setBorder(Border.NO_BORDER)
                    .setPadding(0);

            Cell rightCell = new Cell()
                    .add(new Paragraph(period)
                            .setFontSize(10)
                            .setFontColor(GRAY_COLOR)
                            .setTextAlignment(TextAlignment.RIGHT))
                    .setBorder(Border.NO_BORDER)
                    .setPadding(0);

            table.addCell(leftCell);
            table.addCell(rightCell);
            document.add(table);

            // 描述文本（设置行距1.4倍，段底间距8）
            if (!description.isEmpty()) {
                Paragraph descParagraph = new Paragraph(description)
                        .setFontSize(11)
                        .setMarginBottom(8)
                        .setMarginLeft(15);  // 左缩进15
                descParagraph.setProperty(Property.LEADING, new Leading(Leading.MULTIPLIED, 1.4f));
                document.add(descParagraph);
            }
        }
    }

    private void addProjectItems(Document document, JSONArray projects) throws Exception {
        for (int i = 0; i < projects.length(); i++) {
            JSONObject proj = projects.getJSONObject(i);
            String name = proj.optString("name", "");
            String role = proj.optString("role", "");
            String period = proj.optString("period", "");
            String description = proj.optString("description", "");

            // 双栏表格
            Table table = new Table(UnitValue.createPercentArray(new float[]{75, 25}))
                    .setWidth(UnitValue.createPercentValue(100))
                    .setMarginBottom(5);

            StringBuilder leftText = new StringBuilder(name);
            if (!role.isEmpty()) {
                leftText.append("  |  ").append(role);
            }

            Cell leftCell = new Cell()
                    .add(new Paragraph(leftText.toString())
                            .setFontSize(12)
                            .setBold())
                    .setBorder(Border.NO_BORDER)
                    .setPadding(0);

            Cell rightCell = new Cell();
            if (period != null && !period.isEmpty()) {
                rightCell.add(new Paragraph(period)
                        .setFontSize(10)
                        .setFontColor(GRAY_COLOR)
                        .setTextAlignment(TextAlignment.RIGHT));
            }
            rightCell.setBorder(Border.NO_BORDER).setPadding(0);

            table.addCell(leftCell);
            table.addCell(rightCell);
            document.add(table);

            // 描述文本
            if (!description.isEmpty()) {
                Paragraph descParagraph = new Paragraph(description)
                        .setFontSize(11)
                        .setMarginBottom(8)
                        .setMarginLeft(15);
                descParagraph.setProperty(Property.LEADING, new Leading(Leading.MULTIPLIED, 1.4f));
                document.add(descParagraph);
            }
        }
    }

    private void addSkillsItems(Document document, JSONArray skills) throws Exception {
        StringBuilder skillsBuilder = new StringBuilder();
        for (int i = 0; i < skills.length(); i++) {
            if (i > 0) skillsBuilder.append("  |  ");
            skillsBuilder.append(skills.getString(i));
        }

        Paragraph skillsParagraph = new Paragraph(skillsBuilder.toString())
                .setFontSize(11)
                .setMarginBottom(15);
        document.add(skillsParagraph);
    }

    private void addAwardItems(Document document, JSONArray awards) throws Exception {
        for (int i = 0; i < awards.length(); i++) {
            JSONObject award = awards.getJSONObject(i);
            String name = award.optString("name", "");
            String level = award.optString("level", "");
            String year = award.optString("year", "");

            // 使用Table实现左对齐名称，右对齐年份
            Table table = new Table(UnitValue.createPercentArray(new float[]{70, 30}))
                    .setWidth(UnitValue.createPercentValue(100))
                    .setMarginBottom(5);

            StringBuilder leftText = new StringBuilder("• ").append(name);
            if (!level.isEmpty()) {
                leftText.append("  |  ").append(level);
            }

            Cell leftCell = new Cell()
                    .add(new Paragraph(leftText.toString()).setFontSize(11))
                    .setBorder(Border.NO_BORDER)
                    .setPadding(0);

            Cell rightCell = new Cell();
            if (!year.isEmpty()) {
                rightCell.add(new Paragraph(year)
                        .setFontSize(10)
                        .setFontColor(GRAY_COLOR)
                        .setTextAlignment(TextAlignment.RIGHT));
            }
            rightCell.setBorder(Border.NO_BORDER).setPadding(0);

            table.addCell(leftCell);
            table.addCell(rightCell);
            document.add(table);
        }
        document.add(new Paragraph("\n"));
    }

    private void addCompetitionItems(Document document, JSONArray competitions) throws Exception {
        for (int i = 0; i < competitions.length(); i++) {
            JSONObject comp = competitions.getJSONObject(i);
            String name = comp.optString("name", "");
            String result = comp.optString("result", "");
            String year = comp.optString("year", "");

            Table table = new Table(UnitValue.createPercentArray(new float[]{70, 30}))
                    .setWidth(UnitValue.createPercentValue(100))
                    .setMarginBottom(5);

            StringBuilder leftText = new StringBuilder("• ").append(name);
            if (!result.isEmpty()) {
                leftText.append("  |  ").append(result);
            }

            Cell leftCell = new Cell()
                    .add(new Paragraph(leftText.toString()).setFontSize(11))
                    .setBorder(Border.NO_BORDER)
                    .setPadding(0);

            Cell rightCell = new Cell();
            if (!year.isEmpty()) {
                rightCell.add(new Paragraph(year)
                        .setFontSize(10)
                        .setFontColor(GRAY_COLOR)
                        .setTextAlignment(TextAlignment.RIGHT));
            }
            rightCell.setBorder(Border.NO_BORDER).setPadding(0);

            table.addCell(leftCell);
            table.addCell(rightCell);
            document.add(table);
        }
        document.add(new Paragraph("\n"));
    }

    private void addCertificationItems(Document document, JSONArray certifications) throws Exception {
        for (int i = 0; i < certifications.length(); i++) {
            JSONObject cert = certifications.getJSONObject(i);
            String name = cert.optString("name", "");
            String year = cert.optString("year", "");

            Table table = new Table(UnitValue.createPercentArray(new float[]{70, 30}))
                    .setWidth(UnitValue.createPercentValue(100))
                    .setMarginBottom(5);

            Cell leftCell = new Cell()
                    .add(new Paragraph("• " + name).setFontSize(11))
                    .setBorder(Border.NO_BORDER)
                    .setPadding(0);

            Cell rightCell = new Cell();
            if (!year.isEmpty()) {
                rightCell.add(new Paragraph(year)
                        .setFontSize(10)
                        .setFontColor(GRAY_COLOR)
                        .setTextAlignment(TextAlignment.RIGHT));
            }
            rightCell.setBorder(Border.NO_BORDER).setPadding(0);

            table.addCell(leftCell);
            table.addCell(rightCell);
            document.add(table);
        }
        document.add(new Paragraph("\n"));
    }

    private void addCampusActivityItems(Document document, JSONArray activities) throws Exception {
        for (int i = 0; i < activities.length(); i++) {
            JSONObject activity = activities.getJSONObject(i);
            String name = activity.optString("name", "");
            String role = activity.optString("role", "");
            String period = activity.optString("period", "");

            Table table = new Table(UnitValue.createPercentArray(new float[]{70, 30}))
                    .setWidth(UnitValue.createPercentValue(100))
                    .setMarginBottom(5);

            StringBuilder leftText = new StringBuilder("• ").append(name);
            if (!role.isEmpty()) {
                leftText.append("  |  ").append(role);
            }

            Cell leftCell = new Cell()
                    .add(new Paragraph(leftText.toString()).setFontSize(11))
                    .setBorder(Border.NO_BORDER)
                    .setPadding(0);

            Cell rightCell = new Cell();
            if (!period.isEmpty()) {
                rightCell.add(new Paragraph(period)
                        .setFontSize(10)
                        .setFontColor(GRAY_COLOR)
                        .setTextAlignment(TextAlignment.RIGHT));
            }
            rightCell.setBorder(Border.NO_BORDER).setPadding(0);

            table.addCell(leftCell);
            table.addCell(rightCell);
            document.add(table);
        }
        document.add(new Paragraph("\n"));
    }

    private void generateSimplePdf(Document document, String originalText) throws Exception {
        Paragraph titleParagraph = new Paragraph("简历")
                .setFontSize(24)
                .setBold()
                .setFontColor(PRIMARY_COLOR)
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginBottom(20);
        document.add(titleParagraph);

        addSeparator(document);

        if (originalText == null || originalText.isEmpty()) {
            return;
        }

        String[] lines = originalText.split("\\r?\\n");
        Paragraph currentBlock = null;

        for (int i = 0; i < lines.length; i++) {
            String line = lines[i].trim();
            if (line.isEmpty()) {
                continue;
            }

            if (line.startsWith("# ") && !line.startsWith("## ")) {
                flushBlock(document, currentBlock);
                currentBlock = null;

                String text = line.substring(2).trim();
                Paragraph h1 = new Paragraph(text)
                        .setFontSize(18)
                        .setBold()
                        .setFontColor(PRIMARY_COLOR)
                        .setMarginTop(15)
                        .setMarginBottom(8)
                        .setKeepTogether(true);
                document.add(h1);
                continue;
            }

            if (line.startsWith("## ")) {
                flushBlock(document, currentBlock);
                currentBlock = null;

                String text = line.substring(3).trim();
                text = text.replace("|", "  |  ");

                Paragraph h2 = new Paragraph(text)
                        .setFontSize(13)
                        .setBold()
                        .setMarginTop(10)
                        .setMarginBottom(4)
                        .setKeepTogether(true);
                document.add(h2);
                continue;
            }

            if (line.matches("^\\d{4}[\\.\\-]\\d{2}.*") || line.matches("^\\d{4}-\\d{4}.*")) {
                flushBlock(document, currentBlock);
                currentBlock = null;

                Paragraph period = new Paragraph(line)
                        .setFontSize(10)
                        .setFontColor(GRAY_COLOR)
                        .setMarginBottom(6);
                document.add(period);
                continue;
            }

            if (line.contains("|") && line.length() > 50 &&
                    (line.contains("HTML") || line.contains("Vue") || line.contains("JavaScript"))) {
                flushBlock(document, currentBlock);
                currentBlock = null;

                String[] skills = line.split("\\|");
                for (String skill : skills) {
                    skill = skill.trim();
                    if (skill.isEmpty()) continue;

                    Paragraph skillPara = new Paragraph("• " + skill)
                            .setFontSize(11)
                            .setMarginBottom(4);
                    document.add(skillPara);
                }
                document.add(new Paragraph("\n"));
                continue;
            }

            String cleanLine = line.replaceAll("\\*\\*", "");

            if (currentBlock == null) {
                currentBlock = new Paragraph(cleanLine)
                        .setFontSize(11)
                        .setMarginBottom(3);
            } else {
                currentBlock.add("\n" + cleanLine);
            }
        }

        flushBlock(document, currentBlock);
    }

    private void flushBlock(Document document, Paragraph block) {
        if (block != null) {
            block.setKeepTogether(false);
            document.add(block);
        }
    }

}
