@echo off
chcp 65001 >nul
cd /d "%~dp0.."
javac -encoding UTF-8 task_06_result_type\*.java
java -Dstdout.encoding=UTF-8 -cp . task_06_result_type.task_06_result_type
echo.
pause
