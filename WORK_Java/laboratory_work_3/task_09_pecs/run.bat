@echo off
chcp 65001 >nul
cd /d "%~dp0.."
javac -encoding UTF-8 task_09_pecs\*.java
java -Dstdout.encoding=UTF-8 -cp . task_09_pecs.task_09_pecs
echo.
pause
