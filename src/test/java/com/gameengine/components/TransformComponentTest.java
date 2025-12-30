package com.gameengine.components;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import com.gameengine.core.GameObject;
import com.gameengine.math.Vector2;

/**
 * TransformComponent变换组件类的测试用例
 * 测试位置、旋转、缩放等功能
 */
public class TransformComponentTest {
    
    private TransformComponent transform;
    private GameObject owner;
    
    @Before
    public void setUp() {
        owner = new GameObject("TestObject");
        transform = new TransformComponent();
        transform.setOwner(owner);
    }
    
    /**
     * 测试默认构造函数
     */
    @Test
    public void testDefaultConstructor() {
        TransformComponent t = new TransformComponent();
        Vector2 pos = t.getPosition();
        Vector2 scale = t.getScale();
        
        assertEquals("默认位置x应该为0", 0.0f, pos.x, 0.000001f);
        assertEquals("默认位置y应该为0", 0.0f, pos.y, 0.000001f);
        assertEquals("默认缩放x应该为1", 1.0f, scale.x, 0.000001f);
        assertEquals("默认缩放y应该为1", 1.0f, scale.y, 0.000001f);
        assertEquals("默认旋转应该为0", 0.0f, t.getRotation(), 0.000001f);
    }
    
    /**
     * 测试带位置参数的构造函数
     */
    @Test
    public void testConstructorWithPosition() {
        Vector2 position = new Vector2(100.0f, 200.0f);
        TransformComponent t = new TransformComponent(position);
        
        Vector2 pos = t.getPosition();
        assertEquals("位置x应该正确", 100.0f, pos.x, 0.000001f);
        assertEquals("位置y应该正确", 200.0f, pos.y, 0.000001f);
    }
    
    /**
     * 测试带位置、缩放、旋转参数的构造函数
     */
    @Test
    public void testConstructorWithAllParameters() {
        Vector2 position = new Vector2(50.0f, 75.0f);
        Vector2 scale = new Vector2(2.0f, 3.0f);
        float rotation = (float) Math.PI / 4;
        
        TransformComponent t = new TransformComponent(position, scale, rotation);
        
        Vector2 pos = t.getPosition();
        Vector2 scl = t.getScale();
        
        assertEquals("位置x应该正确", 50.0f, pos.x, 0.000001f);
        assertEquals("位置y应该正确", 75.0f, pos.y, 0.000001f);
        assertEquals("缩放x应该正确", 2.0f, scl.x, 0.000001f);
        assertEquals("缩放y应该正确", 3.0f, scl.y, 0.000001f);
        assertEquals("旋转应该正确", Math.PI / 4, t.getRotation(), 0.000001f);
    }
    
    /**
     * 测试设置和获取位置
     */
    @Test
    public void testSetAndGetPosition() {
        Vector2 newPosition = new Vector2(300.0f, 400.0f);
        transform.setPosition(newPosition);
        
        Vector2 pos = transform.getPosition();
        assertEquals("位置x应该正确", 300.0f, pos.x, 0.000001f);
        assertEquals("位置y应该正确", 400.0f, pos.y, 0.000001f);
        
        // 验证返回的是副本（修改原Vector2不应该影响组件内部）
        newPosition.x = 999.0f;
        Vector2 pos2 = transform.getPosition();
        assertEquals("位置x不应该被修改", 300.0f, pos2.x, 0.000001f);
    }
    
    /**
     * 测试移动到指定位置
     */
    @Test
    public void testMoveTo() {
        Vector2 target = new Vector2(150.0f, 250.0f);
        transform.moveTo(target);
        
        Vector2 pos = transform.getPosition();
        assertEquals("位置x应该正确", 150.0f, pos.x, 0.000001f);
        assertEquals("位置y应该正确", 250.0f, pos.y, 0.000001f);
    }
    
    /**
     * 测试平移
     */
    @Test
    public void testTranslate() {
        transform.setPosition(new Vector2(100.0f, 100.0f));
        Vector2 delta = new Vector2(50.0f, 75.0f);
        transform.translate(delta);
        
        Vector2 pos = transform.getPosition();
        assertEquals("平移后位置x应该正确", 150.0f, pos.x, 0.000001f);
        assertEquals("平移后位置y应该正确", 175.0f, pos.y, 0.000001f);
    }
    
    /**
     * 测试多次平移
     */
    @Test
    public void testMultipleTranslate() {
        transform.setPosition(new Vector2(0.0f, 0.0f));
        transform.translate(new Vector2(10.0f, 20.0f));
        transform.translate(new Vector2(30.0f, 40.0f));
        
        Vector2 pos = transform.getPosition();
        assertEquals("多次平移后位置x应该正确", 40.0f, pos.x, 0.000001f);
        assertEquals("多次平移后位置y应该正确", 60.0f, pos.y, 0.000001f);
    }
    
    /**
     * 测试设置和获取旋转
     */
    @Test
    public void testSetAndGetRotation() {
        float angle = (float) Math.PI / 2;
        transform.setRotation(angle);
        
        assertEquals("旋转角度应该正确", Math.PI / 2, transform.getRotation(), 0.000001f);
    }
    
    /**
     * 测试旋转
     */
    @Test
    public void testRotate() {
        transform.setRotation(0.0f);
        transform.rotate((float) Math.PI / 4);
        
        assertEquals("旋转后角度应该正确", Math.PI / 4, transform.getRotation(), 0.000001f);
        
        transform.rotate((float) Math.PI / 4);
        assertEquals("再次旋转后角度应该正确", Math.PI / 2, transform.getRotation(), 0.000001f);
    }
    
