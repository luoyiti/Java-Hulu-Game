package com.gameengine.level;

import com.gameengine.level.LevelConfig.EnemySpawnConfig;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * 关卡管理器
 * 负责关卡配置、敌人生成和关卡进度
 */
public class LevelManager {
    private final EnemyFactory enemyFactory;
    private final Map<Integer, LevelConfig> levelConfigs;
    private int currentLevel;
    private Random random;
    
    public LevelManager(EnemyFactory enemyFactory) {
        this.enemyFactory = enemyFactory;
        this.levelConfigs = new HashMap<>();
        this.currentLevel = 1;
        this.random = new Random();
        
        // 初始化关卡配置
        initializeLevelConfigs();
    }
    
    /**
     * 初始化所有关卡配置
     */
    private void initializeLevelConfigs() {
        // 第一关：只有士兵（4个）
        LevelConfig level1 = new LevelConfig(1);
        level1.addEnemy(LevelConfig.EnemyType.SOLDIER, 4);
        levelConfigs.put(1, level1);
        
        // 第二关：士兵 + 巫师（5个士兵 + 2个巫师）
        LevelConfig level2 = new LevelConfig(2);
        level2.addEnemy(LevelConfig.EnemyType.SOLDIER, 5);
        level2.addEnemy(LevelConfig.EnemyType.WIZARD, 2);
        levelConfigs.put(2, level2);
        
        // 第三关：三娃铜头铁臂（6个士兵 + 2个巫师 + 1个国王）
        LevelConfig level3 = new LevelConfig(3);
        level3.addEnemy(LevelConfig.EnemyType.SOLDIER, 6);
        level3.addEnemy(LevelConfig.EnemyType.WIZARD, 2);
        level3.addEnemy(LevelConfig.EnemyType.KING, 1);
        levelConfigs.put(3, level3);
        
        // 第四关：四娃喷火（7个士兵 + 3个巫师 + 1个国王）
        LevelConfig level4 = new LevelConfig(4);
        level4.addEnemy(LevelConfig.EnemyType.SOLDIER, 7);
        level4.addEnemy(LevelConfig.EnemyType.WIZARD, 3);
        level4.addEnemy(LevelConfig.EnemyType.KING, 1);
        levelConfigs.put(4, level4);
        
        // 第五关：五娃吐水（8个士兵 + 4个巫师 + 2个国王）
        LevelConfig level5 = new LevelConfig(5);
        level5.addEnemy(LevelConfig.EnemyType.SOLDIER, 8);
        level5.addEnemy(LevelConfig.EnemyType.WIZARD, 4);
        level5.addEnemy(LevelConfig.EnemyType.KING, 2);
        levelConfigs.put(5, level5);
        
        // 第六关：六娃隐身（9个士兵 + 5个巫师 + 2个国王）
        LevelConfig level6 = new LevelConfig(6);
        level6.addEnemy(LevelConfig.EnemyType.SOLDIER, 9);
        level6.addEnemy(LevelConfig.EnemyType.WIZARD, 5);
        level6.addEnemy(LevelConfig.EnemyType.KING, 2);
        levelConfigs.put(6, level6);
        
        // 第七关：七娃宝葫芦最终决战（10个士兵 + 6个巫师 + 3个国王）
        LevelConfig level7 = new LevelConfig(7);
        level7.addEnemy(LevelConfig.EnemyType.SOLDIER, 10);
        level7.addEnemy(LevelConfig.EnemyType.WIZARD, 6);
        level7.addEnemy(LevelConfig.EnemyType.KING, 3);
        levelConfigs.put(7, level7);
    }
    
    /**
     * 获取当前关卡号
     */
    public int getCurrentLevel() {
        return currentLevel;
    }
    
    /**
     * 进入下一关
     */
    public void nextLevel() {
        currentLevel++;
    }
    
    /**
     * 生成当前关卡的所有敌人
     */
    public void spawnCurrentLevel() {
        spawnLevel(currentLevel);
    }
    
    /**
     * 生成指定关卡的敌人
     */
    public void spawnLevel(int levelNumber) {
        LevelConfig config = levelConfigs.get(levelNumber);
        if (config == null) {
            System.err.println("关卡配置不存在: " + levelNumber);
            return;
        }
        
        // 根据配置生成敌人
        for (EnemySpawnConfig spawnConfig : config.getEnemies()) {
            for (int i = 0; i < spawnConfig.getCount(); i++) {
                enemyFactory.createEnemy(spawnConfig.getType());
            }
        }
    }
    
    /**
     * 重置关卡进度
     */
    public void reset() {
        currentLevel = 1;
    }
    
    /**
     * 生成无尽模式的敌人（随机组合）
     */
    public void spawnEndlessLevel() {
        // 无尽模式：随机生成敌人组合，难度递增
        int wave = currentLevel - 7; // 从第8关开始，wave从1开始
        
        // 基础敌人数量随波数增加
        int soldierCount = 4 + (wave - 1) * 2; // 4, 6, 8, 10...
        int kingCount = (wave / 2); // 每2波增加1个国王
        int wizardCount = (wave / 3); // 每3波增加1个巫师
        
        // 生成士兵
        for (int i = 0; i < soldierCount; i++) {
            enemyFactory.createEnemy(LevelConfig.EnemyType.SOLDIER);
        }
        
        // 生成国王
        for (int i = 0; i < kingCount; i++) {
            enemyFactory.createEnemy(LevelConfig.EnemyType.KING);
        }
        
        // 生成巫师
        for (int i = 0; i < wizardCount; i++) {
            enemyFactory.createEnemy(LevelConfig.EnemyType.WIZARD);
        }
        
        System.out.println("无尽模式 - 第 " + wave + " 波：" + 
                          soldierCount + " 士兵, " + 
                          kingCount + " 国王, " + 
                          wizardCount + " 巫师");
    }
}
