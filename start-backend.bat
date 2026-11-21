@echo off
chcp 65001 >nul
echo ========================================
echo   启动 Security-Defense 后端服务
echo ========================================
echo.

cd /d %~dp0

echo 正在编译项目...
echo.
call mvnw.cmd package -DskipTests

if %ERRORLEVEL% NEQ 0 (
    echo 编译失败！
    pause
    exit /b 1
)

echo.
echo 正在启动应用...
echo 请打开浏览器访问: http://localhost:3000
echo 后端API地址: https://localhost:8443
echo.
echo 按 Ctrl+C 停止服务
echo.

java -jar target/Security-Defense-0.0.1-SNAPSHOT.jar

pause
