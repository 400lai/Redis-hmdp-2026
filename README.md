# 点评系统后端 (hm-dianping)

<div align="center">

基于 Spring Boot 3 + Redis 的分布式点评系统，模拟大众点评核心业务场景

[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5.9-6DB33F.svg?logo=spring-boot)](https://spring.io/projects/spring-boot)
[![Java](https://img.shields.io/badge/JDK-21-E76F00.svg?logo=openjdk)](https://www.oracle.com/java/)
[![Redis](https://img.shields.io/badge/Redis-Latest-DC382D.svg?logo=redis)](https://redis.io/)
[![MyBatis-Plus](https://img.shields.io/badge/MyBatis--Plus-3.5.5-0078D7.svg)](https://baomidou.com/)
[![License](https://img.shields.io/badge/License-MIT-4C9AFF.svg)](LICENSE)

</div>

---

## 🎯 项目简介

HM-DianPing 是一个基于 Spring Boot 3 和 Redis 的分布式点评系统后端，模拟大众点评的核心业务场景。项目采用现代化的技术栈，深度整合 Redis 实现缓存、分布式锁、全局 ID 生成器、秒杀等高并发解决方案，涵盖用户登录、商铺管理、优惠券秒杀、笔记分享、关注推送等完整业务链。

---


## 📋 项目概览

| 技术 | 版本 | 说明 |
|------|------|------|
| **后端框架** | Spring Boot 3.5.9 | 核心框架 |
| **开发语言** | Java 21 | 基础语言 |
| **数据库** | MySQL 8.4.0 | 数据持久化 |
| **缓存中间件** | Redis | 分布式缓存、分布式锁 |
| **ORM 框架** | MyBatis-Plus 3.5.5 | 数据访问层 |
| **连接池** | Lettuce + Commons Pool2 | Redis 连接池管理 |
| **分布式锁** | Redisson 3.16.2 | 分布式锁实现 |
| **工具库** | Hutool 5.8.32 | Java 工具类库 |
| **开发辅助** | Lombok 1.18.36 | 简化代码 |
| **Web 框架** | Spring MVC | RESTful API |

---

## 🏗 系统架构

```
┌────────────────────────────────────────────────────────────┐
│                    Client (HTTP Requests)                   │
└────────────────────────┬───────────────────────────────────┘
                         │
                         ▼
┌────────────────────────────────────────────────────────────┐
│  Controller Layer                                          │
│  UserController │ ShopController │ BlogController │ ...    │
└────────────────────────┬───────────────────────────────────┘
                         │
                         ▼
┌────────────────────────────────────────────────────────────┐
│  Service Layer (Business Logic + Redis Cache + Lock)       │
│  • 缓存策略    • 分布式锁    • 事务管理    • 异步处理       │
└────────────────────────┬───────────────────────────────────┘
                         │
                         ▼
┌────────────────────────────────────────────────────────────┐
│  Mapper Layer (MyBatis-Plus DAO)                           │
└────────────────────────┬───────────────────────────────────┘
                         │
                         ▼
┌────────────────────────────────────────────────────────────┐
│  Data Layer                                                │
│  MySQL (持久化存储)    +    Redis (缓存/分布式锁)           │
└────────────────────────────────────────────────────────────┘
```

---

## 📁 目录结构

```
hm-dianping/
├── src/main/java/com/hmdp/
│   ├── HmDianPingApplication.java          # Spring Boot 启动类
│   ├── config/                             # 配置类
│   │   ├── MybatisConfig.java              # MyBatis-Plus 配置
│   │   ├── WebExceptionAdvice.java         # 全局异常处理器
│   │   ├── MvcConfig.java                  # MVC 拦截器配置
│   │   └── RedissonConfig.java             # Redisson 配置
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
│       ├── CacheClient.java                # 缓存客户端 (封装缓存操作)
│       ├── RedisIdWorker.java              # 基于 Redis 的全局 ID 生成器
│       ├── SimpleRedisLock.java            # 简易 Redis 分布式锁
│       ├── ILock.java                      # 锁接口
│       ├── UserHolder.java                 # 用户上下文持有者
│       └── ...
├── src/main/resources/
│   ├── application.yml                     # 应用配置文件
│   └── db/
│       └── hmdp.sql                        # 数据库初始化脚本
├── pom.xml                                 # Maven 依赖配置
└── README.md                               # 项目文档
```

---

## 🚀 核心功能模块

### 1. 用户模块
- **手机验证码登录**: 基于 Redis 实现验证码缓存和 Token 会话管理
- **用户信息管理**: 支持用户资料查询和更新
- **登录状态维护**: 使用 Redis String 结构存储用户 Token，实现分布式会话
- **拦截器链**: LoginInterceptor + RefreshTokenInterceptor 双重拦截机制

### 2. 商铺模块
- **商铺查询**: 支持 ID 查询、分类筛选、关键字搜索
- **缓存优化**: 
  - 互斥锁解决缓存击穿问题
  - 逻辑过期实现高并发场景下的缓存可用性
  - 缓存穿透保护 (空值缓存)
- **商铺管理**: 完整的 CRUD 操作

### 3. 优惠券秒杀模块
- **秒杀活动管理**: 秒杀优惠券的发布和管理
- **分布式锁**: 基于 Redis 实现集群环境下的互斥访问
- **高并发优化**:
  - 乐观锁解决超卖问题
  - 一人一单限制
  - 异步下单优化性能

### 4. 笔记模块
- **笔记发布与浏览**: 支持图文笔记的发布和查看
- **评论功能**: 笔记评论的增删改查
- **点赞功能**: 基于 Redis SortedSet 实现点赞排行榜
- **好友关注**: 基于 Feed 流的关注推送系统

### 5. 关注模块
- **关注/取关**: 用户间的关注关系管理
- **关注列表**: 查询用户的关注列表和粉丝列表
- **共同关注**: 计算两个用户的共同关注列表 (Redis 集合交集)
- **Feed 流推送**: 
  - 推模式：主动推送给粉丝
  - 拉模式：从关注列表拉取
  - 滚动分页：基于时间戳的无限滚动

### 6. 文件上传
- **图片上传**: 支持图片文件上传至本地服务器

## 🔧 环境要求


| 软件 | 版本 | 说明 |
|------|------|------|
| JDK | 21+ | Java 开发环境 |
| MySQL | 8.x | 关系型数据库 |
| Redis | Latest | 分布式缓存中间件 |
| Maven | 3.6+ | 项目构建工具 |

---

## 📦 快速开始

### 1. 克隆项目

```bash
git clone https://github.com/400lai/Redis-hmdp-2026
cd hm-dianping
```

### 2. 配置数据库

编辑 [`application.yml`](src/main/resources/application.yml):

```yaml
spring:
  datasource:
    driver-class-name: com.mysql.cj.jdbc.Driver
    url: jdbc:mysql://127.0.0.1:3306/hmdp?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true
    username: root
    password: your_password  # 修改为你的数据库密码
```

### 3. 配置 Redis

```yaml
spring:
  data:
    redis:
      host: 192.168.100.128  # 修改为你的 Redis 服务器地址
      port: 6379
      password: your_redis_password  # 如有密码请配置
      database: 0
      lettuce:
        pool:
          max-active: 10
          max-idle: 10
          min-idle: 1
```

### 4. 初始化数据库

```bash
# 方式一：命令行执行
mysql -u root -p < src/main/resources/db/hmdp.sql
```

**方式二：MySQL 客户端手动执行**
1. 登录 MySQL
2. 执行 `hmdp.sql` 脚本文件

### 5. 构建并启动项目

```bash
# 构建项目
mvn clean install

# 启动应用
mvn spring-boot:run
```

或直接运行 [`HmDianPingApplication.java`](src/main/java/com/hmdp/HmDianPingApplication.java)

✅ 启动成功后访问：`http://localhost:8081`

### 6. 访问 Swagger API 文档

启动应用后，可以通过以下地址访问 API 文档：

- **Swagger UI**: http://localhost:8081/swagger-ui.html
- **OpenAPI JSON**: http://localhost:8081/v3/api-docs

详细说明请参考 [Swagger 使用说明](SWAGGER_USAGE.md)

---

## 📖 API 接口文档

### Swagger 文档说明

项目已集成 Swagger (SpringDoc OpenAPI) 提供交互式 API 文档，所有接口都已添加详细的注解说明。

#### 文档特性

- ✅ 完整的接口说明和参数描述
- ✅ 支持 JWT Token 认证配置
- ✅ 在线测试接口功能
- ✅ 按模块分类展示 (用户管理、商铺管理、博客管理等)

#### 快速使用

1. **访问 Swagger UI**: 浏览器打开 `http://localhost:8081/swagger-ui.html`
2. **配置认证**: 点击右上角 "Authorize" 按钮，输入 `Bearer {your_token}`
3. **测试接口**: 展开接口详情，点击 "Try it out" 进行测试

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
4. 日志级别配置为 DEBUG，便于开发调试

---

## 常见问题

### 数据库连接失败

**解决方案:**
1. 检查 MySQL 服务是否启动
2. 确认 `application.yml` 中的数据库配置正确
3. 检查数据库用户名密码是否正确
4. 确认数据库 `hmdp` 已创建

### Redis 连接失败

**解决方案:**
1. 检查 Redis 服务是否启动
2. 确认 Redis 配置 (IP、端口、密码) 正确
3. 检查防火墙是否阻止 Redis 端口

### 端口被占用

**解决方案:**

修改 `src/main/resources/application.yml`:

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

---

## 📋 开发计划

- [ ] 完善秒杀功能的并发控制
- [ ] 添加单元测试
- [ ] 优化数据库查询性能
- [x] 添加接口文档 (Swagger)
- [ ] 完善日志记录

---

## 📄 License

MIT License

---

<div align="center">

**HM-DianPing** - 学习与实践 Spring Boot 分布式开发的优质项目

[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5.9-6DB33F.svg?logo=spring-boot)](https://spring.io/projects/spring-boot)
[![Redis](https://img.shields.io/badge/Redis-Latest-DC382D.svg?logo=redis)](https://redis.io/)
[![MySQL](https://img.shields.io/badge/MySQL-5.x-4285F4.svg?logo=mysql)](https://www.mysql.com/)

</div>
