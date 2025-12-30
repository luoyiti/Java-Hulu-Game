package com.gameengine.ui;

import com.gameengine.components.LifeFeatureComponent;
import com.gameengine.core.GameObject;
import com.gameengine.core.GameLogic;
import com.gameengine.game.AttackSkillJ;
import com.gameengine.game.HuluPlayer;
import com.gameengine.graphics.IRenderer;

/**
 * 游戏UI管理器
 * 负责渲染所有游戏界面元素
 */
public class GameUIManager {
    private final IRenderer renderer;
    private final GameLogic gameLogic;
    
    public GameUIManager(IRenderer renderer, GameLogic gameLogic) {
        this.renderer = renderer;
        this.gameLogic = gameLogic;
    }
    
    /**
     * 渲染所有UI元素
     */
    public void renderAll(int level, boolean isRecording, boolean isGameOver) {
        renderLevel(level);
        renderPlayerHealthBar();
        renderSkillCooldownBar();
        renderRecordingHint(isRecording);
        
        if (isGameOver) {
            renderGameOverScreen();
        }
    }
    
    /**
     * 在屏幕顶部中央渲染关卡数
     */
    private void renderLevel(int level) {
        String levelText = "Level: " + level;
        renderer.drawText(levelText, 350, 30, 20, 1.0f, 1.0f, 1.0f, 1.0f);
    }
    
    /**
     * 在屏幕左上角渲染玩家血条
     */
    private void renderPlayerHealthBar() {
        GameObject player = gameLogic.getPlayer();
        
        if (player != null) {
            LifeFeatureComponent lifeFeature = player.getComponent(LifeFeatureComponent.class);
            if (lifeFeature != null) {
                int currentHealth = lifeFeature.getBlood();
                int maxHealth = 100;
                
                // 绘制"玩家血量"标签
                renderer.drawText("玩家血量", 20, 20, 20, 1.0f, 1.0f, 1.0f, 1.0f);
                
                // 绘制血条
                renderer.drawHealthBar(20, 50, 120, 10, currentHealth, maxHealth);
                
                // 绘制血量数值
                String healthText = currentHealth + " / " + maxHealth;
                renderer.drawText(healthText, 145, 60, 12, 1.0f, 1.0f, 1.0f, 1.0f);
            }
        }
    }
    
    /**
     * 在屏幕右上角渲染技能冷却条和当前技能类型
     */
    private void renderSkillCooldownBar() {
        if (gameLogic != null) {
            float cooldownPercentage = gameLogic.getSkillCooldownPercentage();
            
            // 冷却条的位置和尺寸
            int barX = 610;
            int barY = 10;
            int barWidth = 180;
            @SuppressWarnings("unused")
            int barHeight = 40;
            
            // 绘制"技能冷却"标签
            renderer.drawText("技能冷却 (J)", barX + 10, barY + 10, 20, 1.0f, 1.0f, 1.0f, 1.0f);
            
            // 绘制当前五行技能类型
            GameObject player = gameLogic.getPlayer();
            if (player instanceof HuluPlayer) {
                HuluPlayer huluPlayer = (HuluPlayer) player;
                AttackSkillJ.SkillType skillType = huluPlayer.getCurrentSkillType();
                if (skillType != null) {
                    String skillName = "当前: " + skillType.getChineseName();
                    // 根据技能类型设置不同颜色
                    float r = 1.0f, g = 1.0f, b = 1.0f;
                    switch (skillType) {
                        case METAL: r = 1.0f; g = 0.84f; b = 0.0f; break;   // 金色
                        case WOOD: r = 0.13f; g = 0.55f; b = 0.13f; break;  // 绿色
                        case WATER: r = 0.0f; g = 0.75f; b = 1.0f; break;   // 蓝色
                        case FIRE: r = 1.0f; g = 0.27f; b = 0.0f; break;    // 红色
                        case EARTH: r = 0.55f; g = 0.27f; b = 0.07f; break; // 棕色
                    }
                    renderer.drawText(skillName, barX + 100, barY + 10, 16, r, g, b, 1.0f);
                }
            }
            
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
    }
    
    /**
     * 在屏幕左下角渲染录制按钮提示
     */
    private void renderRecordingHint(boolean isRecording) {
        int hintX = 10;
        int hintY = renderer.getHeight() - 40;
        
        if (isRecording) {
            // 录制中显示红色圆点和停止提示
            renderer.drawCircle(hintX + 8, hintY + 10, 6, 16, 1.0f, 0.0f, 0.0f, 1.0f);
            renderer.drawText("录制中... (R 停止)", hintX + 20, hintY + 15, 14, 1.0f, 0.3f, 0.3f, 1.0f);
        } else {
            // 未录制时显示灰色圆点和开始提示
            renderer.drawCircle(hintX + 8, hintY + 10, 6, 16, 0.5f, 0.5f, 0.5f, 1.0f);
            renderer.drawText("按 R 开始录制", hintX + 20, hintY + 15, 14, 0.7f, 0.7f, 0.7f, 1.0f);
        }
    }
    
    /**
     * 渲染游戏结束界面
     */
    private void renderGameOverScreen() {
        float cx = renderer.getWidth() / 2.0f;
        float cy = renderer.getHeight() / 2.0f;
        renderer.drawRect(0, 0, renderer.getWidth(), renderer.getHeight(), 0.0f, 0.0f, 0.0f, 0.35f);
        renderer.drawRect(cx - 200, cy - 60, 400, 120, 0.0f, 0.0f, 0.0f, 0.7f);
        renderer.drawText("GAME OVER", cx - 100, cy - 10, 1.0f, 1.0f, 1.0f, 1.0f, cy + 40);
        renderer.drawText("PRESS ANY KEY TO RETURN", cx - 180, cy + 30, 0.8f, 0.8f, 0.8f, 1.0f, cy + 40);
    }
}
