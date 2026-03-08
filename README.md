# 点评项目 (hm-dianping)

基于 Spring Boot 的点评系统后端项目，类似大众点评平台。

## 技术栈

- **后端框架**: Spring Boot 2.3.12.RELEASE
- **数据库**: MySQL 5.x
- **ORM 框架**: MyBatis-Plus 3.4.3
- **缓存**: Redis (使用 Lettuce 连接池)
- **工具库**: 
  - Hutool 5.7.17
  - Lombok
- **JDK 版本**: Java 1.8

## 项目结构

```
hm-dianping/
├── src/main/java/com/hmdp/
│   ├── config/              # 配置类
│   │   ├── MybatisConfig.java
│   │   └── WebExceptionAdvice.java
│   ├── controller/          # 控制器层
│   │   ├── UserController.java
│   │   ├── ShopController.java
│   │   ├── ShopTypeController.java
│   │   ├── BlogController.java
│   │   ├── BlogCommentsController.java
│   │   ├── FollowController.java
│   │   ├── VoucherController.java
│   │   ├── VoucherOrderController.java
│   │   └── UploadController.java
│   ├── service/             # 服务层接口
│   │   └── impl/            # 服务层实现
│   ├── mapper/              # 数据访问层
│   ├── entity/              # 实体类
│   ├── dto/                 # 数据传输对象
│   └── utils/               # 工具类
└── src/main/resources/
    ├── application.yaml     # 应用配置
    └── db/hmdp.sql         # 数据库脚本
```

## 核心功能模块

### 1. 用户模块 (`UserController`)
- 手机验证码登录
- 用户信息查看
- 登录状态管理

### 2. 商铺模块 (`ShopController`)
- 商铺信息查询
- 商铺新增/更新
- 按类型分页查询
- 按名称关键字搜索

### 3. 优惠券模块 (`VoucherController`, `VoucherOrderController`)
- 优惠券管理
- 秒杀优惠券下单

### 4. 笔记模块 (`BlogController`, `BlogCommentsController`)
- 笔记发布与查询
- 笔记评论管理

### 5. 关注模块 (`FollowController`)
- 用户关注功能

### 6. 上传模块 (`UploadController`)
- 文件上传功能

## 快速开始

### 环境要求

- JDK 1.8+
- MySQL 5.x
- Redis
- Maven

### 配置说明

#### 1. 数据库配置

编辑 [`application.yaml`](src/main/resources/application.yaml):

```yaml
spring:
  datasource:
    driver-class-name: com.mysql.jdbc.Driver
    url: jdbc:mysql://127.0.0.1:3306/hmdp?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true
    username: root
    password: your_password
```

#### 2. Redis 配置

```yaml
spring:
  redis:
    host: 192.168.100.128
    port: 6379
    password: your_redis_password
```

#### 3. 初始化数据库

执行 [`hmdp.sql`](src/main/resources/db/hmdp.sql) 脚本创建数据库和表:

```bash
mysql -u root -p < src/main/resources/db/hmdp.sql
```

### 启动项目

```bash
mvn clean install
mvn spring-boot:run
```

或者直接运行 [`HmDianPingApplication.java`](src/main/java/com/hmdp/HmDianPingApplication.java)

项目默认运行在：`http://localhost:8081`

## API 接口

### 用户相关
- `POST /user/code` - 发送手机验证码
- `POST /user/login` - 用户登录
- `POST /user/logout` - 用户登出
- `GET /user/me` - 获取当前登录用户信息
- `GET /user/info/{id}` - 获取指定用户信息

### 商铺相关
- `GET /shop/{id}` - 根据 ID 查询商铺
- `POST /shop` - 新增商铺
- `PUT /shop` - 更新商铺
- `GET /shop/of/type` - 按类型查询商铺
- `GET /shop/of/name` - 按名称搜索商铺

### 优惠券相关
- `POST /voucher-order/seckill/{id}` - 秒杀优惠券

## 开发说明

### 代码规范

- 使用 Lombok 简化代码
- 统一返回 `Result` 对象
- 使用 MyBatis-Plus 进行数据库操作
- 使用 Hutool 工具类处理常见工具方法

### 注意事项

1. 部分功能标记为 `TODO`，需要进一步完善
2. 数据库连接和 Redis 连接需要根据实际环境修改配置
3. 项目使用 Jackson 处理 JSON，默认忽略 null 值字段

## 常见问题

### 1. 数据库连接失败

检查 MySQL 服务是否启动，并确认 `application.yaml` 中的数据库配置正确。

### 2. Redis 连接失败

检查 Redis 服务是否启动，并确认 Redis 配置（IP、端口、密码）正确。

### 3. 端口被占用

修改 `application.yaml` 中的 `server.port` 配置。


## License

MIT License
