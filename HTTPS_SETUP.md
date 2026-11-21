# HTTPS 配置说明

## 概述

本文档说明如何为安全防御系统配置HTTPS访问，确保数据传输的安全性。

## 配置完成的内容

### 1. SSL证书生成

系统已生成自签名证书（用于开发环境）：

**后端证书**:
- **证书文件**: `src/main/resources/security-defense.p12`
- **证书别名**: `securitydefense`
- **证书密码**: `your-keystore-password`
- **有效期**: 365天
- **算法**: RSA 2048位

**前端证书**:
- **证书文件**: `frontend/frontend.p12`
- **证书别名**: `frontend`
- **证书密码**: `your-keystore-password`
- **有效期**: 365天
- **算法**: RSA 2048位

### 2. 后端配置 (Spring Boot)

`src/main/resources/application.properties` 已添加以下SSL配置：

```properties
# SSL/TLS配置
server.port=8443
server.ssl.enabled=true
server.ssl.key-store=classpath:security-defense.p12
server.ssl.key-store-type=PKCS12
server.ssl.key-store-password=your-keystore-password
server.ssl.key-alias=securitydefense
server.ssl.protocol=TLS
```

**重要变更**:
- 端口从 8085 改为 **8443**（HTTPS默认端口）
- 启用了SSL/TLS加密

### 3. 前端配置 (Vue 3 + Vite)

`frontend/vite.config.ts` 已更新HTTPS服务器配置和代理配置：

```typescript
import fs from 'fs'
import path from 'path'

export default defineConfig({
  server: {
    port: 3000,
    https: {
      pfx: fs.readFileSync(path.resolve(__dirname, 'frontend.p12')),
      passphrase: 'your-keystore-password',
    },
    proxy: {
      '/api': {
        target: 'https://localhost:8443',
        changeOrigin: true,
        secure: false, // 允许自签名证书
        ws: false,
      },
      '/uploads': {
        target: 'https://localhost:8443',
        changeOrigin: true,
        secure: false,
      },
    },
  },
})
```

**重要变更**:
- 添加了 `https` 配置，启用HTTPS服务器
- 使用 `frontend.p12` 证书文件
- 前端和后端通信均为HTTPS加密

**说明**:
- `target` 改为 `https://localhost:8443`
- `secure: false` 允许使用自签名证书（仅开发环境）
- 前端开发服务器现在也通过HTTPS提供服务

## 启动说明

### 启动后端服务

```bash
# 方式1: 使用批处理文件
.\start-backend.bat

# 方式2: 使用Maven
mvnw spring-boot:run
```

后端服务将在 `https://localhost:8443` 启动

### 启动前端服务

```bash
cd frontend
npm run dev
```

前端服务将在 `https://localhost:3000` 启动（使用HTTPS协议），所有API请求会自动代理到HTTPS后端

**注意**: 首次启动前端时，浏览器会提示证书不受信任，这是正常的自签名证书警告。

## 浏览器访问

### 访问前端界面

```
https://localhost:3000
```

### 直接访问后端API

```
https://localhost:8443/api/xxx
```

### 访问提示

由于前后端都使用自签名证书，浏览器会提示"连接不安全"或"您的连接不是私密连接"，这是正常的。

**解决方法**:
1. 点击 **"高级"**
2. 点击 **"继续前往 localhost"** 或 **"接受风险并继续"**

**安全说明**:
- 自签名证书仅用于开发环境，生产环境必须使用正式SSL证书
- 浏览器的警告不影响正常开发和测试

## 生产环境部署

### 1. 获取正式SSL证书

对于生产环境，建议使用正式SSL证书：

- **Let's Encrypt** (免费)
- **DigiCert**
- **GlobalSign**
- 其他商业证书颁发机构

### 2. 导入正式证书

```bash
keytool -importkeystore \
  -srckeystore your-certificate.p12 \
  -destkeystore src/main/resources/security-defense.p12 \
  -deststoretype PKCS12
```

### 3. 更新配置

修改 `application.properties`:
```properties
server.ssl.key-store-password=your-actual-password
server.ssl.key-alias=your-actual-alias
```

### 4. 前端环境变量

生产环境无需修改 `frontend/.env.production`，已配置为使用HTTPS。

### 5. 前端生产环境HTTPS部署

对于生产环境，前端部署方案：

**方案1: 使用Nginx反向代理**
```nginx
server {
    listen 443 ssl http2;
    server_name your-domain.com;

    ssl_certificate /path/to/your/certificate.crt;
    ssl_certificate_key /path/to/your/private.key;

    # 前端静态资源
    location / {
        root /path/to/frontend/dist;
        try_files $uri $uri/ /index.html;
    }

    # API代理
    location /api {
        proxy_pass https://localhost:8443;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
    }
}
```

**方案2: 使用CDN + HTTPS**
- 将前端构建产物部署到CDN
- CDN启用HTTPS
- API请求直接到后端HTTPS地址

## 常见问题

### 1. 浏览器提示"连接不安全"

**原因**: 使用自签名证书

**解决**: 开发环境点击"继续访问"；生产环境使用正式证书

### 2. 前端请求失败

**原因**: 证书验证失败

**解决**:
- 确认 `vite.config.ts` 中设置了 `secure: false`
- 确认后端服务已启动且端口为 8443
- 检查前端证书文件 `frontend.p12` 是否存在且路径正确

### 3. 前端启动失败 - 证书相关错误

**原因**: 证书文件不存在或密码错误

