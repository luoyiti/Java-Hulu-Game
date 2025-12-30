package com.gameengine.app;

import com.gameengine.core.GameEngine;
import com.gameengine.core.GameLogic;
import com.gameengine.core.GameObject;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.gameengine.game.InputRecord;
import com.gameengine.game.Record;
import com.gameengine.game.GameObjectRecord;
import com.gameengine.net.NioClient;
import com.gameengine.net.NetworkBuffer;

import com.gameengine.components.LifeFeatureComponent;
import com.gameengine.components.TransformComponent;
import com.gameengine.components.PhysicsComponent;
import com.gameengine.game.HuluPlayer;
import com.gameengine.graphics.IRenderer;
import com.gameengine.input.InputManager;
import com.gameengine.scene.Scene;
import com.gameengine.math.Vector2;
import com.gameengine.net.NetState;

import com.gameengine.net.MultiReactor;
// import com.gameengine.net.NioServer; 旧网络接口留用

/**
 * 网络游戏场景
 * 规则和方法基本继承了 GameScene
 * 但目的是实现网络游戏对战方法
 */

public class OnlineGameScene extends Scene {

    private IRenderer renderer;
    private GameEngine engine;
    private InputManager inputManager;
    private GameLogic gameLogic;
    private int PlayerCount;
    private Status status;
    private MultiReactor server;
    // private NioServer server; 旧网络接口留用
    private NioClient client;
    private boolean isAssigned = false;

    // 客户端渲染用的远程记录
    private Record lastRemoteRecord;
    private List<GameObjectRecord> remoteObjects = new ArrayList<>();

    // 检查是否所有玩家都成功加入游戏当中
    // 作为类的方法，它能够同时影响到所有玩家场景
    private static boolean isWaitingForJoin = true;

    HuluPlayer[] players;

    public enum Status {
        SERVER,
        CLIENT
    }

    public OnlineGameScene(String name, GameEngine engine, int PlayerCount, Status status) {
        super(name);
        this.engine = engine;
        this.PlayerCount = PlayerCount;
        this.players = new HuluPlayer[PlayerCount];
        this.status = status;
    }

    // @Override
    public void initialize() {
        super.initialize();
        this.renderer = engine.getRenderer();
        this.inputManager = engine.getInputManager();
        this.gameLogic = new GameLogic(this, engine);

        // 创建相机（视口800x600，世界地图也使用800x600以适配网络多人模式）
        this.camera = new com.gameengine.core.Camera(
                800, 600,
                800, 600 // 网络多人游戏使用固定屏幕大小作为世界大小
        );
        // 相机固定在屏幕中心
        this.camera.setPosition(new com.gameengine.math.Vector2(400, 300));

        if (this.isServer()) {
            // 旧网络接口留用
            // server = new NioServer(8888);
            // server.start();
            // 服务器端：启动服务器等待客户端连接
            // 使用 MultiReactor，子 Reactor 数量设置为 CPU 核心数
            int subReactorCount = Runtime.getRuntime().availableProcessors();
            server = new MultiReactor(8888, subReactorCount);
            server.start();
            System.out.println("[OnlineGameScene] Server mode - waiting for " + (PlayerCount - 1) + " clients");
        } else {
            // 客户端：连接到服务器
            client = new NioClient();
            if (client.connect("localhost", 8888)) {
                if (client.join("Player")) {
                    client.startInputLoop(inputManager);
                    client.startStateReceiveLoop();
                    System.out.println("[OnlineGameScene] Client mode - connected to server");
                }
            }
            isWaitingForJoin = false;
        }

        // 创建并分配玩家对象（仅服务端创建真实对象）
        if (this.isServer()) {
            createPlayers();
        }

        // 初始化多人游戏模式
        gameLogic.initializeMultiplayer(players, renderer);
    }

    @Override
    public void render() {

        /**
         * 当等待其他玩家加入时，显示等待界面
         */
        if (isWaitingForJoin) {
            renderer.drawRect(
                    0,
                    0,
                    renderer.getWidth(),
                    renderer.getHeight(),
                    0.0f,
                    0.0f,
                    0.0f,
                    1.0f);
            float cx = renderer.getWidth() / 2.0f;
            float cy = renderer.getHeight() / 2.0f;
            renderer.drawText(
                    "正在等待玩家加入",
                    cx - 180,
                    cy - 20,
                    100.0f,
                    2.0f,
                    2.0f,
                    1.0f,
                    cy + 40);
            return;
        }

        if (isServer()) {
            // 服务端：本地渲染完整场景
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
        } else {
            // 客户端：根据服务端广播的 Record 进行渲染
            renderClientFromRemoteRecord();
        }
    }

