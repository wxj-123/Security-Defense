@echo off
chcp 65001 >nul

echo ========================================
echo   IntelliJ IDEA 内存优化配置
echo ========================================
echo.

set IDEA_PATH=
set VM_OPTIONS_PATH=

:: 查找 IntelliJ IDEA 安装路径
for /r "C:\Program Files\JetBrains" %%i in (idea64.exe) do (
    set "IDEA_PATH=%%~dpi"
    goto found
)
:found

if "%IDEA_PATH%"=="" (
    echo 错误：未找到 IntelliJ IDEA 安装路径
    pause
    exit /b 1
)

echo 找到 IntelliJ IDEA: %IDEA_PATH%
echo.

:: 查找 JVM 配置文件
set "VM_OPTIONS_PATH=%IDEA_PATH%bin\idea64.exe.vmoptions"

if not exist "%VM_OPTIONS_PATH%" (
    echo 错误：未找到 JVM 配置文件: %VM_OPTIONS_PATH%
    pause
    exit /b 1
)

echo 找到 JVM 配置文件: %VM_OPTIONS_PATH%
echo.

:: 备份原始配置
set "BACKUP_PATH=%VM_OPTIONS_PATH%.bak"
if exist "%BACKUP_PATH%" (
    echo 备份文件已存在，跳过备份
) else (
    echo 备份原始配置文件...
    copy "%VM_OPTIONS_PATH%" "%BACKUP_PATH%"
)

echo 应用优化配置...
copy "idea-vmoptions-optimized.txt" "%VM_OPTIONS_PATH%"

echo.
echo 优化完成！
echo.
echo 请重启 IntelliJ IDEA 以应用更改。
echo.
echo 原始配置已备份到: %BACKUP_PATH%
echo.

pause