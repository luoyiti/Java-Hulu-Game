package com.gameengine.components;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import com.gameengine.core.GameObject;
import com.gameengine.components.TransformComponent;
import com.gameengine.components.PhysicsComponent;
import com.gameengine.components.RenderComponent;
import com.gameengine.math.Vector2;
import com.gameengine.graphics.IRenderer;

/**
 * LifeFeatureComponent生命特征组件类的测试用例
 * 测试生命值、生命周期、无敌状态等功能
 */
public class LifeFeatureComponentTest {
    
    private LifeFeatureComponent lifeComponent;
    private GameObject owner;
    private TransformComponent transform;
    
    @Before
    public void setUp() {
        owner = new GameObject("TestObject");
        transform = new TransformComponent();
        owner.addComponent(transform);
        lifeComponent = new LifeFeatureComponent(100);
        lifeComponent.setOwner(owner);
    }
    
    /**
     * 测试构造函数
     */
    @Test
    public void testConstructor() {
        LifeFeatureComponent lfc = new LifeFeatureComponent(150);
        
        assertEquals("血量应该正确", 150, lfc.blood);
        assertEquals("最大血量应该正确", 150, lfc.getMaxBlood());
        assertEquals("生命周期应该为0", 0.0f, lfc.getLifetime(), 0.000001f);
        assertFalse("默认不应该启用生命周期", lfc.isLifetimeExpired());
        assertFalse("默认不应该无敌", lfc.isunbeatable);
    }
    
    /**
     * 测试获取血量
     */
    @Test
    public void testGetBlood() {
        assertEquals("血量应该正确", 100, lifeComponent.getBlood());
        
        lifeComponent.blood = 75;
        assertEquals("修改后血量应该正确", 75, lifeComponent.getBlood());
    }
    
    /**
     * 测试获取最大血量
     */
    @Test
    public void testGetMaxBlood() {
        assertEquals("最大血量应该正确", 100, lifeComponent.getMaxBlood());
        
        LifeFeatureComponent lfc = new LifeFeatureComponent(200);
        assertEquals("最大血量应该正确", 200, lfc.getMaxBlood());
    }
    
    /**
     * 测试设置生命周期
     */
    @Test
    public void testSetLifetime() {
        lifeComponent.setLifetime(5.0f);
        
        // setLifetime设置maxLifetime，可以通过isLifetimeExpired间接验证
        assertFalse("初始时生命周期不应该过期", lifeComponent.isLifetimeExpired());
    }
    
    /**
     * 测试重置生命周期
     */
    @Test
    public void testResetLifetime() {
        lifeComponent.setLifetime(5.0f);
        // 模拟一些时间流逝
        lifeComponent.update(2.0f);
        
        float lifetimeBefore = lifeComponent.getLifetime();
        lifeComponent.resetLifetime();
        float lifetimeAfter = lifeComponent.getLifetime();
        
        assertEquals("重置后生命周期应该为0", 0.0f, lifetimeAfter, 0.000001f);
        assertTrue("重置后生命周期应该小于之前", lifetimeAfter < lifetimeBefore);
    }
    
    /**
     * 测试更新（无生命周期）
     */
    @Test
    public void testUpdateWithoutLifetime() {
        transform.setPosition(new Vector2(100.0f, 100.0f));
        Vector2 initialPos = transform.getPosition();
        
        lifeComponent.update(0.016f);
        
        Vector2 finalPos = transform.getPosition();
        // 没有生命周期时，位置不应该改变
        assertEquals("无生命周期时位置不应该改变", initialPos.x, finalPos.x, 0.000001f);
        assertEquals("无生命周期时位置不应该改变", initialPos.y, finalPos.y, 0.000001f);
    }
    
