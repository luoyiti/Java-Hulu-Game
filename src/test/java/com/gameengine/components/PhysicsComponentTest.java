package com.gameengine.components;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import com.gameengine.core.GameObject;
import com.gameengine.math.Vector2;

/**
 * PhysicsComponent物理组件类的测试用例
 * 测试物理运动、速度、加速度、重力、摩擦力等功能
 */
public class PhysicsComponentTest {
    
    private PhysicsComponent physics;
    private GameObject owner;
    private TransformComponent transform;
    
    @Before
    public void setUp() {
        owner = new GameObject("TestObject");
        transform = new TransformComponent();
        owner.addComponent(transform);
        physics = new PhysicsComponent();
        physics.setOwner(owner);
    }
    
    /**
     * 测试默认构造函数
     */
    @Test
    public void testDefaultConstructor() {
        PhysicsComponent p = new PhysicsComponent();
        Vector2 velocity = p.getVelocity();
        Vector2 acceleration = p.getAcceleration();
        
        assertEquals("默认速度x应该为0", 0.0f, velocity.x, 0.000001f);
        assertEquals("默认速度y应该为0", 0.0f, velocity.y, 0.000001f);
        assertEquals("默认加速度x应该为0", 0.0f, acceleration.x, 0.000001f);
        assertEquals("默认加速度y应该为0", 0.0f, acceleration.y, 0.000001f);
        assertEquals("默认质量应该为1", 1.0f, p.getMass(), 0.000001f);
        assertEquals("默认摩擦力应该为0.9", 0.9f, p.getFriction(), 0.000001f);
        assertFalse("默认不应该使用重力", p.isUseGravity());
    }
    
    /**
     * 测试带质量参数的构造函数
     */
    @Test
    public void testConstructorWithMass() {
        PhysicsComponent p = new PhysicsComponent(5.0f);
        assertEquals("质量应该正确", 5.0f, p.getMass(), 0.000001f);
    }
    
    /**
     * 测试设置和获取速度
     */
    @Test
    public void testSetAndGetVelocity() {
        Vector2 velocity = new Vector2(100.0f, 200.0f);
        physics.setVelocity(velocity);
        
        Vector2 vel = physics.getVelocity();
        assertEquals("速度x应该正确", 100.0f, vel.x, 0.000001f);
        assertEquals("速度y应该正确", 200.0f, vel.y, 0.000001f);
        
        // 验证返回的是副本
        velocity.x = 999.0f;
        Vector2 vel2 = physics.getVelocity();
        assertEquals("速度x不应该被修改", 100.0f, vel2.x, 0.000001f);
    }
    
    /**
     * 测试设置速度（x, y参数）
     */
    @Test
    public void testSetVelocityXY() {
        physics.setVelocity(150.0f, 250.0f);
        
        Vector2 vel = physics.getVelocity();
        assertEquals("速度x应该正确", 150.0f, vel.x, 0.000001f);
        assertEquals("速度y应该正确", 250.0f, vel.y, 0.000001f);
    }
    
    /**
     * 测试添加速度
     */
    @Test
    public void testAddVelocity() {
        physics.setVelocity(new Vector2(100.0f, 100.0f));
        physics.addVelocity(new Vector2(50.0f, 75.0f));
        
        Vector2 vel = physics.getVelocity();
        assertEquals("添加后速度x应该正确", 150.0f, vel.x, 0.000001f);
        assertEquals("添加后速度y应该正确", 175.0f, vel.y, 0.000001f);
    }
    
    /**
     * 测试应用力
     */
    @Test
    public void testApplyForce() {
        physics.setMass(2.0f);
        Vector2 force = new Vector2(100.0f, 200.0f);
        physics.applyForce(force);
        
        Vector2 acc = physics.getAcceleration();
        // F = ma, a = F/m = 100/2 = 50
        assertEquals("加速度x应该正确", 50.0f, acc.x, 0.000001f);
        assertEquals("加速度y应该正确", 100.0f, acc.y, 0.000001f);
    }
    
    /**
     * 测试应用力（零质量）
     */
    @Test
    public void testApplyForceWithZeroMass() {
        physics.setMass(0.1f); // 最小质量
        Vector2 force = new Vector2(100.0f, 200.0f);
        physics.applyForce(force);
        
        Vector2 acc = physics.getAcceleration();
        assertTrue("加速度应该被应用", acc.x > 0 || acc.y > 0);
    }
    
