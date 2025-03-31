# 家教管理系统 (Tutor Management System)

本项目是一个完整的家教管理系统，包含前端和后端两个部分，旨在帮助教师和学生更好地管理家教课程、订单和评价等信息。

## 系统功能

- 用户管理：用户注册、登录、角色管理（学生、教师、管理员）
- 教师管理：教师个人资料、资质证书管理和审核
- 课程管理：课程创建、查询、更新和删除
- 订单管理：课程订单创建、支付、取消和完成
- 评价系统：学生对课程的评价功能
- 管理员功能：用户管理、资质审核等

## 技术栈

### 后端技术栈

- Java 8
- Spring Boot 2.7.5
- Spring Data JPA
- Spring Security + JWT
- MySQL 8
- Maven 3.8+
- Lombok

### 前端技术栈

- Vue 3
- Ant Design Vue 4.x
- Axios
- Vue Router
- Vite
- Less
- Dayjs

## 环境要求

- JDK 1.8+
- Node.js 16+
- pnpm 8+
- MySQL 8.0+

## 安装指南

### 克隆项目

```bash
git clone <repository-url>
cd Tutor-Management-System
```

### 后端设置

1. **创建MySQL数据库**

   登录到MySQL并创建数据库：

   ```sql
   CREATE DATABASE tutor_system CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
   ```

2. **配置数据库连接**

   打开文件 `server/src/main/resources/application.yml` 并根据您的MySQL配置修改以下内容：

   ```yaml
   spring:
     datasource:
       url: jdbc:mysql://localhost:3306/tutor_system?useUnicode=true&characterEncoding=utf-8&serverTimezone=Asia/Shanghai
       username: your_username
       password: your_password
       driver-class-name: com.mysql.cj.jdbc.Driver
   ```

3. **构建和运行后端服务**

   ```bash
   cd server
   mvn clean install
   mvn spring-boot:run
   ```

   后端服务将在 http://localhost:8080 上运行。

### 前端设置

1. **安装pnpm（如果尚未安装）**

   ```bash
   npm install -g pnpm
   ```

2. **安装依赖并启动开发服务器**

   ```bash
   cd front-end
   pnpm install
   pnpm dev
   ```

   前端开发服务器将在 http://localhost:5173 上运行。

## 数据库设置

系统默认使用JPA的`create`模式，它会自动创建数据库表。对于生产环境，建议修改为`update`或`validate`模式：

```yaml
spring:
  jpa:
    hibernate:
      ddl-auto: update  # 改为update或validate用于生产环境
```

### 初始数据

系统启动时会自动创建表结构。若需要初始化测试数据，可以添加SQL脚本到`server/src/main/resources/data.sql`文件中（需要创建此文件）。

## API接口文档

后端API接口基本路径为：`http://localhost:8080/api`

### 主要API端点

- 用户相关：`/api/users/*`
- 教师相关：`/api/teachers/*`
- 课程相关：`/api/courses/*`
- 订单相关：`/api/orders/*`
- 评价相关：`/api/reviews/*`
- 资质相关：`/api/qualifications/*` 
- 管理员功能：`/api/admin/*`

## 安全配置

系统使用JWT（JSON Web Token）进行认证，令牌有效期默认为24小时。您可以在`application.yml`中修改JWT密钥和过期时间：

```yaml
jwt:
  secret: your_strong_secret_key
  expiration: 86400000  # 过期时间，以毫秒为单位，默认24小时
```

## 项目结构

### 后端目录结构

```
server/
├── src/
│   ├── main/
│   │   ├── java/com/tutor/
│   │   │   ├── config/         # 配置类
│   │   │   ├── controller/     # 控制器
│   │   │   ├── dto/            # 数据传输对象
│   │   │   ├── entity/         # 实体类
│   │   │   ├── exception/      # 异常处理
│   │   │   ├── repository/     # 数据库仓库
│   │   │   ├── security/       # 安全相关
│   │   │   ├── service/        # 业务逻辑
│   │   │   ├── utils/          # 工具类
│   │   │   └── TutorApplication.java  # 应用入口
│   │   └── resources/
│   │       └── application.yml  # 应用配置
└── pom.xml                      # Maven配置
```

### 前端目录结构

```
front-end/
├── public/                      # 静态资源
├── src/
│   ├── assets/                  # 静态资源
│   ├── components/              # 通用组件
│   ├── router/                  # 路由配置
│   ├── utils/                   # 工具函数
│   ├── views/                   # 页面组件
│   ├── App.vue                  # 主组件
│   └── main.js                  # 应用入口
├── index.html                   # HTML模板
└── package.json                 # 项目配置
```

## 部署

### 后端部署

1. 修改application.yml中的数据库配置
2. 将`spring.jpa.hibernate.ddl-auto`设置为`validate`或`update`
3. 构建JAR文件：`mvn clean package`
4. 运行JAR文件：`java -jar target/tutor-system-1.0-SNAPSHOT.jar`

### 前端部署

1. 构建生产版本：`pnpm build`
2. 将`dist`目录中的文件部署到Web服务器（如Nginx）

## 常见问题

1. **数据库连接失败**
   
   检查MySQL服务是否运行，以及用户名密码是否正确。

2. **JWT认证失败**
   
   清除浏览器缓存和Cookie，重新登录。

3. **服务无法启动**
   
   检查日志输出，查看是否有端口冲突或其他配置问题。

## 维护与支持

如有任何问题，请提交Issue或联系项目维护人员。

## 许可证

MIT许可证 