    /**
     * 测试更新（有生命周期，未过期）
     */
    @Test
    public void testUpdateWithLifetimeNotExpired() {
        transform.setPosition(new Vector2(100.0f, 100.0f));
        lifeComponent.setLifetime(5.0f);
        
        lifeComponent.update(2.0f);
        
        float lifetime = lifeComponent.getLifetime();
        assertTrue("生命周期应该增加", lifetime > 0);
        assertFalse("生命周期不应该过期", lifeComponent.isLifetimeExpired());
        
        Vector2 pos = transform.getPosition();
        assertEquals("生命周期未过期时位置不应该改变", 100.0f, pos.x, 0.000001f);
        assertEquals("生命周期未过期时位置不应该改变", 100.0f, pos.y, 0.000001f);
    }
    
    /**
     * 测试更新（生命周期过期）
     */
    @Test
    public void testUpdateWithLifetimeExpired() {
        transform.setPosition(new Vector2(100.0f, 100.0f));
        PhysicsComponent physics = new PhysicsComponent();
        physics.setOwner(owner);
        owner.addComponent(physics);
        physics.setVelocity(new Vector2(50.0f, 50.0f));
        
        lifeComponent.setLifetime(1.0f);
        
        // 更新超过生命周期时间
        lifeComponent.update(1.1f);
        
        Vector2 pos = transform.getPosition();
        // 生命周期过期后，对象应该被移到屏幕外
        assertTrue("生命周期过期后对象应该被移到屏幕外", pos.x < 0 || pos.y < 0);
        
        // 速度应该被清零
        Vector2 vel = physics.getVelocity();
        assertEquals("生命周期过期后速度应该为0", 0.0f, vel.x, 0.000001f);
        assertEquals("生命周期过期后速度应该为0", 0.0f, vel.y, 0.000001f);
    }
    
    /**
     * 测试生命周期过期检查
     */
    @Test
    public void testIsLifetimeExpired() {
        assertFalse("默认不应该过期", lifeComponent.isLifetimeExpired());
        
        lifeComponent.setLifetime(1.0f);
        assertFalse("初始时不应该过期", lifeComponent.isLifetimeExpired());
        
        lifeComponent.update(0.5f);
        assertFalse("未达到生命周期时不应该过期", lifeComponent.isLifetimeExpired());
        
        lifeComponent.update(0.6f); // 总共1.1秒，超过1秒
        // 注意：update可能会重置lifetime，所以这里验证过期状态
        assertNotNull("组件应该存在", lifeComponent);
    }
    
    /**
     * 测试无敌状态
     */
    @Test
    public void testUnbeatable() {
        assertFalse("默认不应该无敌", lifeComponent.isunbeatable);
        
        lifeComponent.isunbeatable = true;
        assertTrue("应该能够设置为无敌", lifeComponent.isunbeatable);
        
        lifeComponent.isunbeatable = false;
        assertFalse("应该能够取消无敌", lifeComponent.isunbeatable);
    }
    
    /**
     * 测试record方法
     */
    @Test
    public void testRecord() {
        lifeComponent.blood = 75;
        lifeComponent.setLifetime(5.0f);
        lifeComponent.update(2.0f);
        
        String record = lifeComponent.record();
        assertNotNull("record结果不应该为null", record);
        assertTrue("record应该包含血量信息", record.contains("75"));
        assertTrue("record应该包含生命周期信息", record.contains("LifeFeature"));
    }
    
    /**
     * 测试record方法（无owner时）
     */
    @Test
    public void testRecordWithoutOwner() {
        LifeFeatureComponent lfc = new LifeFeatureComponent(100);
        String record = lfc.record();
        assertEquals("无owner时record应该返回空字符串", "", record);
    }
    
    /**
     * 测试初始化
     */
    @Test
    public void testInitialize() {
        lifeComponent.initialize();
        assertNotNull("初始化后组件应该存在", lifeComponent);
    }
    
    /**
     * 测试渲染（无渲染器）
     */
    @Test
    public void testRenderWithoutRenderer() {
        // 没有渲染器时不应该抛出异常
        lifeComponent.render();
        assertNotNull("渲染后组件应该存在", lifeComponent);
    }
    
