#!/bin/bash
# Huluwa Game - Mac Double-click Launcher
# Double-click this file in Finder to run the game

# Get script directory
SCRIPT_DIR="$( cd "$( dirname "$0" )" && pwd )"
cd "$SCRIPT_DIR"

# Check if Java is installed
if ! command -v java &> /dev/null; then
    osascript -e 'display alert "Java Not Found" message "Please install Java 11 or higher first.\n\nDownload: https://adoptium.net/\n\nOr use Homebrew: brew install openjdk@11" as critical'
    exit 1
fi

echo "Starting Huluwa Game..."

# Mac requires -XstartOnFirstThread for LWJGL graphics
java -XstartOnFirstThread --enable-native-access=ALL-UNNAMED -jar lib/HuluwaGame.jar

if [ $? -ne 0 ]; then
    osascript -e 'display alert "Game Error" message "An error occurred. Please check the console output for details." as critical'
fi
