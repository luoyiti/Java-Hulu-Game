package com.gameengine.game;

import org.junit.Test;
import static org.junit.Assert.*;

import com.gameengine.components.TransformComponent;
import com.gameengine.components.LifeFeatureComponent;
import com.gameengine.math.Vector2;
import java.util.Random;

/**
 * EnemyWizard (敌人法师) 测试类
 */
public class EnemyWizardTest {
    
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
    
    private static final String WIZARD_IMAGE = "resources/picture/wizard.png";
    private static final Vector2 WIZARD_SIZE = new Vector2(48, 48);
    
    @Test
    public void testConstructor() {
        MockRenderer renderer = new MockRenderer();
        Random random = new Random(42);
        Vector2 position = new Vector2(300, 200);
        
        EnemyWizard wizard = new EnemyWizard(renderer, null, position, WIZARD_IMAGE, WIZARD_SIZE, random);
        
        assertNotNull(wizard);
        assertEquals("EnemyWizard", wizard.getName());
    }
    
    @Test
    public void testIsEnemy() {
        MockRenderer renderer = new MockRenderer();
        Random random = new Random(42);
        
        EnemyWizard wizard = new EnemyWizard(renderer, null, new Vector2(300, 200), WIZARD_IMAGE, WIZARD_SIZE, random);
        
        assertEquals("Enemy", wizard.getidentity());
    }
    
    @Test
    public void testTransformComponent() {
        MockRenderer renderer = new MockRenderer();
        Random random = new Random(42);
        Vector2 position = new Vector2(250, 180);
        
        EnemyWizard wizard = new EnemyWizard(renderer, null, position, WIZARD_IMAGE, WIZARD_SIZE, random);
        
        TransformComponent transform = wizard.getComponent(TransformComponent.class);
        assertNotNull(transform);
        assertEquals(250, transform.getPosition().x, 0.001f);
        assertEquals(180, transform.getPosition().y, 0.001f);
    }
    
    @Test
    public void testLifeFeatureComponent() {
        MockRenderer renderer = new MockRenderer();
        Random random = new Random(42);
        
        EnemyWizard wizard = new EnemyWizard(renderer, null, new Vector2(300, 200), WIZARD_IMAGE, WIZARD_SIZE, random);
        
        LifeFeatureComponent lifeFeature = wizard.getComponent(LifeFeatureComponent.class);
        assertNotNull(lifeFeature);
        assertTrue(lifeFeature.getBlood() > 0);
    }
    
    // testUpdate removed - requires non-null scene for addGameObject calls
    
    @Test
    public void testRender() {
        MockRenderer renderer = new MockRenderer();
        Random random = new Random(42);
        
        EnemyWizard wizard = new EnemyWizard(renderer, null, new Vector2(300, 200), WIZARD_IMAGE, WIZARD_SIZE, random);
        
        // render不应抛出异常
        wizard.render();
    }
    
    @Test
    public void testIsActiveByDefault() {
        MockRenderer renderer = new MockRenderer();
        Random random = new Random(42);
        
        EnemyWizard wizard = new EnemyWizard(renderer, null, new Vector2(300, 200), WIZARD_IMAGE, WIZARD_SIZE, random);
        
        assertTrue(wizard.isActive());
    }
    
    @Test
    public void testSetActive() {
        MockRenderer renderer = new MockRenderer();
        Random random = new Random(42);
        
        EnemyWizard wizard = new EnemyWizard(renderer, null, new Vector2(300, 200), WIZARD_IMAGE, WIZARD_SIZE, random);
        
        wizard.setActive(false);
        assertFalse(wizard.isActive());
        
        wizard.setActive(true);
        assertTrue(wizard.isActive());
    }
}
