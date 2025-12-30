package com.gameengine.app;

import com.gameengine.core.GameEngine;
import com.gameengine.graphics.IRenderer;
import com.gameengine.input.InputManager;
import com.gameengine.math.Vector2;
import com.gameengine.scene.Scene;

/**
 * 本地多人游戏的菜单选择界面，在此选择游戏对战人数
 */
public class OfflineMenuScene extends Scene {

    private IRenderer renderer;
    private GameEngine engine;
    private InputManager inputManager;

    // 玩家人数选项
    private static final int[] PLAYER_OPTIONS = { 2, 3, 4 };
    private int selectedIndex = 0;
    private int selectedPlayerCount = 2;

    // 按钮区域
    private static class OptionButton {
        float x, y, size;
        int playerCount;

        OptionButton(float x, float y, float size, int playerCount) {
            this.x = x;
            this.y = y;
            this.size = size;
            this.playerCount = playerCount;
        }

        boolean contains(float mx, float my) {
            return mx >= x - size / 2 && mx <= x + size / 2 && my >= y - size / 2 && my <= y + size / 2;
        }
    }

    private OptionButton[] buttons = new OptionButton[3];

    public OfflineMenuScene(String name, GameEngine engine) {
        super(name);
        this.engine = engine;
    }

    @Override
    public void initialize() {
        this.renderer = engine.getRenderer();
        this.inputManager = engine.getInputManager();

        // 初始化按钮位置
        float centerY = renderer.getHeight() / 2.0f + 70f;
        float spacing = 180f;
        float startX = renderer.getWidth() / 2.0f - spacing;
        float buttonSize = 100f;

        for (int i = 0; i < PLAYER_OPTIONS.length; i++) {
            buttons[i] = new OptionButton(startX + i * spacing, centerY, buttonSize, PLAYER_OPTIONS[i]);
        }
    }

    @Override
    public void render() {
        int width = renderer.getWidth();
        int height = renderer.getHeight();

        // 绘制浅灰白背景
        renderer.drawRect(0, 0, width, height, 0.95f, 0.95f, 0.95f, 1.0f);

        // 绘制标题 "请选择玩家人数"
        String title = "本地多人 - 请选择玩家人数";
        renderer.drawText(title, 250, 196, 48, 0.15f, 0.15f, 0.2f, 1.0f);

        // 绘制三个选项按钮
        for (int i = 0; i < buttons.length; i++) {
            renderOptionButton(i);
        }

        // 绘制底部提示
        String hint = "← → 选择  Enter 确认  ESC 返回";
        float hintWidth = hint.length() * 14f;
        renderer.drawText(hint, width / 2.0f - hintWidth / 2.0f, height - 60, 14, 0.5f, 0.5f, 0.5f, 1.0f);

        super.render();
    }

    private void renderOptionButton(int index) {
        OptionButton btn = buttons[index];
        boolean isSelected = (index == selectedIndex);
        float size = btn.size;
        float x = btn.x;
        float y = btn.y;

        // 根据不同数字绘制不同形状和颜色
        switch (btn.playerCount) {
            case 2:
                renderer.drawImage(
                        "resources/picture/2.png",
                        165, 313,
                        100, 100,
                        1.0f);
                break;
            case 3:
                renderer.drawImage(
                        "resources/picture/3.png",
                        350, 313,
                        100, 100,
                        1.0f);
                break;
            case 4:
                renderer.drawImage(
                        "resources/picture/4.png",
                        535, 313,
                        100, 100,
                        1.0f);
                break;
        }

        // 绘制选中高亮效果
        if (isSelected) {
            // 外发光效果
            renderer.drawRect(x - size / 2 - 8, y - size / 2 - 8, size + 16, size + 16,
                    0.3f, 0.6f, 1.0f, 0.3f);
        }
    }

    @Override
    public void update(float deltaTime) {
        // ESC 返回主菜单
        if (inputManager.isKeyJustPressed(256)) { // GLFW_KEY_ESCAPE
            MenuScene menuScene = new MenuScene(engine, "MainMenu");
            engine.setScene(menuScene);
            return;
        }

        // 左箭头键
        if (inputManager.isKeyJustPressed(263)) { // GLFW_KEY_LEFT
            selectedIndex = (selectedIndex - 1 + PLAYER_OPTIONS.length) % PLAYER_OPTIONS.length;
        }

        // 右箭头键
        if (inputManager.isKeyJustPressed(262)) { // GLFW_KEY_RIGHT
            selectedIndex = (selectedIndex + 1) % PLAYER_OPTIONS.length;
        }

        // 回车键确认
        if (inputManager.isKeyJustPressed(257)) { // GLFW_KEY_ENTER
            handleSelection();
        }

        // 鼠标悬停检测
        Vector2 mousePos = inputManager.getMousePosition();
        for (int i = 0; i < buttons.length; i++) {
            if (buttons[i] != null && buttons[i].contains(mousePos.x, mousePos.y)) {
                selectedIndex = i;
                break;
            }
        }

        // 鼠标左键点击
        if (inputManager.isMouseButtonJustPressed(0)) { // GLFW_MOUSE_BUTTON_LEFT
            for (int i = 0; i < buttons.length; i++) {
                if (buttons[i] != null && buttons[i].contains(mousePos.x, mousePos.y)) {
                    selectedIndex = i;
                    handleSelection();
                    break;
                }
            }
        }
    }

    private void handleSelection() {
        selectedPlayerCount = PLAYER_OPTIONS[selectedIndex];
        destroyScene();
        OfflineGameScene offlineGameScene = new OfflineGameScene("葫芦娃本地多人游戏", engine, selectedPlayerCount);
        engine.setScene(offlineGameScene);
        System.out.println("选择了 " + selectedPlayerCount + " 名玩家（本地多人）");
    }

    private void destroyScene() {
        this.clear();
    }

    public int getSelectedPlayerCount() {
        return selectedPlayerCount;
    }
}