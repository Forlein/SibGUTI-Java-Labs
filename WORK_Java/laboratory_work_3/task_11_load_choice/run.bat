@echo off
chcp 65001 >nul
cd /d "%~dp0.."
javac -encoding UTF-8 task_11_load_choice\*.java
java -Dstdout.encoding=UTF-8 -cp . task_11_load_choice.task_11_load_choice
echo.
pause
