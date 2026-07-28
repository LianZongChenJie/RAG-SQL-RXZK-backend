# API 接口文档

## 目录
- [认证接口](#认证接口)
- [用户管理接口](#用户管理接口)
- [异常测试接口](#异常测试接口)
- [拦截器配置](#拦截器配置)

---

## 认证接口

**基础路径：** `/api/auth`

### 1. 用户注册

**请求信息**
- **URL：** `/api/auth/register`
- **方法：** `POST`
- **Content-Type：** `application/json`

**请求参数**
```json
{
  "account": "string",      // 账号，必填，长度8-20
  "password": "string",     // 密码，必填，长度8-20，需包含字母、数字和特殊字符
  "phone": "string",        // 手机号，必填
  "email": "string",        // 邮箱，选填，邮箱格式
  "nickname": "string"      // 昵称，必填，长度1-8
}
```

**响应示例**
```json
{
  "code": 200,
  "message": "操作成功",
  "data": "string"
}
```

**文件位置：** `src/main/java/com/example/demo/controller/AuthController.java:27-30`

---

### 2. 用户登录

**请求信息**
- **URL：** `/api/auth/login`
- **方法：** `POST`
- **Content-Type：** `application/json`

**请求参数**
```json
{
  "account": "string",      // 账号，必填
  "password": "string"      // 密码，必填
}
```

**响应示例**
```json
{
  "code": 200,
  "message": "操作成功",
  "data": "string"          // 返回token
}
```

**文件位置：** `src/main/java/com/example/demo/controller/AuthController.java:34-37`

---

## 用户管理接口

**基础路径：** `/api/user`

**注意：** 除 `/api/auth/login` 和 `/api/auth/register` 外，所有 `/api/**` 路径都需要在请求头中携带 token

### 1. 获取用户列表（不分页）

**请求信息**
- **URL：** `/api/user/list`
- **方法：** `GET`
- **请求头：** `Authorization: <token>`

**响应示例**
```json
{
  "code": 200,
  "message": "操作成功",
  "data": [...]
}
```

**文件位置：** `src/main/java/com/example/demo/controller/UserController.java:30-33`

---

### 2. 分页获取用户列表

**请求信息**
- **URL：** `/api/user/pageList`
- **方法：** `GET`
- **请求头：** `Authorization: <token>`

**请求参数**
- `pageNum`：页码，默认1
- `pageSize`：每页数量，默认10

**示例请求：** `GET /api/user/pageList?pageNum=1&pageSize=10`

**响应示例**
```json
{
  "code": 200,
  "message": "操作成功",
  "data": {...}
}
```

**文件位置：** `src/main/java/com/example/demo/controller/UserController.java:38-46`

---

### 3. 根据用户ID获取用户信息

**请求信息**
- **URL：** `/api/user/{userId}`
- **方法：** `GET`
- **请求头：** `Authorization: <token>`

**路径参数**
- `userId`：用户ID

**示例请求：** `GET /api/user/123`

**响应示例**
```json
{
  "code": 200,
  "message": "操作成功",
  "data": {...}
}
```

**文件位置：** `src/main/java/com/example/demo/controller/UserController.java:51-54`

---

### 4. 根据账号获取用户信息

**请求信息**
- **URL：** `/api/user/account/{account}`
- **方法：** `GET`
- **请求头：** `Authorization: <token>`

**路径参数**
- `account`：用户账号

**示例请求：** `GET /api/user/account/user123`

**响应示例**
```json
{
  "code": 200,
  "message": "操作成功",
  "data": {...}
}
```

**文件位置：** `src/main/java/com/example/demo/controller/UserController.java:59-62`

---

### 5. 通过token获取用户信息

**请求信息**
- **URL：** `/api/user/info`
- **方法：** `GET`
- **请求头：** `Authorization: <token>`

**响应示例**
```json
{
  "code": 200,
  "message": "操作成功",
  "data": {...}
}
```

**文件位置：** `src/main/java/com/example/demo/controller/UserController.java:67-70`

---

## 异常测试接口

**基础路径：** `/api/test`

### 1. 测试业务异常

**请求信息**
- **URL：** `/api/test/businessException`
- **方法：** `GET`

**响应示例**
```json
{
  "code": 400,
  "message": "这是一个业务异常测试",
  "data": null
}
```

**文件位置：** `src/main/java/com/example/demo/controller/TestExceptionController.java:25-28`

---

### 2. 测试运行时异常

**请求信息**
- **URL：** `/api/test/runtimeException`
- **方法：** `GET`

**响应示例**
```json
{
  "code": 500,
  "message": "这是一个运行时异常测试",
  "data": null
}
```

**文件位置：** `src/main/java/com/example/demo/controller/TestExceptionController.java:33-36`

---

### 3. 测试参数校验异常

**请求信息**
- **URL：** `/api/test/validation`
- **方法：** `POST`
- **Content-Type：** `application/json`

**请求参数**
```json
{
  "username": "string",  // 用户名，必填
  "age": 18             // 年龄，>=18
}
```

**响应示例（成功）**
```json
{
  "code": 200,
  "message": "操作成功",
  "data": "参数校验通过"
}
```

**文件位置：** `src/main/java/com/example/demo/controller/TestExceptionController.java:41-44`

---

### 4. 测试空指针异常

**请求信息**
- **URL：** `/api/test/nullPointerException`
- **方法：** `GET`

**响应示例**
```json
{
  "code": 500,
  "message": "NullPointerException",
  "data": null
}
```

**文件位置：** `src/main/java/com/example/demo/controller/TestExceptionController.java:49-53`

---

### 5. 测试查询参数校验

**请求信息**
- **URL：** `/api/test/queryValidation`
- **方法：** `GET`

**请求参数**
- `name`：姓名，必填
- `age`：年龄，>=1

**示例请求：** `GET /api/test/queryValidation?name=张三&age=25`

**响应示例（成功）**
```json
{
  "code": 200,
  "message": "操作成功",
  "data": "查询参数校验通过"
}
```

**文件位置：** `src/main/java/com/example/demo/controller/TestExceptionController.java:58-63`

---

### 6. 正常请求（对比用）

**请求信息**
- **URL：** `/api/test/normal`
- **方法：** `GET`

**响应示例**
```json
{
  "code": 200,
  "message": "这是一个正常请求",
  "data": null
}
```

**文件位置：** `src/main/java/com/example/demo/controller/TestExceptionController.java:68-71`

---

## 拦截器配置

**拦截器：** `TokenInterceptor`

**拦截规则：**
- **拦截路径：** `/api/**`
- **排除路径：**
  - `/api/auth/login`
  - `/api/auth/register`
  - `/api/auth/**`

**说明：** 除登录和注册接口外，所有其他接口都需要在请求头中携带有效的 token

**文件位置：** `src/main/java/com/example/demo/config/WebConfig.java:16-30`

---

## 全局异常处理

系统配置了全局异常处理器 `GlobalExceptionHandler`，统一处理以下异常：

- **业务异常 (BusinessException)**
- **参数校验异常 (MethodArgumentNotValidException)**
- **运行时异常 (RuntimeException)**
- **空指针异常 (NullPointerException)**
- 其他未捕获异常

所有异常都会返回统一的 JSON 格式响应：

```json
{
  "code": 400/500,
  "message": "错误信息",
  "data": null
}
```

**文件位置：** `src/main/java/com/example/demo/handler/GlobalExceptionHandler.java`

---

## 接口汇总

| 控制器 | 接口数量 | 接口类型 |
|--------|---------|---------|
| AuthController | 2 | 认证相关（注册、登录）|
| UserController | 5 | 用户管理（查询、分页）|
| TestExceptionController | 6 | 异常测试（业务、校验、NPE等）|
| **总计** | **13** | **完整接口列表** |

---

## 注意事项

1. **CORS 配置：** 所有接口均允许跨域访问
2. **Token 认证：** 除 `/api/auth/login` 和 `/api/auth/register` 外，所有接口都需要携带 token
3. **参数校验：** 使用 `@Valid` 注解进行参数校验，校验失败会自动返回错误信息
4. **统一响应格式：** 所有接口返回统一的 Result 格式
5. **全局异常处理：** 所有异常由 `GlobalExceptionHandler` 统一处理