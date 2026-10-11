@echo off
chcp 65001 >nul
cd /d "%~dp0.."
javac -encoding UTF-8 task_02_set_uniqueness\*.java
java -Dstdout.encoding=UTF-8 -cp . task_02_set_uniqueness.task_02_set_uniqueness
echo.
pause
