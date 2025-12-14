# API Keys 配置指南

## 概述

本项目集成了三个外部 API，需要配置相应的 API Keys：

1. **OpenAI API** - 图片识别功能（必需）
2. **USDA FoodData Central API** - 营养数据查询（推荐）
3. **Open Food Facts** - 条形码扫描（无需 Key，免费）

## 获取 API Keys

### 1. OpenAI API Key（用于图片识别）

**步骤：**
1. 访问 https://platform.openai.com/
2. 注册/登录账户
3. 进入 API Keys 页面：https://platform.openai.com/api-keys
4. 点击 "Create new secret key"
5. 复制生成的 API Key（格式：sk-...）

**费用：**
- GPT-4 Vision API 按使用量计费
- 大约 $0.01-0.03 每次图片分析
- 新用户通常有免费额度

### 2. USDA FoodData Central API Key（用于营养数据）

**步骤：**
1. 访问 https://fdc.nal.usda.gov/api-guide.html
2. 点击 "Get an API Key"
3. 填写申请表单
4. 接收邮件中的 API Key

**费用：**
- 完全免费
- 每小时限制 1000 次请求

### 3. Open Food Facts（条形码扫描）

**无需配置！**
- 完全免费开放 API
- 无需注册
- 已经可以直接使用

## 配置方法

### 方法一：使用 .env 文件（推荐）

在项目根目录创建或编辑 `.env` 文件：

```bash
# 数据库配置
DB_USERNAME=appuser
DB_PASSWORD=123456

# OpenAI API Key（替换为你的实际 Key）
OPENAI_API_KEY=sk-your-actual-openai-api-key-here

# USDA API Key（替换为你的实际 Key）
USDA_API_KEY=your-usda-api-key-here

# JWT 密钥
JWT_SECRET=your-secret-key-change-this-in-production-minimum-256-bits

# 文件上传目录
UPLOAD_DIR=./uploads
```

### 方法二：修改 application.yml

编辑 `src/main/resources/application.yml`：

```yaml
api:
  openai:
    key: sk-your-actual-openai-api-key-here
    url: https://api.openai.com/v1/chat/completions
    model: gpt-4-vision-preview

  usda:
    key: your-usda-api-key-here
    url: https://api.nal.usda.gov/fdc/v1
```

### 方法三：环境变量

在启动应用前设置环境变量：

```bash
export OPENAI_API_KEY=sk-your-actual-key-here
export USDA_API_KEY=your-usda-key-here
mvn spring-boot:run
```

## 重启应用

配置 API Keys 后，重启后端：

```bash
# 停止后端
pkill -f spring-boot:run

# 启动后端
mvn spring-boot:run
```

## 测试功能

### 测试图片识别（OpenAI）
1. 登录应用
2. 进入 "Add Meal" 页面
3. 上传一张食物图片
4. 等待 AI 分析结果

### 测试营养数据（USDA）
1. 在 "Add Meal" 页面搜索食物
2. 系统会从 USDA 数据库查询营养信息

### 测试条形码扫描（Open Food Facts）
1. 在 "Add Meal" 页面输入条形码
2. 点击 "Scan Barcode"
3. 无需配置，直接可用

## 功能对照表

| 功能 | 需要 API Key | 免费额度 | 状态 |
|------|------------|---------|------|
| 用户注册/登录 | ❌ 不需要 | ♾️ 无限 | ✅ 可用 |
| 膳食记录 | ❌ 不需要 | ♾️ 无限 | ✅ 可用 |
| 条形码扫描 | ❌ 不需要 | ♾️ 无限 | ✅ 可用 |
| 图片识别 | ✅ OpenAI | 💰 按使用付费 | ⚙️ 需配置 |
| 营养数据查询 | ✅ USDA | ✅ 免费 | ⚙️ 需配置 |

## 无 API Key 使用

如果不配置 API Keys，以下功能仍然可用：
- ✅ 用户注册和登录
- ✅ 手动添加食物
- ✅ 创建和管理膳食记录
- ✅ 查看每日卡路里统计
- ✅ 条形码扫描（Open Food Facts）
- ❌ 图片识别（需要 OpenAI Key）
- ⚠️ 营养数据查询（建议配置 USDA Key）

## 故障排除

### OpenAI API 返回 401
- 检查 API Key 是否正确
- 确认 Key 以 `sk-` 开头
- 检查账户是否有余额

### USDA API 返回错误
- 检查 API Key 是否正确
- 确认每小时请求未超过 1000 次

### 应用启动后配置不生效
- 确保重启了后端应用
- 检查环境变量是否正确设置
- 查看后端日志确认配置加载

## 安全提示

⚠️ **重要：**
- 永远不要将 API Keys 提交到 Git
- `.env` 文件已在 `.gitignore` 中
- 使用环境变量存储敏感信息
- 定期轮换 API Keys
