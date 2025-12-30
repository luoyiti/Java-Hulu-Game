package com.gameengine.components;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import com.gameengine.core.GameObject;
import com.gameengine.components.TransformComponent;
import com.gameengine.math.Vector2;
import com.gameengine.graphics.IRenderer;

/**
 * RenderComponent渲染组件类的测试用例
 * 测试渲染类型、颜色、尺寸、可见性、图片渲染等功能
 */
public class RenderComponentTest {
    
    private RenderComponent renderComponent;
    private GameObject owner;
    private TransformComponent transform;
    private MockRenderer mockRenderer;
    
    @Before
    public void setUp() {
        owner = new GameObject("TestObject");
        transform = new TransformComponent();
        owner.addComponent(transform);
        renderComponent = new RenderComponent();
        renderComponent.setOwner(owner);
        mockRenderer = new MockRenderer();
        renderComponent.setRenderer(mockRenderer);
    }
    
    /**
     * 测试默认构造函数
     */
    @Test
    public void testDefaultConstructor() {
        RenderComponent rc = new RenderComponent();
        Vector2 size = rc.getSize();
        RenderComponent.Color color = rc.getColor();
        
        assertEquals("默认渲染类型应该为RECTANGLE", RenderComponent.RenderType.RECTANGLE, rc.getRenderType());
        assertEquals("默认尺寸x应该为20", 20.0f, size.x, 0.000001f);
        assertEquals("默认尺寸y应该为20", 20.0f, size.y, 0.000001f);
        assertEquals("默认颜色r应该为1.0", 1.0f, color.r, 0.000001f);
        assertEquals("默认颜色g应该为1.0", 1.0f, color.g, 0.000001f);
        assertEquals("默认颜色b应该为1.0", 1.0f, color.b, 0.000001f);
        assertEquals("默认颜色a应该为1.0", 1.0f, color.a, 0.000001f);
        assertTrue("默认应该可见", rc.isVisible());
        assertNull("默认图片路径应该为null", rc.getImagePath());
        assertEquals("默认旋转应该为0", 0.0f, rc.getRotation(), 0.000001f);
        assertEquals("默认透明度应该为1.0", 1.0f, rc.getAlpha(), 0.000001f);
    }
    
    /**
     * 测试带参数的构造函数
     */
    @Test
    public void testConstructorWithParameters() {
        RenderComponent.RenderType type = RenderComponent.RenderType.CIRCLE;
        Vector2 size = new Vector2(50.0f, 50.0f);
        RenderComponent.Color color = new RenderComponent.Color(1.0f, 0.0f, 0.0f, 1.0f);
        
        RenderComponent rc = new RenderComponent(type, size, color);
        
        assertEquals("渲染类型应该正确", type, rc.getRenderType());
        Vector2 s = rc.getSize();
        assertEquals("尺寸x应该正确", 50.0f, s.x, 0.000001f);
        assertEquals("尺寸y应该正确", 50.0f, s.y, 0.000001f);
        RenderComponent.Color c = rc.getColor();
        assertEquals("颜色r应该正确", 1.0f, c.r, 0.000001f);
        assertEquals("颜色g应该正确", 0.0f, c.g, 0.000001f);
        assertEquals("颜色b应该正确", 0.0f, c.b, 0.000001f);
    }
    
    /**
     * 测试图片构造函数
     */
    @Test
    public void testImageConstructor() {
        String imagePath = "test.png";
        Vector2 size = new Vector2(100.0f, 100.0f);
        
        RenderComponent rc = new RenderComponent(imagePath, size);
        
        assertEquals("渲染类型应该为IMAGE", RenderComponent.RenderType.IMAGE, rc.getRenderType());
        assertEquals("图片路径应该正确", imagePath, rc.getImagePath());
        Vector2 s = rc.getSize();
        assertEquals("尺寸x应该正确", 100.0f, s.x, 0.000001f);
        assertEquals("尺寸y应该正确", 100.0f, s.y, 0.000001f);
    }
    
