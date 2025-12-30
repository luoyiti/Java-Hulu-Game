package com.gameengine.level;

import org.junit.Test;
import org.junit.Before;
import static org.junit.Assert.*;

import com.gameengine.core.GameObject;
import com.gameengine.math.Vector2;
import com.gameengine.scene.Scene;

/**
 * LevelManager 关卡管理器测试类
 */
public class LevelManagerTest {
    
    // 模拟渲染器
    private static class MockRenderer implements com.gameengine.graphics.IRenderer {
        @Override
        public void beginFrame() {}
        @Override
        public void endFrame() {}
        @Override
        public void drawRect(float x, float y, float width, float height, float r, float g, float b, float a) {}
        @Override
        public void drawCircle(float x, float y, float radius, int segments, float r, float g, float b, float a) {}
        @Override
        public void drawLine(float x1, float y1, float x2, float y2, float r, float g, float b, float a) {}
        @Override
        public void drawText(String text, float x, float y, float fontSize, float r, float g, float b, float a) {}
        @Override
        public void drawHealthBar(float x, float y, float width, float height, int currentHealth, int maxHealth) {}
        @Override
        public void drawImage(String imagePath, float x, float y, float width, float height, float alpha) {}
        @Override
        public void drawImageRotated(String imagePath, float x, float y, float width, float height, float rotation, float alpha) {}
        @Override
        public boolean shouldClose() { return false; }
        @Override
        public void pollEvents() {}
        @Override
        public void cleanup() {}
        @Override
        public int getWidth() { return 800; }
        @Override
        public int getHeight() { return 600; }
        @Override
        public String getTitle() { return "Test"; }
    }
    
    private MockRenderer renderer;
    private Scene scene;
    private EnemyFactory enemyFactory;
    private LevelManager levelManager;
    
    @Before
    public void setUp() {
        renderer = new MockRenderer();
        scene = new Scene("TestScene");
        enemyFactory = new EnemyFactory(renderer, scene, null);
        levelManager = new LevelManager(enemyFactory);
    }
    
    @Test
    public void testInitialLevel() {
        assertEquals(1, levelManager.getCurrentLevel());
    }
    
    @Test
    public void testNextLevel() {
        levelManager.nextLevel();
        assertEquals(2, levelManager.getCurrentLevel());
        
        levelManager.nextLevel();
        assertEquals(3, levelManager.getCurrentLevel());
    }
    
    @Test
    public void testReset() {
        levelManager.nextLevel();
        levelManager.nextLevel();
        assertEquals(3, levelManager.getCurrentLevel());
        
        levelManager.reset();
        assertEquals(1, levelManager.getCurrentLevel());
    }
    
    @Test
    public void testSpawnLevel1() {
        // 生成关卡1的敌人
        levelManager.spawnLevel(1);
        
        // 调用update让场景处理添加队列
        scene.update(0.016f);
        
        // Level 1 spawns 4 soldiers
        assertTrue(scene.getGameObjects().size() >= 4);
    }
    
    @Test
    public void testSpawnLevel2() {
        // 生成关卡2的敌人
        levelManager.spawnLevel(2);
        scene.update(0.016f);
        
        // Level 2 spawns 5 soldiers + 2 wizards = 7 enemies
        assertTrue(scene.getGameObjects().size() >= 7);
    }
    
    @Test
    public void testSpawnLevel3() {
        // 生成关卡3的敌人
        levelManager.spawnLevel(3);
        scene.update(0.016f);
        
        // Level 3 spawns 6 soldiers + 2 wizards + 1 king = 9 enemies
        assertTrue(scene.getGameObjects().size() >= 9);
    }
    
    @Test
    public void testSpawnLevel4() {
        // 生成关卡4的敌人（四娃喷火）
        levelManager.spawnLevel(4);
        scene.update(0.016f);
        
        // Level 4 spawns 7 soldiers + 3 wizards + 1 king = 11 enemies
        assertTrue(scene.getGameObjects().size() >= 11);
    }
    
    @Test
    public void testSpawnLevel5() {
        // 生成关卡5的敌人（五娃吐水）
        levelManager.spawnLevel(5);
        scene.update(0.016f);
        
        // Level 5 spawns 8 soldiers + 4 wizards + 2 kings = 14 enemies
        assertTrue(scene.getGameObjects().size() >= 14);
    }
    
    @Test
    public void testSpawnLevel6() {
        // 生成关卡6的敌人（六娃隐身）
        levelManager.spawnLevel(6);
        scene.update(0.016f);
        
        // Level 6 spawns 9 soldiers + 5 wizards + 2 kings = 16 enemies
        assertTrue(scene.getGameObjects().size() >= 16);
    }
    
    @Test
    public void testSpawnLevel7() {
        // 生成关卡7的敌人（七娃宝葫芦最终决战）
        levelManager.spawnLevel(7);
        scene.update(0.016f);
        
        // Level 7 spawns 10 soldiers + 6 wizards + 3 kings = 19 enemies
        assertTrue(scene.getGameObjects().size() >= 19);
    }
    
    @Test
    public void testSpawnCurrentLevel() {
        // 生成当前关卡的敌人
        levelManager.spawnCurrentLevel();
        scene.update(0.016f);
        
        // Current level is 1, which spawns 4 soldiers
        assertTrue(scene.getGameObjects().size() >= 4);
    }
    
    @Test
    public void testSpawnInvalidLevel() {
        // 不存在的关卡不应抛出异常
        levelManager.spawnLevel(999);
    }
    
    @Test
    public void testSpawnEndlessMode() {
        // 设置到无尽模式关卡（关卡8及以上，即完成7关后）
        levelManager.nextLevel(); // 2
        levelManager.nextLevel(); // 3
        levelManager.nextLevel(); // 4
        levelManager.nextLevel(); // 5
        levelManager.nextLevel(); // 6
        levelManager.nextLevel(); // 7
        levelManager.nextLevel(); // 8
        
        // 无尽模式不应抛出异常
        levelManager.spawnEndlessLevel();
        scene.update(0.016f);
        
        // 应该生成一些敌人
        assertTrue(scene.getGameObjects().size() > 0);
    }
}
