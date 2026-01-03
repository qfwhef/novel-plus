# NovelPlus 项目说明文档

## 项目简介

NovelPlus 是一个小说阅读平台，提供用户注册、登录、小说浏览、评论、搜索等功能。同时，它也支持作者发布小说、管理章节、使用 AI 辅助创作等专业功能。该项目采用 Spring Boot 框架构建，结合 MyBatis Plus、Redis、RabbitMQ 等技术，具备良好的可扩展性和高性能。

## 功能模块

### 用户模块
- 用户注册与登录
- 用户信息管理
- 发表评论与管理评论
- 小说浏览与搜索

### 作者模块
- 作者注册与状态查询
- 小说发布、更新与删除
- 章节管理（发布、更新、删除）
- AI 辅助创作（扩写、续写、润色、缩写）

### 小说模块
- 小说分类浏览
- 小说详情查看
- 小说章节列表与内容阅读
- 小说排行榜（点击榜、新书榜、更新榜）
- 小说推荐与搜索

### 新闻模块
- 新闻浏览与详情查看

### 资源模块
- 图片验证码生成
- 图片上传管理

## 技术架构

- **后端框架**：Spring Boot + MyBatis Plus
- **数据库**：MySQL
- **缓存**：Redis + Caffeine
- **消息队列**：RabbitMQ
- **文件存储**：R2 对象存储
- **安全**：Spring Security + JWT
- **接口文档**：OpenAPI 3
- **任务调度**：XXL-JOB

## 核心功能说明

### AI 辅助创作
通过集成 AI 模型，为作者提供扩写、续写、润色、缩写等功能，提升创作效率。

### 缓存管理
使用 Redis 和 Caffeine 实现多级缓存，提高系统性能，减少数据库压力。

### 评论审核
通过 RabbitMQ 实现异步评论审核机制，支持敏感词过滤和死信队列处理。

### 接口文档
使用 OpenAPI 3 生成接口文档，便于前后端协作开发。

### 任务调度
通过 XXL-JOB 实现缓存预热、排行榜更新等定时任务。

## 安装与部署

### 环境要求
- JDK 17+
- Maven 3.8+
- MySQL 8.0+
- Redis 6.0+
- RabbitMQ 3.8+
- XXL-JOB 调度中心

### 构建步骤
1. 克隆项目：
   ```bash
   git clone https://gitee.com/wcola/novel-plus.git
   ```
2. 导入数据库：
   ```bash
   mysql -u<username> -p<password> < doc/sql/*.sql
   ```
3. 修改配置文件：
   ```bash
   vim src/main/resources/application.yml
   ```
4. 构建项目：
   ```bash
   mvn clean package
   ```
5. 启动项目：
   ```bash
   java -jar target/novel-plus.jar
   ```

## 使用说明

### 接口文档
访问 `/swagger-ui.html` 查看接口文档（仅限开发环境）。

### 管理后台
通过 `/admin` 路径访问管理后台，进行小说、章节、评论等管理操作。

### 作者后台
通过 `/author` 路径访问作者后台，进行小说发布、章节管理、AI 辅助创作等操作。

### 前台门户
通过 `/front` 路径访问前台门户，进行小说浏览、评论、搜索等操作。

## 贡献指南

欢迎贡献代码！请遵循以下步骤：
1. Fork 项目
2. 创建新分支
3. 提交 Pull Request

## 许可证

本项目采用 Apache 2.0 许可证。详情请查看 LICENSE 文件。