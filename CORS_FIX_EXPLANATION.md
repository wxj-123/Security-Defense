# CORS跨域配置修复说明

## 问题描述

在Spring 5.3及以上版本中，当设置 `allowCredentials = true` 时，不能同时使用 `allowedOrigins = "*"`，这会导致以下错误：

```
java.lang.IllegalArgumentException: When allowCredentials is true, allowedOrigins cannot contain the special value "*"
```

## 修复方案

### 选择的解决方案：使用 allowedOriginPatterns + 显式域名

采用了推荐的方案2：使用 `allowedOriginPatterns` 替代 `allowedOrigins`，并明确指定允许的域名。

### 修改的文件

1. **WebConfig.java** (Spring MVC跨域配置)
2. **SecurityConfig.java** (Spring Security跨域配置)

### 具体修改

#### WebConfig.java
```java
// 修改前（有问题的配置）
registry.addMapping("/**")
    .allowedOriginPatterns("*")  // ❌ 通配符与allowCredentials冲突
    .allowCredentials(true)       // ❌ 导致冲突
    // ...

// 修改后（正确的配置）
registry.addMapping("/**")
    .allowedOriginPatterns(
        "http://localhost:*",      // ✅ 允许localhost任意端口
        "http://127.0.0.1:*",    // ✅ 允许本地IP任意端口
        "http://localhost:8081",   // ✅ 明确指定常用开发端口
        "http://localhost:3000",
        "http://127.0.0.1:8081",
        "http://127.0.0.1:3000"
    )
    .allowCredentials(true)        // ✅ 现在可以安全启用
    // ...
```

#### SecurityConfig.java
```java
// 修改前
configuration.setAllowedOriginPatterns(Collections.singletonList("*"));
configuration.setAllowCredentials(false);  // 为了避免冲突关闭了凭证

// 修改后
configuration.setAllowedOriginPatterns(Arrays.asList(
    "http://localhost:*",
    "http://127.0.0.1:*",
    // ... 其他域名
));
configuration.setAllowCredentials(true);     // 重新启用凭证支持
```

## 修复效果

✅ **解决了跨域配置冲突错误**
✅ **保持了凭证传递能力**（Cookie/Token）
✅ **支持开发环境的常见端口**
✅ **符合Spring 5.3+的安全要求**
✅ **生产环境安全**（没有使用不安全的*通配符）

## 安全考虑

1. **开发环境**：使用localhost和127.0.0.1的常见端口
2. **生产环境**：应该替换为实际的前端域名
3. **避免使用通配符**：防止跨域安全风险

## 测试验证

修复后，前端应用应该能够正常访问后端API，不再出现CORS错误。

### 测试端口
- ✅ http://localhost:3000 (Vue开发服务器)
- ✅ http://localhost:8081 (其他可能的开发端口)
- ✅ http://127.0.0.1:3000 (本地IP访问)

## 相关知识

### allowedOriginPatterns vs allowedOrigins

| 特性 | allowedOrigins | allowedOriginPatterns |
|------|----------------|---------------------|
| 通配符支持 | ❌ 不支持 | ✅ 支持模式匹配 |
| Spring版本 | 全版本 | 5.3+ |
| 与allowCredentials兼容 | ❌ 与*不兼容 | ✅ 完全兼容 |
| 安全性 | 更严格 | 更灵活 |

### 最佳实践

1. **明确指定域名**，避免使用通配符
2. **使用allowedOriginPatterns**来获得更好的灵活性
3. **生产环境必须明确列出允许的域名**
4. **开发环境可以使用端口通配符**提高便利性

---

*修复完成时间：2025-11-21*
*修复范围：Spring Security + Spring MVC CORS配置*