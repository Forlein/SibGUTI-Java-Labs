@echo off
chcp 65001 >nul
cd /d "%~dp0.."
javac -encoding UTF-8 task_04_waitlist_queue\*.java
java -Dstdout.encoding=UTF-8 -cp . task_04_waitlist_queue.task_04_waitlist_queue
echo.
pause
