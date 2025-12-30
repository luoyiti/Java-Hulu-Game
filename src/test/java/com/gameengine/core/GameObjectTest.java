package com.gameengine.core;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import com.gameengine.components.LifeFeatureComponent;
import com.gameengine.components.PhysicsComponent;
import com.gameengine.components.RenderComponent;
import com.gameengine.components.TransformComponent;
import com.gameengine.math.Vector2;

/**
 * GameObject游戏对象类的测试用例
 * 测试游戏对象的创建、组件管理、更新、渲染等功能
 */
public class GameObjectTest {
    
    private GameObject gameObject;
    
    /**
     * 测试前的初始化工作
     */
    @Before
    public void setUp() {
        gameObject = new GameObject("TestObject");
    }
    
    /**
     * 测试游戏对象的创建
     */
    @Test
    public void testCreation() {
        // 测试默认创建的游戏对象
        assertNotNull("游戏对象应该被创建", gameObject);
        assertTrue("游戏对象应该处于活跃状态", gameObject.isActive());
        assertEquals("游戏对象名称应该正确", "TestObject", gameObject.getName());
    }
    
    /**
     * 测试游戏对象的名称设置
     */
    @Test
    public void testName() {
        // 测试设置名称
        gameObject.setName("NewName");
        assertEquals("游戏对象名称应该正确设置", "NewName", gameObject.getName());
    }
    
    /**
     * 测试游戏对象的活跃状态
     */
    @Test
    public void testActive() {
        // 测试默认活跃状态
        assertTrue("游戏对象默认应该活跃", gameObject.isActive());
        
        // 测试设置非活跃状态
        gameObject.setActive(false);
        assertFalse("游戏对象应该被设置为非活跃", gameObject.isActive());
        
        // 测试重新设置为活跃状态
        gameObject.setActive(true);
        assertTrue("游戏对象应该被重新设置为活跃", gameObject.isActive());
    }
    
    /**
     * 测试添加组件
     */
    @Test
    public void testAddComponent() {
        // 测试添加TransformComponent
        TransformComponent transform = new TransformComponent();
        TransformComponent added = gameObject.addComponent(transform);
        
        assertNotNull("添加的组件不应该为null", added);
        assertEquals("添加的组件应该正确", transform, added);
        assertEquals("组件的所有者应该被设置", gameObject, added.getOwner());
    }
    
    /**
     * 测试获取组件
     */
    @Test
    public void testGetComponent() {
        // 添加组件
        TransformComponent transform = new TransformComponent();
        gameObject.addComponent(transform);
        
        // 测试获取组件
        TransformComponent retrieved = gameObject.getComponent(TransformComponent.class);
        assertNotNull("获取的组件不应该为null", retrieved);
        assertEquals("获取的组件应该正确", transform, retrieved);
        
        // 测试获取不存在的组件
        PhysicsComponent physics = gameObject.getComponent(PhysicsComponent.class);
        assertNull("不存在的组件应该返回null", physics);
    }
    
    /**
     * 测试检查是否有组件
     */
    @Test
    public void testHasComponent() {
        // 测试没有组件的情况
        assertFalse("不应该有TransformComponent", gameObject.hasComponent(TransformComponent.class));
        
        // 添加组件后测试
        gameObject.addComponent(new TransformComponent());
        assertTrue("应该有TransformComponent", gameObject.hasComponent(TransformComponent.class));
    }
    
    /**
     * 测试游戏对象的更新
     */
    @Test
    public void testUpdate() {
        // 添加组件
        TransformComponent transform = new TransformComponent(new Vector2(10, 20));
        gameObject.addComponent(transform);
        
        // 测试更新不会抛出异常
        gameObject.update(0.016f); // 模拟60FPS的deltaTime
        
        // 验证组件仍然存在
        assertNotNull("组件应该仍然存在", gameObject.getComponent(TransformComponent.class));
    }
    
    /**
     * 测试游戏对象的渲染
     */
    @Test
    public void testRender() {
        // 添加组件
        TransformComponent transform = new TransformComponent();
        gameObject.addComponent(transform);
        
        // 测试渲染不会抛出异常
        gameObject.render();
        
        // 验证组件仍然存在
        assertNotNull("组件应该仍然存在", gameObject.getComponent(TransformComponent.class));
    }
    
    /**
     * 测试游戏对象的初始化
     */
    @Test
    public void testInitialize() {
        // 测试初始化不会抛出异常
        gameObject.initialize();
        assertTrue("游戏对象应该被正确初始化", gameObject != null);
    }
    
    /**
     * 测试游戏对象的销毁
     */
    @Test
    public void testDestroy() {
        // 添加组件
        TransformComponent transform = new TransformComponent();
        gameObject.addComponent(transform);
        
        // 测试销毁
        assertTrue("游戏对象应该处于活跃状态", gameObject.isActive());
        gameObject.destroy();
        assertFalse("游戏对象应该被销毁（非活跃）", gameObject.isActive());
        assertFalse("组件应该被禁用", transform.isEnabled());
    }
    
