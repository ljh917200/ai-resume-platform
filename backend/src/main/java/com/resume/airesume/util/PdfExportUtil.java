package com.resume.airesume.util;

import com.itextpdf.kernel.colors.Color;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.kernel.pdf.canvas.draw.SolidLine;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.LineSeparator;
import com.itextpdf.layout.element.Paragraph;
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
 * 支持3套模板：
 * - 模板1：简约蓝（蓝色主题）
 * - 模板2：商务灰（灰色主题）
 * - 模板3：创意橙（橙色主题）
 *
 * 注意：不使用Table布局和特殊符号，避免字体兼容问题
 *
 * @author AI Resume Platform
 * @version 2.1.0
 */
@Component
public class PdfExportUtil {

    // ==================== 颜色定义 ====================

    // 模板一：简约蓝 - 主色调
    private static final Color BLUE_PRIMARY = new DeviceRgb(74, 144, 226);
    // 模板一：简约蓝 - 次要颜色
    private static final Color BLUE_GRAY = new DeviceRgb(100, 100, 100);

    // 模板二：商务灰 - 主色调
    private static final Color GRAY_PRIMARY = new DeviceRgb(90, 106, 122);
    // 模板二：商务灰 - 深色
    private static final Color GRAY_DARK = new DeviceRgb(60, 60, 60);

    // 模板三：创意橙 - 主色调
    private static final Color ORANGE_PRIMARY = new DeviceRgb(245, 166, 35);
    // 模板三：创意橙 - 深色
    private static final Color ORANGE_DARK = new DeviceRgb(247, 107, 28);

    // 通用颜色
    private static final Color LIGHT_GRAY = new DeviceRgb(220, 220, 220);
    private static final Color WHITE = new DeviceRgb(255, 255, 255);

    /**
     * 生成PDF（入口方法）
     * 根据模板ID生成不同风格的简历PDF
     *
     * @param structuredData 结构化简历数据（JSON格式）
     * @param originalText   原始简历文本（当结构化数据为空时使用）
     * @param templateId     模板ID：1-简约蓝 2-商务灰 3-创意橙
     * @return PDF字节数组
     */
    public byte[] generatePdfFromStructuredData(String structuredData, String originalText, Integer templateId) throws Exception {

        // 确保 templateId 有效（默认使用模板1）
        if (templateId == null || templateId < 1 || templateId > 3) {
            templateId = 1;
        }

        // 创建PDF输出流
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        PdfWriter writer = new PdfWriter(outputStream);
        PdfDocument pdfDoc = new PdfDocument(writer);
        Document document = new Document(pdfDoc);

        // 设置页面边距
        document.setMargins(50, 45, 50, 45);

        // 设置中文字体（使用宋体）
        PdfFont chineseFont = PdfFontFactory.createFont(
                "STSong-Light", "UniGB-UCS2-H", PdfFontFactory.EmbeddingStrategy.PREFER_EMBEDDED
        );
        document.setFont(chineseFont);

        // 根据是否有结构化数据选择生成方式
        if (structuredData == null || structuredData.trim().isEmpty() || "null".equals(structuredData)) {
            // 无结构化数据，生成简单PDF
            generateSimplePdf(document, originalText, templateId);
        } else {
            // 有结构化数据，根据模板ID选择对应的生成方法
            JSONObject json = new JSONObject(structuredData);
            switch (templateId) {
                case 1:
                    generateBlueTemplate(document, json);
                    break;
                case 2:
                    generateGrayTemplate(document, json);
                    break;
                case 3:
                    generateOrangeTemplate(document, json);
                    break;
                default:
                    generateBlueTemplate(document, json);
            }
        }

        document.close();
        return outputStream.toByteArray();
    }

    // ==================== 模板一：简约蓝 ====================

