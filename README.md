# 点评系统后端 (hm-dianping)

基于 Spring Boot 构建的分布式点评系统后端，模拟大众点评核心业务场景，深度整合 Redis 实现高性能缓存、分布式锁、秒杀等高级功能。

## 📋 项目概览

| 项目信息 | 详情 |
|---------|------|
| **后端框架** | Spring Boot 2.3.12.RELEASE |
| **开发语言** | Java 1.8 |
| **数据库** | MySQL 5.x |
| **缓存中间件** | Redis (Lettuce 连接池) |
| **ORM 框架** | MyBatis-Plus 3.4.3 |
| **工具库** | Hutool 5.7.17, Lombok |
| **服务端口** | 8081 |

## 🏗 系统架构

```
┌─────────────────────────────────────────────────────────┐
│                    Client Layer                          │
└─────────────────────────────────────────────────────────┘
                          ↓
┌─────────────────────────────────────────────────────────┐
│                   Controller Layer                       │
│  (UserController, ShopController, BlogController...)    │
└─────────────────────────────────────────────────────────┘
                          ↓
┌─────────────────────────────────────────────────────────┐
│                    Service Layer                         │
│  (UserService, ShopService, BlogService, VoucherService)│
└─────────────────────────────────────────────────────────┘
                          ↓
┌─────────────────────────────────────────────────────────┐
│                    Mapper Layer                          │
│         (MyBatis-Plus Data Access Objects)              │
└─────────────────────────────────────────────────────────┘
                          ↓
┌─────────────────────────────────────────────────────────┐
│                  Data Storage Layer                      │
│        MySQL (Persistent) + Redis (Cache)               │
└─────────────────────────────────────────────────────────┘
```

## 📁 目录结构

```
hm-dianping/
├── src/main/java/com/hmdp/
│   ├── HmDianPingApplication.java          # Spring Boot 启动类
│   ├── config/                             # 配置类
│   │   ├── MybatisConfig.java              # MyBatis-Plus 配置
│   │   ├── WebExceptionAdvice.java         # 全局异常处理器
│   │   └── MvcConfig.java                  # MVC 拦截器配置
│   ├── controller/                         # RESTful API 控制器
│   │   ├── UserController.java             # 用户管理接口
│   │   ├── ShopController.java             # 商铺管理接口
│   │   ├── ShopTypeController.java         # 商铺分类接口
│   │   ├── BlogController.java             # 笔记管理接口
│   │   ├── BlogCommentsController.java     # 评论管理接口
│   │   ├── FollowController.java           # 关注功能接口
│   │   ├── VoucherController.java          # 优惠券管理接口
│   │   ├── VoucherOrderController.java     # 优惠券订单接口
│   │   └── UploadController.java           # 文件上传接口
│   ├── service/                            # 业务逻辑层
│   │   ├── I*.java                         # 服务接口
│   │   └── impl/                           # 服务实现类
│   ├── mapper/                             # 数据访问层
│   ├── entity/                             # 实体类
│   ├── dto/                                # 数据传输对象
│   └── utils/                              # 工具类
├── src/main/resources/
│   ├── application.yaml                    # 应用配置文件
│   └── db/
│       └── hmdp.sql                        # 数据库初始化脚本
├── pom.xml                                 # Maven 依赖配置
└── README.md                               # 项目文档
```

## 🚀 核心功能模块

### 1️⃣ 用户模块
- **手机验证码登录**: 基于 Redis 实现验证码缓存和 Token 会话管理
- **用户信息管理**: 支持用户资料查询和更新
- **登录状态维护**: 使用 Redis String 结构存储用户 Token，实现分布式会话
- **拦截器链**: LoginInterceptor + RefreshTokenInterceptor 双重拦截机制

### 2️⃣ 商铺模块
- **商铺查询**: 支持 ID 查询、分类筛选、关键字搜索
- **缓存优化**: 
  - 互斥锁解决缓存击穿问题
  - 逻辑过期实现高并发场景下的缓存可用性
  - 缓存穿透保护 (空值缓存)