    @Override
    public void update(float deltaTime) {
        super.update(deltaTime);

        if (this.isServer() && NetState.getClientCount() == this.PlayerCount - 1) {
            isWaitingForJoin = false;
        }

        if (engine.getInputManager().isKeyJustPressed(256)) { // GLFW_KEY_ESCAPE（Esc 键）
            if (this.isServer()) {
                MenuScene menuScene = new MenuScene(engine, "MainMenu");
                engine.setScene(menuScene);
            } else {
                System.out.println("抱歉，客户端暂时没有资格退出游戏");
            }
        }

        // 等待玩家加入时不执行游戏更新操作
        if (isWaitingForJoin) {
            return;
        }

        // 客户端不执行本地游戏逻辑更新，只依赖服务端状态
        if (!isServer()) {
            return;
        }

        // 分配玩家到客户端
        if (isServer() && !isAssigned) {
            assignPlayer();
            isAssigned = true;
        }

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

        // 处理玩家技能释放
        gameLogic.handleMultiPlayerSkills();

        // 处理服务端的渲染记录
        if (isServer()) {
            Record records = gameLogic.getRecord(deltaTime);
            NetState.currentRecords = records;
        }

    }

    /**
     * 处理所有玩家的输入
     * Server端负责处理自己的输入，同时处理来自客户端的输入
     * Client端只记录输入，并交由Server端处理
     */
    private void handlePlayerInputs(float deltaTime) {
        if (!isServer())
            return; // 只有服务端处理输入

        // 服务端玩家（玩家1）使用 WASD 控制
        if (players[0] != null && players[0].isActive()) {
            handleSinglePlayerInput(players[0], 87, 83, 65, 68); // W, S, A, D
        }

        // 处理远程客户端玩家的输入
        Map<String, InputRecord> clientInputs = NetState.getAllClientInputs();
        for (int i = 1; i < PlayerCount; i++) {
            if (players[i] == null || !players[i].isActive())
                continue;

            String remoteAddr = players[i].getRemoteAddress();
            if (remoteAddr != null) {
                InputRecord input = clientInputs.get(remoteAddr);
                if (input != null) {
                    applyRemoteInput(players[i], input);
                }
            }
        }
    }

    /**
     * 应用远程客户端的输入到玩家
     */
    private void applyRemoteInput(HuluPlayer player, InputRecord input) {
        PhysicsComponent physics = player.getComponent(PhysicsComponent.class);
        TransformComponent transform = player.getComponent(TransformComponent.class);

        if (physics == null || transform == null)
            return;

        // 直接应用客户端发送的速度
        physics.setVelocity(new Vector2(input.vx, input.vy));

        // 边界检查（使用世界大小800x600）
        Vector2 pos = transform.getPosition();
        pos.x = Math.max(0, Math.min(800, pos.x));
        pos.y = Math.max(0, Math.min(600, pos.y));
        transform.setPosition(pos);
    }

    /**
     * 客户端：根据远程 Record 渲染一帧
     */
    private void renderClientFromRemoteRecord() {
        // 获取最新记录
        Record record = NetState.currentRecords;
        if (record != null) {
            lastRemoteRecord = record;
            List<GameObjectRecord> list = record.getGameObjectsMove();
            if (list != null) {
                remoteObjects = new ArrayList<>(list);
            } else {
                remoteObjects.clear();
            }
        }

        // 绘制背景（与服务端一致）
        renderer.drawImage(
                "resources/picture/online_game_background1.png",
                0, 0,
                800, 600,
                1.0f);

        // 使用插值后的坐标进行渲染，减少抖动
        Map<String, float[]> interp = NetworkBuffer.sample();

        for (GameObjectRecord obj : remoteObjects) {
            float[] pos = interp.get(obj.id);
            if (pos != null) {
                obj.x = pos[0];
                obj.y = pos[1];
            }
            renderRemoteObject(obj);
        }

        // 渲染基于 Record 的血条和技能冷却 UI
        renderRemotePlayerHealthBar();
        renderRemoteSkillCooldownBar();
    }

    /**
     * 渲染远程对象
     */
    private void renderRemoteObject(GameObjectRecord obj) {
        if (obj == null || obj.rt == null)
            return;

        switch (obj.rt) {
            case RECTANGLE:
                // 对于玩家技能，使用与 AttackSkillJ 相同的箭头形状渲染
                if ("Player Skill".equals(obj.identity)) {
                    float x = obj.x;
                    float y = obj.y;
                    renderer.drawRect(x + 5f, y - 4f, 5f, 3f, 1.0f, 1.0f, 1.0f, 1.0f);
                    renderer.drawRect(x + 5f, y + 1f, 5f, 3f, 1.0f, 1.0f, 1.0f, 1.0f);
                } else {
                    renderer.drawRect(
                            obj.x,
                            obj.y,
                            obj.width,
                            obj.height,
                            obj.r,
                            obj.g,
                            obj.b,
                            obj.a);
                }
                break;
            case CIRCLE:
                renderer.drawCircle(
                        obj.x + obj.width / 2,
                        obj.y + obj.height / 2,
                        obj.width / 2,
                        obj.segments,
                        obj.r,
                        obj.g,
                        obj.b,
                        obj.a);
                break;
            case LINE:
                renderer.drawLine(
                        obj.x,
                        obj.y,
                        obj.x + obj.width,
                        obj.y + obj.height,
                        obj.r,
                        obj.g,
                        obj.b,
                        obj.a);
                break;
            case IMAGE:
                renderer.drawImage(
                        obj.imagePath,
                        obj.x,
                        obj.y,
                        obj.width,
                        obj.height,
                        obj.alpha);
                renderRemoteEntityHealthBar(obj);
                break;
            case IMAGE_ROTATED:
                renderer.drawImageRotated(
                        obj.imagePath,
                        obj.x + obj.width / 2,
                        obj.y + obj.height / 2,
                        obj.width,
                        obj.height,
                        obj.rotation,
                        obj.alpha);
                renderRemoteEntityHealthBar(obj);
                break;
            case TEXT:
                // 暂不处理文本
                break;
            default:
                break;
        }
    }