    /**
     * 测试图片构造函数（带透明度和旋转）
     */
    @Test
    public void testImageConstructorWithAlphaAndRotation() {
        String imagePath = "test.png";
        Vector2 size = new Vector2(100.0f, 100.0f);
        float alpha = 0.5f;
        float rotation = (float) Math.PI / 4;
        
        RenderComponent rc = new RenderComponent(imagePath, size, alpha, rotation);
        
        assertEquals("渲染类型应该为IMAGE", RenderComponent.RenderType.IMAGE, rc.getRenderType());
        assertEquals("图片路径应该正确", imagePath, rc.getImagePath());
        assertEquals("透明度应该正确", alpha, rc.getAlpha(), 0.000001f);
        assertEquals("旋转应该正确", rotation, rc.getRotation(), 0.000001f);
    }
    
    /**
     * 测试获取渲染类型
     */
    @Test
    public void testGetRenderType() {
        assertEquals("默认渲染类型应该为RECTANGLE", RenderComponent.RenderType.RECTANGLE, renderComponent.getRenderType());
        
        renderComponent.setImagePath("test.png");
        assertEquals("设置图片路径后渲染类型应该为IMAGE", RenderComponent.RenderType.IMAGE, renderComponent.getRenderType());
    }
    
    /**
     * 测试设置和获取颜色
     */
    @Test
    public void testSetAndGetColor() {
        RenderComponent.Color newColor = new RenderComponent.Color(0.5f, 0.6f, 0.7f, 0.8f);
        renderComponent.setColor(newColor);
        
        RenderComponent.Color color = renderComponent.getColor();
        assertEquals("颜色r应该正确", 0.5f, color.r, 0.000001f);
        assertEquals("颜色g应该正确", 0.6f, color.g, 0.000001f);
        assertEquals("颜色b应该正确", 0.7f, color.b, 0.000001f);
        assertEquals("颜色a应该正确", 0.8f, color.a, 0.000001f);
    }
    
    /**
     * 测试设置颜色（RGBA参数）
     */
    @Test
    public void testSetColorRGBA() {
        renderComponent.setColor(0.1f, 0.2f, 0.3f, 0.4f);
        
        RenderComponent.Color color = renderComponent.getColor();
        assertEquals("颜色r应该正确", 0.1f, color.r, 0.000001f);
        assertEquals("颜色g应该正确", 0.2f, color.g, 0.000001f);
        assertEquals("颜色b应该正确", 0.3f, color.b, 0.000001f);
        assertEquals("颜色a应该正确", 0.4f, color.a, 0.000001f);
    }
    
    /**
     * 测试设置和获取尺寸
     */
    @Test
    public void testSetAndGetSize() {
        Vector2 newSize = new Vector2(150.0f, 200.0f);
        renderComponent.setSize(newSize);
        
        Vector2 size = renderComponent.getSize();
        assertEquals("尺寸x应该正确", 150.0f, size.x, 0.000001f);
        assertEquals("尺寸y应该正确", 200.0f, size.y, 0.000001f);
        
        // 验证返回的是副本
        newSize.x = 999.0f;
        Vector2 size2 = renderComponent.getSize();
        assertEquals("尺寸x不应该被修改", 150.0f, size2.x, 0.000001f);
    }
    
    /**
     * 测试设置和获取可见性
     */
    @Test
    public void testSetAndGetVisible() {
        assertTrue("默认应该可见", renderComponent.isVisible());
        
        renderComponent.setVisible(false);
        assertFalse("应该能够设置为不可见", renderComponent.isVisible());
        
        renderComponent.setVisible(true);
        assertTrue("应该能够设置为可见", renderComponent.isVisible());
    }
    
    /**
     * 测试设置和获取图片路径
     */
    @Test
    public void testSetAndGetImagePath() {
        String imagePath = "resources/picture/test.png";
        renderComponent.setImagePath(imagePath);
        
        assertEquals("图片路径应该正确", imagePath, renderComponent.getImagePath());
        assertEquals("设置图片路径后渲染类型应该为IMAGE", RenderComponent.RenderType.IMAGE, renderComponent.getRenderType());
    }
    
