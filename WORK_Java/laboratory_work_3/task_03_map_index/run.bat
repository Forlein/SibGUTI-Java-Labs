@echo off
chcp 65001 >nul
cd /d "%~dp0.."
javac -encoding UTF-8 task_03_map_index\*.java
java -Dstdout.encoding=UTF-8 -cp . task_03_map_index.task_03_map_index
echo.
pause
