# 点评系统后端 (hm-dianping)

基于 Spring Boot 的点评系统后端项目，实现仿大众点评的核心功能。

## 🛠 技术栈

| 技术 | 版本 | 说明 |
|------|------|------|
| **后端框架** | Spring Boot 2.3.12.RELEASE | 核心框架 |
| **数据库** | MySQL 5.x | 关系型数据库 |
| **ORM** | MyBatis-Plus 3.4.3 | 数据持久层 |
| **缓存** | Redis | 分布式缓存 |
| **连接池** | Lettuce | Redis 客户端 |
| **工具库** | Hutool 5.7.17 | Java 工具类库 |
| **代码简化** | Lombok | 注解简化代码 |
| **JDK** | Java 1.8 | 开发环境 |

## 📁 项目结构

```
hm-dianping/
├── src/main/java/com/hmdp/
│   ├── HmDianPingApplication.java    # 启动类
│   ├── config/                       # 配置类
│   │   ├── MybatisConfig.java        # MyBatis 配置
│   │   └── WebExceptionAdvice.java   # 全局异常处理
│   ├── controller/                   # 控制器层
│   │   ├── UserController.java       # 用户接口
│   │   ├── ShopController.java       # 商铺接口
│   │   ├── ShopTypeController.java   # 商铺类型接口
│   │   ├── BlogController.java       # 笔记接口
│   │   ├── BlogCommentsController.java  # 评论接口
│   │   ├── FollowController.java     # 关注接口
│   │   ├── VoucherController.java    # 优惠券接口
│   │   ├── VoucherOrderController.java  # 优惠券订单接口
│   │   └── UploadController.java     # 文件上传接口
│   ├── service/                      # 服务层接口
│   │   └── impl/                     # 服务层实现
│   ├── mapper/                       # 数据访问层
│   ├── entity/                       # 实体类
│   ├── dto/                          # 数据传输对象
│   └── utils/                        # 工具类
└── src/main/resources/
    ├── application.yaml              # 应用配置文件
    └── db/
        └── hmdp.sql                  # 数据库初始化脚本
```

## ✨ 核心功能

### 用户模块
- ✅ 手机验证码登录
- ✅ 用户信息管理
- ✅ 登录状态维护

### 商铺模块
- ✅ 商铺详情查询
- ✅ 商铺 CRUD 操作
- ✅ 按类型分页查询
- ✅ 关键字搜索商铺

### 优惠券模块
- ✅ 优惠券管理
- ✅ 秒杀下单
- ✅ 订单查询

### 笔记模块
- ✅ 笔记发布与浏览
- ✅ 评论管理
- ✅ 点赞功能

### 关注模块
- ✅ 用户关注/取关
- ✅ 关注列表查询

### 文件上传
- ✅ 图片上传

## 🚀 快速开始

### 环境要求

- JDK 1.8+
- MySQL 5.x
- Redis
- Maven 3.6+

### 安装步骤

#### 1. 克隆项目

```bash
git clone https://github.com/400lai/Redis-hmdp-2026
cd hm-dianping
```

#### 2. 配置数据库

编辑 `src/main/resources/application.yaml`:

```yaml
spring:
  datasource:
    driver-class-name: com.mysql.jdbc.Driver
    url: jdbc:mysql://127.0.0.1:3306/hmdp?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true
    username: root
    password: your_password  # 修改为你的数据库密码
```

#### 3. 配置 Redis

```yaml
spring:
  redis:
    host: 192.168.100.128  # 修改为你的 Redis 地址
    port: 6379
    password: your_redis_password  # 如有密码请配置
```

#### 4. 初始化数据库

执行 SQL 脚本创建数据库和表:

```bash
mysql -u root -p < src/main/resources/db/hmdp.sql
```

或手动在 MySQL 客户端执行 `hmdp.sql` 文件。

#### 5. 安装依赖

```bash
mvn clean install
```

#### 6. 启动项目

**方式一：使用 Maven**

```bash
mvn spring-boot:run
```

**方式二：运行启动类**

直接运行 `src/main/java/com/hmdp/HmDianPingApplication.java`

项目启动后访问：`http://localhost:8081`

## 📚 API 文档

### 用户接口

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/user/code` | 发送手机验证码 |
| POST | `/user/login` | 用户登录 |
| POST | `/user/logout` | 用户登出 |
| GET | `/user/me` | 获取当前用户信息 |
| GET | `/user/info/{id}` | 获取指定用户信息 |

### 商铺接口

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/shop/{id}` | 根据 ID 查询商铺 |
| POST | `/shop` | 新增商铺 |
| PUT | `/shop` | 更新商铺 |
| GET | `/shop/of/type` | 按类型查询商铺 |
| GET | `/shop/of/name` | 按名称搜索商铺 |

### 优惠券接口

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/voucher-order/seckill/{id}` | 秒杀优惠券 |

### 笔记接口

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/blog/{id}` | 查询笔记详情 |
| POST | `/blog` | 发布笔记 |

### 文件上传接口

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
