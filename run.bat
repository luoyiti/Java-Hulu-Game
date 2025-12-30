@echo off
REM Windows batch script to build and run the game
REM Note: -XstartOnFirstThread is macOS specific and not needed on Windows

echo Cleaning previous build...
call mvn clean -DskipTests
if %errorlevel% neq 0 (
    echo Build cleaning failed!
    pause
    exit /b %errorlevel%
)

echo Building the project...
call mvn package -DskipTests
if %errorlevel% neq 0 (
    echo Build failed!
    pause
    exit /b %errorlevel%
)

echo Starting the game...
java --enable-native-access=ALL-UNNAMED -cp target\game-1.0-SNAPSHOT.jar com.gameengine.app.Game

pause
