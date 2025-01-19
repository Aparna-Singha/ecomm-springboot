@echo off
rem Maven Wrapper script for Windows

rem Find the project base dir
set SCRIPT_DIR=%~dp0
cd /d "%SCRIPT_DIR%"

rem Check if Maven is in PATH
where mvn >nul 2>&1
if %errorlevel% equ 0 (
    mvn %*
) else (
    echo Maven is not installed. Please install Maven or use the Maven Wrapper JAR.
    echo Download Maven from: https://maven.apache.org/download.cgi
    exit /b 1
)