    /**
     * 客户端为远程敌人渲染血条
     */
    private void renderRemoteEntityHealthBar(GameObjectRecord record) {
        if (record.identity == null)
            return;
        if (!"Enemy".equals(record.identity))
            return;
        if (record.maxHealth <= 0)
            return;

        float barWidth = record.width;
        float barHeight = 4f;
        float barX = record.x;
        float barY = record.y - 10f;

        renderer.drawHealthBar(barX, barY, barWidth, barHeight, record.currentHealth, record.maxHealth);
    }

    /**
     * 客户端左上角血条（基于 Record）
     */
    private void renderRemotePlayerHealthBar() {
        int currentHealth = 100;
        int maxHealth = 100;

        if (lastRemoteRecord != null) {
            currentHealth = lastRemoteRecord.getPlayerHealth();
            maxHealth = lastRemoteRecord.getPlayerMaxHealth();
            if (maxHealth <= 0)
                maxHealth = 100;
        }

        renderer.drawText("玩家血量", 20, 20, 20, 1.0f, 1.0f, 1.0f, 1.0f);
        renderer.drawHealthBar(20, 50, 120, 10, currentHealth, maxHealth);
        String healthText = currentHealth + " / " + maxHealth;
        renderer.drawText(healthText, 145, 60, 12, 1.0f, 1.0f, 1.0f, 1.0f);
    }

    /**
     * 客户端右上角技能冷却条（基于 Record）
     */
    private void renderRemoteSkillCooldownBar() {
        float cooldownPercentage = 1.0f;
        if (lastRemoteRecord != null) {
            cooldownPercentage = lastRemoteRecord.getSkillCooldownPercent();
        }

        int barX = 610;
        int barY = 10;

        renderer.drawText("技能冷却 (J)", barX + 10, barY + 10, 20, 1.0f, 1.0f, 1.0f, 1.0f);

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
     * 根据初始选择的玩家人数创建玩家对象
     * 只有服务端才有权限创建葫芦对象
     */
    public void createPlayers() {

        // 在这里统一设置玩家位置
        float startX = 100.0f;
        float startY = 300.0f;
        float gap = 100.0f;

        // 主玩家对象，首先会被创建
        HuluPlayer mainPlayer = new HuluPlayer(renderer, this, "Hulu Player 1", Status.SERVER);
        mainPlayer.getComponent(com.gameengine.components.TransformComponent.class)
                .setPosition(new Vector2(startX, startY));
        startX += gap;
        addGameObject(mainPlayer);
        players[0] = mainPlayer;

        for (int i = 2; i <= PlayerCount; i++) {
            // 根据玩家数量创建对应的服务端

            HuluPlayer player = new HuluPlayer(renderer, this, "Hulu Player " + i, Status.CLIENT);
            player.getComponent(com.gameengine.components.TransformComponent.class)
                    .setPosition(new Vector2(startX, startY));
            startX += gap;
            addGameObject(player);
            players[i - 1] = player;

        }
    }

    /**
     * 将玩家对象分配到不同的客户端中, 只有所有玩家都到达时才会开始游戏
     */
    public void assignPlayer() {
        if (isWaitingForJoin)
            return;

        List<String> addresses = NetState.getClientAddressesSnapshot();
        if (addresses.size() < PlayerCount - 1)
            return; // 需要 PlayerCount-1 个客户端

        Iterator<String> iterator = addresses.iterator();

        for (HuluPlayer player : players) {
            if (player == null)
                continue;
            if (player.getStatus() == Status.SERVER)
                continue;
            if (player.hasRemoteAddress())
                continue; // 已分配则跳过

            if (iterator.hasNext()) {
                String addr = iterator.next();
                player.setRemoteAddress(addr);
            }
        }
    }

    private void renderPlayerHealthBar() {
        int index = 0;

        for (GameObject obj : getGameObjects()) {
            if (!"Player".equals(obj.getidentity())) {
                continue;
            }

            LifeFeatureComponent lifeFeature = obj.getComponent(LifeFeatureComponent.class);
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

    /**
     * 是服务端返回 true
     * 其它返回 false
     * 
     * @return
     */
    public boolean isServer() {
        return this.status == Status.SERVER ? true : false;
    }

}
