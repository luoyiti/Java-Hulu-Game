package com.gameengine.graphics;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

/**
 * IRenderer接口的测试用例
 * 测试渲染器接口的所有方法，包括基本绘制功能、状态管理等
 */
public class IRendererTest {
    
    private MockRenderer mockRenderer;
    
    /**
     * 测试前的初始化工作
     */
    @Before
    public void setUp() {
        mockRenderer = new MockRenderer();
    }
    
    /**
     * 测试接口的基本方法调用
     */
    @Test
    public void testBasicMethods() {
        // 测试 beginFrame 和 endFrame
        mockRenderer.beginFrame();
        assertTrue("beginFrame 应该被调用", mockRenderer.beginFrameCalled);
        
        mockRenderer.endFrame();
        assertTrue("endFrame 应该被调用", mockRenderer.endFrameCalled);
        
        // 测试 shouldClose
        boolean closeResult = mockRenderer.shouldClose();
        assertFalse("默认情况下窗口不应该关闭", closeResult);
        
        // 测试 pollEvents
        mockRenderer.pollEvents();
        assertTrue("pollEvents 应该被调用", mockRenderer.pollEventsCalled);
        
        // 测试 cleanup
        mockRenderer.cleanup();
        assertTrue("cleanup 应该被调用", mockRenderer.cleanupCalled);
    }
    
    /**
     * 测试绘制矩形方法
     */
    @Test
    public void testDrawRect() {
        mockRenderer.drawRect(10, 20, 100, 50, 1.0f, 0.0f, 0.0f, 1.0f);
        
        assertTrue("drawRect 应该被调用", mockRenderer.drawRectCalled);
        assertEquals("X 坐标应该正确", 10, mockRenderer.lastRectX, 0.001);
        assertEquals("Y 坐标应该正确", 20, mockRenderer.lastRectY, 0.001);
        assertEquals("宽度应该正确", 100, mockRenderer.lastRectWidth, 0.001);
        assertEquals("高度应该正确", 50, mockRenderer.lastRectHeight, 0.001);
        assertEquals("红色分量应该正确", 1.0f, mockRenderer.lastRectR, 0.001);
        assertEquals("绿色分量应该正确", 0.0f, mockRenderer.lastRectG, 0.001);
        assertEquals("蓝色分量应该正确", 0.0f, mockRenderer.lastRectB, 0.001);
        assertEquals("透明度应该正确", 1.0f, mockRenderer.lastRectA, 0.001);
    }
    
    /**
     * 测试绘制圆形方法
     */
    @Test
    public void testDrawCircle() {
        mockRenderer.drawCircle(50, 60, 30, 32, 0.0f, 1.0f, 0.0f, 0.8f);
        
        assertTrue("drawCircle 应该被调用", mockRenderer.drawCircleCalled);
        assertEquals("X 坐标应该正确", 50, mockRenderer.lastCircleX, 0.001);
        assertEquals("Y 坐标应该正确", 60, mockRenderer.lastCircleY, 0.001);
        assertEquals("半径应该正确", 30, mockRenderer.lastCircleRadius, 0.001);
        assertEquals("段数应该正确", 32, mockRenderer.lastCircleSegments);
        assertEquals("绿色分量应该正确", 1.0f, mockRenderer.lastCircleG, 0.001);
        assertEquals("透明度应该正确", 0.8f, mockRenderer.lastCircleA, 0.001);
    }
    
    /**
     * 测试绘制线条方法
     */
    @Test
    public void testDrawLine() {
        mockRenderer.drawLine(0, 0, 100, 100, 0.5f, 0.5f, 0.5f, 1.0f);
        
        assertTrue("drawLine 应该被调用", mockRenderer.drawLineCalled);
        assertEquals("起点 X 坐标应该正确", 0, mockRenderer.lastLineX1, 0.001);
        assertEquals("起点 Y 坐标应该正确", 0, mockRenderer.lastLineY1, 0.001);
        assertEquals("终点 X 坐标应该正确", 100, mockRenderer.lastLineX2, 0.001);
        assertEquals("终点 Y 坐标应该正确", 100, mockRenderer.lastLineY2, 0.001);
        assertEquals("颜色分量应该正确", 0.5f, mockRenderer.lastLineR, 0.001);
    }
    
