@echo off
chcp 65001 >nul
echo ========================================
echo   更新数据库密码脚本
echo ========================================
echo.
echo 此脚本将更新所有用户密码为: admin123
echo.

set /p mysql_user="请输入MySQL用户名 [默认: root]: "
if "%mysql_user%"=="" set mysql_user=root

set /p mysql_password="请输入MySQL密码: "

echo.
echo 正在更新密码...
mysql -u %mysql_user% -p%mysql_password% security_defense < src\main\resources\sql\update-passwords.sql

if %errorlevel% neq 0 (
    echo 错误: 密码更新失败！
    pause
    exit /b 1
)

echo.
echo ✓ 密码更新成功！
echo.
echo 所有用户密码已更新为: admin123
echo.
pause
