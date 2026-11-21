# IntelliJ IDEA 内存优化指南

## 问题分析
- 当前可用内存：828 MB
- 总内存：2048 MB  
- 内存使用接近上限，需要优化

## 优化方案

### 1. 自动优化（推荐）
运行 `optimize-idea-memory.bat` 脚本：
```batch
optimize-idea-memory.bat
```
这将自动：
- 备份原始 JVM 配置文件
- 应用优化后的内存设置
- 重启 IDEA 后生效

### 2. 手动优化步骤

#### 步骤 1：找到 JVM 配置文件
通常位于：
```
C:\Program Files\JetBrains\IntelliJ IDEA <版本>\bin\idea64.exe.vmoptions
```

#### 步骤 2：备份原始配置
```batch
copy "C:\Program Files\JetBrains\IntelliJ IDEA <版本>\bin\idea64.exe.vmoptions" "C:\Program Files\JetBrains\IntelliJ IDEA <版本>\bin\idea64.exe.vmoptions.bak"
```

#### 步骤 3：编辑配置文件
将以下内容复制到配置文件中：

```properties
# 堆内存设置
-Xms512m
-Xmx1024m

# 元空间内存
-XX:MetaspaceSize=256m
-XX:MaxMetaspaceSize=512m

# 垃圾回收优化
-XX:+UseG1GC
-XX:MaxGCPauseMillis=200
-XX:InitiatingHeapOccupancyPercent=35

# 压缩类指针（64位 JVM）
-XX:+UseCompressedOops
-XX:+UseCompressedClassPointers

# 禁用不必要的后台处理
-Didea.max.content.load.files.limit=1000

# 优化文件监控
-Dsun.nio.ch.maxUpdateArraySize=100

# 减少 JIT 编译器内存使用
-XX:ReservedCodeCacheSize=256m

# 优化字符串去重
-XX:+UseStringDeduplication

# 减少线程栈大小
-Xss256k

# 启用并行类加载
-XX:+ParallelRefProcEnabled

# 优化 DirectByteBuffer
-XX:MaxDirectMemorySize=256m

# 禁用部分可视化效果以节省内存
-Dide.mac.screen.menu=false
-Dapple.awt.UIElement=true

# 优化字体渲染
-Dawt.useSystemAAFontSettings=lcd
-Dswing.aatext=true

# 减少 Swing 线程数
-Dswing.threadpoolsize=4

# 优化文件监控
-Didea.io.buffer.size.kb=64

# 减少 IDEA 后台任务线程数
-Didea.max.content.load.files.limit=500

# 优化内存分析
-XX:+HeapDumpOnOutOfMemoryError
-XX:HeapDumpPath=./logs/heapdump.hprof

# 减少类加载器缓存
-XX:ClassUnloadingInterval=300

# 优化 JIT 编译
-XX:+TieredCompilation
-XX:TieredStopAtLevel=1
```

#### 步骤 4：应用更改
- 保存文件
- 重启 IntelliJ IDEA

## 额外优化建议

### 1. 项目特定优化
- 在 IDEA 设置中减少后台任务线程数
- 禁用不必要的插件
- 减少自动代码分析频率

### 2. 系统级优化
```batch
# 增加 JVM 堆外内存（如果需要）
set _JAVA_OPTIONS="-Xmx1024m -XX:MaxDirectMemorySize=512m"

# 或者创建快捷方式，添加 JVM 参数
```

### 3. 监控内存使用
- 使用 IDEA 内置的内存分析工具
- 监控 GC 日志
- 观察内存泄漏

## 验证优化效果
1. 重启 IDEA
2. 打开任务管理器
3. 观察 IDEA 内存使用情况
4. 检查是否有明显的性能提升

如果问题仍然存在，请考虑：
- 增加系统物理内存
- 使用 64 位 JDK
- 禁用某些内存密集型功能（如实时代码分析）