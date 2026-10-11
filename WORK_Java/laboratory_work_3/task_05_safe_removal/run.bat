@echo off
chcp 65001 >nul
cd /d "%~dp0.."
javac -encoding UTF-8 task_05_safe_removal\*.java
java -Dstdout.encoding=UTF-8 -cp . task_05_safe_removal.task_05_safe_removal
echo.
pause
