@echo off
cd /d "%~dp0"
title DeutschCraft Debug Launcher

:: Run gradle with console visible
call .\gradlew.bat :desktopApp:run --info

if %errorlevel% neq 0 (
    echo.
    echo =========================================
    echo    BUILD/Run FAILED ^(exit code: %errorlevel%^)
    echo =========================================
    pause
)
