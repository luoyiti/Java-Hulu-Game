package com.gameengine.app;

import com.gameengine.core.GameEngine;
import com.gameengine.graphics.IRenderer;
import com.gameengine.input.InputManager;
import com.gameengine.math.Vector2;
import com.gameengine.scene.Scene;

/**
 * 主菜单场景
 * 提供开始游戏、回放、退出等选项
 */
public class MenuScene extends Scene {
    private enum MenuOption {
        START_GAME,
        ONLINE_GAME,
        REPLAY,
        EXIT
    }
    
    private static class MenuButton {
        float x, y, width, height;
        MenuOption option;
        
        MenuButton(float x, float y, float width, float height, MenuOption option) {
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
            this.option = option;
        }
        
        boolean contains(float mx, float my) {
            return mx >= x && mx <= x + width && my >= y && my <= y + height;
        }
    }
    
    private GameEngine engine;
    private IRenderer renderer;
    private InputManager inputManager;
    private MenuOption[] options = {MenuOption.START_GAME, MenuOption.ONLINE_GAME, MenuOption.REPLAY, MenuOption.EXIT};
    private int selectedIndex = 0;
    private boolean showReplayInfo = false;
    private float replayInfoTimer = 0.0f;
    private int debugFrames = 0;
    private MenuButton[] menuButtons = new MenuButton[4];
    
    public MenuScene(GameEngine engine, String name) {
        super(name);
        this.engine = engine;
    }
    
    @Override
    public void initialize() {
        super.initialize();
        this.renderer = engine.getRenderer();
        this.inputManager = engine.getInputManager();
    }
    
    @Override
    public void update(float deltaTime) {
        super.update(deltaTime);
        
        if (showReplayInfo) {
            replayInfoTimer += deltaTime;
            if (replayInfoTimer > 2.0f) {
                showReplayInfo = false;
                replayInfoTimer = 0.0f;
            }
        }
        
        // 鼠标悬停检测
        Vector2 mousePos = inputManager.getMousePosition();
        for (int i = 0; i < menuButtons.length; i++) {
            if (menuButtons[i] != null && menuButtons[i].contains(mousePos.x, mousePos.y)) {
                selectedIndex = i;
                break;
            }
        }
        
        // 鼠标左键点击
        if (inputManager.isMouseButtonJustPressed(0)) { // GLFW_MOUSE_BUTTON_LEFT = 0（鼠标左键）
            for (int i = 0; i < menuButtons.length; i++) {
                if (menuButtons[i] != null && menuButtons[i].contains(mousePos.x, mousePos.y)) {
                    selectedIndex = i;
                    handleSelection();
                    break;
                }
            }
        }
        
        // 向上箭头
        if (inputManager.isKeyJustPressed(265)) { // GLFW_KEY_UP（上键）
            selectedIndex = (selectedIndex - 1 + options.length) % options.length;
        }
        
        // 向下箭头
        if (inputManager.isKeyJustPressed(264)) { // GLFW_KEY_DOWN（下键）
            selectedIndex = (selectedIndex + 1) % options.length;
        }
        
        // 回车键确认
        if (inputManager.isKeyJustPressed(257)) { // GLFW_KEY_ENTER（回车键）
            handleSelection();
        }
        
        // ESC键退出
        if (inputManager.isKeyPressed(256)) { // GLFW_KEY_ESCAPE（Esc 键）
            destroyScene();
        }
    }
    
    private void handleSelection() {
        MenuOption selected = options[selectedIndex];
        
        switch (selected) {
            case START_GAME:
                switchToGameScene();
                break;
            case ONLINE_GAME:
                switchToOnlineScene();
                break;
            case REPLAY:
                switchToReplayScene();
                break;
            case EXIT:
                engine.stop();
                break;
        }
    }
    
    private void switchToGameScene() {
        destroyScene();
        Scene gameScene = new GameScene("Hulu Game", engine);
        engine.setScene(gameScene);
        
    }
    
    private void switchToOnlineScene() {
        destroyScene();
        Scene onlineMenuScene = new OnlineMenuScene("Online Menu Game", engine);
        engine.setScene(onlineMenuScene);
        
    }
    
