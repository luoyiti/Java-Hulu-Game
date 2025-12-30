#!/bin/bash
# 打包游戏脚本
# 运行此脚本将生成可分发的游戏包

SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"

echo "================================================"
echo "      葫芦娃大战妖怪 - 游戏打包脚本"
echo "================================================"
echo ""

# 检查是否安装了 Maven
if ! command -v mvn &> /dev/null; then
    echo "错误: 未找到 Maven，请先安装 Maven"
    exit 1
fi

echo "正在编译和打包游戏..."
cd "$SCRIPT_DIR"
mvn clean package -DskipTests

if [ $? -eq 0 ]; then
    echo ""
    echo "================================================"
    echo "打包成功！"
    echo "================================================"
    echo ""
    echo "分发包位置: target/HuluwaGame-dist.zip"
    echo ""
    echo "使用说明:"
    echo "1. 将 HuluwaGame-dist.zip 发送给用户"
    echo "2. 用户解压后运行对应系统的启动脚本"
    echo "   - Windows: StartGame.bat"
    echo "   - macOS: HuluwaGame.command (双击运行)"
    echo "   - Linux: StartGame.sh"
    echo ""
    ls -lh target/HuluwaGame-dist.zip
else
    echo ""
    echo "打包失败，请检查上方错误信息"
    exit 1
fi
