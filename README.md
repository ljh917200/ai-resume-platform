AI智能简历优化平台
项目简介


AI智能简历优化平台是一个基于 Spring Boot + Vue 3 的全栈Web应用，通过接入 DeepSeek 大模型API，帮助用户智能优化简历内容，提升简历竞争力。


核心功能


用户注册/登录（JWT认证）
简历文件上传（支持PDF、Word格式）
AI智能优化简历内容
基于目标岗位的定制化优化建议
简历历史记录管理


在线访问：http://8.138.190.51


技术栈


后端


技术	版本	说明
Spring Boot	3.4.1	核心框架
MyBatis	3.0.x	ORM框架
MySQL	8.0	数据库
JWT	0.12.x	身份认证
DeepSeek API	-	AI大模型接口
Apache POI	5.x	Word文件解析
Apache PDFBox	3.x	PDF文件解析


前端


技术	版本	说明
Vue 3	3.x	前端框架
Vite	8.x	构建工具
Element Plus	2.x	UI组件库
Axios	1.x	HTTP请求库
Vue Router	4.x	路由管理
Pinia	2.x	状态管理


部署


技术	说明
Nginx	Web服务器、反向代理
阿里云轻量服务器	服务器托管


项目结构


后端目录


backend/src/main/java/com/resume/airesume/
config/ - 配置类
controller/ - 控制器层
service/ - 服务层
mapper/ - MyBatis映射器
entity/ - 实体类
dto/ - 数据传输对象
util/ - 工具类


前端目录


frontend/src/
views/ - 页面组件
components/ - 公共组件
router/ - 路由配置
stores/ - Pinia状态管理
utils/ - 工具函数
api/ - API接口


文档目录


学习笔记/ - 开发学习笔记
项目文档/ - 项目文档（接口文档、数据库设计等）


快速开始
环境要求


JDK 17+
Node.js 20+
MySQL 8.0+
Maven 3.8+
本地运行


1. 克隆项目


bash
git clone https://gitee.com/你的用户名/ai-resume-platform.git
cd ai-resume-platform



2. 配置数据库


sql
CREATE DATABASE ai_resume DEFAULT CHARACTER SET utf8mb4;



建表SQL见 项目文档/数据库设计文档.md


3. 配置后端


修改 backend/src/main/resources/application.properties：


properties
# 数据库配置
spring.datasource.url=jdbc:mysql://localhost:3306/ai_resume
spring.datasource.username=root
spring.datasource.password=你的密码

# DeepSeek API
deepseek.api.key=你的API Key



4. 启动后端


bash
cd backend
mvn clean package -DskipTests
java -jar target/ai-resume-0.0.1-SNAPSHOT.jar



后端地址：http://localhost:8080


5. 启动前端


bash
cd frontend
npm install
npm run dev



前端地址：http://localhost:5173


核心功能
1. 用户认证


使用JWT（JSON Web Token）实现用户认证：


用户注册时密码使用BCrypt加密存储
登录成功后返回JWT Token
后续请求携带Token进行身份验证
Token有效期24小时
2. 文件上传与解析


支持上传PDF和Word格式的简历文件：


PDF解析：使用Apache PDFBox提取文本内容
Word解析：使用Apache POI提取文本内容
文件大小限制：10MB
3. AI简历优化


接入DeepSeek大模型API：


用户输入原文和目标岗位
AI根据岗位要求优化简历内容
添加量化数据和技术亮点
4. 两种优化模式


模式	接口	说明
文本优化	POST /api/ai/optimize	用户手动输入文本，AI优化
一键优化	POST /api/resume/optimize/{id}	根据简历ID，自动获取内容并优化


项目亮点


全栈开发：独立完成前后端开发，掌握完整开发流程
AI应用：接入大模型API，实现智能化功能
文件处理：实现PDF/Word文件解析
安全认证：使用JWT实现无状态认证，密码BCrypt加密
生产部署：完整部署到云服务器，真实线上环境


学习收获


Spring Boot 3.x 新特性
MyBatis 原生SQL开发
JWT 无状态认证机制
文件上传与解析处理
大模型API集成
Vue 3 Composition API
前后端跨域问题解决
Nginx反向代理配置
云服务器部署运维


后续规划


简历模板功能（多种模板选择）
简历导出PDF功能
简历评分功能
多语言支持
移动端适配优化


作者


开发者：赖俊煌
开发时间：2026年4月
项目类型：大三下学期实习作品


许可证


本项目仅供学习交流使用。