    private void switchToReplayScene() {
        destroyScene();
        Scene replayMenuScene = new ReplayScene(engine, null);
        engine.setScene(replayMenuScene);
        showReplayInfo = true;
        
    }

    /**
     * 从引擎中移除并清理当前菜单场景实例。
     * 如果当前正在作为引擎的活动场景，会将引擎的场景置为 null 并清理本场景资源。
     */
    public void destroyScene() {
        // 清理场景内部对象
        this.clear();
    }

    
    
    @Override
    public void render() {
        if (renderer == null) return;
        
        int width = renderer.getWidth();
        int height = renderer.getHeight();
        
        if (debugFrames < 5) {
            debugFrames++;
        }
        
        // 绘制背景
        // renderer.drawRect(0, 0, width, height, 0.25f, 0.25f, 0.35f, 1.0f);
        renderer.drawImage(
            "resources/picture/menu_background.png",
            0, 0,
            800, 600,
            1.0f
        );
        
        super.render();
        
        renderMainMenu();
    }
    
    private void renderMainMenu() {
        if (renderer == null) return;
        
        int height = renderer.getHeight();
        
        float centerY = height / 2.0f;
        float leftMargin = 80.0f; // 左侧边距
        
        // 绘制标题
        String title = "葫芦娃大战妖怪";
        float titleWidth = title.length() * 20.0f;
        float titleX = leftMargin;
        float titleY = 100.0f;
        
        renderer.drawRect(leftMargin - 20, titleY - 35, titleWidth + 40, 70, 0.35f, 0.3f, 0.45f, 0.9f);
        renderer.drawText(title, titleX, titleY, 20, 1.0f, 0.9f, 0.6f, 1.0f);
        
        // 绘制菜单选项（左对齐）
        float buttonWidth = 200.0f;
        float buttonHeight = 50.0f;
        float startY = centerY - 60.0f;
        float spacing = 70.0f;
        
        for (int i = 0; i < options.length; i++) {
            String text = "";
            if (options[i] == MenuOption.START_GAME) {
                text = "▶ 开始游戏";
            } else if (options[i] == MenuOption.ONLINE_GAME) {
                text = "▶ 网络对战";
            } else if (options[i] == MenuOption.REPLAY) {
                text = "▶ 观看回放";
            } else if (options[i] == MenuOption.EXIT) {
                text = "▶ 退出";
            }
            
            float buttonX = leftMargin - 20;
            float buttonY = startY + i * spacing;
            float textX = leftMargin;
            float textY = buttonY + 15;
            
            // 更新按钮区域
            menuButtons[i] = new MenuButton(buttonX, buttonY, buttonWidth, buttonHeight, options[i]);
            
            float r, g, b;
            
            if (i == selectedIndex) {
                r = 1.0f;
                g = 1.0f;
                b = 0.5f;
                // 选中状态：更亮的背景 + 左侧高亮条
                renderer.drawRect(buttonX, buttonY, buttonWidth, buttonHeight, 0.5f, 0.45f, 0.25f, 0.95f);
                renderer.drawRect(buttonX, buttonY, 5, buttonHeight, 1.0f, 0.8f, 0.3f, 1.0f);
            } else {
                r = 0.85f;
                g = 0.85f;
                b = 0.85f;
                renderer.drawRect(buttonX, buttonY, buttonWidth, buttonHeight, 0.25f, 0.25f, 0.35f, 0.7f);
            }
            
            renderer.drawText(text, textX, textY, 18, r, g, b, 1.0f);
        }
        
        // 绘制操作提示（左下角）
        String hint1 = "↑↓ 选择  Enter 确认";
        renderer.drawText(hint1, leftMargin - 20, height - 80, 16, 0.55f, 0.55f, 0.65f, 1.0f);
        
        String hint2 = "ESC 退出游戏";
        renderer.drawText(hint2, leftMargin - 20, height - 50, 16, 0.55f, 0.55f, 0.65f, 1.0f);

        if (showReplayInfo) {
            String info = "暂无可用的回放文件";
            renderer.drawText(info, leftMargin - 20, height - 110, 16, 0.9f, 0.8f, 0.2f, 1.0f);
        }
    }
}