- **商铺管理**: 完整的 CRUD 操作

### 3️⃣ 优惠券秒杀模块
- **秒杀活动管理**: 秒杀优惠券的发布和管理
- **分布式锁**: 基于 Redis 实现集群环境下的互斥访问
- **高并发优化**:
  - 乐观锁解决超卖问题
  - 一人一单限制
  - 异步下单优化性能

### 4️⃣ 笔记模块
- **笔记发布与浏览**: 支持图文笔记的发布和查看
- **评论功能**: 笔记评论的增删改查
- **点赞功能**: 基于 Redis SortedSet 实现点赞排行榜
- **好友关注**: 基于 Feed 流的关注推送系统

### 5️⃣ 关注模块
- **关注/取关**: 用户间的关注关系管理
- **关注列表**: 查询用户的关注列表和粉丝列表
- **共同关注**: 计算两个用户的共同关注列表 (Redis 集合交集)
- **Feed 流推送**: 
  - 推模式：主动推送给粉丝
  - 拉模式：从关注列表拉取
  - 滚动分页：基于时间戳的无限滚动

### 6️⃣ 文件上传
- **图片上传**: 支持图片文件上传至本地服务器

## 🔧 环境要求

| 软件 | 版本 | 说明 |
|------|------|------|
| JDK | 1.8+ | Java 开发环境 |
| MySQL | 5.x | 关系型数据库 |
| Redis | 最新稳定版 | 分布式缓存中间件 |
| Maven | 3.6+ | 项目构建工具 |

## 📦 快速开始

### 步骤 1: 克隆项目

```bash
git clone https://github.com/400lai/Redis-hmdp-2026
cd hm-dianping
```

### 步骤 2: 配置数据库

编辑 [`application.yaml`](d:\code\Java_IDEAProject\hm-dianping\src\main\resources\application.yaml):

```yaml
spring:
  datasource:
    driver-class-name: com.mysql.jdbc.Driver
    url: jdbc:mysql://127.0.0.1:3306/hmdp?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true
    username: root
    password: your_password  # 修改为你的数据库密码
```

### 步骤 3: 配置 Redis

```yaml
spring:
  redis:
    host: 192.168.100.128  # 修改为你的 Redis 服务器地址
    port: 6379
    password: your_redis_password  # 如有密码请配置
    lettuce:
      pool:
        max-active: 10
        max-idle: 10
        min-idle: 1
```

### 步骤 4: 初始化数据库

**方式一：命令行执行**
```bash
mysql -u root -p < src/main/resources/db/hmdp.sql
```

**方式二：MySQL 客户端手动执行**
1. 登录 MySQL
2. 执行 `hmdp.sql` 脚本文件

### 步骤 5: 构建项目

```bash
mvn clean install
```

### 步骤 6: 启动应用

**方式一：Maven 运行**
```bash
mvn spring-boot:run
```

**方式二：IDE 运行**
直接运行 [`HmDianPingApplication.java`](d:\code\Java_IDEAProject\hm-dianping\src\main\java\com\hmdp\HmDianPingApplication.java)

启动成功后访问：`http://localhost:8081`

## 📖 API 接口文档

### 用户接口

| 方法 | 路径 | 说明 | 请求参数 |
|------|------|------|----------|
| POST | `/user/code` | 发送验证码 | `phone` |
| POST | `/user/login` | 登录 | `LoginFormDTO` |
| POST | `/user/logout` | 登出 | - |
| GET | `/user/me` | 当前用户信息 | - |
| GET | `/user/info/{id}` | 指定用户信息 | `id` |

### 商铺接口

| 方法 | 路径 | 说明 | 请求参数 |
|------|------|------|----------|
| GET | `/shop/{id}` | 商铺详情 | `id` |
| POST | `/shop` | 新增商铺 | `Shop` |
| PUT | `/shop` | 更新商铺 | `Shop` |
| GET | `/shop/of/type` | 按类型查询 | `typeId` |
| GET | `/shop/of/name` | 按名称搜索 | `name` |

