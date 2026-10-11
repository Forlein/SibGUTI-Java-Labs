@echo off
chcp 65001 >nul
cd /d "%~dp0.."
javac -encoding UTF-8 task_08_comparable_max\*.java
java -Dstdout.encoding=UTF-8 -cp . task_08_comparable_max.task_08_comparable_max
echo.
pause