**解决**:
```bash
cd frontend
# 检查证书文件是否存在
dir frontend.p12

# 如果不存在，重新生成
keytool -genkeypair -alias frontend -keyalg RSA -keysize 2048 \
  -storetype PKCS12 -keystore frontend.p12 -validity 365 \
  -storepass your-keystore-password -keypass your-keystore-password \
  -dname "CN=localhost, OU=IT, O=SecurityDefense, L=City, ST=State, C=CN"
```

### 4. 浏览器反复提示证书不安全

**原因**: 自签名证书不受浏览器信任

**解决**:
- **开发环境**: 正常现象，点击"继续访问"即可
- **长期使用**: 可以手动将自签名证书添加到浏览器信任存储
  - Chrome: 设置 → 隐私和安全 → 安全 → 管理证书 → 受信任的根证书颁发机构 → 导入
  - Firefox: 选项 → 隐私与安全 → 证书 → 查看证书 → 证书颁发机构 → 导入

### 3. 重新生成证书

如果需要重新生成证书：

**后端证书**:

```bash
cd d:/dask/Security-Defense

# 删除旧证书
del src\main\resources\security-defense.p12

# 生成新证书
keytool -genkeypair -alias securitydefense -keyalg RSA -keysize 2048 \
  -storetype PKCS12 -keystore security-defense.p12 -validity 365 \
  -storepass your-keystore-password -keypass your-keystore-password \
  -dname "CN=localhost, OU=IT, O=SecurityDefense, L=City, ST=State, C=CN"

# 移动到resources目录
move security-defense.p12 src\main\resources\
```

**前端证书**:

```bash
cd d:/dask/Security-Defense/frontend

# 删除旧证书
del frontend.p12

# 生成新证书
keytool -genkeypair -alias frontend -keyalg RSA -keysize 2048 \
  -storetype PKCS12 -keystore frontend.p12 -validity 365 \
  -storepass your-keystore-password -keypass your-keystore-password \
  -dname "CN=localhost, OU=IT, O=SecurityDefense, L=City, ST=State, C=CN"
```

### 5. 重新生成证书

如果需要重新生成证书：

**后端证书**:

```bash
cd d:/dask/Security-Defense

# 删除旧证书
del src\main\resources\security-defense.p12

# 生成新证书
keytool -genkeypair -alias securitydefense -keyalg RSA -keysize 2048 \
  -storetype PKCS12 -keystore security-defense.p12 -validity 365 \
  -storepass your-keystore-password -keypass your-keystore-password \
  -dname "CN=localhost, OU=IT, O=SecurityDefense, L=City, ST=State, C=CN"

# 移动到resources目录
move security-defense.p12 src\main\resources\
```

**前端证书**:

```bash
cd d:/dask/Security-Defense/frontend

# 删除旧证书
del frontend.p12

# 生成新证书
keytool -genkeypair -alias frontend -keyalg RSA -keysize 2048 \
  -storetype PKCS12 -keystore frontend.p12 -validity 365 \
  -storepass your-keystore-password -keypass your-keystore-password \
  -dname "CN=localhost, OU=IT, O=SecurityDefense, L=City, ST=State, C=CN"
```

## 安全建议

1. **生产环境**:
   - 使用正式SSL证书
   - 定期更新证书（推荐有效期90天）
   - 使用强密码保护keystore

2. **开发环境**:
   - 使用环境变量存储密码
   - 不将证书文件提交到版本控制
   - 定期更新开发证书

3. **配置文件安全**:
   - 将敏感配置（密码、密钥）放入 `application-local.properties`
   - 添加 `.gitignore` 规则忽略本地配置文件

## HTTP重定向到HTTPS（可选）

如需自动将HTTP请求重定向到HTTPS，可在Spring Boot中添加以下配置类：

```java
@Configuration
public class HttpsConfig {
    @Bean
    public ServletWebServerFactory servletContainer() {
        TomcatServletWebServerFactory tomcat = new TomcatServletWebServerFactory() {
            @Override
            protected void postProcessContext(Context context) {
                SecurityConstraint securityConstraint = new SecurityConstraint();
                securityConstraint.setUserConstraint("CONFIDENTIAL");
                SecurityCollection collection = new SecurityCollection();
                collection.addPattern("/*");
                securityConstraint.addCollection(collection);
                context.addConstraint(securityConstraint);
            }
        };
        tomcat.addAdditionalTomcatConnectors(redirectConnector());
        return tomcat;
    }

    private Connector redirectConnector() {
        Connector connector = new Connector("org.apache.coyote.http11.Http11NioProtocol");
        connector.setScheme("http");
        connector.setPort(8085);
        connector.setSecure(false);
        connector.setRedirectPort(8443);
        return connector;
    }
}
```

## 参考资料

- [Spring Boot SSL配置官方文档](https://docs.spring.io/spring-boot/docs/current/reference/html/howto.html#howto-configure-ssl)
- [Vite开发服务器HTTPS配置](https://vitejs.dev/config/server-options.html#server-https)
- [Vite开发服务器代理配置](https://vitejs.dev/config/server-options.html#server-proxy)
- [HTTPS与TLS最佳实践](https://cheatsheetseries.owasp.org/cheatsheets/Transport_Layer_Protection_Cheat_Sheet.html)

---

**配置完成日期**: 2026-02-03
**配置版本**: v2.0 (前端已启用HTTPS)
**更新日志**:
- v1.0: 后端HTTPS配置
- v2.0: 前端开发服务器HTTPS配置，实现全链路加密
