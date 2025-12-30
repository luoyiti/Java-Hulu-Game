package com.gameengine.core;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import com.gameengine.graphics.IRenderer;

/**
 * GamePerformance游戏性能类的测试用例
 * 测试FPS计算、性能统计等功能
 */
public class GamePerformanceTest {
    
    private GamePerformance gamePerformance;
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
        gamePerformance = new GamePerformance();
        mockRenderer = new MockRenderer();
    }
    
    /**
     * 测试FPS的初始值
     */
    @Test
    public void testInitialFPS() {
        // 测试初始FPS应该为0
        assertEquals("初始FPS应该为0", 0.0f, gamePerformance.getFPS(), 0.000001f);
    }
    
    /**
     * 测试FPS的更新
     */
    @Test
    public void testUpdateFPS() {
        // 模拟60FPS的情况（每帧16.67ms）
        float deltaTime = 1.0f / 60.0f; // 约0.0167秒
        
        // 更新61次以确保累计时间超过1.0秒（61 * 0.0167 ≈ 1.018秒）
        for (int i = 0; i < 61; i++) {
            gamePerformance.update(deltaTime);
        }
        
        // 验证FPS应该接近60（因为FPS只在累计时间>=1秒时计算）
        float fps = gamePerformance.getFPS();
        assertTrue("FPS应该接近60，实际值: " + fps, fps > 55.0f && fps < 65.0f);
    }
    
    /**
     * 测试FPS的渲染
     */
    @Test
    public void testRenderFPS() {
        // 更新足够次数以确保累计时间>=1秒，从而产生FPS值
        // 0.016秒 * 63次 ≈ 1.008秒
        for (int i = 0; i < 63; i++) {
            gamePerformance.update(0.016f);
        }
        
        // 验证FPS已经被计算（不为0）
        float fps = gamePerformance.getFPS();
        assertTrue("FPS应该被计算（不为0），实际值: " + fps, fps > 0.0f);
        
        // 测试渲染不会抛出异常
        gamePerformance.render(mockRenderer);
        assertTrue("渲染应该成功", true);
    }
    
    /**
     * 测试性能统计的打印
     */
    @Test
    public void testPrintSummary() {
        // 模拟一些帧（确保有足够的数据用于统计）
        // 0.016秒 * 63次 ≈ 1.008秒
        for (int i = 0; i < 63; i++) {
            gamePerformance.update(0.016f);
        }
        
        // 测试打印统计信息不会抛出异常
        gamePerformance.printSummary();
        assertTrue("打印统计信息应该成功", true);
    }
    
    /**
     * 测试不同帧率下的FPS计算
     */
    @Test
    public void testDifferentFrameRates() {
        // 测试30FPS
        // 31次 * (1/30)秒 ≈ 1.033秒，确保超过1秒阈值
        float deltaTime30 = 1.0f / 30.0f;
        for (int i = 0; i < 31; i++) {
            gamePerformance.update(deltaTime30);
        }
        float fps30 = gamePerformance.getFPS();
        assertTrue("30FPS应该被正确计算，实际值: " + fps30, fps30 > 28.0f && fps30 < 32.0f);
        
        // 重置
        gamePerformance = new GamePerformance();
        
        // 测试120FPS
        // 121次 * (1/120)秒 ≈ 1.008秒，确保超过1秒阈值
        float deltaTime120 = 1.0f / 120.0f;
        for (int i = 0; i < 121; i++) {
            gamePerformance.update(deltaTime120);
        }
        float fps120 = gamePerformance.getFPS();
        assertTrue("120FPS应该被正确计算，实际值: " + fps120, fps120 > 115.0f && fps120 < 125.0f);
    }
    
    /**
     * 测试长时间运行的性能统计
     */
    @Test
    public void testLongRunningPerformance() {
        // 模拟运行5秒（60FPS）
        float deltaTime = 1.0f / 60.0f;
        int totalFrames = 60 * 5; // 5秒的帧数（300帧）
        
        for (int i = 0; i < totalFrames; i++) {
            gamePerformance.update(deltaTime);
        }
        
        // 验证FPS仍然接近60
        // 注意：由于FPS是每1秒计算一次，最后一次计算会基于最后一个1秒窗口的帧数
        float fps = gamePerformance.getFPS();
        assertTrue("长时间运行后FPS应该仍然接近60，实际值: " + fps, fps > 55.0f && fps < 65.0f);
        
        // 测试打印统计信息
        gamePerformance.printSummary();
        assertTrue("长时间运行后打印统计信息应该成功", true);
    }
}

