# 如何启动项目

## 方法一：使用启动脚本（推荐）

### 1. 启动后端
```bash
cd /home/tim/java-project
./start-backend.sh
```

### 2. 启动前端（在新终端）
```bash
cd /home/tim/java-project/frontend
./start-frontend.sh
```

## 方法二：手动命令

### 启动后端
```bash
cd /home/tim/java-project
mvn spring-boot:run
```

### 启动前端（在新终端）
```bash
cd /home/tim/java-project/frontend
npm run dev
```

## 停止服务

### 停止当前终端的服务
在运行服务的终端按 **Ctrl+C**

### 停止后台运行的服务
```bash
# 查找进程
ps aux | grep -E "spring-boot|vite" | grep -v grep

# 停止后端 (Spring Boot)
pkill -f spring-boot:run

# 停止前端 (Vite)
pkill -f vite
```

## 检查服务状态

### 检查后端是否运行
```bash
curl http://localhost:8080/api/auth/login
# 如果返回错误信息（而不是连接失败），说明后端正在运行
```

### 检查前端是否运行
```bash
curl http://localhost:3000
# 如果返回 HTML 内容，说明前端正在运行
```

### 查看端口占用
```bash
# 检查 8080 端口（后端）
lsof -i :8080

# 检查 3000 端口（前端）
lsof -i :3000
```

## 开发流程

### 第一次启动
1. 确保 MySQL 正在运行
2. 启动后端（等待约 5-10 秒启动完成）
3. 启动前端（等待约 2-3 秒）
4. 访问 http://localhost:3000

### 后续启动
1. 启动后端
2. 启动前端
3. 开始开发

### 停止所有服务
```bash
# 一次性停止所有服务
pkill -f spring-boot:run
pkill -f vite
```

## 常见问题

### 端口已被占用
如果提示端口已被占用，说明服务已经在运行，可以：
- 直接使用（无需重新启动）
- 或停止旧进程后重新启动

### 后端启动失败
检查：
1. MySQL 是否运行：`systemctl status mysql`
2. 数据库配置是否正确：检查 `application.yml`
3. Java 版本：`java --version`（需要 Java 17）

### 前端启动失败
检查：
1. 依赖是否安装：`cd frontend && npm install`
2. Node 版本：`node --version`（需要 Node 18+）

## 访问应用

- **前端界面**: http://localhost:3000
- **后端 API**: http://localhost:8080/api

## 后台运行（可选）

### 使用 nohup 后台运行
```bash
# 后端
nohup mvn spring-boot:run > backend.log 2>&1 &

# 前端
cd frontend
nohup npm run dev > frontend.log 2>&1 &
```

### 查看日志
```bash
# 后端日志
tail -f backend.log

# 前端日志
tail -f frontend/frontend.log
```
