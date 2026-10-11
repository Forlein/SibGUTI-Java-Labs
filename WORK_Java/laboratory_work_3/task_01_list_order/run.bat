@echo off
chcp 65001 >nul
cd /d "%~dp0.."
javac -encoding UTF-8 task_01_list_order\*.java
java -Dstdout.encoding=UTF-8 -cp . task_01_list_order.task_01_list_order
echo.
pause
