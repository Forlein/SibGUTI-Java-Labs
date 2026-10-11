@echo off
chcp 65001 >nul
cd /d "%~dp0.."
javac -encoding UTF-8 task_15_prerequisites\*.java
java -Dstdout.encoding=UTF-8 -cp . task_15_prerequisites.task_15_prerequisites
echo.
pause