    /**
     * 生成简约蓝模板PDF
     * 特点：蓝色主题，居中布局，清新简洁
     */
    private void generateBlueTemplate(Document document, JSONObject json) throws Exception {

        // 1. 姓名（大号居中，蓝色）
        String name = json.optString("name", null);
        if (name != null && !name.isEmpty()) {
            Paragraph titleParagraph = new Paragraph(name)
                    .setFontSize(26)
                    .setBold()
                    .setFontColor(BLUE_PRIMARY)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setMarginBottom(8);
            document.add(titleParagraph);
        }

        // 2. 联系信息（灰色小字，居中）
        addContactInfo(document, json, BLUE_GRAY);

        // 3. 分隔线
        addSeparator(document, LIGHT_GRAY);

        // 4. 自我评价
        String selfEvaluation = json.optString("selfEvaluation", null);
        if (selfEvaluation != null && !selfEvaluation.isEmpty()) {
            addSectionTitle(document, "自我评价", BLUE_PRIMARY, LIGHT_GRAY);
            addParagraph(document, selfEvaluation, 11, 15);
        }

        // 5. 教育经历
        JSONArray education = json.optJSONArray("education");
        if (education != null && education.length() > 0) {
            addSectionTitle(document, "教育经历", BLUE_PRIMARY, LIGHT_GRAY);
            addEducationItems(document, education, BLUE_GRAY);
        }

        // 6. 工作经历
        JSONArray experience = json.optJSONArray("experience");
        if (experience != null && experience.length() > 0) {
            addSectionTitle(document, "工作经历", BLUE_PRIMARY, LIGHT_GRAY);
            addExperienceItems(document, experience, BLUE_GRAY);
        }

        // 7. 项目经历
        JSONArray projects = json.optJSONArray("projects");
        if (projects != null && projects.length() > 0) {
            addSectionTitle(document, "项目经历", BLUE_PRIMARY, LIGHT_GRAY);
            addProjectItems(document, projects, BLUE_GRAY);
        }

        // 8. 技能特长
        JSONArray skills = json.optJSONArray("skills");
        if (skills != null && skills.length() > 0) {
            addSectionTitle(document, "技能特长", BLUE_PRIMARY, LIGHT_GRAY);
            addSkills(document, skills);
        }

        // 9. 其他模块
        addOtherSections(document, json, BLUE_PRIMARY, LIGHT_GRAY, BLUE_GRAY);
    }

    // ==================== 模板二：商务灰 ====================

    /**
     * 生成商务灰模板PDF
     * 特点：灰色主题，稳重专业
     */
    private void generateGrayTemplate(Document document, JSONObject json) throws Exception {

        // 1. 姓名
        String name = json.optString("name", null);
        if (name != null && !name.isEmpty()) {
            Paragraph titleParagraph = new Paragraph(name)
                    .setFontSize(28)
                    .setBold()
                    .setFontColor(GRAY_DARK)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setMarginBottom(5);
            document.add(titleParagraph);
        }

        // 2. 联系信息
        addContactInfo(document, json, GRAY_PRIMARY);

        // 3. 粗分隔线
        addSeparator(document, GRAY_PRIMARY, 2f);

        // 4. 自我评价
        String selfEvaluation = json.optString("selfEvaluation", null);
        if (selfEvaluation != null && !selfEvaluation.isEmpty()) {
            addSectionTitle(document, "自我评价", GRAY_PRIMARY, LIGHT_GRAY);
            addParagraph(document, selfEvaluation, 11, 15);
        }

        // 5. 教育经历
        JSONArray education = json.optJSONArray("education");
        if (education != null && education.length() > 0) {
            addSectionTitle(document, "教育经历", GRAY_PRIMARY, LIGHT_GRAY);
            addEducationItems(document, education, GRAY_PRIMARY);
        }

        // 6. 工作经历
        JSONArray experience = json.optJSONArray("experience");
        if (experience != null && experience.length() > 0) {
            addSectionTitle(document, "工作经历", GRAY_PRIMARY, LIGHT_GRAY);
            addExperienceItems(document, experience, GRAY_PRIMARY);
        }

        // 7. 项目经历
        JSONArray projects = json.optJSONArray("projects");
        if (projects != null && projects.length() > 0) {
            addSectionTitle(document, "项目经历", GRAY_PRIMARY, LIGHT_GRAY);
            addProjectItems(document, projects, GRAY_PRIMARY);
        }

        // 8. 技能特长
        JSONArray skills = json.optJSONArray("skills");
        if (skills != null && skills.length() > 0) {
            addSectionTitle(document, "专业技能", GRAY_PRIMARY, LIGHT_GRAY);
            addSkills(document, skills);
        }

        // 9. 其他模块
        addOtherSections(document, json, GRAY_PRIMARY, LIGHT_GRAY, GRAY_PRIMARY);
    }

