# 葫芦娃大战妖怪 - 游戏发布包

## 系统要求

- **Java**: Java 11 或更高版本
- **操作系统**: Windows 10/11, macOS 10.14+, Linux

## Java 下载地址

如果你的电脑没有安装 Java，可以从以下地址下载:

- **官方下载**: https://adoptium.net/
- **macOS (Homebrew)**: `brew install openjdk@11`

## 如何运行游戏

### Windows 用户
1. 解压 `HuluwaGame-dist.zip`
2. 双击 `StartGame.bat`

### macOS 用户
1. 解压 `HuluwaGame-dist.zip`
2. 双击 `HuluwaGame.command`
   - 如果提示"无法打开"，请右键点击文件，选择"打开"
   - 或者在终端中运行: `./StartGame.sh`

### Linux 用户
1. 解压 `HuluwaGame-dist.zip`
2. 在终端中运行:
   ```bash
   cd HuluwaGame
   chmod +x StartGame.sh
   ./StartGame.sh
   ```

## 文件结构

```
HuluwaGame/
├── lib/
│   └── HuluwaGame.jar    # 游戏主程序
├── resources/
│   └── picture/          # 游戏图片资源
├── recordings/           # 游戏录像保存目录
├── StartGame.bat         # Windows 启动脚本
├── StartGame.sh          # Linux/macOS 启动脚本
├── HuluwaGame.command    # macOS 双击运行脚本
└── README.md             # 说明文档
```

## 常见问题

### 1. 游戏无法启动
- 确保已安装 Java 11 或更高版本
- 在命令行运行 `java -version` 检查 Java 版本

### 2. macOS 提示"应用来自身份不明的开发者"
- 右键点击 `HuluwaGame.command`，选择"打开"
- 在弹出的对话框中点击"打开"

### 3. 图片无法显示
- 确保 `resources/picture/` 目录中包含所有图片文件
- 不要移动或删除任何资源文件

## 游戏操作

- **W/A/S/D**: 移动角色
- **J**: 普通攻击
- **K**: 技能攻击
- **ESC**: 暂停/菜单

祝你游戏愉快！
