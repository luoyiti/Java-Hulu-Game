package com.gameengine.level;

import org.junit.Test;
import org.junit.Before;
import static org.junit.Assert.*;

import com.gameengine.core.GameObject;
import com.gameengine.math.Vector2;
import com.gameengine.scene.Scene;

/**
 * EnemyFactory 敌人工厂测试类
 */
public class EnemyFactoryTest {
    
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
    private EnemyFactory factory;
    
    @Before
    public void setUp() {
        renderer = new MockRenderer();
        scene = new Scene("TestScene");
        factory = new EnemyFactory(renderer, scene, null);
    }
    
    @Test
    public void testCreateSoldier() {
        GameObject enemy = factory.createEnemy(LevelConfig.EnemyType.SOLDIER);
        scene.update(0.016f);
        
        assertNotNull(enemy);
        assertEquals("EnemySoldier", enemy.getName());
        assertTrue(scene.getGameObjects().size() >= 1);
    }
    
    @Test
    public void testCreateWizard() {
        GameObject enemy = factory.createEnemy(LevelConfig.EnemyType.WIZARD);
        scene.update(0.016f);
        
        assertNotNull(enemy);
        assertEquals("EnemyWizard", enemy.getName());
        assertTrue(scene.getGameObjects().size() >= 1);
    }
    
    @Test
    public void testCreateKing() {
        GameObject enemy = factory.createEnemy(LevelConfig.EnemyType.KING);
        scene.update(0.016f);
        
        assertNotNull(enemy);
        assertEquals("EnemyKing", enemy.getName());
        assertTrue(scene.getGameObjects().size() >= 1);
    }
    
    @Test
    public void testCreatedEnemyIsEnemy() {
        GameObject soldier = factory.createEnemy(LevelConfig.EnemyType.SOLDIER);
        assertEquals("Enemy", soldier.getidentity());
        
        GameObject wizard = factory.createEnemy(LevelConfig.EnemyType.WIZARD);
        assertEquals("Enemy", wizard.getidentity());
        
        GameObject king = factory.createEnemy(LevelConfig.EnemyType.KING);
        assertEquals("Enemy", king.getidentity());
    }
    
    @Test
    public void testCreateMultipleEnemies() {
        factory.createEnemy(LevelConfig.EnemyType.SOLDIER);
        factory.createEnemy(LevelConfig.EnemyType.WIZARD);
        factory.createEnemy(LevelConfig.EnemyType.KING);
        scene.update(0.016f);
        
        assertTrue(scene.getGameObjects().size() >= 3);
    }
    
    @Test
    public void testEnemiesHavePositions() {
        GameObject soldier = factory.createEnemy(LevelConfig.EnemyType.SOLDIER);
        
        var transform = soldier.getComponent(com.gameengine.components.TransformComponent.class);
        assertNotNull(transform);
        assertNotNull(transform.getPosition());
    }
    
    @Test
    public void testEnemiesHaveLifeFeature() {
        GameObject soldier = factory.createEnemy(LevelConfig.EnemyType.SOLDIER);
        
        var lifeFeature = soldier.getComponent(com.gameengine.components.LifeFeatureComponent.class);
        assertNotNull(lifeFeature);
        assertTrue(lifeFeature.getBlood() > 0);
    }
}