    // ==================== 模板三：创意橙 ====================

    /**
     * 生成创意橙模板PDF
     * 特点：橙色主题，活力醒目
     */
    private void generateOrangeTemplate(Document document, JSONObject json) throws Exception {

        // 1. 姓名（橙色）
        String name = json.optString("name", null);
        if (name != null && !name.isEmpty()) {
            Paragraph titleParagraph = new Paragraph(name)
                    .setFontSize(28)
                    .setBold()
                    .setFontColor(ORANGE_DARK)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setMarginBottom(8);
            document.add(titleParagraph);
        }

        // 2. 联系信息
        addContactInfo(document, json, ORANGE_DARK);

        // 3. 分隔线
        addSeparator(document, ORANGE_PRIMARY, 1f);

        // 4. 自我评价
        String selfEvaluation = json.optString("selfEvaluation", null);
        if (selfEvaluation != null && !selfEvaluation.isEmpty()) {
            addSectionTitle(document, "自我评价", ORANGE_DARK, ORANGE_PRIMARY);
            addParagraph(document, selfEvaluation, 11, 15);
        }

        // 5. 教育经历
        JSONArray education = json.optJSONArray("education");
        if (education != null && education.length() > 0) {
            addSectionTitle(document, "教育经历", ORANGE_DARK, ORANGE_PRIMARY);
            addEducationItems(document, education, ORANGE_DARK);
        }

        // 6. 工作经历
        JSONArray experience = json.optJSONArray("experience");
        if (experience != null && experience.length() > 0) {
            addSectionTitle(document, "工作经历", ORANGE_DARK, ORANGE_PRIMARY);
            addExperienceItems(document, experience, ORANGE_DARK);
        }

        // 7. 项目经历
        JSONArray projects = json.optJSONArray("projects");
        if (projects != null && projects.length() > 0) {
            addSectionTitle(document, "项目经历", ORANGE_DARK, ORANGE_PRIMARY);
            addProjectItems(document, projects, ORANGE_DARK);
        }

        // 8. 技能特长
        JSONArray skills = json.optJSONArray("skills");
        if (skills != null && skills.length() > 0) {
            addSectionTitle(document, "专业技能", ORANGE_DARK, ORANGE_PRIMARY);
            addSkills(document, skills);
        }

        // 9. 其他模块
        addOtherSections(document, json, ORANGE_DARK, ORANGE_PRIMARY, ORANGE_DARK);
    }

    // ==================== 通用辅助方法 ====================

