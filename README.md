# 个人博客系统（Personal Blog System）

基于 **Vue 3 + Spring Boot** 的前后端分离个人博客系统，支持用户注册登录、文章管理、分类、标签、评论等功能。

## 技术栈

### 后端（blog-backend）
- Java 11、Spring Boot 2.7.14
- Spring Security + JWT（无状态认证）
- Spring Data JPA（Hibernate，`ddl-auto: update` 自动建表）
- MySQL 8、springdoc-openapi（Swagger 接口文档）

### 前端（blog-frontend）
- Vue 3 + Vite 7
- Vue Router、Vuex、Element Plus、Axios

## 环境要求

| 环境 | 版本要求 |
|------|----------|
| JDK | 11+ |
| Maven | 3.6+ |
| Node.js | ^20.19.0 或 >=22.12.0 |
| MySQL | 8.x |

## 启动步骤

### 1. 创建数据库（只需建库，无需执行 SQL 脚本）

登录 MySQL 后执行：

```sql
CREATE DATABASE personal_blog DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

> 项目中没有 SQL 初始化脚本，数据表由 Hibernate 在后端启动时根据实体类自动创建（`ddl-auto: update`）。

### 2. 启动后端（端口 8080）

后端数据库连接配置在 [blog-backend/src/main/resources/application.yml](blog-backend/src/main/resources/application.yml)：

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/personal_blog?...
    username: root
    password: 123456   # 如与本地 MySQL 密码不一致，请修改此处
```

启动：

```bash
cd blog-backend
mvn spring-boot:run
```

- 接口基地址：`http://localhost:8080/api`
- Swagger 文档：`http://localhost:8080/api/swagger-ui/index.html`

### 3. 启动前端（端口 5173）

```bash
cd blog-frontend
npm install
npm run dev
```

- 访问地址：`http://localhost:5173`

> 前端 axios 的 `baseURL` 在 [blog-frontend/src/api/index.js](blog-frontend/src/api/index.js) 中写死为 `http://localhost:8080/api`，后端 CORS 已放行 `http://localhost:*`，本地开发无需额外配置代理。

## 常用端口一览

| 服务 | 端口 |
|------|------|
| 后端 API | 8080（context-path `/api`） |
| 前端开发服务器 | 5173 |
| MySQL | 3306 |

## 功能模块

- 用户注册 / 登录（JWT 鉴权，角色分为 USER / ADMIN）
- 文章：发布、编辑、审核（管理员）、浏览
- 分类 / 标签管理
- 评论与回复
- 管理员后台（Dashboard 统计、用户与内容管理）

## 项目结构

```
personal-blog-system
├── blog-backend          # Spring Boot 后端
│   └── src/main/java/com/cg
│       ├── controller    # REST 接口层
│       ├── Service       # 业务逻辑层
│       ├── repository    # JPA 数据访问层
│       ├── entity        # 数据库实体
│       ├── dto           # 请求/响应对象
│       ├── Security      # JWT 过滤器与工具
│       └── config        # Security / 全局异常配置
└── blog-frontend         # Vue 3 前端
    └── src
        ├── api           # axios 接口封装
        ├── views         # 页面组件
        ├── router        # 路由
        └── store         # Vuex 状态管理
```
