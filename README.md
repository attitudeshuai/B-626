# 五子棋对战平台 (Gomoku Battle)

一款支持人机对战和双人对战的在线五子棋游戏平台。

## 🛠 技术栈

- **Frontend**: Vue 3 + Vite + Tailwind CSS + Pinia
- **Backend**: Java 17 + Spring Boot 3.2 + MyBatis-Plus
- **Database**: MySQL 8.0
- **Security**: Spring Security + JWT

## 🚀 快速启动 (Docker)

1. 确保 Docker Desktop 已运行

2. 在根目录执行：
```bash
docker compose up --build
```

3. 等待所有服务启动完成（首次启动需要较长时间下载依赖）

4. 访问应用：
   - 前端：http://localhost:3000

## 🧪 测试账号

| 用户名 | 密码 | 说明 |
|--------|------|------|
| player1 | test123456 | 测试玩家账户1 |
| player2 | test123456 | 测试玩家账户2 |

## 📸 功能介绍

### 1. 用户系统
- 用户注册/登录
- 个人信息管理
- 战绩统计

### 2. 游戏模式
- **人机对战**：支持简单/中等/困难三种难度
- **双人对战**：本地双人轮流落子
- **联机对战**：联机双人轮流落子

### 3. 游戏功能
- 15×15标准棋盘
- 悔棋功能（每局3次）
- 认输功能
- 落子历史显示

### 4. 其他功能
- 历史记录查看
- 棋谱回放
- 排行榜（胜率/胜场）

