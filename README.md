# ai-resume-platform
AI智能简历优化平台
项目简介


AI智能简历优化平台是一个基于Spring Boot + Vue 3的全栈Web应用，通过接入DeepSeek大模型API，帮助用户智能优化简历内容，提升简历竞争力。


核心功能：


用户注册/登录（JWT认证）
简历文件上传（支持PDF、Word格式）
AI智能优化简历内容
基于目标岗位的定制化优化建议
简历历史记录管理


在线访问： http://8.138.190.51


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


plaintext
ai-resume-platform/
├── backend/                          # 后端项目
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/resume/airesume/
│   │   │   │   ├── config/           # 配置类
│   │   │   │   │   ├── DeepSeekConfig.java      # DeepSeek配置
│   │   │   │   │   ├── CorsConfig.java          # 跨域配置
│   │   │   │   │   └── JwtConfig.java           # JWT配置
│   │   │   │   ├── controller/       # 控制器层
│   │   │   │   │   ├── AuthController.java      # 认证接口
│   │   │   │   │   ├── ResumeController.java    # 简历接口
│   │   │   │   │   └── AiController.java        # AI优化接口
│   │   │   │   ├── service/          # 服务层
│   │   │   │   │   ├── UserService.java
│   │   │   │   │   ├── ResumeService.java
│   │   │   │   │   ├── impl/         # 服务实现
│   │   │   │   │   │   ├── UserServiceImpl.java
│   │   │   │   │   │   └── ResumeServiceImpl.java
│   │   │   │   │   └── DeepSeekService.java     # AI服务
│   │   │   │   ├── mapper/           # MyBatis映射器
│   │   │   │   │   ├── UserMapper.java
│   │   │   │   │   └── ResumeMapper.java
│   │   │   │   ├── entity/           # 实体类
│   │   │   │   │   ├── User.java
│   │   │   │   │   └── Resume.java
│   │   │   │   ├── dto/              # 数据传输对象
│   │   │   │   │   ├── LoginRequest.java
│   │   │   │   │   ├── RegisterRequest.java
│   │   │   │   │   └── OptimizeRequest.java
│   │   │   │   ├── util/             # 工具类
│   │   │   │   │   ├── JwtUtil.java             # JWT工具
│   │   │   │   │   └── FileParserUtil.java      # 文件解析工具
│   │   │   │   └── AiResumeApplication.java     # 启动类
│   │   │   └── resources/
│   │   │       ├── application.properties        # 配置文件
│   │   │       └── mapper/            # MyBatis XML
│   │   └── test/                      # 测试代码
│   ├── pom.xml                        # Maven配置
│   └── target/                        # 编译输出
│
├── frontend/                          # 前端项目
│   ├── src/
│   │   ├── views/                     # 页面组件
│   │   │   ├── Login.vue              # 登录页
│   │   │   ├── Register.vue           # 注册页
│   │   │   ├── Home.vue               # 首页
│   │   │   └── Optimize.vue           # 优化页
│   │   ├── components/                # 公共组件
│   │   ├── router/                    # 路由配置
│   │   │   └── index.js
│   │   ├── stores/                    # Pinia状态管理
│   │   ├── utils/                     # 工具函数
│   │   │   └── request.js             # Axios封装
│   │   ├── api/                       # API接口
│   │   │   ├── auth.js                # 认证接口
│   │   │   └── resume.js              # 简历接口
│   │   ├── App.vue                    # 根组件
│   │   └── main.js                    # 入口文件
│   ├── package.json                   # 依赖配置
│   └── dist/                          # 打包输出
│
├── 学习笔记/                           # 学习笔记目录
│   ├── JWT登录认证笔记.md
│   ├── AI大模型API调用笔记.md
│   ├── 文件上传与解析笔记.md
│   ├── 接口设计说明-两个优化接口对比.md
│   ├── 前端开发笔记.md
│   └── 项目部署上线笔记.md
│
├── 项目文档/                           # 项目文档目录
│   ├── README.md                      # 项目说明
│   ├── 接口文档.md
│   ├── 数据库设计文档.md
│   └── 用户使用手册.md
│
└── 说明.md                             # 仓库说明



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


创建数据库：


sql
CREATE DATABASE ai_resume DEFAULT CHARACTER SET utf8mb4;



执行建表SQL（见 项目文档/数据库设计文档.md）。


3. 配置后端


修改 backend/src/main/resources/application.properties：


properties
# 数据库配置
spring.datasource.url=jdbc:mysql://localhost:3306/ai_resume
spring.datasource.username=root
spring.datasource.password=你的密码

# DeepSeek API（需要自己申请）
deepseek.api.key=你的API Key



4. 启动后端


bash
cd backend
mvn clean package -DskipTests
java -jar target/ai-resume-0.0.1-SNAPSHOT.jar



后端启动后访问：http://localhost:8080


5. 启动前端


bash
cd frontend
npm install
npm run dev



前端启动后访问：http://localhost:5173


核心功能说明
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
解析后的文本存储到数据库
3. AI简历优化


接入DeepSeek大模型API：


用户输入原文和目标岗位
AI根据岗位要求优化简历内容
添加量化数据和技术亮点
输出优化后的简历文本
4. 两种优化模式


模式	接口	说明
文本优化	POST /api/ai/optimize	用户手动输入文本，AI优化
一键优化	POST /api/resume/optimize/{id}	根据简历ID，自动获取内容并优化


项目亮点


全栈开发：独立完成前后端开发，掌握完整开发流程
AI应用：接入大模型API，实现智能化功能
文件处理：实现PDF/Word文件解析，处理非结构化数据
安全认证：使用JWT实现无状态认证，密码BCrypt加密
生产部署：完整部署到云服务器，真实线上环境


学习收获


通过本项目，学习并实践了：


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
项目类型：大三下学期课程设计/实习作品


许可证


本项目仅供学习交流使用。
