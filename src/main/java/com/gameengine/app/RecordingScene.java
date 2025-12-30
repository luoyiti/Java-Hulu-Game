package com.gameengine.app;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import com.gameengine.core.Camera;
import com.gameengine.core.GameEngine;
import com.gameengine.core.GameLogic;
import com.gameengine.graphics.IRenderer;
import com.gameengine.math.Vector2;
import com.gameengine.scene.Scene;
import com.google.gson.Gson;
import com.gameengine.game.GameObjectRecord;
import com.gameengine.game.Record;

public class RecordingScene extends Scene {
    private String recordingPath;
    private LinkedList<Record> records;
    private IRenderer renderer;
    private GameEngine engine;

    // 回放控制变量
    private int currentFrameIndex = 0;
    private float accumulatedTime = 0f;
    private float startTime = 0f;

    // 回放状态变量
    private int playerHealth = 100;
    private float skillCooldown = 1.0f;

    private Record currentRecord;

    private static Gson gson = new Gson();

    private List<GameObjectRecord> currentFrameObjects = new ArrayList<>();
    
    // 摄像头相关
    private Vector2 playerWorldPosition = null; // 玩家在世界坐标中的位置

    public RecordingScene(GameEngine engine, String recordingPath) {
        super("Recording");
        this.recordingPath = recordingPath;
        this.renderer = engine.getRenderer();
        this.engine = engine;

        try {
            this.records = loadRecording(recordingPath);
        } catch (IOException e) {
            System.out.println("无法加载回放文件");
            e.printStackTrace();
            this.records = new LinkedList<>();
        }

        if (!records.isEmpty()) {
            startTime = records.get(0).getKey();
            accumulatedTime = 0f;
            currentFrameIndex = 0;
            currentRecord = null;
        }
        
        // 创建相机（视口800x600，世界地图2000x1500）
        this.camera = new Camera(
                800, 600,
                GameLogic.WORLD_WIDTH,
                GameLogic.WORLD_HEIGHT);
        this.camera.setSmoothSpeed(0.15f); // 设置平滑跟随速度
        
        // 初始化相机位置到世界中心
        this.camera.setPosition(new Vector2(GameLogic.WORLD_WIDTH / 2, GameLogic.WORLD_HEIGHT / 2));
    }

    @Override
    public void render() {

        // 绘制背景（基于图片，根据相机位置滚动）
        if (camera != null) {
            Vector2 camPos = camera.getPosition();
            float bgOffsetX = -(camPos.x - 400) * 0.5f; // 视差滚动效果（背景移动速度减半）
            float bgOffsetY = -(camPos.y - 300) * 0.5f;

            // 绘制平铺背景以覆盖整个视口
            renderer.drawImage(
                    "resources/picture/game_scene.png",
                    bgOffsetX, bgOffsetY,
                    2000.0f, 1500.0f,
                    1.0f);
        } else {
            // 如果没有相机，使用原来的固定背景
            renderer.drawImage(
                    "resources/picture/game_scene.png",
                    0, 0,
                    800, 600,
                    1.0f);
        }

        // 根据 currentFrameObjects 画出这一帧的所有对象
        for (GameObjectRecord obj : currentFrameObjects) {
            renderFrame(obj);
        }

        // 渲染玩家血量条（左上角）
        renderPlayerHealthBar();

        // 渲染技能冷却条（右上角）
        renderSkillCooldownBar();

        // 渲染UI元素
        renderReplayHint();
    }

    /**
     * 渲染回放提示
     */
    private void renderReplayHint() {
        int hintX = 10;
        int hintY = renderer.getHeight() - 40;

        renderer.drawCircle(hintX + 8, hintY + 10, 6, 16, 0.0f, 0.8f, 1.0f, 1.0f);
        renderer.drawText("回放中... (ESC 返回)", hintX + 20, hintY + 15, 14, 0.7f, 0.9f, 1.0f, 1.0f);
    }

    /**
     * 在屏幕左上角渲染玩家血条（模拟 GameScene）
     */
    private void renderPlayerHealthBar() {
        int currentHealth = playerHealth;
        int maxHealth = 100;

        // 从当前记录中获取玩家血量
        if (currentRecord != null) {
            currentHealth = currentRecord.getPlayerHealth();
            maxHealth = currentRecord.getPlayerMaxHealth();
            if (maxHealth <= 0)
                maxHealth = 100;
        }

        // 绘制"玩家血量"标签
        renderer.drawText("玩家血量", 20, 20, 20, 1.0f, 1.0f, 1.0f, 1.0f);

        // 绘制血条
        renderer.drawHealthBar(20, 50, 120, 10, currentHealth, maxHealth);

        // 绘制血量数值
        String healthText = currentHealth + " / " + maxHealth;
        renderer.drawText(healthText, 145, 60, 12, 1.0f, 1.0f, 1.0f, 1.0f);
    }