    /**
     * 测试旋转超过2π
     */
    @Test
    public void testRotateOverTwoPi() {
        transform.setRotation(0.0f);
        transform.rotate((float) (3 * Math.PI));
        
        assertEquals("旋转超过2π应该正确", 3 * Math.PI, transform.getRotation(), 0.000001f);
    }
    
    /**
     * 测试设置和获取缩放
     */
    @Test
    public void testSetAndGetScale() {
        Vector2 newScale = new Vector2(2.5f, 3.5f);
        transform.setScale(newScale);
        
        Vector2 scale = transform.getScale();
        assertEquals("缩放x应该正确", 2.5f, scale.x, 0.000001f);
        assertEquals("缩放y应该正确", 3.5f, scale.y, 0.000001f);
    }
    
    /**
     * 测试缩放
     */
    @Test
    public void testScale() {
        transform.setScale(new Vector2(2.0f, 2.0f));
        Vector2 scaleFactor = new Vector2(1.5f, 2.0f);
        transform.scale(scaleFactor);
        
        Vector2 scale = transform.getScale();
        assertEquals("缩放后x应该正确", 3.0f, scale.x, 0.000001f);
        assertEquals("缩放后y应该正确", 4.0f, scale.y, 0.000001f);
    }
    
    /**
     * 测试多次缩放
     */
    @Test
    public void testMultipleScale() {
        transform.setScale(new Vector2(1.0f, 1.0f));
        transform.scale(new Vector2(2.0f, 2.0f));
        transform.scale(new Vector2(3.0f, 4.0f));
        
        Vector2 scale = transform.getScale();
        assertEquals("多次缩放后x应该正确", 6.0f, scale.x, 0.000001f);
        assertEquals("多次缩放后y应该正确", 8.0f, scale.y, 0.000001f);
    }
    
    /**
     * 测试record方法
     */
    @Test
    public void testRecord() {
        transform.setPosition(new Vector2(100.5f, 200.5f));
        transform.setRotation((float) Math.PI / 4);
        transform.setScale(new Vector2(2.0f, 3.0f));
        
        String record = transform.record();
        assertNotNull("record结果不应该为null", record);
        assertTrue("record应该包含位置信息", record.contains("100.5"));
        assertTrue("record应该包含位置信息", record.contains("200.5"));
    }
    
    /**
     * 测试record方法（无owner时）
     */
    @Test
    public void testRecordWithoutOwner() {
        TransformComponent t = new TransformComponent();
        // 不设置owner
        String record = t.record();
        assertEquals("无owner时record应该返回空字符串", "", record);
    }
    
    /**
     * 测试初始化
     */
    @Test
    public void testInitialize() {
        transform.initialize();
        assertNotNull("初始化后组件应该存在", transform);
    }
    
    /**
     * 测试更新
     */
    @Test
    public void testUpdate() {
        // TransformComponent的update方法为空实现
        transform.update(0.016f);
        assertNotNull("更新后组件应该存在", transform);
    }
    
    /**
     * 测试渲染
     */
    @Test
    public void testRender() {
        // TransformComponent的render方法为空实现
        transform.render();
        assertNotNull("渲染后组件应该存在", transform);
    }
    
    /**
     * 测试组合操作（移动+旋转+缩放）
     */
    @Test
    public void testCombinedOperations() {
        transform.setPosition(new Vector2(0.0f, 0.0f));
        transform.setRotation(0.0f);
        transform.setScale(new Vector2(1.0f, 1.0f));
        
        // 移动到新位置
        transform.moveTo(new Vector2(100.0f, 100.0f));
        // 旋转
        transform.rotate((float) Math.PI / 2);
        // 缩放
        transform.scale(new Vector2(2.0f, 2.0f));
        
        Vector2 pos = transform.getPosition();
        Vector2 scale = transform.getScale();
        
        assertEquals("组合操作后位置x应该正确", 100.0f, pos.x, 0.000001f);
        assertEquals("组合操作后位置y应该正确", 100.0f, pos.y, 0.000001f);
        assertEquals("组合操作后旋转应该正确", Math.PI / 2, transform.getRotation(), 0.000001f);
        assertEquals("组合操作后缩放x应该正确", 2.0f, scale.x, 0.000001f);
        assertEquals("组合操作后缩放y应该正确", 2.0f, scale.y, 0.000001f);
    }
    
    /**
     * 测试负值位置
     */
    @Test
    public void testNegativePosition() {
        Vector2 negPos = new Vector2(-100.0f, -200.0f);
        transform.setPosition(negPos);
        
        Vector2 pos = transform.getPosition();
        assertEquals("负位置x应该正确", -100.0f, pos.x, 0.000001f);
        assertEquals("负位置y应该正确", -200.0f, pos.y, 0.000001f);
    }
    
    /**
     * 测试负值缩放
     */
    @Test
    public void testNegativeScale() {
        Vector2 negScale = new Vector2(-1.0f, -2.0f);
        transform.setScale(negScale);
        
        Vector2 scale = transform.getScale();
        assertEquals("负缩放x应该正确", -1.0f, scale.x, 0.000001f);
        assertEquals("负缩放y应该正确", -2.0f, scale.y, 0.000001f);
    }
    
    /**
     * 测试零缩放
     */
    @Test
    public void testZeroScale() {
        Vector2 zeroScale = new Vector2(0.0f, 0.0f);
        transform.setScale(zeroScale);
        
        Vector2 scale = transform.getScale();
        assertEquals("零缩放x应该正确", 0.0f, scale.x, 0.000001f);
        assertEquals("零缩放y应该正确", 0.0f, scale.y, 0.000001f);
    }
}

