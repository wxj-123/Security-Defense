# 网络安全防御系统 - 前端

基于 Vue3 + Vite + TypeScript + Element Plus 开发的网络安全防御系统前端界面。

## 技术栈

- **框架**: Vue 3.4+ (Composition API + script setup)
- **构建工具**: Vite 5.2+
- **语言**: TypeScript (严格模式)
- **UI 组件库**: Element Plus 2.7+
- **图标**: @element-plus/icons-vue
- **路由**: Vue Router 4.3+
- **状态管理**: Pinia 2.1+
- **HTTP 客户端**: Axios 1.6+
- **数据可视化**: ECharts 5.5+
- **加密**: crypto-js 4.2+

## 功能模块

### 1. 用户认证模块
- ✅ 登录/登出界面
- ✅ 双因素认证支持
- ✅ 会话管理
- ✅ JWT Token 管理
- ✅ 路由守卫

### 2. 仪表盘模块
- ✅ 实时安全态势展示
- ✅ 攻击流量统计图表
- ✅ 威胁等级指示器
- ✅ 系统健康状态监控
- ✅ 数据自动刷新

### 3. 安全监控模块
- ✅ 安全事件列表
- ✅ 事件筛选与搜索
- ✅ 事件处理操作
- 🚧 实时日志流展示
- 🚧 网络流量监控
- 🚧 异常行为检测

### 4. 策略管理模块
- ✅ 防火墙规则配置
- ✅ 规则增删改查
- ✅ 规则启用/禁用
- 🚧 访问控制列表(ACL)
- 🚧 安全策略模板
- 🚧 策略审批工作流

### 5. 报表分析模块
- 🚧 安全事件统计
- 🚧 威胁情报分析
- 🚧 合规性报告
- 🚧 自定义报表生成

## 安全特性

- ✅ 路由守卫验证
- ✅ API 请求加密 (可配置)
- ✅ JWT Token 管理
- ✅ CSRF 令牌验证
- ✅ 请求时间戳防重放
- ✅ 敏感数据加密存储
- ✅ XSS 防护 (Element Plus 内置)

## 项目结构

```
frontend/
├── src/
│   ├── api/              # API 接口定义
│   │   ├── auth.ts       # 认证接口
│   │   ├── dashboard.ts  # 仪表盘接口
│   │   ├── security.ts   # 安全监控接口
│   │   ├── policy.ts     # 策略管理接口
│   │   └── report.ts     # 报表接口
│   ├── assets/           # 静态资源
│   ├── components/       # 公共组件
│   │   └── MenuItem.vue  # 菜单项组件
│   ├── router/           # 路由配置
│   │   └── index.ts      # 路由定义
│   ├── stores/           # 状态管理
│   │   ├── user.ts       # 用户状态
│   │   └── app.ts        # 应用状态
│   ├── styles/           # 样式文件
│   │   └── index.css     # 全局样式
│   ├── types/            # TypeScript 类型定义
│   │   └── index.ts      # 类型声明
│   ├── utils/            # 工具函数
│   │   └── request.ts    # Axios 封装
│   ├── views/            # 页面组件
│   │   ├── Login.vue     # 登录页
│   │   ├── Layout.vue    # 布局组件
│   │   ├── 404.vue       # 404 页面
│   │   ├── Dashboard/    # 仪表盘
│   │   ├── Security/     # 安全监控
│   │   ├── Policy/       # 策略管理
│   │   └── Report/       # 报表分析
│   ├── App.vue           # 根组件
│   ├── main.ts           # 入口文件
│   └── vite-env.d.ts     # 环境变量类型
├── index.html            # HTML 模板
├── vite.config.ts        # Vite 配置
├── tsconfig.json         # TypeScript 配置
├── package.json          # 项目依赖
├── .env.development      # 开发环境变量
└── .env.production       # 生产环境变量
```

## 快速开始

### 安装依赖

```bash
cd frontend
npm install
```

### 开发运行

```bash
npm run dev
```

访问 http://localhost:3000

### 生产构建

```bash
npm run build
```

### 预览构建结果

```bash
npm run preview
```

## 环境变量

### 开发环境 (.env.development)
```env
VITE_API_BASE_URL=/api
VITE_AES_KEY=your-local-key
VITE_ENABLE_ENCRYPTION=false
```

### 生产环境 (.env.production)
```env
VITE_API_BASE_URL=https://api.security-defense.com
VITE_AES_KEY=your-production-key-here
VITE_ENABLE_ENCRYPTION=true
```

## API 代理配置

在 `vite.config.ts` 中配置了开发环境的 API 代理：

```typescript
server: {
  port: 3000,
  proxy: {
    '/api': {
      target: 'http://localhost:8085',
      changeOrigin: true,
    },
  },
}
```

## 代码规范

- 使用 TypeScript 严格模式
- 使用 Vue 3 Composition API
- 使用 `<script setup>` 语法糖
- 遵循 ESLint 代码规范
- 组件命名使用 PascalCase
- 文件命名使用 kebab-case

## 浏览器支持

- Chrome >= 87
- Firefox >= 78
- Safari >= 14
- Edge >= 88

## 开发说明

### 添加新页面

1. 在 `src/views/` 下创建页面组件
2. 在 `src/router/index.ts` 中添加路由配置
3. 如需权限控制，在路由 meta 中添加权限标识

### 添加新 API

1. 在 `src/api/` 下创建或修改 API 模块
2. 在 `src/types/index.ts` 中定义类型
3. 使用封装的 `http` 对象发起请求

### 状态管理

使用 Pinia 进行状态管理，每个模块一个 store 文件。

## License

MIT
