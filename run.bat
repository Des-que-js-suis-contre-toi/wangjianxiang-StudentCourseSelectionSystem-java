@echo off
cd /d "%~dp0"

if not exist out mkdir out
dir /s /b src\*.java > out\sources.txt
javac -encoding UTF-8 -d out @out\sources.txt
if errorlevel 1 goto compile_failed

java -cp out course.Main
goto end

:compile_failed
echo compile failed, check errors above

:end