    /**
     * 测试设置图片路径为null
     */
    @Test
    public void testSetImagePathNull() {
        renderComponent.setImagePath("test.png");
        renderComponent.setImagePath(null);
        
        assertNull("图片路径应该为null", renderComponent.getImagePath());
    }
    
    /**
     * 测试设置和获取旋转
     */
    @Test
    public void testSetAndGetRotation() {
        float rotation = (float) Math.PI / 2;
        renderComponent.setRotation(rotation);
        
        assertEquals("旋转应该正确", rotation, renderComponent.getRotation(), 0.000001f);
    }
    
    /**
     * 测试设置和获取透明度
     */
    @Test
    public void testSetAndGetAlpha() {
        float alpha = 0.5f;
        renderComponent.setAlpha(alpha);
        
        assertEquals("透明度应该正确", alpha, renderComponent.getAlpha(), 0.000001f);
    }
    
    /**
     * 测试透明度范围
     */
    @Test
    public void testAlphaRange() {
        renderComponent.setAlpha(0.0f);
        assertEquals("透明度可以为0", 0.0f, renderComponent.getAlpha(), 0.000001f);
        
        renderComponent.setAlpha(1.0f);
        assertEquals("透明度可以为1", 1.0f, renderComponent.getAlpha(), 0.000001f);
        
        renderComponent.setAlpha(1.5f);
        assertEquals("透明度可以超过1", 1.5f, renderComponent.getAlpha(), 0.000001f);
        
        renderComponent.setAlpha(-0.5f);
        assertEquals("透明度可以为负", -0.5f, renderComponent.getAlpha(), 0.000001f);
    }
    
    /**
     * 测试设置和获取渲染器
     */
    @Test
    public void testSetAndGetRenderer() {
        MockRenderer newRenderer = new MockRenderer();
        renderComponent.setRenderer(newRenderer);
        
        IRenderer renderer = renderComponent.getRenderer();
        assertEquals("渲染器应该正确", newRenderer, renderer);
    }
    
    /**
     * 测试渲染矩形
     */
    @Test
    public void testRenderRectangle() {
        transform.setPosition(new Vector2(100.0f, 100.0f));
        // renderComponent默认就是RECTANGLE类型
        renderComponent.setSize(new Vector2(50.0f, 50.0f));
        renderComponent.setColor(1.0f, 0.0f, 0.0f, 1.0f);
        renderComponent.setVisible(true);
        
        renderComponent.render();
        
        assertTrue("应该调用drawRect", mockRenderer.drawRectCalled);
        assertEquals("drawRect x应该正确", 100.0f, mockRenderer.drawRectX, 0.000001f);
        assertEquals("drawRect y应该正确", 100.0f, mockRenderer.drawRectY, 0.000001f);
    }
    
    /**
     * 测试渲染圆形
     */
    @Test
    public void testRenderCircle() {
        transform.setPosition(new Vector2(200.0f, 200.0f));
        // 使用构造函数创建CIRCLE类型的组件
        RenderComponent circleComponent = new RenderComponent(
            RenderComponent.RenderType.CIRCLE,
            new Vector2(50.0f, 50.0f),
            new RenderComponent.Color(0.0f, 1.0f, 0.0f, 1.0f)
        );
        circleComponent.setOwner(owner);
        circleComponent.setRenderer(mockRenderer);
        
        circleComponent.render();
        
        assertTrue("应该调用drawCircle", mockRenderer.drawCircleCalled);
    }
    
    /**
     * 测试渲染线条
     */
    @Test
    public void testRenderLine() {
        transform.setPosition(new Vector2(0.0f, 0.0f));
        // 使用构造函数创建LINE类型的组件
        RenderComponent lineComponent = new RenderComponent(
            RenderComponent.RenderType.LINE,
            new Vector2(100.0f, 100.0f),
            new RenderComponent.Color(0.0f, 0.0f, 1.0f, 1.0f)
        );
        lineComponent.setOwner(owner);
        lineComponent.setRenderer(mockRenderer);
        
        lineComponent.render();
        
        assertTrue("应该调用drawLine", mockRenderer.drawLineCalled);
    }
    
