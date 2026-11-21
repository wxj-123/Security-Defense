@echo off
chcp 65001 >nul
echo ========================================
echo   Security-Defense 数据库初始化脚本
echo ========================================
echo.

set /p mysql_user="请输入MySQL用户名 [默认: root]: "
if "%mysql_user%"=="" set mysql_user=root

set /p mysql_password="请输入MySQL密码: "

echo.
echo 正在连接MySQL并初始化数据库...
echo.

rem 创建数据库
echo [1/4] 创建数据库...
mysql -u %mysql_user% -p%mysql_password% -e "CREATE DATABASE IF NOT EXISTS security_defense CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;"
if %errorlevel% neq 0 (
    echo 错误: 数据库创建失败！请检查MySQL连接和权限。
    pause
    exit /b 1
)
echo ✓ 数据库创建成功

rem 执行建表脚本
echo [2/4] 创建数据库表结构...
mysql -u %mysql_user% -p%mysql_password% security_defense < src\main\resources\sql\schema.sql
if %errorlevel% neq 0 (
    echo 错误: 建表失败！请检查MySQL连接和权限。
    pause
    exit /b 1
)
echo ✓ 数据库表创建成功

rem 执行系统管理模块建表脚本
echo [3/4] 创建系统管理模块表结构...
mysql -u %mysql_user% -p%mysql_password% security_defense < src\main\resources\sql\system-manage.sql
if %errorlevel% neq 0 (
    echo 错误: 系统管理模块建表失败！
    pause
    exit /b 1
)
echo ✓ 系统管理模块表创建成功

rem 执行初始数据脚本
echo [4/4] 导入初始数据...
mysql -u %mysql_user% -p%mysql_password% security_defense < src\main\resources\sql\init-data.sql
if %errorlevel% neq 0 (
    echo 错误: 数据导入失败！
    pause
    exit /b 1
)
echo ✓ 初始数据导入成功

echo.
echo ========================================
echo   数据库初始化完成！
echo ========================================
echo.
echo 数据库名: security_defense
echo 默认管理员账户:
echo   用户名: admin
echo   密码: admin123
echo.
echo 您现在可以启动应用程序了。
echo.
pause