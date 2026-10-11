@echo off
chcp 65001 >nul
cd /d "%~dp0.."
javac -encoding UTF-8 task_10_key_contract\*.java
java -Dstdout.encoding=UTF-8 -cp . task_10_key_contract.task_10_key_contract
echo.
pause
