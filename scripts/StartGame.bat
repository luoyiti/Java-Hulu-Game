@echo off
chcp 65001 >nul
title Huluwa Game
cd /d "%~dp0"

REM Check if Java is installed
java -version >nul 2>&1
if errorlevel 1 (
    echo Error: Java is not installed!
    echo Please install Java 11 or higher first.
    echo Download: https://adoptium.net/
    pause
    exit /b 1
)

REM Run game
echo Starting Huluwa Game...
java --enable-native-access=ALL-UNNAMED -jar lib\HuluwaGame.jar

if errorlevel 1 (
    echo.
    echo Game error occurred. Please check the error message.
    pause
)
