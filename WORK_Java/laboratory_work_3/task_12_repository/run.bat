@echo off
chcp 65001 >nul
cd /d "%~dp0.."
javac -encoding UTF-8 task_12_repository\*.java
java -Dstdout.encoding=UTF-8 -cp . task_12_repository.task_12_repository
echo.
pause
