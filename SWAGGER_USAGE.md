# Swagger API 文档使用说明

## 访问地址

启动应用后，可以通过以下地址访问 Swagger UI 界面：

- **Swagger UI**: http://localhost:8081/swagger-ui.html
- **OpenAPI JSON**: http://localhost:8081/v3/api-docs

## 功能说明

### 1. API 文档分类

所有接口已按功能模块分类：

- **用户管理** - 用户登录、注册、信息查询等接口
- **商铺管理** - 商铺查询、新增、修改等接口
- **博客管理** - 探店博文发布、点赞、查询等接口
- **优惠券管理** - 优惠券创建、查询等接口
- **优惠券订单** - 秒杀下单相关接口
- **关注管理** - 用户关注、取关、关注列表等接口
- **店铺类型** - 店铺类型查询接口
- **文件上传** - 图片上传、删除等接口

### 2. 认证说明

系统使用 JWT Token 进行认证，在 Swagger UI 中：

1. 点击右上角的 **Authorize** 按钮
2. 在 `Authorization` 输入框中输入：`Bearer {your_token}`
3. 点击 **Authorize** 完成认证
4. 之后调用需要登录的接口时会自动携带 Token

### 3. 接口测试

每个接口都支持在线测试：

1. 点击接口展开详细信息
2. 点击 **Try it out** 按钮
3. 填写请求参数
4. 点击 **Execute** 执行请求
5. 查看响应结果

## 配置说明

### OpenAPI 配置

配置类位于：`com.hmdp.config.OpenApiConfig`

- **文档标题**: 黑马点评 API 文档
- **版本**: 1.0
- **描述**: 基于 SpringBoot3 + Redis 的分布式点评系统接口文档

### 安全配置

- **认证类型**: HTTP Bearer (JWT)
- **Token 格式**: `Bearer {token}`

## 依赖信息

项目使用的 Swagger 依赖：

```xml
<dependency>
    <groupId>org.springdoc</groupId>
    <artifactId>springdoc-openapi-starter-webmvc-ui</artifactId>
    <version>2.8.6</version>
</dependency>
```

## 注意事项

1. 部分接口需要先登录才能访问
2. 上传文件接口需要在本地配置好图片上传路径
3. 秒杀相关接口需要 Redis 环境支持
4. 数据库相关操作需要 MySQL 环境支持

## 常见问题

### Q: 无法访问 Swagger UI？
A: 确保应用已启动且端口为 8081，检查防火墙设置

### Q: 接口返回 401 错误？
A: 需要先通过 Authorize 按钮配置 JWT Token

### Q: 如何获取 Token？
A: 调用 `/user/login` 接口登录，响应中会返回 Token