    /**
     * 测试绘制文字方法
     */
    @Test
    public void testDrawText() {
        mockRenderer.drawText("Hello World", 25, 35, 20, 1.0f, 1.0f, 1.0f, 1.0f);
        
        assertTrue("drawText 应该被调用", mockRenderer.drawTextCalled);
        assertEquals("文字内容应该正确", "Hello World", mockRenderer.lastText);
        assertEquals("X 坐标应该正确", 25, mockRenderer.lastTextX, 0.001);
        assertEquals("Y 坐标应该正确", 35, mockRenderer.lastTextY, 0.001);
        assertEquals("字体大小应该正确", 20, mockRenderer.lastTextSize, 0.001);
    }
    
    /**
     * 测试绘制血条方法
     */
    @Test
    public void testDrawHealthBar() {
        mockRenderer.drawHealthBar(10, 10, 100, 10, 75, 100);
        
        assertTrue("drawHealthBar 应该被调用", mockRenderer.drawHealthBarCalled);
        assertEquals("X 坐标应该正确", 10, mockRenderer.lastHealthBarX, 0.001);
        assertEquals("Y 坐标应该正确", 10, mockRenderer.lastHealthBarY, 0.001);
        assertEquals("宽度应该正确", 100, mockRenderer.lastHealthBarWidth, 0.001);
        assertEquals("高度应该正确", 10, mockRenderer.lastHealthBarHeight, 0.001);
        assertEquals("当前血量应该正确", 75, mockRenderer.lastHealthBarCurrent);
        assertEquals("最大血量应该正确", 100, mockRenderer.lastHealthBarMax);
    }
    
    /**
     * 测试绘制图片方法
     */
    @Test
    public void testDrawImage() {
        mockRenderer.drawImage("test.png", 10, 20, 50, 50, 0.8f);
        
        assertTrue("drawImage 应该被调用", mockRenderer.drawImageCalled);
        assertEquals("图片路径应该正确", "test.png", mockRenderer.lastImagePath);
        assertEquals("X 坐标应该正确", 10, mockRenderer.lastImageX, 0.001);
        assertEquals("Y 坐标应该正确", 20, mockRenderer.lastImageY, 0.001);
        assertEquals("宽度应该正确", 50, mockRenderer.lastImageWidth, 0.001);
        assertEquals("高度应该正确", 50, mockRenderer.lastImageHeight, 0.001);
        assertEquals("透明度应该正确", 0.8f, mockRenderer.lastImageAlpha, 0.001);
    }
    
    /**
     * 测试绘制旋转图片方法
     */
    @Test
    public void testDrawImageRotated() {
        float rotation = (float) (Math.PI / 4); // 45度
        mockRenderer.drawImageRotated("test.png", 50, 60, 40, 40, rotation, 1.0f);
        
        assertTrue("drawImageRotated 应该被调用", mockRenderer.drawImageRotatedCalled);
        assertEquals("图片路径应该正确", "test.png", mockRenderer.lastRotatedImagePath);
        assertEquals("中心 X 坐标应该正确", 50, mockRenderer.lastRotatedImageX, 0.001);
        assertEquals("中心 Y 坐标应该正确", 60, mockRenderer.lastRotatedImageY, 0.001);
        assertEquals("宽度应该正确", 40, mockRenderer.lastRotatedImageWidth, 0.001);
        assertEquals("高度应该正确", 40, mockRenderer.lastRotatedImageHeight, 0.001);
        assertEquals("旋转角度应该正确", rotation, mockRenderer.lastRotatedImageRotation, 0.001);
        assertEquals("透明度应该正确", 1.0f, mockRenderer.lastRotatedImageAlpha, 0.001);
    }
    
    /**
     * 测试获取窗口属性
     */
    @Test
    public void testGetWindowProperties() {
        assertEquals("宽度应该正确", 800, mockRenderer.getWidth());
        assertEquals("高度应该正确", 600, mockRenderer.getHeight());
        assertEquals("标题应该正确", "Test Renderer", mockRenderer.getTitle());
    }
    
    /**
     * 测试边界值和异常情况
     */
    @Test
    public void testBoundaryValues() {
        // 测试零值
        mockRenderer.drawRect(0, 0, 0, 0, 0, 0, 0, 0);
        assertTrue("零值矩形应该被处理", mockRenderer.drawRectCalled);
        
        // 测试负值
        mockRenderer.drawRect(-10, -20, -50, -30, 1.0f, 1.0f, 1.0f, 1.0f);
        assertTrue("负值矩形应该被处理", mockRenderer.drawRectCalled);
        
        // 测试大数值
        mockRenderer.drawRect(10000, 20000, 5000, 3000, 1.0f, 1.0f, 1.0f, 1.0f);
        assertTrue("大数值矩形应该被处理", mockRenderer.drawRectCalled);
    }
    
