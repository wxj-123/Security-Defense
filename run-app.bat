@echo off
setlocal enabledelayedexpansion

echo ========================================
echo   Security Defense - 后端服务启动
echo ========================================
echo.

cd /d %~dp0

REM 设置 Maven 本地仓库路径
set M2_REPO=%USERPROFILE%\.m2\repository

REM 构建完整的类路径
set CLASSPATH=target\classes
set CLASSPATH=%CLASSPATH%;%M2_REPO%\org\springframework\boot\spring-boot-starter-web\2.7.18\spring-boot-starter-web-2.7.18.jar
set CLASSPATH=%CLASSPATH%;%M2_REPO%\org\springframework\boot\spring-boot-starter\2.7.18\spring-boot-starter-2.7.18.jar
set CLASSPATH=%CLASSPATH%;%M2_REPO%\org\springframework\boot\spring-boot\2.7.18\spring-boot-2.7.18.jar
set CLASSPATH=%CLASSPATH%;%M2_REPO%\org\springframework\boot\spring-boot-autoconfigure\2.7.18\spring-boot-autoconfigure-2.7.18.jar
set CLASSPATH=%CLASSPATH%;%M2_REPO%\org\springframework\spring-core\5.3.31\spring-core-5.3.31.jar
set CLASSPATH=%CLASSPATH%;%M2_REPO%\org\springframework\spring-context\5.3.31\spring-context-5.3.31.jar
set CLASSPATH=%CLASSPATH%;%M2_REPO%\org\springframework\spring-web\5.3.31\spring-web-5.3.31.jar
set CLASSPATH=%CLASSPATH%;%M2_REPO%\org\springframework\spring-webmvc\5.3.31\spring-webmvc-5.3.31.jar
set CLASSPATH=%CLASSPATH%;%M2_REPO%\org\springframework\spring-beans\5.3.31\spring-beans-5.3.31.jar
set CLASSPATH=%CLASSPATH%;%M2_REPO%\org\springframework\spring-aop\5.3.31\spring-aop-5.3.31.jar
set CLASSPATH=%CLASSPATH%;%M2_REPO%\org\springframework\spring-expression\5.3.31\spring-expression-5.3.31.jar
set CLASSPATH=%CLASSPATH%;%M2_REPO%\org\springframework\spring-jdbc\5.3.31\spring-jdbc-5.3.31.jar
set CLASSPATH=%CLASSPATH%;%M2_REPO%\com\mysql\mysql-connector-j\8.0.33\mysql-connector-j-8.0.33.jar
set CLASSPATH=%CLASSPATH%;%M2_REPO%\com\alibaba\druid-spring-boot-starter\1.2.20\druid-spring-boot-starter-1.2.20.jar
set CLASSPATH=%CLASSPATH%;%M2_REPO%\com\alibaba\druid\1.2.20\druid-1.2.20.jar
set CLASSPATH=%CLASSPATH%;%M2_REPO%\com\baomidou\mybatis-plus-boot-starter\3.5.5\mybatis-plus-boot-starter-3.5.5.jar
set CLASSPATH=%CLASSPATH%;%M2_REPO%\com\baomidou\mybatis-plus\3.5.5\mybatis-plus-3.5.5.jar
set CLASSPATH=%CLASSPATH%;%M2_REPO%\com\baomidou\mybatis-plus-core\3.5.5\mybatis-plus-core-3.5.5.jar
set CLASSPATH=%CLASSPATH%;%M2_REPO%\com\baomidou\mybatis-plus-extension\3.5.5\mybatis-plus-extension-3.5.5.jar
set CLASSPATH=%CLASSPATH%;%M2_REPO%\org\mybatis\mybatis-spring\2.1.2\mybatis-spring-2.1.2.jar
set CLASSPATH=%CLASSPATH%;%M2_REPO%\org\mybatis\mybatis\3.5.15\mybatis-3.5.15.jar
set CLASSPATH=%CLASSPATH%;%M2_REPO%\org\springframework\data\spring-data-redis\2.7.18\spring-data-redis-2.7.18.jar
set CLASSPATH=%CLASSPATH%;%M2_REPO%\io\lettuce\lettuce-core\6.1.10.RELEASE\lettuce-core-6.1.10.RELEASE.jar
set CLASSPATH=%CLASSPATH%;%M2_REPO%\org\springframework\boot\spring-boot-starter-data-redis\2.7.18\spring-boot-starter-data-redis-2.7.18.jar
set CLASSPATH=%CLASSPATH%;%M2_REPO%\org\springframework\security\spring-security-core\5.7.11\spring-security-core-5.7.11.jar
set CLASSPATH=%CLASSPATH%;%M2_REPO%\org\springframework\security\spring-security-config\5.7.11\spring-security-config-5.7.11.jar
set CLASSPATH=%CLASSPATH%;%M2_REPO%\org\springframework\security\spring-security-web\5.7.11\spring-security-web-5.7.11.jar
set CLASSPATH=%CLASSPATH%;%M2_REPO%\io\jsonwebtoken\jjwt-api\0.12.3\jjwt-api-0.12.3.jar
set CLASSPATH=%CLASSPATH%;%M2_REPO%\io\jsonwebtoken\jjwt-impl\0.12.3\jjwt-impl-0.12.3.jar
set CLASSPATH=%CLASSPATH%;%M2_REPO%\io\jsonwebtoken\jjwt-jackson\0.12.3\jjwt-jackson-0.12.3.jar
set CLASSPATH=%CLASSPATH%;%M2_REPO%\cn\hutool\hutool-all\5.8.23\hutool-all-5.8.23.jar
set CLASSPATH=%CLASSPATH%;%M2_REPO%\org\projectlombok\lombok\1.18.30\lombok-1.18.30.jar
set CLASSPATH=%CLASSPATH%;%M2_REPO%\ch\qos\logback\logback-classic\1.2.12\logback-classic-1.2.12.jar
set CLASSPATH=%CLASSPATH%;%M2_REPO%\ch\qos\logback\logback-core\1.2.12\logback-core-1.2.12.jar
set CLASSPATH=%CLASSPATH%;%M2_REPO%\org\slf4j\slf4j-api\1.7.36\slf4j-api-1.7.36.jar
set CLASSPATH=%CLASSPATH%;%M2_REPO%\com\fasterxml\jackson\core\jackson-databind\2.13.5\jackson-databind-2.13.5.jar
set CLASSPATH=%CLASSPATH%;%M2_REPO%\com\fasterxml\jackson\core\jackson-core\2.13.5\jackson-core-2.13.5.jar
set CLASSPATH=%CLASSPATH%;%M2_REPO%\com\fasterxml\jackson\core\jackson-annotations\2.13.5\jackson-annotations-2.13.5.jar
set CLASSPATH=%CLASSPATH%;%M2_REPO%\org\apache\tomcat\embed\tomcat-embed-core\9.0.83\tomcat-embed-core-9.0.83.jar
set CLASSPATH=%CLASSPATH%;%M2_REPO%\jakarta\servlet\jakarta.servlet-api\4.0.4\jakarta.servlet-api-4.0.4.jar

echo 正在启动应用...
echo.
echo 后端地址: http://localhost:8085
echo 前端地址: http://localhost:3000
echo.
echo 按 Ctrl+C 停止服务
echo.

java -cp "%CLASSPATH%" com.example.securitydefense.SecurityDefenseApplication

pause
