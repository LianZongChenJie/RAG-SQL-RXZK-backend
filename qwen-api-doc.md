# 千问AI聊天接口文档

## 接口信息

**基础路径：** `/qwen`

---

## 聊天接口

### 1. 千问AI聊天

**请求信息**
- **URL：** `/qwen/chat`
- **方法：** `POST`
- **Content-Type：** `application/json`

**请求参数**
```json
{
  "userId": "1",      // 用户ID，必填
  "message": "你好"    // 消息内容，必填
}
```

**响应示例（成功）**
```json
{
  "code": 200,
  "message": "AI回复成功",
  "data": {
    "userId": "1",
    "userMessage": "你好",
    "aiReply": "你好！有什么我可以帮助你的吗？",
    "timestamp": 1719046800000
  }
}
```

**响应示例（失败）**
```json
{
  "code": 500,
  "message": "AI服务异常: 详细错误信息",
  "data": null
}
```

**文件位置：**
- Controller: `src/main/java/com/example/demo/controller/QwenController.java`
- Service: `src/main/java/com/example/demo/service/QwenService.java`

---

## 技术实现

**使用的依赖：**
- Spring Web (RestTemplate)
- Fastjson2 (JSON处理)
- 阿里云千问API

**配置信息：**
```yaml
qwen:
  api:
    key: sk-ws-H.RPEELDP.GBk3.MEUCIC7TzF9BUIx53nBvntodcjL2u0ze4OXDWAMi97g6NtF4AiEAg_cu2_DwcUBj3J8neG0DXp5e2aRGu5yQxf0OcxUcv_E
    host: llm-yfsmtft5nvzlpqx9.cn-beijing.maas.aliyuncs.com
```

---

## 注意事项

1. **CORS 配置：** 接口允许跨域访问
2. **参数校验：** 使用 `@Valid` 注解进行参数校验
3. **超时设置：** 连接超时30秒，读取超时60秒
4. **错误处理：** 所有异常由全局异常处理器统一处理

---

## 测试示例

**cURL 命令：**
```bash
curl -X POST http://localhost:9091/qwen/chat \
  -H "Content-Type: application/json" \
  -d '{
    "userId": "1",
    "message": "你好"
  }'
```

**Postman 测试：**
1. 选择 POST 方法
2. 输入URL: `http://localhost:9091/qwen/chat`
3. 在Headers中添加: `Content-Type: application/json`
4. 在Body中选择raw/JSON，输入：
   ```json
   {
     "userId": "1",
     "message": "你好"
   }
   ```
5. 点击Send发送请求