package com.gameengine.game;

import org.junit.Test;
import static org.junit.Assert.*;

import com.gameengine.components.TransformComponent;
import com.gameengine.components.RenderComponent;
import com.gameengine.math.Vector2;

/**
 * Tree 装饰类测试
 */
public class TreeTest {
    
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
        Tree tree = new Tree(renderer, new Vector2(100, 200));
        
        assertNotNull(tree);
        assertEquals("tree", tree.getName());
    }
    
    @Test
    public void testTransformComponent() {
        MockRenderer renderer = new MockRenderer();
        Tree tree = new Tree(renderer, new Vector2(150, 250));
        
        TransformComponent transform = tree.getComponent(TransformComponent.class);
        assertNotNull(transform);
        assertEquals(150, transform.getPosition().x, 0.001f);
        assertEquals(250, transform.getPosition().y, 0.001f);
    }
    
    @Test
    public void testRenderComponent() {
        MockRenderer renderer = new MockRenderer();
        Tree tree = new Tree(renderer, new Vector2(100, 200));
        
        RenderComponent render = tree.getComponent(RenderComponent.class);
        assertNotNull(render);
    }
    
    @Test
    public void testUpdate() {
        MockRenderer renderer = new MockRenderer();
        Tree tree = new Tree(renderer, new Vector2(100, 200));
        // update不应抛出异常
        tree.update(0.016f);
    }
    
    @Test
    public void testRender() {
        MockRenderer renderer = new MockRenderer();
        Tree tree = new Tree(renderer, new Vector2(100, 200));
        // render不应抛出异常
        tree.render();
    }
    
    @Test
    public void testDifferentPositions() {
        MockRenderer renderer = new MockRenderer();
        Tree tree1 = new Tree(renderer, new Vector2(0, 0));
        Tree tree2 = new Tree(renderer, new Vector2(500, 300));
        Tree tree3 = new Tree(renderer, new Vector2(-100, -200));
        
        assertEquals(0, tree1.getComponent(TransformComponent.class).getPosition().x, 0.001f);
        assertEquals(500, tree2.getComponent(TransformComponent.class).getPosition().x, 0.001f);
        assertEquals(-100, tree3.getComponent(TransformComponent.class).getPosition().x, 0.001f);
    }
    
    @Test
    public void testIsActiveByDefault() {
        MockRenderer renderer = new MockRenderer();
        Tree tree = new Tree(renderer, new Vector2(100, 200));
        assertTrue(tree.isActive());
    }
    
    @Test
    public void testSetActive() {
        MockRenderer renderer = new MockRenderer();
        Tree tree = new Tree(renderer, new Vector2(100, 200));
        tree.setActive(false);
        assertFalse(tree.isActive());
        
        tree.setActive(true);
        assertTrue(tree.isActive());
    }
}