    /**
     * 在屏幕右上角渲染技能冷却条（模拟 GameScene）
     */
    private void renderSkillCooldownBar() {
        float cooldownPercentage = skillCooldown;

        // 从当前记录中获取技能冷却百分比
        if (currentRecord != null) {
            cooldownPercentage = currentRecord.getSkillCooldownPercent();
        }

        // 冷却条的位置和尺寸（根据地图尺寸800x600调整位置）
        int barX = 610;
        int barY = 10;

        // 绘制"技能冷却"标签
        renderer.drawText("技能冷却 (J)", barX + 10, barY + 10, 20, 1.0f, 1.0f, 1.0f, 1.0f);

        // 绘制冷却条
        int cooldownBarWidth = 140;
        int cooldownBarHeight = 10;
        int cooldownBarX = barX + 20;
        int cooldownBarY = barY + 25;

        // 根据冷却状态绘制冷却进度条
        int filledWidth = (int) (cooldownBarWidth * cooldownPercentage);

        if (cooldownPercentage >= 1.0f) {
            // 冷却完成，绘制绿色条
            renderer.drawRect(cooldownBarX, cooldownBarY + 10, filledWidth, cooldownBarHeight,
                    0.0f, 1.0f, 0.0f, 1.0f);
        } else {
            // 冷却中，绘制黄色条
            renderer.drawRect(cooldownBarX, cooldownBarY + 10, filledWidth, cooldownBarHeight,
                    1.0f, 0.8f, 0.0f, 1.0f);
        }

        // 绘制百分比文字
        String percentText = String.format("%.0f%%", cooldownPercentage * 100);
        renderer.drawText(percentText, barX + 165, barY + 35, 12, 1.0f, 1.0f, 1.0f, 1.0f);
    }

    @Override
    public void update(float deltaTime) {
        // ESC键返回菜单
        if (engine.getInputManager().isKeyJustPressed(256)) { // GLFW_KEY_ESCAPE（Esc 键）
            engine.setScene(new MenuScene(engine, "MainMenu"));
            return;
        }

        if (records == null || records.isEmpty()) {
            return;
        }
        
        // 更新相机位置跟随玩家
        if (camera != null && playerWorldPosition != null) {
            camera.follow(playerWorldPosition, deltaTime);
        }

        // 累加时间（从回放开始到现在的时间）
        accumulatedTime += deltaTime;

        // 根据录制时的绝对时间戳推进回放
        while (currentFrameIndex < records.size()) {
            Record nextRecord = records.get(currentFrameIndex);
            float scheduledTime = nextRecord.getKey() - startTime;

            if (accumulatedTime >= scheduledTime) {
                // 应用这一条记录（更新当前帧的对象列表）
                currentRecord = nextRecord;
                loadFrame(currentFrameIndex);
                currentFrameIndex++;
                System.out.println(currentFrameIndex);
            } else {
                break;
            }
        }

        // 如果已经播放完所有帧，则从头开始循环
        if (currentFrameIndex >= records.size()) {
            accumulatedTime = 0f;
            currentFrameIndex = 0;
            currentRecord = null;
            currentFrameObjects.clear();
        }
    }

    /**
     * 加载指定索引的帧数据
     */
    private void loadFrame(int index) {
        if (index < 0 || index >= records.size()) {
            return;
        }

        this.currentRecord = records.get(index);

        // 解析 deltaTime
        // float deltaIndex = this.currentRecord.getKey();

        if ("input".equals(this.currentRecord.getType())) {
            // 这里暂不处理，留作网络游戏的记录
            return;
        } else if ("object_move".equals(this.currentRecord.getType())) {
            // 此处作为重新加载的核心点
            List<GameObjectRecord> list = this.currentRecord.getGameObjectsMove();
            currentFrameObjects = (list != null) ? new ArrayList<>(list)
                    : new ArrayList<>();
            
            // 从当前帧数据中查找玩家位置，用于摄像头跟随
            updatePlayerPosition();
        }

    }
    
    /**
     * 从当前帧对象中提取玩家位置
     */
    private void updatePlayerPosition() {
        if (currentFrameObjects == null || currentFrameObjects.isEmpty()) {
            return;
        }
        
        // 查找玩家对象（根据 identity 或 imagePath 判断）
        for (GameObjectRecord obj : currentFrameObjects) {
            if (obj.identity != null && obj.identity.contains("Player")) {
                // 计算玩家中心位置（世界坐标）
                float centerX = obj.x + obj.width / 2;
                float centerY = obj.y + obj.height / 2;
                playerWorldPosition = new Vector2(centerX, centerY);
                break;
            }
        }
    }

