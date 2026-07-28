#!/bin/bash

# 启动应用
./mvnw spring-boot:run -Dspring-boot.run.jvmArguments="-Dspring.main.allow-bean-definition-overriding=true" > app.log 2>&1 &
APP_PID=$!

# 等待应用启动
echo "等待应用启动..."
sleep 5

# 检查应用是否启动成功
if ! grep -q "Started DemoApplication" app.log; then
    echo "应用启动失败，查看错误日志："
    cat app.log
    kill $APP_PID
    exit 1
fi

echo "应用启动成功"

# 注册新用户
echo "注册新用户..."
REGISTER_RESPONSE=$(curl -s -X POST \
  http://localhost:9091/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "account": "testuser",
    "password": "password123",
    "phone": "13800138000",
    "email": "test@example.com",
    "nickname": "测试用户"
  }')

echo "注册响应: $REGISTER_RESPONSE"

# 从注册响应中提取token
TOKEN=$(echo $REGISTER_RESPONSE | grep -o '"token":"[^"]*' | cut -d'"' -f4)

if [ -z "$TOKEN" ]; then
    echo "注册失败或未获取到token，尝试登录..."
    
    # 如果注册失败，尝试登录（假设用户已存在）
    LOGIN_RESPONSE=$(curl -s -X POST \
      http://localhost:9091/api/auth/login \
      -H "Content-Type: application/json" \
      -d '{
        "account": "testuser",
        "password": "password123"
      }')
    
    echo "登录响应: $LOGIN_RESPONSE"
    TOKEN=$(echo $LOGIN_RESPONSE | grep -o '"token":"[^"]*' | cut -d'"' -f4)
fi

if [ -n "$TOKEN" ]; then
    echo "获取到的Token: $TOKEN"
    
    # 使用token获取用户信息
    echo "调用用户信息接口..."
    USER_INFO_RESPONSE=$(curl -s -X GET \
      http://localhost:9091/api/user/info \
      -H "Authorization: Bearer $TOKEN")
    
    echo "用户信息响应: $USER_INFO_RESPONSE"
else
    echo "无法获取有效Token"
fi

# 停止应用
echo "停止应用..."
kill $APP_PID