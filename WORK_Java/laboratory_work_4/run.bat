@echo off
chcp 65001 >nul
cd /d "%~dp0"
if not exist out mkdir out
javac -encoding UTF-8 -d out common\*.java tasks\*.java Main.java
if errorlevel 1 exit /b 1
if "%~1"=="" (
    java -Dstdout.encoding=UTF-8 -cp out Main
) else (
    java -Dstdout.encoding=UTF-8 -cp out tasks.%~1
)