    /**
     * 渲染一帧的所有对象
     */
    private void renderFrame(GameObjectRecord gameObjectRecord) {
        if (gameObjectRecord.rt == null)
            return;
        
        // 将世界坐标转换为屏幕坐标
        Vector2 worldPos = new Vector2(gameObjectRecord.x, gameObjectRecord.y);
        Vector2 screenPos = (camera != null) ? camera.worldToScreen(worldPos) : worldPos;
        float screenX = screenPos.x;
        float screenY = screenPos.y;

        switch (gameObjectRecord.rt) {
            case RECTANGLE:
                // 对于玩家技能，使用与 AttackSkillJ 相同的箭头形状渲染
                if ("Player Skill".equals(gameObjectRecord.identity)) {
                    renderPlayerSkill(gameObjectRecord, screenX, screenY);
                } else {
                    renderer.drawRect(
                            screenX,
                            screenY,
                            gameObjectRecord.width,
                            gameObjectRecord.height,
                            gameObjectRecord.r,
                            gameObjectRecord.g,
                            gameObjectRecord.b,
                            gameObjectRecord.a);
                }
                break;
            case CIRCLE:
                renderer.drawCircle(
                        screenX + gameObjectRecord.width / 2,
                        screenY + gameObjectRecord.height / 2,
                        gameObjectRecord.width / 2,
                        gameObjectRecord.segments,
                        gameObjectRecord.r,
                        gameObjectRecord.g,
                        gameObjectRecord.b,
                        gameObjectRecord.a);
                break;
            case LINE:
                renderer.drawLine(
                        screenX,
                        screenY,
                        screenX + gameObjectRecord.width,
                        screenY + gameObjectRecord.height,
                        gameObjectRecord.r,
                        gameObjectRecord.g,
                        gameObjectRecord.b,
                        gameObjectRecord.a);
                break;
            case IMAGE:
                renderer.drawImage(
                        gameObjectRecord.imagePath,
                        screenX,
                        screenY,
                        gameObjectRecord.width,
                        gameObjectRecord.height,
                        gameObjectRecord.alpha);
                // 为敌人渲染血条
                renderEntityHealthBar(gameObjectRecord, screenX, screenY);
                break;
            case IMAGE_ROTATED:
                renderer.drawImageRotated(
                        gameObjectRecord.imagePath,
                        screenX + gameObjectRecord.width / 2,
                        screenY + gameObjectRecord.height / 2,
                        gameObjectRecord.width,
                        gameObjectRecord.height,
                        gameObjectRecord.rotation,
                        gameObjectRecord.alpha);
                // 为敌人渲染血条
                renderEntityHealthBar(gameObjectRecord, screenX, screenY);
                break;
            case TEXT:
                // 文本类型暂不处理
                break;
            default:
                break;
        }
    }

    /**
     * 为敌人渲染血条（使用屏幕坐标）
     */
    private void renderEntityHealthBar(GameObjectRecord record, float screenX, float screenY) {
        // 只为敌人渲染血条
        if (record.identity == null)
            return;
        if (!"Enemy".equals(record.identity))
            return;
        if (record.maxHealth <= 0)
            return;

        // 血条位于实体头顶上方（使用屏幕坐标）
        float barWidth = record.width;
        float barHeight = 4f;
        float barX = screenX;
        float barY = screenY - 10f;

        renderer.drawHealthBar(barX, barY, barWidth, barHeight, record.currentHealth, record.maxHealth);
    }

    /**
     * 渲染玩家技能（J 技能）的箭头形状（使用屏幕坐标）
     */
    private void renderPlayerSkill(GameObjectRecord record, float screenX, float screenY) {
        // 与 AttackSkillJ.renderBodyParts 中的形状保持一致
        renderer.drawRect(screenX + 5f, screenY - 4f, 5f, 3f, 1.0f, 1.0f, 1.0f, 1.0f);
        renderer.drawRect(screenX + 5f, screenY + 1f, 5f, 3f, 1.0f, 1.0f, 1.0f, 1.0f);
    }

    /**
     * 加载录像文件
     */
    public static LinkedList<Record> loadRecording(String filePath) throws IOException {
        LinkedList<Record> records = new LinkedList<>();

        Path path = Paths.get(filePath);

        try (BufferedReader reader = Files.newBufferedReader(path)) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty())
                    continue;
                try {
                    Record record = gson.fromJson(line, Record.class);
                    records.add(record);
                } catch (Exception e) {
                    System.err.println("无法解析这一行: " + line);
                }
            }
        }

        return records;
    }

}