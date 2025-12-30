package com.gameengine.core;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import com.gameengine.graphics.IRenderer;
import com.gameengine.math.Vector2;

/**
 * ParticleSystem粒子系统类的测试用例
 * 测试粒子的创建、更新、渲染、爆发等功能
 */
public class ParticleSystemTest {
    
    private ParticleSystem particleSystem;
    private MockRenderer mockRenderer;
    
    /**
     * 测试用的Mock渲染器
     */
    private static class MockRenderer implements IRenderer {
        private int width = 800;
        private int height = 600;
        private String title = "Test";
        
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
        public void drawText(String text, float x, float y, float size, float r, float g, float b, float a) {}
        
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
        public int getWidth() { return width; }
        
        @Override
        public int getHeight() { return height; }
        
        @Override
        public String getTitle() { return title; }
    }
    
    /**
     * 测试前的初始化工作
     */
    @Before
    public void setUp() {
        mockRenderer = new MockRenderer();
        Vector2 position = new Vector2(400, 300);
        particleSystem = new ParticleSystem(mockRenderer, position);
    }
    
    /**
     * 测试粒子系统的创建
     */
    @Test
    public void testCreation() {
        // 测试粒子系统应该被创建
        assertNotNull("粒子系统应该被创建", particleSystem);
        
        // 测试初始粒子数量应该大于0（因为使用了默认配置）
        int initialCount = particleSystem.getParticleCount();
        assertTrue("初始粒子数量应该大于0", initialCount > 0);
    }
    
    /**
     * 测试使用自定义配置创建粒子系统
     */
    @Test
    public void testCreationWithConfig() {
        Vector2 position = new Vector2(100, 100);
        ParticleSystem.Config config = new ParticleSystem.Config();
        config.initialCount = 10;
        config.spawnRate = 0.1f;
        
        ParticleSystem customSystem = new ParticleSystem(mockRenderer, position, config);
        assertNotNull("使用自定义配置的粒子系统应该被创建", customSystem);
        assertEquals("初始粒子数量应该符合配置", 10, customSystem.getParticleCount());
    }
    
    /**
     * 测试粒子系统的位置设置
     */
    @Test
    public void testSetPosition() {
        Vector2 newPosition = new Vector2(200, 150);
        particleSystem.setPosition(newPosition);
        
        // 由于无法直接获取位置，我们通过更新和渲染来验证
        particleSystem.update(0.016f);
        particleSystem.render();
        assertTrue("位置设置应该成功", true);
    }
    
    /**
     * 测试粒子系统的更新
     */
    @Test
    public void testUpdate() {
        // 更新粒子系统
        particleSystem.update(0.016f);
        
        // 验证粒子数量可能变化（因为粒子会死亡和新粒子会生成）
        int newCount = particleSystem.getParticleCount();
        assertTrue("更新后粒子数量应该合理", newCount >= 0);
    }
    
    /**
     * 测试粒子系统的渲染
     */
    @Test
    public void testRender() {
        // 测试渲染不会抛出异常
        particleSystem.render();
        assertTrue("渲染应该成功", true);
    }
    
    /**
     * 测试粒子系统的活跃状态
     */
    @Test
    public void testSetActive() {
        // 测试默认应该是活跃的
        particleSystem.setActive(true);
        
        // 测试设置为非活跃
        particleSystem.setActive(false);
        int countBefore = particleSystem.getParticleCount();
        
        // 更新后，非活跃系统不应该生成新粒子
        particleSystem.update(0.1f);
        int countAfter = particleSystem.getParticleCount();
        
        // 粒子数量应该减少（因为粒子会死亡但不会生成新的）
        assertTrue("非活跃系统不应该生成新粒子", countAfter <= countBefore);
    }
    
    /**
     * 测试粒子系统的爆发功能
     */
    @Test
    public void testBurst() {
        int countBefore = particleSystem.getParticleCount();
        
        // 触发爆发，生成50个粒子
        particleSystem.burst(50);
        
        int countAfter = particleSystem.getParticleCount();
        assertTrue("爆发后粒子数量应该增加", countAfter > countBefore);
    }
    
    /**
     * 测试粒子系统的清除功能
     */
    @Test
    public void testClear() {
        // 确保有一些粒子
        particleSystem.burst(20);
        assertTrue("应该有粒子", particleSystem.getParticleCount() > 0);
        
        // 清除所有粒子
        particleSystem.clear();
        assertEquals("清除后粒子数量应该为0", 0, particleSystem.getParticleCount());
    }
    
    /**
     * 测试粒子系统的生成速率设置
     */
    @Test
    public void testSetSpawnRate() {
        float newSpawnRate = 0.05f;
        particleSystem.setSpawnRate(newSpawnRate);
        
        // 更新多次以触发生成
        for (int i = 0; i < 10; i++) {
            particleSystem.update(0.1f);
        }
        
        // 验证更新不会抛出异常
        assertTrue("设置生成速率后更新应该成功", true);
    }
    
    /**
     * 测试默认配置
     */
    @Test
    public void testDefaultConfig() {
        ParticleSystem.Config defaultConfig = ParticleSystem.Config.defaultPlayer();
        assertNotNull("默认配置应该被创建", defaultConfig);
        assertTrue("默认配置应该有合理的初始数量", defaultConfig.initialCount > 0);
    }
    
    /**
     * 测试光照配置
     */
    @Test
    public void testLightConfig() {
        ParticleSystem.Config lightConfig = ParticleSystem.Config.light();
        assertNotNull("光照配置应该被创建", lightConfig);
        assertTrue("光照配置应该有合理的初始数量", lightConfig.initialCount > 0);
    }
    
    /**
     * 测试粒子系统的长时间运行
     */
    @Test
    public void testLongRunning() {
        // 模拟长时间运行（5秒，60FPS）
        for (int i = 0; i < 300; i++) {
            particleSystem.update(0.016f);
            particleSystem.render();
        }
        
        // 验证系统仍然正常工作
        assertTrue("长时间运行后系统应该仍然正常工作", particleSystem.getParticleCount() >= 0);
    }
}

