@echo off
echo 正在恢复MySQL数据库配置...

REM 1. 创建数据库
echo 创建数据库 security_defense...
mysql -u root -p -e "CREATE DATABASE IF NOT EXISTS security_defense DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;"

if %errorlevel% neq 0 (
    echo 数据库创建失败，请检查MySQL服务是否启动及密码是否正确
    pause
    exit /b 1
)

REM 2. 导入表结构
echo 导入表结构...
mysql -u root -p security_defense < "src/main/resources/sql/schema.sql"

if %errorlevel% neq 0 (
    echo 表结构导入失败
    pause
    exit /b 1
)

REM 3. 导入初始数据
echo 导入初始数据...
mysql -u root -p security_defense < "src/main/resources/sql/init-data.sql"

if %errorlevel% neq 0 (
    echo 初始数据导入失败
    pause
    exit /b 1
)

echo MySQL数据库配置恢复完成！
echo 默认用户名密码：admin/admin123
pause