    /**
     * 测试渲染图片（无旋转）
     */
    @Test
    public void testRenderImage() {
        transform.setPosition(new Vector2(50.0f, 50.0f));
        renderComponent.setImagePath("test.png");
        renderComponent.setSize(new Vector2(100.0f, 100.0f));
        renderComponent.setRotation(0.0f);
        renderComponent.setAlpha(0.8f);
        
        renderComponent.render();
        
        assertTrue("应该调用drawImage", mockRenderer.drawImageCalled);
        assertEquals("drawImage路径应该正确", "test.png", mockRenderer.drawImagePath);
    }
    
    /**
     * 测试渲染图片（有旋转）
     */
    @Test
    public void testRenderImageRotated() {
        transform.setPosition(new Vector2(50.0f, 50.0f));
        renderComponent.setImagePath("test.png");
        renderComponent.setSize(new Vector2(100.0f, 100.0f));
        renderComponent.setRotation((float) Math.PI / 4);
        renderComponent.setAlpha(0.8f);
        
        renderComponent.render();
        
        assertTrue("应该调用drawImageRotated", mockRenderer.drawImageRotatedCalled);
        assertEquals("drawImageRotated路径应该正确", "test.png", mockRenderer.drawImageRotatedPath);
    }
    
    /**
     * 测试渲染（不可见）
     */
    @Test
    public void testRenderInvisible() {
        renderComponent.setVisible(false);
        
        renderComponent.render();
        
        assertFalse("不可见时不应该调用drawRect", mockRenderer.drawRectCalled);
    }
    
    /**
     * 测试渲染（无渲染器）
     */
    @Test
    public void testRenderWithoutRenderer() {
        renderComponent.setRenderer(null);
        
        // 不应该抛出异常
        renderComponent.render();
        assertNotNull("渲染后组件应该存在", renderComponent);
    }
    
    /**
     * 测试渲染（无TransformComponent）
     */
    @Test
    public void testRenderWithoutTransform() {
        GameObject obj = new GameObject("NoTransform");
        RenderComponent rc = new RenderComponent();
        rc.setOwner(obj);
        rc.setRenderer(mockRenderer);
        
        // 不应该抛出异常
        rc.render();
        assertNotNull("渲染后组件应该存在", rc);
    }
    
    /**
     * 测试渲染（图片路径为null）
     */
    @Test
    public void testRenderImageWithNullPath() {
        transform.setPosition(new Vector2(50.0f, 50.0f));
        // setImagePath(null)不会将renderType设置为IMAGE，所以需要先设置一个路径再设为null
        renderComponent.setImagePath("test.png");
        renderComponent.setImagePath(null);
        
        renderComponent.render();
        
        // 图片路径为null时不应该调用drawImage
        assertFalse("图片路径为null时不应该调用drawImage", mockRenderer.drawImageCalled);
        assertFalse("图片路径为null时不应该调用drawImageRotated", mockRenderer.drawImageRotatedCalled);
    }
    
    /**
     * 测试record方法（图片类型）
     */
    @Test
    public void testRecordImage() {
        renderComponent.setImagePath("test.png");
        renderComponent.setSize(new Vector2(100.0f, 200.0f));
        renderComponent.setRotation((float) Math.PI / 4);
        renderComponent.setAlpha(0.5f);
        
        String record = renderComponent.record();
        assertNotNull("record结果不应该为null", record);
        assertTrue("record应该包含图片路径", record.contains("test.png"));
    }
    
    /**
     * 测试record方法（非图片类型）
     */
    @Test
    public void testRecordNonImage() {
        // renderComponent默认就是RECTANGLE类型
        renderComponent.setColor(1.0f, 0.5f, 0.25f, 1.0f);
        
        String record = renderComponent.record();
        assertNotNull("record结果不应该为null", record);
        assertTrue("record应该包含渲染类型", record.contains("RECTANGLE"));
    }
    
    /**
     * 测试初始化
     */
    @Test
    public void testInitialize() {
        renderComponent.initialize();
        assertNotNull("初始化后组件应该存在", renderComponent);
    }
    
