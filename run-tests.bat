@echo off
REM 炉批卷追溯系统 - 一键运行测试脚本
REM 需要 JDK 21+ 和 JAVA_HOME 环境变量指向 JDK 21+

echo ========================================
echo  炉批卷追溯系统 - 测试套件
echo ========================================

cd /d "%~dp0backend"

echo.
echo [1/3] 编译并运行后端单元测试...
if not "%JAVA_HOME%"=="" (
    echo 使用 JDK: %JAVA_HOME%
)
call mvn test
if %ERRORLEVEL% NEQ 0 (
    echo [失败] 测试未通过，请检查错误信息
    pause
    exit /b 1
)

echo.
echo [2/3] 生成测试报告...
echo 测试报告: target\site\surefire-report.html

echo.
echo [3/3] 构建项目 JAR...
call mvn package -DskipTests -q
if %ERRORLEVEL% EQU 0 (
    echo [成功] JAR 包: target\trace-system.jar
)

echo.
echo ========================================
echo  全部完成！
echo  测试报告: backend\target\site\surefire-report.html
echo  后端 JAR: backend\target\trace-system.jar
echo ========================================
pause