### 优惠券接口

| 方法 | 路径 | 说明 | 请求参数 |
|------|------|------|----------|
| GET | `/voucher/list` | 优惠券列表 | - |
| POST | `/voucher-order/seckill/{id}` | 秒杀下单 | `id` |
| GET | `/voucher-order/{id}` | 订单查询 | `id` |

### 笔记接口

| 方法 | 路径 | 说明 | 请求参数 |
|------|------|------|----------|
| GET | `/blog/hot` | 热门笔记 | - |
| GET | `/blog/{id}` | 笔记详情 | `id` |
| POST | `/blog` | 发布笔记 | `Blog` |
| PUT | `/blog` | 更新笔记 | `Blog` |
| DELETE | `/blog/{id}` | 删除笔记 | `id` |

### 评论接口

| 方法 | 路径 | 说明 | 请求参数 |
|------|------|------|----------|
| GET | `/blog/comments/{blogId}` | 笔记评论列表 | `blogId` |
| POST | `/blog/comments` | 发布评论 | `BlogComments` |
| PUT | `/blog/comments` | 更新评论 | `BlogComments` |
| DELETE | `/blog/comments/{id}` | 删除评论 | `id` |

### 关注接口

| 方法 | 路径 | 说明 | 请求参数 |
|------|------|------|----------|
| PUT | `/follow/{followUserId}` | 关注/取关 | `followUserId` |
| GET | `/follow/list/{userId}` | 关注列表 | `userId` |
| GET | `/follow/common/{uid}` | 共同关注 | `uid` |
| GET | `/follow/feeds` | Feed 流 | 滚动分页参数 |

### 文件上传

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/upload` | 上传图片 |

## 💻 开发指南

### 代码规范

- 使用 Lombok 注解简化代码 (`@Data`, `@Builder`, 等)
- 统一返回 `Result` 对象
- 使用 MyBatis-Plus 进行数据库操作
- 使用 Hutool 工具类处理常见工具方法
- 遵循 RESTful API 设计规范

### 项目分层架构

```
Controller → Service → Mapper
    ↓          ↓         ↓
   DTO      Entity    Database
```

### 开发注意事项

1. 部分功能标记为 `TODO`，需要进一步完善
2. 数据库连接和 Redis 连接需要根据实际环境修改配置
3. 项目使用 Jackson 处理 JSON，默认忽略 null 值字段

## ❓ 常见问题

### 数据库连接失败

**解决方案:**
1. 检查 MySQL 服务是否启动
2. 确认 `application.yaml` 中的数据库配置正确
3. 检查数据库用户名密码是否正确
4. 确认数据库 `hmdp` 已创建

### Redis 连接失败

**解决方案:**
1. 检查 Redis 服务是否启动
2. 确认 Redis 配置 (IP、端口、密码) 正确
3. 检查防火墙是否阻止 Redis 端口

### 端口被占用

**解决方案:**

修改 `src/main/resources/application.yaml`:

```yaml
server:
  port: 8081  # 修改为其他端口
```

### 依赖下载失败

**解决方案:**

配置 Maven 国内镜像，在 `pom.xml` 或 Maven 配置中添加:

```xml
<mirror>
  <id>aliyun</id>
  <mirrorOf>central</mirrorOf>
  <name>Aliyun Maven</name>
  <url>https://maven.aliyun.com/repository/public</url>
</mirror>
```

## 📝 开发计划

- [ ] 完善秒杀功能的并发控制
- [ ] 添加单元测试
- [ ] 优化数据库查询性能
- [ ] 添加接口文档 (Swagger)
- [ ] 完善日志记录

## 📄 License

MIT License

---

**项目说明**: 本项目主要用于学习和实践 Spring Boot 开发，实现了点评系统的核心功能模块。
