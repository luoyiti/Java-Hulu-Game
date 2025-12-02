package com.gameengine.app;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.LinkedList;

import com.gameengine.core.GameEngine;
import com.gameengine.graphics.IRenderer;
import com.gameengine.scene.Scene;

public class RecordingScene extends Scene {
    private String recordingPath;
    private LinkedList<String> moves;
    private IRenderer renderer;
    private GameEngine engine;
    
    // 回放控制变量
    private int currentFrameIndex = 0;
    private float accumulatedTime = 0f;
    private float targetTime = 0f;
    private String currentFrameData = "";
    
    // 回放状态变量
    private int playerHealth = 100;
    private float skillCooldown = 1.0f;

    public RecordingScene(GameEngine engine, String recordingPath) {
        super("Recording");
        this.recordingPath = recordingPath;
        this.engine = engine;
        this.moves = getMoves();
        this.renderer = engine.getRenderer();
        
        // 加载第一帧
        if (!moves.isEmpty()) {
            loadFrame(0);
        }
    }

    @Override
    public void render() {

        // 绘制背景（地图800x600）
        renderer.drawRect(0, 0, 800, 600, 0.1f, 0.1f, 0.2f, 1.0f);

        // 渲染当前帧的所有对象
        if (!currentFrameData.isEmpty()) {
            renderFrame(currentFrameData);
        }
        
        // 渲染UI元素
        renderPlayerHealthBar();
        renderSkillCooldownBar();
        renderReplayHint();
    }
    
    /**
     * 渲染玩家血条
     */
    private void renderPlayerHealthBar() {
        // 绘制"玩家血量"标签
        renderer.drawText("玩家血量", 20, 20, 20, 1.0f, 1.0f, 1.0f, 1.0f);
        
        // 绘制血条
        renderer.drawHealthBar(20, 50, 120, 10, playerHealth, 100);
        
        // 绘制血量数值
        String healthText = playerHealth + " / 100";
        renderer.drawText(healthText, 145, 60, 12, 1.0f, 1.0f, 1.0f, 1.0f);
    }
    