    /**
     * 测试更新
     */
    @Test
    public void testUpdate() {
        // RenderComponent的update方法为空实现
        renderComponent.update(0.016f);
        assertNotNull("更新后组件应该存在", renderComponent);
    }
    
    /**
     * 测试Color类构造函数（RGBA）
     */
    @Test
    public void testColorConstructorRGBA() {
        RenderComponent.Color color = new RenderComponent.Color(0.1f, 0.2f, 0.3f, 0.4f);
        
        assertEquals("颜色r应该正确", 0.1f, color.r, 0.000001f);
        assertEquals("颜色g应该正确", 0.2f, color.g, 0.000001f);
        assertEquals("颜色b应该正确", 0.3f, color.b, 0.000001f);
        assertEquals("颜色a应该正确", 0.4f, color.a, 0.000001f);
    }
    
    /**
     * 测试Color类构造函数（RGB，alpha默认为1.0）
     */
    @Test
    public void testColorConstructorRGB() {
        RenderComponent.Color color = new RenderComponent.Color(0.5f, 0.6f, 0.7f);
        
        assertEquals("颜色r应该正确", 0.5f, color.r, 0.000001f);
        assertEquals("颜色g应该正确", 0.6f, color.g, 0.000001f);
        assertEquals("颜色b应该正确", 0.7f, color.b, 0.000001f);
        assertEquals("颜色a应该为1.0", 1.0f, color.a, 0.000001f);
    }
    
    /**
     * 测试所有RenderType枚举值
     */
    @Test
    public void testRenderTypeEnum() {
        // 验证所有枚举值存在
        assertNotNull("RECTANGLE应该存在", RenderComponent.RenderType.RECTANGLE);
        assertNotNull("CIRCLE应该存在", RenderComponent.RenderType.CIRCLE);
        assertNotNull("LINE应该存在", RenderComponent.RenderType.LINE);
        assertNotNull("IMAGE应该存在", RenderComponent.RenderType.IMAGE);
        assertNotNull("TEXT应该存在", RenderComponent.RenderType.TEXT);
        assertNotNull("HEALTH_BAR应该存在", RenderComponent.RenderType.HEALTH_BAR);
        assertNotNull("IMAGE_ROTATED应该存在", RenderComponent.RenderType.IMAGE_ROTATED);
    }
    
    /**
     * Mock渲染器类
     */
    private static class MockRenderer implements IRenderer {
        public boolean drawRectCalled = false;
        public boolean drawCircleCalled = false;
        public boolean drawLineCalled = false;
        public boolean drawImageCalled = false;
        public boolean drawImageRotatedCalled = false;
        
        public float drawRectX, drawRectY, drawRectWidth, drawRectHeight;
        public String drawImagePath;
        public String drawImageRotatedPath;
        
        @Override
        public void beginFrame() {}
        
        @Override
        public void endFrame() {}
        
        @Override
        public void drawRect(float x, float y, float width, float height, float r, float g, float b, float a) {
            drawRectCalled = true;
            drawRectX = x;
            drawRectY = y;
            drawRectWidth = width;
            drawRectHeight = height;
        }
        
        @Override
        public void drawCircle(float x, float y, float radius, int segments, float r, float g, float b, float a) {
            drawCircleCalled = true;
        }
        
        @Override
        public void drawLine(float x1, float y1, float x2, float y2, float r, float g, float b, float a) {
            drawLineCalled = true;
        }
        
        @Override
        public void drawText(String text, float x, float y, float size, float r, float g, float b, float a) {}
        
        @Override
        public void drawHealthBar(float x, float y, float width, float height, int currentHealth, int maxHealth) {}
        
        @Override
        public void drawImage(String imagePath, float x, float y, float width, float height, float alpha) {
            drawImageCalled = true;
            drawImagePath = imagePath;
        }
        
        @Override
        public void drawImageRotated(String imagePath, float x, float y, float width, float height, float rotation, float alpha) {
            drawImageRotatedCalled = true;
            drawImageRotatedPath = imagePath;
        }
        
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
}

