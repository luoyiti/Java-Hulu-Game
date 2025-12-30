package com.gameengine.level;

import org.junit.Test;
import org.junit.Before;
import static org.junit.Assert.*;

import com.gameengine.level.LevelConfig.EnemySpawnConfig;
import com.gameengine.level.LevelConfig.EnemyType;

/**
 * LevelConfig 测试类
 */
public class LevelConfigTest {
    
    private LevelConfig config;
    
    @Before
    public void setUp() {
        config = new LevelConfig(1);
    }
    
    @Test
    public void testConstructor() {
        assertEquals(1, config.getLevelNumber());
        assertNotNull(config.getEnemies());
        assertTrue(config.getEnemies().isEmpty());
    }
    
    @Test
    public void testAddEnemy() {
        config.addEnemy(EnemyType.SOLDIER, 5);
        
        assertEquals(1, config.getEnemies().size());
        EnemySpawnConfig spawnConfig = config.getEnemies().get(0);
        assertEquals(EnemyType.SOLDIER, spawnConfig.getType());
        assertEquals(5, spawnConfig.getCount());
    }
    
    @Test
    public void testAddMultipleEnemies() {
        config.addEnemy(EnemyType.SOLDIER, 4);
        config.addEnemy(EnemyType.WIZARD, 2);
        config.addEnemy(EnemyType.KING, 1);
        
        assertEquals(3, config.getEnemies().size());
    }
    
    @Test
    public void testEnemySpawnConfig() {
        EnemySpawnConfig spawnConfig = new EnemySpawnConfig(EnemyType.WIZARD, 3);
        
        assertEquals(EnemyType.WIZARD, spawnConfig.getType());
        assertEquals(3, spawnConfig.getCount());
    }
    
    @Test
    public void testEnemyTypeValues() {
        assertEquals(3, EnemyType.values().length);
        assertEquals(EnemyType.SOLDIER, EnemyType.valueOf("SOLDIER"));
        assertEquals(EnemyType.WIZARD, EnemyType.valueOf("WIZARD"));
        assertEquals(EnemyType.KING, EnemyType.valueOf("KING"));
    }
    
    @Test
    public void testDifferentLevelNumbers() {
        LevelConfig level2 = new LevelConfig(2);
        LevelConfig level3 = new LevelConfig(3);
        
        assertEquals(2, level2.getLevelNumber());
        assertEquals(3, level3.getLevelNumber());
    }
}
