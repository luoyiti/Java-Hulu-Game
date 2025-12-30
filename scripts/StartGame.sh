#!/bin/bash
# Huluwa Game - Mac Launcher

# Get script directory
SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
cd "$SCRIPT_DIR"

# Check if Java is installed
if ! command -v java &> /dev/null; then
    echo "Error: Java is not installed!"
    echo "Please install Java 11 or higher first."
    echo "Download: https://adoptium.net/"
    echo ""
    echo "Or install via Homebrew: brew install openjdk@11"
    read -p "Press Enter to exit..."
    exit 1
fi

echo "Starting Huluwa Game..."

# Mac requires -XstartOnFirstThread for LWJGL graphics
java -XstartOnFirstThread --enable-native-access=ALL-UNNAMED -jar lib/HuluwaGame.jar

if [ $? -ne 0 ]; then
    echo ""
    echo "Game error occurred. Please check the error message."
    read -p "Press Enter to exit..."
fi
