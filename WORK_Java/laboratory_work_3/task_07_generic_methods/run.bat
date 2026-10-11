@echo off
chcp 65001 >nul
cd /d "%~dp0.."
javac -encoding UTF-8 task_07_generic_methods\*.java
java -Dstdout.encoding=UTF-8 -cp . task_07_generic_methods.task_07_generic_methods
echo.
pause
