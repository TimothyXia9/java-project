#!/bin/bash
# 启动 Spring Boot 后端

echo "启动后端服务..."
echo "端口: 8080"
echo "Profile: local"
echo "按 Ctrl+C 停止服务"
echo ""

# 加载 .env 文件（可选，如果使用环境变量）
if [ -f .env ]; then
    echo "加载 .env 配置..."
    export $(cat .env | grep -v '^#' | xargs)
fi

# 使用 local profile，这会加载 application-local.yml
mvn spring-boot:run -Dspring-boot.run.profiles=local

