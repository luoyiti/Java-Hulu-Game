package com.gameengine.level;

import java.util.ArrayList;
import java.util.List;

/**
 * 关卡配置数据类
 * 定义每个关卡的敌人类型和数量
 */
public class LevelConfig {
    private final int levelNumber;
    private final List<EnemySpawnConfig> enemies;
    
    public LevelConfig(int levelNumber) {
        this.levelNumber = levelNumber;
        this.enemies = new ArrayList<>();
    }
    
    public int getLevelNumber() {
        return levelNumber;
    }
    
    public List<EnemySpawnConfig> getEnemies() {
        return enemies;
    }
    
    public void addEnemy(EnemyType type, int count) {
        enemies.add(new EnemySpawnConfig(type, count));
    }
    
    /**
     * 敌人生成配置
     */
    public static class EnemySpawnConfig {
        private final EnemyType type;
        private final int count;
        
        public EnemySpawnConfig(EnemyType type, int count) {
            this.type = type;
            this.count = count;
        }
        
        public EnemyType getType() {
            return type;
        }
        
        public int getCount() {
            return count;
        }
    }
    
    /**
     * 敌人类型枚举
     */
    public enum EnemyType {
        SOLDIER,    // 士兵
        WIZARD,     // 法师
        KING        // 国王
    }
}