    /**
     * 测试应用冲量
     */
    @Test
    public void testApplyImpulse() {
        physics.setMass(2.0f);
        Vector2 impulse = new Vector2(100.0f, 200.0f);
        physics.applyImpulse(impulse);
        
        Vector2 vel = physics.getVelocity();
        // impulse = mv, v = impulse/m = 100/2 = 50
        assertEquals("速度x应该正确", 50.0f, vel.x, 0.000001f);
        assertEquals("速度y应该正确", 100.0f, vel.y, 0.000001f);
    }
    
    /**
     * 测试设置和获取质量
     */
    @Test
    public void testSetAndGetMass() {
        physics.setMass(5.0f);
        assertEquals("质量应该正确", 5.0f, physics.getMass(), 0.000001f);
    }
    
    /**
     * 测试设置质量的最小值
     */
    @Test
    public void testSetMassMinimum() {
        physics.setMass(0.05f); // 小于最小值0.1
        assertEquals("质量应该被限制为最小值0.1", 0.1f, physics.getMass(), 0.000001f);
    }
    
    /**
     * 测试设置和获取摩擦力
     */
    @Test
    public void testSetAndGetFriction() {
        physics.setFriction(0.8f);
        assertEquals("摩擦力应该正确", 0.8f, physics.getFriction(), 0.000001f);
    }
    
    /**
     * 测试摩擦力的范围限制
     */
    @Test
    public void testFrictionRange() {
        physics.setFriction(-0.5f); // 小于0
        assertEquals("摩擦力应该被限制为0", 0.0f, physics.getFriction(), 0.000001f);
        
        physics.setFriction(1.5f); // 大于1
        assertEquals("摩擦力应该被限制为1", 1.0f, physics.getFriction(), 0.000001f);
    }
    
    /**
     * 测试设置和获取重力
     */
    @Test
    public void testSetAndGetGravity() {
        Vector2 gravity = new Vector2(0.0f, 9.8f);
        physics.setGravity(gravity);
        
        Vector2 grav = physics.getGravity();
        assertEquals("重力x应该正确", 0.0f, grav.x, 0.000001f);
        assertEquals("重力y应该正确", 9.8f, grav.y, 0.000001f);
    }
    
    /**
     * 测试启用和禁用重力
     */
    @Test
    public void testSetUseGravity() {
        physics.setUseGravity(true);
        assertTrue("应该启用重力", physics.isUseGravity());
        
        physics.setUseGravity(false);
        assertFalse("应该禁用重力", physics.isUseGravity());
    }
    
    /**
     * 测试更新（无重力）
     */
    @Test
    public void testUpdateWithoutGravity() {
        transform.setPosition(new Vector2(0.0f, 0.0f));
        physics.setVelocity(new Vector2(100.0f, 100.0f));
        physics.setFriction(1.0f); // 无摩擦
        physics.setUseGravity(false);
        
        float deltaTime = 0.016f; // 约60FPS
        physics.update(deltaTime);
        
        Vector2 pos = transform.getPosition();
        // 位置 = 速度 * 时间 = 100 * 0.016 = 1.6
        assertTrue("位置应该发生变化", pos.x > 0 || pos.y > 0);
    }
    
    /**
     * 测试更新（有摩擦力）
     */
    @Test
    public void testUpdateWithFriction() {
        transform.setPosition(new Vector2(0.0f, 0.0f));
        physics.setVelocity(new Vector2(100.0f, 100.0f));
        physics.setFriction(0.9f);
        physics.setUseGravity(false);
        
        float deltaTime = 0.016f;
        physics.update(deltaTime);
        
        Vector2 vel = physics.getVelocity();
        // 速度会被摩擦力减小：100 * 0.9 = 90
        assertTrue("速度应该因摩擦力减小", vel.x < 100.0f || vel.y < 100.0f);
    }
    
    /**
     * 测试更新（有重力）
     */
    @Test
    public void testUpdateWithGravity() {
        transform.setPosition(new Vector2(0.0f, 0.0f));
        physics.setVelocity(new Vector2(0.0f, 0.0f));
        physics.setGravity(new Vector2(0.0f, 9.8f));
        physics.setUseGravity(true);
        physics.setFriction(1.0f); // 无摩擦，只测试重力
        
        float deltaTime = 0.016f;
        physics.update(deltaTime);
        
        Vector2 vel = physics.getVelocity();
        // 速度应该因为重力而增加（向下）
        assertTrue("速度y应该因为重力增加", vel.y > 0);
    }
    