    /**
     * 添加联系信息
     * @param dateColor 日期/次要信息的颜色
     */
    private void addContactInfo(Document document, JSONObject json, Color dateColor) throws Exception {
        String phone = json.optString("phone", null);
        String email = json.optString("email", null);
        String location = json.optString("location", null);

        // 拼接联系方式字符串
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
                    .setFontSize(10)
                    .setFontColor(dateColor)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setMarginBottom(10);
            document.add(contactParagraph);
        }
    }

    /**
     * 添加分隔线
     */
    private void addSeparator(Document document, Color color) throws Exception {
        addSeparator(document, color, 0.3f);
    }

    /**
     * 添加分隔线（可指定粗细）
     */
    private void addSeparator(Document document, Color color, float width) throws Exception {
        SolidLine line = new SolidLine(width);
        line.setColor(color);
        LineSeparator separator = new LineSeparator(line);
        separator.setWidth(UnitValue.createPercentValue(100));
        document.add(separator);
        document.add(new Paragraph("\n"));
    }

    /**
     * 添加章节标题
     */
    private void addSectionTitle(Document document, String title, Color titleColor, Color lineColor) throws Exception {
        // 标题
        Paragraph sectionParagraph = new Paragraph(title)
                .setFontSize(15)
                .setBold()
                .setFontColor(titleColor)
                .setMarginTop(12)
                .setMarginBottom(4);
        document.add(sectionParagraph);

        // 下划线
        SolidLine underline = new SolidLine(0.5f);
        underline.setColor(lineColor);
        LineSeparator separator = new LineSeparator(underline);
        separator.setWidth(UnitValue.createPercentValue(100));
        document.add(separator);
    }

    /**
     * 添加普通段落
     */
    private void addParagraph(Document document, String text, int fontSize, float marginBottom) throws Exception {
        Paragraph paragraph = new Paragraph(text)
                .setFontSize(fontSize)
                .setMarginBottom(marginBottom);
        paragraph.setProperty(Property.LEADING, new Leading(Leading.MULTIPLIED, 1.4f));
        document.add(paragraph);
    }

    /**
     * 添加教育经历条目
     * 使用简单的文本格式，不用Table
     */
    private void addEducationItems(Document document, JSONArray education, Color dateColor) throws Exception {
        for (int i = 0; i < education.length(); i++) {
            JSONObject edu = education.getJSONObject(i);
            String school = edu.optString("school", "");
            String degree = edu.optString("degree", "");
            String major = edu.optString("major", "");
            String period = edu.optString("period", "");

            // 第一行：学校名称（加粗）
            if (!school.isEmpty()) {
                Paragraph schoolPara = new Paragraph(school)
                        .setFontSize(12)
                        .setBold()
                        .setMarginBottom(2);
                document.add(schoolPara);
            }

            // 第二行：专业 | 学位
            StringBuilder line2 = new StringBuilder();
            if (!major.isEmpty()) line2.append(major);
            if (!degree.isEmpty()) {
                if (line2.length() > 0) line2.append("  |  ");
                line2.append(degree);
            }
            if (line2.length() > 0) {
                Paragraph majorPara = new Paragraph(line2.toString())
                        .setFontSize(11)
                        .setMarginBottom(2);
                document.add(majorPara);
            }

            // 第三行：时间（灰色）
            if (!period.isEmpty()) {
                Paragraph periodPara = new Paragraph(period)
                        .setFontSize(10)
                        .setFontColor(dateColor)
                        .setMarginBottom(10);
                document.add(periodPara);
            }
        }
    }

    /**
     * 添加工作经历条目
     */
    private void addExperienceItems(Document document, JSONArray experience, Color dateColor) throws Exception {
        for (int i = 0; i < experience.length(); i++) {
            JSONObject exp = experience.getJSONObject(i);
            String company = exp.optString("company", "");
            String position = exp.optString("position", "");
            String period = exp.optString("period", "");
            String description = exp.optString("description", "");

            // 第一行：公司名称（加粗）
            if (!company.isEmpty()) {
                Paragraph companyPara = new Paragraph(company)
                        .setFontSize(12)
                        .setBold()
                        .setMarginBottom(2);
                document.add(companyPara);
            }

            // 第二行：职位
            if (!position.isEmpty()) {
                Paragraph positionPara = new Paragraph(position)
                        .setFontSize(11)
                        .setMarginBottom(2);
                document.add(positionPara);
            }

            // 第三行：时间
            if (!period.isEmpty()) {
                Paragraph periodPara = new Paragraph(period)
                        .setFontSize(10)
                        .setFontColor(dateColor)
                        .setMarginBottom(5);
                document.add(periodPara);
            }

            // 第四行：描述（缩进）
            if (!description.isEmpty()) {
                Paragraph descPara = new Paragraph(description)
                        .setFontSize(10)
                        .setMarginBottom(12)
                        .setMarginLeft(15);
                descPara.setProperty(Property.LEADING, new Leading(Leading.MULTIPLIED, 1.4f));
                document.add(descPara);
            }
        }
    }

    /**
     * 添加项目经历条目
     */
    private void addProjectItems(Document document, JSONArray projects, Color dateColor) throws Exception {
        for (int i = 0; i < projects.length(); i++) {
            JSONObject proj = projects.getJSONObject(i);
            String name = proj.optString("name", "");
            String role = proj.optString("role", "");
            String period = proj.optString("period", "");
            String description = proj.optString("description", "");

            // 第一行：项目名称（加粗）
            if (!name.isEmpty()) {
                Paragraph namePara = new Paragraph(name)
                        .setFontSize(12)
                        .setBold()
                        .setMarginBottom(2);
                document.add(namePara);
            }

            // 第二行：角色
            if (!role.isEmpty()) {
                Paragraph rolePara = new Paragraph(role)
                        .setFontSize(11)
                        .setMarginBottom(2);
                document.add(rolePara);
            }

            // 第三行：时间
            if (!period.isEmpty()) {
                Paragraph periodPara = new Paragraph(period)
                        .setFontSize(10)
                        .setFontColor(dateColor)
                        .setMarginBottom(5);
                document.add(periodPara);
            }

            // 第四行：描述
            if (!description.isEmpty()) {
                Paragraph descPara = new Paragraph(description)
                        .setFontSize(10)
                        .setMarginBottom(12)
                        .setMarginLeft(15);
                descPara.setProperty(Property.LEADING, new Leading(Leading.MULTIPLIED, 1.4f));
                document.add(descPara);
            }
        }
    }

    /**
     * 添加技能特长
     */
    private void addSkills(Document document, JSONArray skills) throws Exception {
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

    /**
     * 添加其他模块（获奖、比赛、证书、校园活动）
     */
    private void addOtherSections(Document document, JSONObject json,
                                  Color titleColor, Color lineColor, Color dateColor) throws Exception {

        // 获奖情况
        JSONArray awards = json.optJSONArray("awards");
        if (awards != null && awards.length() > 0) {
            addSectionTitle(document, "获奖情况", titleColor, lineColor);
            addAwardItems(document, awards, dateColor);
        }

        // 比赛经历
        JSONArray competitions = json.optJSONArray("competitions");
        if (competitions != null && competitions.length() > 0) {
            addSectionTitle(document, "比赛经历", titleColor, lineColor);
            addCompetitionItems(document, competitions, dateColor);
        }

        // 证书资质
        JSONArray certifications = json.optJSONArray("certifications");
        if (certifications != null && certifications.length() > 0) {
            addSectionTitle(document, "证书资质", titleColor, lineColor);
            addCertificationItems(document, certifications, dateColor);
        }

        // 校园活动
        JSONArray campusActivities = json.optJSONArray("campusActivities");
        if (campusActivities != null && campusActivities.length() > 0) {
            addSectionTitle(document, "校园活动", titleColor, lineColor);
            addCampusActivityItems(document, campusActivities, dateColor);
        }
    }

    /**
     * 添加获奖情况条目
     * 使用中文横线代替特殊符号
     */
    private void addAwardItems(Document document, JSONArray awards, Color dateColor) throws Exception {
        for (int i = 0; i < awards.length(); i++) {
            JSONObject award = awards.getJSONObject(i);
            String name = award.optString("name", "");
            String level = award.optString("level", "");
            String year = award.optString("year", "");

            // 使用中文横线代替特殊符号
            StringBuilder line = new StringBuilder();
            line.append("- ").append(name);
            if (!level.isEmpty()) {
                line.append("  |  ").append(level);
            }
            if (!year.isEmpty()) {
                line.append("  |  ").append(year);
            }

            Paragraph awardPara = new Paragraph(line.toString())
                    .setFontSize(11)
                    .setMarginBottom(5);
            document.add(awardPara);
        }
        document.add(new Paragraph("\n"));
    }

    /**
     * 添加比赛经历条目
     */
    private void addCompetitionItems(Document document, JSONArray competitions, Color dateColor) throws Exception {
        for (int i = 0; i < competitions.length(); i++) {
            JSONObject comp = competitions.getJSONObject(i);
            String name = comp.optString("name", "");
            String result = comp.optString("result", "");
            String year = comp.optString("year", "");

            StringBuilder line = new StringBuilder();
            line.append("- ").append(name);
            if (!result.isEmpty()) {
                line.append("  |  ").append(result);
            }
            if (!year.isEmpty()) {
                line.append("  |  ").append(year);
            }

            Paragraph compPara = new Paragraph(line.toString())
                    .setFontSize(11)
                    .setMarginBottom(5);
            document.add(compPara);
        }
        document.add(new Paragraph("\n"));
    }

    /**
     * 添加证书资质条目
     */
    private void addCertificationItems(Document document, JSONArray certifications, Color dateColor) throws Exception {
        for (int i = 0; i < certifications.length(); i++) {
            JSONObject cert = certifications.getJSONObject(i);
            String name = cert.optString("name", "");
            String year = cert.optString("year", "");

            StringBuilder line = new StringBuilder();
            line.append("- ").append(name);
            if (!year.isEmpty()) {
                line.append("  |  ").append(year);
            }

            Paragraph certPara = new Paragraph(line.toString())
                    .setFontSize(11)
                    .setMarginBottom(5);
            document.add(certPara);
        }
        document.add(new Paragraph("\n"));
    }

    /**
     * 添加校园活动条目
     */
    private void addCampusActivityItems(Document document, JSONArray activities, Color dateColor) throws Exception {
        for (int i = 0; i < activities.length(); i++) {
            JSONObject activity = activities.getJSONObject(i);
            String name = activity.optString("name", "");
            String role = activity.optString("role", "");
            String period = activity.optString("period", "");

            StringBuilder line = new StringBuilder();
            line.append("- ").append(name);
            if (!role.isEmpty()) {
                line.append("  |  ").append(role);
            }
            if (!period.isEmpty()) {
                line.append("  |  ").append(period);
            }

            Paragraph activityPara = new Paragraph(line.toString())
                    .setFontSize(11)
                    .setMarginBottom(5);
            document.add(activityPara);
        }
        document.add(new Paragraph("\n"));
    }

    /**
     * 生成简单PDF（当没有结构化数据时使用）
     */
    private void generateSimplePdf(Document document, String originalText, Integer templateId) throws Exception {
        // 根据模板选择颜色
        Color primaryColor;
        Color grayColor;

        switch (templateId) {
            case 1:
                primaryColor = BLUE_PRIMARY;
                grayColor = BLUE_GRAY;
                break;
            case 2:
                primaryColor = GRAY_PRIMARY;
                grayColor = GRAY_PRIMARY;
                break;
            case 3:
                primaryColor = ORANGE_PRIMARY;
                grayColor = ORANGE_DARK;
                break;
            default:
                primaryColor = BLUE_PRIMARY;
                grayColor = BLUE_GRAY;
        }

        // 标题
        Paragraph titleParagraph = new Paragraph("简历")
                .setFontSize(24)
                .setBold()
                .setFontColor(primaryColor)
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginBottom(20);
        document.add(titleParagraph);

        addSeparator(document, LIGHT_GRAY);

        if (originalText == null || originalText.isEmpty()) {
            return;
        }

        // 直接输出原始文本
        Paragraph textParagraph = new Paragraph(originalText)
                .setFontSize(11);
        textParagraph.setProperty(Property.LEADING, new Leading(Leading.MULTIPLIED, 1.5f));
        document.add(textParagraph);
    }
}

