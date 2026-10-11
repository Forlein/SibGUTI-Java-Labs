@echo off
chcp 65001 >nul
cd /d "%~dp0.."
javac -encoding UTF-8 task_14_lru_cache\*.java
java -Dstdout.encoding=UTF-8 -cp . task_14_lru_cache.task_14_lru_cache
echo.
pause