    /**
     * 渲染技能冷却条
     */
    private void renderSkillCooldownBar() {
        int barX = 610;
        int barY = 10;
        
        // 绘制"技能冷却"标签
        renderer.drawText("技能冷却 (J)", barX + 10, barY + 10, 20, 1.0f, 1.0f, 1.0f, 1.0f);
        
        // 绘制冷却条
        int cooldownBarWidth = 140;
        int cooldownBarHeight = 10;
        int cooldownBarX = barX + 20;
        int cooldownBarY = barY + 35;
        
        int filledWidth = (int)(cooldownBarWidth * skillCooldown);
        
        if (skillCooldown >= 1.0f) {
            renderer.drawRect(cooldownBarX, cooldownBarY, filledWidth, cooldownBarHeight, 
                            0.0f, 1.0f, 0.0f, 1.0f);
        } else {
            renderer.drawRect(cooldownBarX, cooldownBarY, filledWidth, cooldownBarHeight, 
                            1.0f, 0.8f, 0.0f, 1.0f);
        }
        
        // 绘制百分比文字
        String percentText = String.format("%.0f%%", skillCooldown * 100);
        renderer.drawText(percentText, barX + 165, barY + 35, 12, 1.0f, 1.0f, 1.0f, 1.0f);
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

    @Override
    public void update(float deltaTime) {
        // ESC键返回菜单
        if (engine.getInputManager().isKeyJustPressed(256)) { // GLFW_KEY_ESCAPE（Esc 键）
            engine.setScene(new MenuScene(engine, "MainMenu"));
            return;
        }
        
        // 累加时间
        accumulatedTime += deltaTime;
        
        // 当累计时间达到目标时间时，切换到下一帧
        if (accumulatedTime >= targetTime && currentFrameIndex < moves.size() - 1) {
            currentFrameIndex++;
            loadFrame(currentFrameIndex);
        }
        
        // 如果已经播放完所有帧，可以选择循环或停止
        if (currentFrameIndex >= moves.size() - 1) {
            // 循环播放
            currentFrameIndex = 0;
            accumulatedTime = 0f;
            loadFrame(0);
        }
    }
    
    /**
     * 加载指定索引的帧数据
     */
    private void loadFrame(int index) {
        if (index < 0 || index >= moves.size()) {
            return;
        }
        
        String frameData = moves.get(index);
        
        // 解析 deltaTime
        int deltaIndex = frameData.indexOf("deltaTime=");
        if (deltaIndex != -1) {
            int semicolonIndex = frameData.indexOf(";", deltaIndex);
            if (semicolonIndex != -1) {
                String deltaTimeStr = frameData.substring(deltaIndex + 10, semicolonIndex);
                targetTime = Float.parseFloat(deltaTimeStr);
                currentFrameData = frameData.substring(semicolonIndex + 1);
            }
        }
        
        // 解析 PlayerHealth
        int healthIndex = frameData.indexOf("PlayerHealth=");
        if (healthIndex != -1) {
            int semicolonIndex = frameData.indexOf(";", healthIndex);
            if (semicolonIndex != -1) {
                String healthStr = frameData.substring(healthIndex + 13, semicolonIndex);
                try {
                    playerHealth = Integer.parseInt(healthStr);
                } catch (NumberFormatException e) {
                    playerHealth = 100;
                }
            }
        }
        
        // 解析 SkillCooldown
        int cooldownIndex = frameData.indexOf("SkillCooldown=");
        if (cooldownIndex != -1) {
            int semicolonIndex = frameData.indexOf(";", cooldownIndex);
            if (semicolonIndex != -1) {
                String cooldownStr = frameData.substring(cooldownIndex + 14, semicolonIndex);
                try {
                    skillCooldown = Float.parseFloat(cooldownStr);
                } catch (NumberFormatException e) {
                    skillCooldown = 1.0f;
                }
            }
        }
    }
    
    /**
     * 渲染一帧的所有对象
     */
    private void renderFrame(String frameData) {
        // 按照 { } 分割每个游戏对象
        String[] objects = frameData.split("\\}");
        
        for (String obj : objects) {
            if (obj.trim().isEmpty() || !obj.contains("{")) {
                continue;
            }
            
            // 移除开头的 {
            obj = obj.substring(obj.indexOf("{") + 1);
            
            // 解析对象属性
            String[] properties = obj.split(",");
            
            String identity = "";
            float x = 0, y = 0;
            
            for (String prop : properties) {
                prop = prop.trim();
                
                if (prop.startsWith("GameIdentity=")) {
                    identity = prop.substring(13);
                } else if (prop.startsWith("TransformComponent=")) {
                    String transformData = prop.substring(19);
                    String[] coords = transformData.split("\\|");
                    if (coords.length >= 2) {
                        try {
                            x = Float.parseFloat(coords[0]);
                            y = Float.parseFloat(coords[1]);
                        } catch (NumberFormatException e) {
                            // 忽略解析错误
                        }
                    }
                }
            }
            
            // 根据 identity 渲染不同的对象
            if ("Player".equals(identity)) {
                renderHuluBodyParts(x, y);
            } else if ("Enemy".equals(identity)) {
                renderEnemy(x, y);
            } else if ("ImageEnemy".equals(identity)) {
                // 解析图片敌人的额外数据
                String imageData = "";
                for (String prop : properties) {
                    prop = prop.trim();
                    if (prop.startsWith("RenderComponent=")) {
                        imageData = prop.substring(16);
                    }
                }
                renderImageEnemy(x, y, imageData);
            } else if ("Player Skill".equals(identity)) {
                renderPlayerSkill(x, y);
            } else if ("Enemy Skill".equals(identity)) {
                renderEnemySkill(x, y);
            }
            // 可以添加更多对象类型的渲染
        }
    }

    public LinkedList<String> getMoves() {

        // 初始化 moves 列表
        LinkedList<String> movesList = new LinkedList<>();

        try (BufferedReader reader = new BufferedReader(new FileReader(recordingPath))) {

            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println(line);
                movesList.add(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return movesList;

    }

    private void renderHuluBodyParts(float x, float y) {
        // 使用图片渲染葫芦娃
        float imageWidth = 40f;
        float imageHeight = 50f;
        String imagePath = "resources/picture/huluBro1.png";
        
        renderer.drawImage(
            imagePath,
            x - imageWidth / 2,
            y - imageHeight / 2,
            imageWidth,
            imageHeight,
            1.0f
        );
    }
    
    /**
     * 渲染敌人士兵
     */
    private void renderEnemy(float x, float y) {
        // 躯干（制服）
        renderer.drawRect(x - 8f, y - 2f, 16f, 20f, 0.12f, 0.40f, 0.18f, 1f);

        // 头部
        renderer.drawCircle(x, y - 14f, 6f, 24, 1.0f, 0.86f, 0.72f, 1.0f);

        // 头盔
        renderer.drawRect(x - 7f, y - 19f, 14f, 6f, 0.10f, 0.30f, 0.12f, 1.0f);
        renderer.drawRect(x - 7f, y - 14f, 14f, 2f, 0.08f, 0.25f, 0.10f, 1.0f);

        // 眼睛
        renderer.drawCircle(x - 2.0f, y - 14.0f, 0.8f, 12, 0f, 0f, 0f, 1f);
        renderer.drawCircle(x + 2.0f, y - 14.0f, 0.8f, 12, 0f, 0f, 0f, 1f);

        // 手臂（制服）
        renderer.drawRect(x - 14f, y - 2f, 6f, 14f, 0.12f, 0.40f, 0.18f, 1f);
        renderer.drawRect(x + 8f, y - 2f, 6f, 14f, 0.12f, 0.40f, 0.18f, 1f);

        // 腰带
        renderer.drawRect(x - 8f, y + 6f, 16f, 2f, 0.05f, 0.05f, 0.05f, 1f);

        // 腿（裤子）
        renderer.drawRect(x - 6f, y + 12f, 6f, 12f, 0.10f, 0.35f, 0.15f, 1f);
        renderer.drawRect(x + 0f, y + 12f, 6f, 12f, 0.10f, 0.35f, 0.15f, 1f);

        // 靴子
        renderer.drawRect(x - 6f, y + 22f, 6f, 3f, 0f, 0f, 0f, 1f);
        renderer.drawRect(x + 0f, y + 22f, 6f, 3f, 0f, 0f, 0f, 1f);

        // 步枪
        renderer.drawRect(x + 12f, y - 2f, 14f, 2f, 0.1f, 0.1f, 0.1f, 1f);
        renderer.drawRect(x + 12f, y + 0f, 3f, 6f, 0.1f, 0.1f, 0.1f, 1f);
    }
    
    /**
     * 渲染玩家技能
     */
    private void renderPlayerSkill(float x, float y) {
        // 渲染箭头形状的攻击技能（白色）
        renderer.drawRect(x + 5f, y - 4f, 5f, 3f, 1.0f, 1.0f, 1.0f, 1.0f);
        renderer.drawRect(x + 5f, y + 1f, 5f, 3f, 1.0f, 1.0f, 1.0f, 1.0f);
    }
    
    /**
     * 渲染敌人技能
     */
    private void renderEnemySkill(float x, float y) {
        // 渲染箭头形状的攻击技能（红色）
        renderer.drawRect(x - 10f, y - 1f, 14f, 3f, 1.0f, 0.0f, 0.0f, 1f);
        renderer.drawRect(x + 5f, y - 4f, 5f, 3f, 1.0f, 0.0f, 0.0f, 1f);
        renderer.drawRect(x + 5f, y + 1f, 5f, 3f, 1.0f, 0.0f, 0.0f, 1f);
        renderer.drawRect(x + 7f, y - 2f, 3f, 2f, 1.0f, 0.0f, 0.0f, 1f);
        renderer.drawRect(x + 7f, y + 1f, 3f, 2f, 1.0f, 0.0f, 0.0f, 1f);
    }
    
    /**
     * 渲染图片敌人
     * 使用 PNG 图片进行渲染
     * 
     * @param x 位置 x 坐标
     * @param y 位置 y 坐标
     * @param imageData 图片数据字符串，格式: "imagePath|width|height|rotation|alpha"
     */
    private void renderImageEnemy(float x, float y, String imageData) {
        // 默认值
        String imagePath = "images/enemy.png";
        float width = 64f;
        float height = 64f;
        float rotation = 0f;
        float alpha = 1.0f;
        
        // 解析图片数据
        if (imageData != null && !imageData.isEmpty()) {
            String[] parts = imageData.split("\\|");
            if (parts.length >= 1 && !parts[0].isEmpty()) {
                imagePath = parts[0];
            }
            if (parts.length >= 2) {
                try {
                    width = Float.parseFloat(parts[1]);
                } catch (NumberFormatException e) { /* 使用默认值 */ }
            }
            if (parts.length >= 3) {
                try {
                    height = Float.parseFloat(parts[2]);
                } catch (NumberFormatException e) { /* 使用默认值 */ }
            }
            if (parts.length >= 4) {
                try {
                    rotation = Float.parseFloat(parts[3]);
                } catch (NumberFormatException e) { /* 使用默认值 */ }
            }
            if (parts.length >= 5) {
                try {
                    alpha = Float.parseFloat(parts[4]);
                } catch (NumberFormatException e) { /* 使用默认值 */ }
            }
        }
        
        // 使用图片渲染
        if (rotation != 0) {
            // 带旋转的图片渲染
            renderer.drawImageRotated(imagePath, x, y, width, height, rotation, alpha);
        } else {
            // 普通图片渲染（左上角坐标）
            renderer.drawImage(imagePath, x - width/2, y - height/2, width, height, alpha);
        }
        
        // 渲染血条（如果需要的话，可以从录制数据中获取血量）
        // 这里使用默认满血显示
        renderer.drawHealthBar(x - width/2, y - height/2 - 10f, width, 4f, 100, 100);
    }

}
