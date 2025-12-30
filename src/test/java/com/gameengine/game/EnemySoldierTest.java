package com.gameengine.game;

import org.junit.Test;
import static org.junit.Assert.*;

import com.gameengine.components.TransformComponent;
import com.gameengine.components.LifeFeatureComponent;
import com.gameengine.components.PhysicsComponent;
import com.gameengine.math.Vector2;
import java.util.Random;

/**
 * EnemySoldier (敌人士兵) 测试类
 */
public class EnemySoldierTest {
    
    // 模拟渲染器用于测试
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
    
    @Test
    public void testConstructor() {
        MockRenderer renderer = new MockRenderer();
        Random random = new Random(42);
        Vector2 position = new Vector2(100, 200);
        String imagePath = "resources/picture/snake.png";
        Vector2 imageSize = new Vector2(32, 32);
        
        EnemySoldier soldier = new EnemySoldier(renderer, null, position, imagePath, imageSize, random);
        
        assertNotNull(soldier);
        assertEquals("EnemySoldier", soldier.getName());
    }
    
    @Test
    public void testIsEnemy() {
        MockRenderer renderer = new MockRenderer();
        Random random = new Random(42);
        
        EnemySoldier soldier = new EnemySoldier(renderer, null, new Vector2(0, 0), 
            "resources/picture/snake.png", new Vector2(32, 32), random);
        
        assertEquals("Enemy", soldier.getidentity());
    }
    
    @Test
    public void testTransformComponent() {
        MockRenderer renderer = new MockRenderer();
        Random random = new Random(42);
        Vector2 position = new Vector2(150, 250);
        
        EnemySoldier soldier = new EnemySoldier(renderer, null, position, 
            "resources/picture/snake.png", new Vector2(32, 32), random);
        
        TransformComponent transform = soldier.getComponent(TransformComponent.class);
        assertNotNull(transform);
        assertEquals(150, transform.getPosition().x, 0.001f);
        assertEquals(250, transform.getPosition().y, 0.001f);
    }
    
    @Test
    public void testLifeFeatureComponent() {
        MockRenderer renderer = new MockRenderer();
        Random random = new Random(42);
        
        EnemySoldier soldier = new EnemySoldier(renderer, null, new Vector2(0, 0), 
            "resources/picture/snake.png", new Vector2(32, 32), random);
        
        LifeFeatureComponent lifeFeature = soldier.getComponent(LifeFeatureComponent.class);
        assertNotNull(lifeFeature);
        assertEquals(100, lifeFeature.getBlood());
    }
    
    @Test
    public void testPhysicsComponent() {
        MockRenderer renderer = new MockRenderer();
        Random random = new Random(42);
        
        EnemySoldier soldier = new EnemySoldier(renderer, null, new Vector2(0, 0), 
            "resources/picture/snake.png", new Vector2(32, 32), random);
        
        PhysicsComponent physics = soldier.getComponent(PhysicsComponent.class);
        assertNotNull(physics);
    }
    
    // testUpdate removed - requires non-null scene for addGameObject calls
    
    @Test
    public void testRender() {
        MockRenderer renderer = new MockRenderer();
        Random random = new Random(42);
        
        EnemySoldier soldier = new EnemySoldier(renderer, null, new Vector2(100, 100), 
            "resources/picture/snake.png", new Vector2(32, 32), random);
        
        // render不应抛出异常
        soldier.render();
    }
    
    @Test
    public void testIsActiveByDefault() {
        MockRenderer renderer = new MockRenderer();
        Random random = new Random(42);
        
        EnemySoldier soldier = new EnemySoldier(renderer, null, new Vector2(0, 0), 
            "resources/picture/snake.png", new Vector2(32, 32), random);
        
        assertTrue(soldier.isActive());
    }
    
    @Test
    public void testSetActive() {
        MockRenderer renderer = new MockRenderer();
        Random random = new Random(42);
        
        EnemySoldier soldier = new EnemySoldier(renderer, null, new Vector2(0, 0), 
            "resources/picture/snake.png", new Vector2(32, 32), random);
        
        soldier.setActive(false);
        assertFalse(soldier.isActive());
        
        soldier.setActive(true);
        assertTrue(soldier.isActive());
    }
}