    /**
     * 测试多次调用
     */
    @Test
    public void testMultipleCalls() {
        // 重置计数器
        mockRenderer.resetCounters();
        
        // 多次调用各种方法
        for (int i = 0; i < 5; i++) {
            mockRenderer.drawRect(i, i, 10, 10, 1, 1, 1, 1);
            mockRenderer.drawCircle(i * 10, i * 10, 5, 8, 1, 1, 1, 1);
            mockRenderer.drawLine(i, i, i + 10, i + 10, 1, 1, 1, 1);
            mockRenderer.drawText("Test", i, i, 12, 1, 1, 1, 1);
            mockRenderer.drawHealthBar(i, i, 20, 5, 50, 100);
            mockRenderer.drawImage("test.png", i, i, 10, 10, 1);
            mockRenderer.drawImageRotated("test.png", i, i, 10, 10, 0, 1);
        }
        
        // 验证所有方法都被调用了5次
        assertEquals("drawRect 应该被调用5次", 5, mockRenderer.drawRectCallCount);
        assertEquals("drawCircle 应该被调用5次", 5, mockRenderer.drawCircleCallCount);
        assertEquals("drawLine 应该被调用5次", 5, mockRenderer.drawLineCallCount);
        assertEquals("drawText 应该被调用5次", 5, mockRenderer.drawTextCallCount);
        assertEquals("drawHealthBar 应该被调用5次", 5, mockRenderer.drawHealthBarCallCount);
        assertEquals("drawImage 应该被调用5次", 5, mockRenderer.drawImageCallCount);
        assertEquals("drawImageRotated 应该被调用5次", 5, mockRenderer.drawImageRotatedCallCount);
    }
    
    /**
     * 测试空字符串和null值处理
     */
    @Test
    public void testNullAndEmptyValues() {
        // 测试空字符串
        mockRenderer.drawText("", 0, 0, 10, 1, 1, 1, 1);
        assertTrue("空字符串应该被处理", mockRenderer.drawTextCalled);
        
        // 测试空图片路径
        mockRenderer.drawImage("", 0, 0, 10, 10, 1);
        assertTrue("空图片路径应该被处理", mockRenderer.drawImageCalled);
        
        // 测试null字符串（虽然接口不强制检查，但应该能处理）
        mockRenderer.drawText(null, 0, 0, 10, 1, 1, 1, 1);
        assertTrue("null字符串应该被处理", mockRenderer.drawTextCalled);
    }
    
    /**
     * MockRenderer 实现类
     * 用于测试 IRenderer 接口
     */
    private static class MockRenderer implements IRenderer {
        // 状态标志
        public boolean beginFrameCalled = false;
        public boolean endFrameCalled = false;
        public boolean pollEventsCalled = false;
        public boolean cleanupCalled = false;
        
        // 绘制方法调用标志
        public boolean drawRectCalled = false;
        public boolean drawCircleCalled = false;
        public boolean drawLineCalled = false;
        public boolean drawTextCalled = false;
        public boolean drawHealthBarCalled = false;
        public boolean drawImageCalled = false;
        public boolean drawImageRotatedCalled = false;
        
        // 调用计数器
        public int drawRectCallCount = 0;
        public int drawCircleCallCount = 0;
        public int drawLineCallCount = 0;
        public int drawTextCallCount = 0;
        public int drawHealthBarCallCount = 0;
        public int drawImageCallCount = 0;
        public int drawImageRotatedCallCount = 0;
        
        // 最后一次调用的参数
        public float lastRectX, lastRectY, lastRectWidth, lastRectHeight, lastRectR, lastRectG, lastRectB, lastRectA;
        public float lastCircleX, lastCircleY, lastCircleRadius, lastCircleR, lastCircleG, lastCircleB, lastCircleA;
        public int lastCircleSegments;
        public float lastLineX1, lastLineY1, lastLineX2, lastLineY2, lastLineR, lastLineG, lastLineB, lastLineA;
        public String lastText;
        public float lastTextX, lastTextY, lastTextSize, lastTextR, lastTextG, lastTextB, lastTextA;
        public float lastHealthBarX, lastHealthBarY, lastHealthBarWidth, lastHealthBarHeight;
        public int lastHealthBarCurrent, lastHealthBarMax;
        public String lastImagePath;
        public float lastImageX, lastImageY, lastImageWidth, lastImageHeight, lastImageAlpha;
        public String lastRotatedImagePath;
        public float lastRotatedImageX, lastRotatedImageY, lastRotatedImageWidth, lastRotatedImageHeight, lastRotatedImageRotation, lastRotatedImageAlpha;
        
