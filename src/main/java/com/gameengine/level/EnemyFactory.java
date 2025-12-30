package com.gameengine.level;

import com.gameengine.components.TransformComponent;
import com.gameengine.core.GameObject;
import com.gameengine.core.GameLogic;
import com.gameengine.game.EnemyKing;
import com.gameengine.game.EnemySoldier;
import com.gameengine.game.EnemyWizard;
import com.gameengine.graphics.IRenderer;
import com.gameengine.math.Vector2;
import com.gameengine.scene.Scene;

import java.util.Random;

/**
 * 敌人工厂类
 * 统一管理敌人的创建和位置生成
 */
public class EnemyFactory {
    private final IRenderer renderer;
    private final Scene scene;
    private final GameLogic gameLogic;
    private final Random random;
    
    public EnemyFactory(IRenderer renderer, Scene scene, GameLogic gameLogic) {
        this.renderer = renderer;
        this.scene = scene;
        this.gameLogic = gameLogic;
        this.random = new Random();
    }
    
    /**
     * 根据类型创建敌人
     */
    public GameObject createEnemy(LevelConfig.EnemyType type) {
        switch (type) {
            case SOLDIER:
                return createSoldier();
            case WIZARD:
                return createWizard();
            case KING:
                return createKing();
            default:
                throw new IllegalArgumentException("Unknown enemy type: " + type);
        }
    }
    
    /**
     * 创建士兵敌人
     */
    private GameObject createSoldier() {
        Vector2 position = generateSpawnPosition(150);
        EnemySoldier soldier = new EnemySoldier(
            renderer,
            scene,
            position, 
            "resources/picture/bee.png",
            new Vector2(40, 60), 
            random
        );
        scene.addGameObject(soldier);
        return soldier;
    }
    
    /**
     * 创建法师敌人
     */
    private GameObject createWizard() {
        Vector2 position = generateSpawnPosition(150);
        EnemyWizard wizard = new EnemyWizard(
            renderer,
            scene,
            position, 
            "resources/picture/wizard.png",
            new Vector2(50, 70), 
            random
        );
        scene.addGameObject(wizard);
        return wizard;
    }
    
    /**
     * 创建国王敌人
     */
    private GameObject createKing() {
        Vector2 position = generateSpawnPosition(200);
        EnemyKing king = new EnemyKing(
            renderer, 
            scene, 
            position, 
            "resources/picture/snake_queen.png",
            new Vector2(80, 80), 
            random
        );
        scene.addGameObject(king);
        return king;
    }
    
    /**
     * 生成远离玩家但在可见范围内的位置
     * @param minDistance 与玩家的最小距离
     */
    private Vector2 generateSpawnPosition(int minDistance) {
        Vector2 position;
        int maxAttempts = 50;
        int attempts = 0;
        
        // 获取玩家位置
        Vector2 playerCenter = new Vector2(400, 300);
        if (gameLogic != null) {
            GameObject player = gameLogic.getPlayer();
            if (player != null) {
                TransformComponent pt = player.getComponent(TransformComponent.class);
                if (pt != null) {
                    playerCenter = pt.getPosition();
                }
            }
        }
        
        // 视口范围（800x600）
        float viewportHalfWidth = 400;
        float viewportHalfHeight = 300;
        
        do {
            // 在玩家可见范围内生成敌人
            position = new Vector2(
                playerCenter.x + (random.nextFloat() - 0.5f) * 2 * viewportHalfWidth,
                playerCenter.y + (random.nextFloat() - 0.5f) * 2 * viewportHalfHeight
            );
            
            // 确保位置在世界边界内
            position.x = Math.max(50, Math.min(GameLogic.WORLD_WIDTH - 50, position.x));
            position.y = Math.max(50, Math.min(GameLogic.WORLD_HEIGHT - 50, position.y));
            
            attempts++;
            
            // 计算与玩家中心的距离
            float dx = position.x - playerCenter.x;
            float dy = position.y - playerCenter.y;
            float distance = (float) Math.sqrt(dx * dx + dy * dy);
            
            // 如果距离足够远，或者尝试次数过多，就使用这个位置
            if (distance >= minDistance || attempts >= maxAttempts) {
                break;
            }
        } while (true);
        
        return position;
    }
}
