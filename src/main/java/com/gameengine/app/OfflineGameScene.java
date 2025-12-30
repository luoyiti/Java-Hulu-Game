package com.gameengine.app;

import com.gameengine.core.GameEngine;
import com.gameengine.core.GameLogic;
import com.gameengine.components.LifeFeatureComponent;
import com.gameengine.components.TransformComponent;
import com.gameengine.components.PhysicsComponent;
import com.gameengine.game.HuluPlayer;
import com.gameengine.graphics.IRenderer;
import com.gameengine.input.InputManager;
import com.gameengine.scene.Scene;
import com.gameengine.math.Vector2;

/**
 * 本地多人游戏场景
 * 规则和方法基本继承了 OnlineGameScene
 * 但目的是实现本地多人对战功能
 */

public class OfflineGameScene extends Scene {

    private IRenderer renderer;
    private GameEngine engine;
    private InputManager inputManager;
    private GameLogic gameLogic;
    private int PlayerCount;

    HuluPlayer[] players;

    public OfflineGameScene(String name, GameEngine engine, int PlayerCount) {
        super(name);
        this.engine = engine;
        this.PlayerCount = PlayerCount;
        this.players = new HuluPlayer[PlayerCount];
    }

    // @Override
    public void initialize() {
        super.initialize();
        this.renderer = engine.getRenderer();
        this.inputManager = engine.getInputManager();
        this.gameLogic = new GameLogic(this, engine);

        // 创建相机（视口800x600，世界地图也使用800x600以适配本地多人模式）
        this.camera = new com.gameengine.core.Camera(
                800, 600,
                800, 600 // 本地多人游戏使用固定屏幕大小作为世界大小
        );
        // 相机固定在屏幕中心
        this.camera.setPosition(new com.gameengine.math.Vector2(400, 300));

        // 创建玩家对象
        createPlayers();

        // 初始化多人游戏模式
        gameLogic.initializeMultiplayer(players, renderer);
    }

    @Override
    public void render() {

        // 绘制背景（基于图片）
        renderer.drawImage(
                "resources/picture/online_game_background1.png",
                0, 0,
                800, 600,
                1.0f);
        super.render();

        // 渲染粒子效果
        gameLogic.renderMultiplayerParticles();

        if (gameLogic.isMultiplayerGameOver()) {
            float cx = renderer.getWidth() / 2.0f;
            float cy = renderer.getHeight() / 2.0f;
            renderer.drawRect(0, 0, renderer.getWidth(), renderer.getHeight(), 0.0f, 0.0f, 0.0f, 0.35f);
            renderer.drawRect(cx - 200, cy - 60, 400, 120, 0.0f, 0.0f, 0.0f, 0.7f);

            String winText = gameLogic.getWinnerIndex() >= 0 ? "玩家" + (gameLogic.getWinnerIndex() + 1) + " 获胜!"
                    : "GAME OVER";
            renderer.drawText(winText, cx - 80, cy - 10, 1.0f, 1.0f, 1.0f, 1.0f, cy + 40);
            renderer.drawText("按 ESC 返回菜单", cx - 100, cy + 30, 0.8f, 0.8f, 0.8f, 1.0f, cy + 40);
        }

        renderPlayerHealthBar();
        renderSkillCooldownBar();
    }

    @Override
    public void update(float deltaTime) {
        super.update(deltaTime);

        if (gameLogic.isMultiplayerGameOver()) {
            // 游戏结束后只响应 ESC 键
            if (engine.getInputManager().isKeyJustPressed(256)) {
                MenuScene menuScene = new MenuScene(engine, "MainMenu");
                engine.setScene(menuScene);
            }
            return;
        }

        // 处理玩家输入（独立控制）
        handlePlayerInputs(deltaTime);

        // 更新技能冷却
        gameLogic.updateMultiplayerSkillCooldowns(deltaTime);

        // 处理玩家技能释放
        gameLogic.handleMultiPlayerSkills();

        // 玩家之间的碰撞检测（PvP）
        gameLogic.checkMultiplayerPlayerCollisions(deltaTime);

        // 检查玩家技能与其他玩家的碰撞
        gameLogic.checkMultiplayerSkillCollisions();

        // 更新物理
        gameLogic.updatePhysics();
        gameLogic.updateAttack(deltaTime);

        // 更新粒子效果
        gameLogic.updateMultiplayerParticles(deltaTime);

        // 检查游戏结束条件
        gameLogic.checkMultiplayerGameOver();

        if (engine.getInputManager().isKeyJustPressed(256)) { // GLFW_KEY_ESCAPE（Esc 键）
            MenuScene menuScene = new MenuScene(engine, "MainMenu");
            engine.setScene(menuScene);
        }
    }