        @Override
        public void beginFrame() {
            beginFrameCalled = true;
        }
        
        @Override
        public void endFrame() {
            endFrameCalled = true;
        }
        
        @Override
        public void drawRect(float x, float y, float width, float height, float r, float g, float b, float a) {
            drawRectCalled = true;
            drawRectCallCount++;
            lastRectX = x;
            lastRectY = y;
            lastRectWidth = width;
            lastRectHeight = height;
            lastRectR = r;
            lastRectG = g;
            lastRectB = b;
            lastRectA = a;
        }
        
        @Override
        public void drawCircle(float x, float y, float radius, int segments, float r, float g, float b, float a) {
            drawCircleCalled = true;
            drawCircleCallCount++;
            lastCircleX = x;
            lastCircleY = y;
            lastCircleRadius = radius;
            lastCircleSegments = segments;
            lastCircleR = r;
            lastCircleG = g;
            lastCircleB = b;
            lastCircleA = a;
        }
        
        @Override
        public void drawLine(float x1, float y1, float x2, float y2, float r, float g, float b, float a) {
            drawLineCalled = true;
            drawLineCallCount++;
            lastLineX1 = x1;
            lastLineY1 = y1;
            lastLineX2 = x2;
            lastLineY2 = y2;
            lastLineR = r;
            lastLineG = g;
            lastLineB = b;
            lastLineA = a;
        }
        
        @Override
        public void drawText(String text, float x, float y, float size, float r, float g, float b, float a) {
            drawTextCalled = true;
            drawTextCallCount++;
            lastText = text;
            lastTextX = x;
            lastTextY = y;
            lastTextSize = size;
            lastTextR = r;
            lastTextG = g;
            lastTextB = b;
            lastTextA = a;
        }
        
        @Override
        public void drawHealthBar(float x, float y, float width, float height, int currentHealth, int maxHealth) {
            drawHealthBarCalled = true;
            drawHealthBarCallCount++;
            lastHealthBarX = x;
            lastHealthBarY = y;
            lastHealthBarWidth = width;
            lastHealthBarHeight = height;
            lastHealthBarCurrent = currentHealth;
            lastHealthBarMax = maxHealth;
        }
        
        @Override
        public void drawImage(String imagePath, float x, float y, float width, float height, float alpha) {
            drawImageCalled = true;
            drawImageCallCount++;
            lastImagePath = imagePath;
            lastImageX = x;
            lastImageY = y;
            lastImageWidth = width;
            lastImageHeight = height;
            lastImageAlpha = alpha;
        }
        
        @Override
        public void drawImageRotated(String imagePath, float x, float y, float width, float height, float rotation, float alpha) {
            drawImageRotatedCalled = true;
            drawImageRotatedCallCount++;
            lastRotatedImagePath = imagePath;
            lastRotatedImageX = x;
            lastRotatedImageY = y;
            lastRotatedImageWidth = width;
            lastRotatedImageHeight = height;
            lastRotatedImageRotation = rotation;
            lastRotatedImageAlpha = alpha;
        }
        
        @Override
        public boolean shouldClose() {
            return false;
        }
        
        @Override
        public void pollEvents() {
            pollEventsCalled = true;
        }
        
        @Override
        public void cleanup() {
            cleanupCalled = true;
        }
        
        @Override
        public int getWidth() {
            return 800;
        }
        
        @Override
        public int getHeight() {
            return 600;
        }
        
        @Override
        public String getTitle() {
            return "Test Renderer";
        }
        
        /**
         * 重置所有计数器和标志
         */
        public void resetCounters() {
            drawRectCallCount = 0;
            drawCircleCallCount = 0;
            drawLineCallCount = 0;
            drawTextCallCount = 0;
            drawHealthBarCallCount = 0;
            drawImageCallCount = 0;
            drawImageRotatedCallCount = 0;
        }
    }
}