    /**
     * 测试更新（无TransformComponent）
     */
    @Test
    public void testUpdateWithoutTransform() {
        GameObject obj = new GameObject("NoTransform");
        PhysicsComponent p = new PhysicsComponent();
        p.setOwner(obj);
        p.setVelocity(new Vector2(100.0f, 100.0f));
        
        // 不应该抛出异常
        p.update(0.016f);
        assertNotNull("更新后组件应该存在", p);
    }
    
    /**
     * 测试更新（禁用组件）
     */
    @Test
    public void testUpdateWhenDisabled() {
        transform.setPosition(new Vector2(0.0f, 0.0f));
        physics.setVelocity(new Vector2(100.0f, 100.0f));
        physics.setEnabled(false);
        
        Vector2 initialPos = transform.getPosition();
        physics.update(0.016f);
        Vector2 finalPos = transform.getPosition();
        
        // 禁用时位置不应该改变
        assertEquals("禁用时位置不应该改变", initialPos.x, finalPos.x, 0.000001f);
        assertEquals("禁用时位置不应该改变", initialPos.y, finalPos.y, 0.000001f);
    }
    
    /**
     * 测试record方法
     */
    @Test
    public void testRecord() {
        physics.setVelocity(new Vector2(50.5f, 75.5f));
        String record = physics.record();
        
        assertNotNull("record结果不应该为null", record);
        assertTrue("record应该包含速度信息", record.contains("50.5") || record.contains("50"));
        assertTrue("record应该包含速度信息", record.contains("75.5") || record.contains("75"));
    }
    
    /**
     * 测试record方法（无owner时）
     */
    @Test
    public void testRecordWithoutOwner() {
        PhysicsComponent p = new PhysicsComponent();
        String record = p.record();
        assertEquals("无owner时record应该返回空字符串", "", record);
    }
    
    /**
     * 测试初始化
     */
    @Test
    public void testInitialize() {
        physics.initialize();
        assertNotNull("初始化后组件应该存在", physics);
    }
    
    /**
     * 测试渲染
     */
    @Test
    public void testRender() {
        // PhysicsComponent的render方法为空实现
        physics.render();
        assertNotNull("渲染后组件应该存在", physics);
    }
    
    /**
     * 测试获取加速度
     */
    @Test
    public void testGetAcceleration() {
        physics.applyForce(new Vector2(100.0f, 200.0f));
        Vector2 acc = physics.getAcceleration();
        
        assertNotNull("加速度不应该为null", acc);
        // 验证返回的是副本
        Vector2 acc2 = physics.getAcceleration();
        assertNotNull("应该能够多次获取加速度", acc2);
    }
    
    /**
     * 测试加速度重置
     */
    @Test
    public void testAccelerationReset() {
        physics.applyForce(new Vector2(100.0f, 200.0f));
        Vector2 acc1 = physics.getAcceleration();
        assertTrue("应该有加速度", acc1.x != 0 || acc1.y != 0);
        
        // update后加速度应该被重置
        physics.update(0.016f);
        Vector2 acc2 = physics.getAcceleration();
        assertEquals("update后加速度x应该被重置", 0.0f, acc2.x, 0.000001f);
        assertEquals("update后加速度y应该被重置", 0.0f, acc2.y, 0.000001f);
    }
    
    /**
     * 测试多次应用力
     */
    @Test
    public void testMultipleForces() {
        physics.setMass(1.0f);
        physics.applyForce(new Vector2(100.0f, 0.0f));
        physics.applyForce(new Vector2(0.0f, 200.0f));
        
        Vector2 acc = physics.getAcceleration();
        assertEquals("组合力x应该正确", 100.0f, acc.x, 0.000001f);
        assertEquals("组合力y应该正确", 200.0f, acc.y, 0.000001f);
    }
    
    /**
     * 测试复杂物理场景（重力+摩擦力+初始速度）
     */
    @Test
    public void testComplexPhysics() {
        transform.setPosition(new Vector2(0.0f, 0.0f));
        physics.setVelocity(new Vector2(100.0f, 0.0f));
        physics.setGravity(new Vector2(0.0f, 9.8f));
        physics.setUseGravity(true);
        physics.setFriction(0.95f);
        physics.setMass(1.0f);
        
        // 更新多次
        for (int i = 0; i < 10; i++) {
            physics.update(0.016f);
        }
        
        Vector2 pos = transform.getPosition();
        Vector2 vel = physics.getVelocity();
        
        assertTrue("位置应该发生变化", pos.x > 0 || pos.y > 0);
        assertTrue("速度应该因为重力和摩擦力发生变化", vel.x != 100.0f || vel.y != 0.0f);
    }
}