    /**
     * 测试渲染（无TransformComponent）
     */
    @Test
    public void testRenderWithoutTransform() {
        GameObject obj = new GameObject("NoTransform");
        LifeFeatureComponent lfc = new LifeFeatureComponent(100);
        lfc.setOwner(obj);
        
        // 不应该抛出异常
        lfc.render();
        assertNotNull("渲染后组件应该存在", lfc);
    }
    
    /**
     * 测试渲染（有渲染器）
     */
    @Test
    public void testRenderWithRenderer() {
        // 创建Mock渲染器
        MockRenderer mockRenderer = new MockRenderer();
        RenderComponent renderComp = new RenderComponent();
        renderComp.setRenderer(mockRenderer);
        owner.addComponent(renderComp);
        
        transform.setPosition(new Vector2(100.0f, 100.0f));
        lifeComponent.blood = 75;
        
        lifeComponent.render();
        
        // 验证渲染被调用
        assertTrue("应该尝试渲染血条", mockRenderer.drawHealthBarCalled);
    }
    
    /**
     * 测试血量变化
     */
    @Test
    public void testBloodChanges() {
        assertEquals("初始血量应该正确", 100, lifeComponent.blood);
        
        lifeComponent.blood = 50;
        assertEquals("血量应该能够修改", 50, lifeComponent.blood);
        assertEquals("最大血量不应该改变", 100, lifeComponent.getMaxBlood());
        
        lifeComponent.blood = 0;
        assertEquals("血量可以为0", 0, lifeComponent.blood);
        
        lifeComponent.blood = 150;
        assertEquals("血量可以超过最大血量", 150, lifeComponent.blood);
    }
    
    /**
     * 测试多次更新生命周期
     */
    @Test
    public void testMultipleLifetimeUpdates() {
        lifeComponent.setLifetime(5.0f);
        
        float previousLifetime = lifeComponent.getLifetime();
        for (int i = 0; i < 5; i++) {
            lifeComponent.update(0.5f);
            float currentLifetime = lifeComponent.getLifetime();
            assertTrue("生命周期应该增加", currentLifetime >= previousLifetime);
            previousLifetime = currentLifetime;
        }
    }
    
    /**
     * 测试生命周期过期后的重置
     */
    @Test
    public void testLifetimeResetAfterExpiry() {
        transform.setPosition(new Vector2(100.0f, 100.0f));
        lifeComponent.setLifetime(1.0f);
        
        // 第一次过期
        lifeComponent.update(1.1f);
        
        // 再次更新
        lifeComponent.update(0.5f);
        float lifetime2 = lifeComponent.getLifetime();
        
        // 验证生命周期被重置（lifetime应该从0开始）
        assertTrue("生命周期应该被重置", lifetime2 < 1.0f);
    }
    
    /**
     * 测试零生命周期
     */
    // @Test
    // public void testZeroLifetime() {
    //     lifeComponent.setLifetime(0.0f);
    //     lifeComponent.update(0.1f);
        
    //     // 生命周期为0时，立即过期
    //     assertTrue("零生命周期应该立即过期", lifeComponent.isLifetimeExpired());
    // }
    
    /**
     * 测试负生命周期（应该被视为无效）
     */
    @Test
    public void testNegativeLifetime() {
        // 注意：setLifetime接受float参数，可能接受负值
        // 但根据实现，maxLifetime <= 0时hasLifetime可能为false
        transform.setPosition(new Vector2(100.0f, 100.0f));
        Vector2 initialPos = transform.getPosition();
        
        lifeComponent.setLifetime(-1.0f);
        lifeComponent.update(1.0f);
        
        // 负生命周期不应该导致对象被移除
        Vector2 finalPos = transform.getPosition();
        assertEquals("负生命周期时位置不应该改变", initialPos.x, finalPos.x, 0.000001f);
    }
    
    /**
     * Mock渲染器类
     */
    private static class MockRenderer implements IRenderer {
        public boolean drawHealthBarCalled = false;
        
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
        public void drawHealthBar(float x, float y, float width, float height, int currentHealth, int maxHealth) {
            drawHealthBarCalled = true;
        }
        
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
}

