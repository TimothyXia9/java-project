#!/bin/bash
# 启动 Spring Boot 后端

echo "启动后端服务..."
echo "端口: 8080"
echo "按 Ctrl+C 停止服务"
echo ""

# 加载 .env 文件
if [ -f .env ]; then
    echo "加载 .env 配置..."
    export $(cat .env | grep -v '^#' | xargs)
fi

mvn spring-boot:run