    /**
     * 处理所有玩家的输入
     * 玩家1: WASD + J
     * 玩家2: 方向键 + Enter
     * 玩家3: IJKL + Space
     * 玩家4: 数字键盘 8456 + Numpad 0
     */
    private void handlePlayerInputs(float deltaTime) {
        // 玩家1 使用 WASD 控制
        if (players[0] != null && players[0].isActive()) {
            handleSinglePlayerInput(players[0], 87, 83, 65, 68); // W, S, A, D
        }

        // 玩家2 使用方向键控制
        if (PlayerCount >= 2 && players[1] != null && players[1].isActive()) {
            handleSinglePlayerInput(players[1], 265, 264, 263, 262); // Up, Down, Left, Right
        }

        // 玩家3 使用 IJKL 控制（如果有）
        if (PlayerCount >= 3 && players[2] != null && players[2].isActive()) {
            handleSinglePlayerInput(players[2], 73, 75, 74, 76); // I, K, J, L
        }

        // 玩家4 使用数字键盘控制（如果有）
        if (PlayerCount >= 4 && players[3] != null && players[3].isActive()) {
            handleSinglePlayerInput(players[3], 328, 322, 324, 326); // Numpad 8, 2, 4, 6
        }
    }

    private void handleSinglePlayerInput(HuluPlayer player, int upKey, int downKey, int leftKey, int rightKey) {
        TransformComponent transform = player.getComponent(TransformComponent.class);
        PhysicsComponent physics = player.getComponent(PhysicsComponent.class);

        if (transform == null || physics == null)
            return;

        Vector2 movement = new Vector2();

        if (inputManager.isKeyPressed(upKey)) {
            movement.y -= 1;
        }
        if (inputManager.isKeyPressed(downKey)) {
            movement.y += 1;
        }
        if (inputManager.isKeyPressed(leftKey)) {
            movement.x -= 1;
        }
        if (inputManager.isKeyPressed(rightKey)) {
            movement.x += 1;
        }

        if (movement.magnitude() > 0) {
            movement = movement.normalize().multiply(200);
            physics.setVelocity(movement);
        }

        // 边界检查（使用世界大小800x600）
        Vector2 pos = transform.getPosition();
        pos.x = Math.max(0, Math.min(800, pos.x));
        pos.y = Math.max(0, Math.min(600, pos.y));
        transform.setPosition(pos);
    }

    /**
     * 创建玩家对象
     */

    public void createPlayers() {

        // 主玩家对象，首先会被创建

        // 在这里统一设置玩家位置
        float startX = 100.0f;
        float startY = 300.0f;
        float spacing = 150.0f;

        for (int i = 0; i < PlayerCount; i++) {
            // 创建葫芦娃玩家对象
            players[i] = new HuluPlayer(renderer, this, "葫芦娃玩家" + (i + 1));

            // 设置玩家位置（水平排列）
            TransformComponent playerTransform = players[i].getComponent(TransformComponent.class);
            if (playerTransform != null) {
                playerTransform.setPosition(new Vector2(startX + i * spacing, startY));
            }

            // 添加到场景
            this.addGameObject(players[i]);
        }
    }

    private void renderPlayerHealthBar() {
        int index = 0;
        for (int i = 0; i < PlayerCount; i++) {
            if (players[i] == null) {
                continue;
            }

            LifeFeatureComponent lifeFeature = players[i].getComponent(LifeFeatureComponent.class);
            if (lifeFeature == null) {
                continue;
            }

            int currentHealth = lifeFeature.getBlood();
            int maxHealth = 100;

            int baseY = 20 + index * 40;
            String label = "玩家" + (index + 1) + " 血量";

            renderer.drawText(label, 20, baseY, 20,
                    1.0f, 1.0f, 1.0f, 1.0f);
            renderer.drawHealthBar(20, baseY + 30, 120, 10,
                    currentHealth, maxHealth);

            String healthText = currentHealth + " / " + maxHealth;
            renderer.drawText(healthText, 145, baseY + 40, 12,
                    1.0f, 1.0f, 1.0f, 1.0f);

            index++;
        }
    }

    private void renderSkillCooldownBar() {
        // 每个玩家的技能按键提示
        String[] skillKeys = { "J", "Enter", "Space", "Num0" };

        for (int index = 0; index < PlayerCount; index++) {
            if (players[index] == null)
                continue;

            // 使用每个玩家独立的冷却值
            float[] cooldownTimers = gameLogic.getPlayerSkillCooldownTimers();
            float cooldownPercentage = Math.min(1.0f, cooldownTimers[index] / 0.5f); // SKILL_COOLDOWN_DURATION = 0.5f

            int barX = 610;
            int barY = 10 + index * 40;

            String keyHint = index < skillKeys.length ? skillKeys[index] : "?";
            String label = "玩家" + (index + 1) + " 技能 (" + keyHint + ")";
            renderer.drawText(label, barX + 10, barY + 10, 20,
                    1.0f, 1.0f, 1.0f, 1.0f);

            int cooldownBarWidth = 140;
            int cooldownBarHeight = 10;
            int cooldownBarX = barX + 20;
            int cooldownBarY = barY + 25;

            int filledWidth = (int) (cooldownBarWidth * cooldownPercentage);

            if (cooldownPercentage >= 1.0f) {
                renderer.drawRect(cooldownBarX, cooldownBarY + 10, filledWidth, cooldownBarHeight,
                        0.0f, 1.0f, 0.0f, 1.0f);
            } else {
                renderer.drawRect(cooldownBarX, cooldownBarY + 10, filledWidth, cooldownBarHeight,
                        1.0f, 0.8f, 0.0f, 1.0f);
            }

            String percentText = String.format("%.0f%%", cooldownPercentage * 100);
            renderer.drawText(percentText, barX + 165, barY + 35, 12,
                    1.0f, 1.0f, 1.0f, 1.0f);
        }
    }

}
