@echo off
chcp 65001 >nul
cd /d "%~dp0.."
javac -encoding UTF-8 task_13_inverted_index\*.java
java -Dstdout.encoding=UTF-8 -cp . task_13_inverted_index.task_13_inverted_index
echo.
pause