    /**
     * 测试游戏对象的身份设置
     */
    @Test
    public void testIdentity() {
        // 测试默认身份
        assertEquals("默认身份应该是None", "None", gameObject.getidentity());
        
        // 测试设置玩家身份
        gameObject.setPlayer();
        assertEquals("身份应该是Player", "Player", gameObject.getidentity());
        
        // 测试设置敌人身份
        gameObject.setEnemy();
        assertEquals("身份应该是Enemy", "Enemy", gameObject.getidentity());
        
        // 测试设置玩家技能身份
        gameObject.setPlayerSkill();
        assertEquals("身份应该是Player Skill", "Player Skill", gameObject.getidentity());
        
        // 测试设置敌人技能身份
        gameObject.setEnemySkill();
        assertEquals("身份应该是Enemy Skill", "Enemy Skill", gameObject.getidentity());
        
        // 测试设置图片敌人身份
        gameObject.setImageEnemy();
        assertEquals("身份应该是ImageEnemy", "ImageEnemy", gameObject.getidentity());
    }
    
    /**
     * 测试多个组件的管理
     */
    @Test
    public void testMultipleComponents() {
        // 添加多个不同类型的组件
        TransformComponent transform = new TransformComponent();
        PhysicsComponent physics = new PhysicsComponent();
        LifeFeatureComponent life = new LifeFeatureComponent(100);
        RenderComponent render = new RenderComponent();
        
        gameObject.addComponent(transform);
        gameObject.addComponent(physics);
        gameObject.addComponent(life);
        gameObject.addComponent(render);
        
        // 验证所有组件都被正确添加
        assertTrue("应该有TransformComponent", gameObject.hasComponent(TransformComponent.class));
        assertTrue("应该有PhysicsComponent", gameObject.hasComponent(PhysicsComponent.class));
        assertTrue("应该有LifeFeatureComponent", gameObject.hasComponent(LifeFeatureComponent.class));
        assertTrue("应该有RenderComponent", gameObject.hasComponent(RenderComponent.class));
        
        // 验证可以正确获取所有组件
        assertNotNull("应该能获取TransformComponent", gameObject.getComponent(TransformComponent.class));
        assertNotNull("应该能获取PhysicsComponent", gameObject.getComponent(PhysicsComponent.class));
        assertNotNull("应该能获取LifeFeatureComponent", gameObject.getComponent(LifeFeatureComponent.class));
        assertNotNull("应该能获取RenderComponent", gameObject.getComponent(RenderComponent.class));
    }
    
    /**
     * 测试游戏对象的记录功能
     */
    @Test
    public void testGetRecords() {
        // 添加必要的组件
        TransformComponent transform = new TransformComponent(new Vector2(100, 200));
        LifeFeatureComponent life = new LifeFeatureComponent(80);
        gameObject.addComponent(transform);
        gameObject.addComponent(life);
        
        // 测试获取记录
        com.gameengine.game.GameObjectRecord record = gameObject.getRecords();
        assertNotNull("记录不应该为null", record);
        assertEquals("记录ID应该正确", "TestObject", record.id);
    }
    
    /**
     * 测试设置玩家身份
     */
    @Test
    public void testSetPlayer() {
        gameObject.setPlayer();
        assertEquals("Player", gameObject.getidentity());
    }
    
    /**
     * 测试设置敌人身份
     */
    @Test
    public void testSetEnemy() {
        gameObject.setEnemy();
        assertEquals("Enemy", gameObject.getidentity());
    }
    
    /**
     * 测试设置玩家技能身份
     */
    @Test
    public void testSetPlayerSkill() {
        gameObject.setPlayerSkill();
        assertEquals("Player Skill", gameObject.getidentity());
    }
    
    /**
     * 测试设置敌人技能身份
     */
    @Test
    public void testSetEnemySkill() {
        gameObject.setEnemySkill();
        assertEquals("Enemy Skill", gameObject.getidentity());
    }
    
    /**
     * 测试设置图片敌人身份
     */
    @Test
    public void testSetImageEnemy() {
        gameObject.setImageEnemy();
        assertEquals("ImageEnemy", gameObject.getidentity());
    }
    
    /**
     * 测试默认构造函数
     */
    @Test
    public void testDefaultConstructor() {
        GameObject obj = new GameObject();
        assertNotNull(obj);
        assertTrue(obj.isActive());
        assertEquals("GameObject", obj.getName());
        assertEquals("None", obj.getidentity());
    }
    
    /**
     * 测试getRecordsWithRenderComponent方法
     */
    @Test
    public void testGetRecordsWithRenderComponent() {
        TransformComponent transform = new TransformComponent(new Vector2(50, 100));
        LifeFeatureComponent life = new LifeFeatureComponent(100);
        RenderComponent render = new RenderComponent();
        render.setSize(new Vector2(32, 32));
        
        gameObject.addComponent(transform);
        gameObject.addComponent(life);
        gameObject.addComponent(render);
        
        com.gameengine.game.GameObjectRecord record = gameObject.getRecords();
        assertNotNull(record);
        assertEquals(50, record.x, 0.01f);
        assertEquals(100, record.y, 0.01f);
    }
}
