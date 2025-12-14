# 营养追踪系统 - 启动指南

## 前置要求

1. Java 17 或更高版本
2. Maven 3.6+
3. MySQL 8.0+
4. Node.js 18+ 和 npm
5. API 密钥：
   - OpenAI API Key (用于图片识别)
   - USDA FoodData Central API Key (用于营养数据)

## 数据库设置

1. 启动 MySQL 服务

2. 创建数据库：
```sql
CREATE DATABASE nutrition_tracker;
```

3. 更新配置文件或设置环境变量：
   - 方式一：修改 `src/main/resources/application.yml`
   - 方式二：设置环境变量（推荐）

## 环境变量配置

在项目根目录创建 `.env` 文件（或在系统中设置）：

```bash
# 数据库配置
DB_USERNAME=root
DB_PASSWORD=your_password

# API 密钥
OPENAI_API_KEY=your_openai_api_key
USDA_API_KEY=your_usda_api_key

# JWT 密钥（至少 256 位）
JWT_SECRET=your-secret-key-at-least-256-bits-long-for-security

# 文件上传目录
UPLOAD_DIR=./uploads
```

## 后端启动

### 方式一：使用 Maven

```bash
# 安装依赖
./mvnw clean install

# 运行应用
./mvnw spring-boot:run
```

### 方式二：使用 IDE

在 IDE 中打开项目，运行 `NutritionTrackerApplication.java`

后端将在 `http://localhost:8080` 启动

## 前端启动

```bash
# 进入前端目录
cd frontend

# 安装依赖
npm install

# 启动开发服务器
npm run dev
```

前端将在 `http://localhost:3000` 启动

## 访问应用

1. 打开浏览器访问 `http://localhost:3000`
2. 注册新账户或使用测试账户登录
3. 开始追踪你的营养摄入！

## 主要功能

### 用户管理
- 注册和登录
- 个人资料管理（年龄、体重、身高、活动水平）
- 每日卡路里目标计算

### 膳食记录
- 添加早餐、午餐、晚餐、零食
- 通过搜索添加食物
- 条形码扫描（通过 Open Food Facts API）
- 图片识别（通过 OpenAI GPT-4 Vision）

### 数据统计
- 每日卡路里摄入总计
- 按日期查看膳食记录
- 营养成分详情

## API 端点

### 认证
- POST `/api/auth/register` - 用户注册
- POST `/api/auth/login` - 用户登录

### 用户
- GET `/api/users/profile` - 获取用户资料
- PUT `/api/users/profile` - 更新用户资料
- GET `/api/users/recommended-calories` - 获取推荐卡路里

### 膳食
- POST `/api/meals` - 创建膳食记录
- GET `/api/meals/date/{date}` - 按日期获取膳食
- GET `/api/meals/{id}` - 获取单个膳食
- DELETE `/api/meals/{id}` - 删除膳食

### 食物
- GET `/api/foods` - 获取所有食物
- GET `/api/foods/search?name={name}` - 搜索食物
- GET `/api/foods/barcode/{barcode}` - 条形码查询
- POST `/api/foods` - 创建食物

### 图片识别
- POST `/api/image/analyze` - 分析食物图片

### 条形码
- GET `/api/barcode/{barcode}` - 通过条形码获取食物信息

## 测试

### 运行后端测试
```bash
./mvnw test
```

### 运行单个测试
```bash
./mvnw test -Dtest=ClassName#methodName
```

### 前端测试
```bash
cd frontend
npm test
```

## 生产环境构建

### 后端
```bash
./mvnw clean package
java -jar target/nutrition-tracker-1.0.0.jar
```

### 前端
```bash
cd frontend
npm run build
```

构建的文件将在 `frontend/dist` 目录中。

## 故障排除

### 数据库连接失败
- 确认 MySQL 服务正在运行
- 检查数据库用户名和密码
- 确认数据库已创建

### API 调用失败
- 检查 API 密钥是否正确配置
- 确认网络连接正常
- 查看后端日志了解详细错误信息

### 前端无法连接后端
- 确认后端服务正在运行在 8080 端口
- 检查 CORS 配置
- 查看浏览器控制台的错误信息

## 技术栈

**后端**
- Spring Boot 3.2.0
- Spring Security + JWT
- Spring Data JPA
- MySQL
- Lombok
- RestTemplate

**前端**
- React 18
- React Router
- Axios
- Vite

**外部 API**
- OpenAI GPT-4 Vision
- USDA FoodData Central
- Open Food Facts
