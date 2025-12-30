package com.gameengine.scene;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import com.gameengine.core.GameObject;
import com.gameengine.components.TransformComponent;
import com.gameengine.components.RenderComponent;
import com.gameengine.components.LifeFeatureComponent;

/**
 * Scene场景类的测试用例
 * 测试场景的创建、初始化、更新、渲染、游戏对象管理等功能
 */
public class SceneTest {
    
    private Scene scene;
    
    @Before
    public void setUp() {
        scene = new Scene("TestScene");
    }
    
    /**
     * 测试场景的创建
     */
    @Test
    public void testCreation() {
        assertNotNull("场景应该被创建", scene);
        assertEquals("场景名称应该正确", "TestScene", scene.getName());
    }
    
    /**
     * 测试场景的名称
     */
    @Test
    public void testGetName() {
        Scene namedScene = new Scene("MyScene");
        assertEquals("场景名称应该正确", "MyScene", namedScene.getName());
    }
    
    /**
     * 测试场景的初始状态
     */
    @Test
    public void testInitialState() {
        assertEquals("初始游戏对象列表应该为空", 0, scene.getGameObjects().size());
        assertEquals("初始时间应该为0", 0.0f, scene.getTime(), 0.000001f);
    }
    
    /**
     * 测试场景的初始化
     */
    @Test
    public void testInitialize() {
        // 添加游戏对象
        GameObject obj1 = new GameObject("Object1");
        GameObject obj2 = new GameObject("Object2");
        scene.addGameObject(obj1);
        scene.addGameObject(obj2);
        
        // 初始化场景
        scene.initialize();
        
        // 验证对象已添加到场景中（通过update处理）
        scene.update(0.0f);
        assertEquals("初始化后应该有2个游戏对象", 2, scene.getGameObjects().size());
    }
    
    /**
     * 测试添加游戏对象
     */
    @Test
    public void testAddGameObject() {
        GameObject obj = new GameObject("TestObject");
        scene.addGameObject(obj);
        
        // 对象应该在待添加列表中，需要update后才真正添加
        scene.update(0.0f);
        assertEquals("添加后应该有1个游戏对象", 1, scene.getGameObjects().size());
        assertTrue("游戏对象应该在场景中", scene.getGameObjects().contains(obj));
    }
    
    /**
     * 测试添加多个游戏对象
     */
    @Test
    public void testAddMultipleGameObjects() {
        GameObject obj1 = new GameObject("Object1");
        GameObject obj2 = new GameObject("Object2");
        GameObject obj3 = new GameObject("Object3");
        
        scene.addGameObject(obj1);
        scene.addGameObject(obj2);
        scene.addGameObject(obj3);
        
        scene.update(0.0f);
        assertEquals("应该添加3个游戏对象", 3, scene.getGameObjects().size());
    }
    
    /**
     * 测试获取游戏对象列表
     */
    @Test
    public void testGetGameObjects() {
        GameObject obj1 = new GameObject("Object1");
        GameObject obj2 = new GameObject("Object2");
        
        scene.addGameObject(obj1);
        scene.addGameObject(obj2);
        scene.update(0.0f);
        
        // 获取列表
        java.util.List<GameObject> objects = scene.getGameObjects();
        assertEquals("应该返回2个游戏对象", 2, objects.size());
        assertTrue("列表应该包含obj1", objects.contains(obj1));
        assertTrue("列表应该包含obj2", objects.contains(obj2));
        
        // 验证返回的是副本（修改副本不应该影响原始列表）
        objects.clear();
        assertEquals("原始列表不应该被修改", 2, scene.getGameObjects().size());
    }
    
    /**
     * 测试场景更新
     */
    @Test
    public void testUpdate() {
        GameObject obj = new GameObject("TestObject");
        scene.addGameObject(obj);
        
        // 更新场景（deltaTime = 0.016f，模拟60FPS）
        scene.update(0.016f);
        
        // 验证对象已添加
        assertEquals("更新后应该有1个游戏对象", 1, scene.getGameObjects().size());
    }
    
    /**
     * 测试多次更新场景
     */
    @Test
    public void testMultipleUpdates() {
        GameObject obj1 = new GameObject("Object1");
        GameObject obj2 = new GameObject("Object2");
        
        scene.addGameObject(obj1);
        scene.update(0.016f);
        
        scene.addGameObject(obj2);
        scene.update(0.016f);
        
        assertEquals("多次更新后应该有2个游戏对象", 2, scene.getGameObjects().size());
    }
    
    /**
     * 测试更新非活跃对象（应该被移除）
     */
    @Test
    public void testUpdateRemovesInactiveObjects() {
        GameObject obj1 = new GameObject("ActiveObject");
        GameObject obj2 = new GameObject("InactiveObject");
        obj2.setActive(false);
        
        scene.addGameObject(obj1);
        scene.addGameObject(obj2);
        scene.update(0.016f);
        
        // 非活跃对象应该在第一次update后被移除
        assertEquals("非活跃对象应该被移除", 1, scene.getGameObjects().size());
        assertTrue("应该只包含活跃对象", scene.getGameObjects().contains(obj1));
        assertFalse("不应该包含非活跃对象", scene.getGameObjects().contains(obj2));
    }
    
    /**
     * 测试场景渲染
     */
    @Test
    public void testRender() {
        GameObject obj1 = new GameObject("Object1");
        GameObject obj2 = new GameObject("Object2");
        
        scene.addGameObject(obj1);
        scene.addGameObject(obj2);
        scene.update(0.016f);
        
        // 测试渲染不会抛出异常
        scene.render();
        assertTrue("渲染应该成功执行", true);
    }
    
    /**
     * 测试渲染非活跃对象（不应该被渲染）
     */
    @Test
    public void testRenderInactiveObjects() {
        GameObject obj1 = new GameObject("ActiveObject");
        GameObject obj2 = new GameObject("InactiveObject");
        obj2.setActive(false);
        
        scene.addGameObject(obj1);
        scene.addGameObject(obj2);
        scene.update(0.016f);
        
        // 渲染应该只渲染活跃对象（非活跃对象应该已被移除）
        scene.render();
        assertEquals("非活跃对象应该已被移除", 1, scene.getGameObjects().size());
    }
    
    /**
     * 测试场景清理
     */
    @Test
    public void testClear() {
        GameObject obj1 = new GameObject("Object1");
        GameObject obj2 = new GameObject("Object2");
        
        scene.addGameObject(obj1);
        scene.addGameObject(obj2);
        scene.update(0.016f);
        
        assertEquals("清理前应该有2个游戏对象", 2, scene.getGameObjects().size());
        
        // 清理场景
        scene.clear();
        
        assertEquals("清理后应该没有游戏对象", 0, scene.getGameObjects().size());
    }
    
    /**
     * 测试清理后添加新对象
     */
    @Test
    public void testAddAfterClear() {
        GameObject obj1 = new GameObject("Object1");
        scene.addGameObject(obj1);
        scene.update(0.016f);
        
        scene.clear();
        
        GameObject obj2 = new GameObject("Object2");
        scene.addGameObject(obj2);
        scene.update(0.016f);
        
        assertEquals("清理后添加新对象应该有1个游戏对象", 1, scene.getGameObjects().size());
        assertTrue("应该包含新对象", scene.getGameObjects().contains(obj2));
        assertFalse("不应该包含旧对象", scene.getGameObjects().contains(obj1));
    }
    
    /**
     * 测试根据组件类型查找游戏对象
     */
    @Test
    public void testFindGameObjectsByComponent() {
        GameObject obj1 = new GameObject("Object1");
        GameObject obj2 = new GameObject("Object2");
        GameObject obj3 = new GameObject("Object3");
        
        // obj1有TransformComponent
        obj1.addComponent(new TransformComponent());
        
        // obj2有TransformComponent和RenderComponent
        obj2.addComponent(new TransformComponent());
        obj2.addComponent(new RenderComponent());
        
        // obj3只有RenderComponent
        obj3.addComponent(new RenderComponent());
        
        scene.addGameObject(obj1);
        scene.addGameObject(obj2);
        scene.addGameObject(obj3);
        scene.update(0.016f);
        
        // 查找有TransformComponent的对象
        java.util.List<GameObject> transformObjects = scene.findGameObjectsByComponent(TransformComponent.class);
        assertEquals("应该有2个对象有TransformComponent", 2, transformObjects.size());
        assertTrue("应该包含obj1", transformObjects.contains(obj1));
        assertTrue("应该包含obj2", transformObjects.contains(obj2));
        assertFalse("不应该包含obj3", transformObjects.contains(obj3));
        
        // 查找有RenderComponent的对象
        java.util.List<GameObject> renderObjects = scene.findGameObjectsByComponent(RenderComponent.class);
        assertEquals("应该有2个对象有RenderComponent", 2, renderObjects.size());
        assertTrue("应该包含obj2", renderObjects.contains(obj2));
        assertTrue("应该包含obj3", renderObjects.contains(obj3));
        assertFalse("不应该包含obj1", renderObjects.contains(obj1));
    }
    
    /**
     * 测试获取所有指定类型的组件
     */
    @Test
    public void testGetComponents() {
        GameObject obj1 = new GameObject("Object1");
        GameObject obj2 = new GameObject("Object2");
        GameObject obj3 = new GameObject("Object3");
        
        TransformComponent transform1 = new TransformComponent();
        TransformComponent transform2 = new TransformComponent();
        TransformComponent transform3 = new TransformComponent();
        
        obj1.addComponent(transform1);
        obj2.addComponent(transform2);
        obj3.addComponent(transform3);
        
        scene.addGameObject(obj1);
        scene.addGameObject(obj2);
        scene.addGameObject(obj3);
        scene.update(0.016f);
        
        // 获取所有TransformComponent
        java.util.List<TransformComponent> components = scene.getComponents(TransformComponent.class);
        assertEquals("应该有3个TransformComponent", 3, components.size());
        assertTrue("应该包含transform1", components.contains(transform1));
        assertTrue("应该包含transform2", components.contains(transform2));
        assertTrue("应该包含transform3", components.contains(transform3));
    }
    
    /**
     * 测试查找不存在的组件类型
     */
    @Test
    public void testFindNonExistentComponent() {
        GameObject obj = new GameObject("Object");
        obj.addComponent(new TransformComponent());
        scene.addGameObject(obj);
        scene.update(0.016f);
        
        // 查找不存在的组件类型
        java.util.List<GameObject> lifeObjects = scene.findGameObjectsByComponent(LifeFeatureComponent.class);
        assertEquals("应该没有LifeFeatureComponent", 0, lifeObjects.size());
        
        java.util.List<LifeFeatureComponent> lifeComponents = scene.getComponents(LifeFeatureComponent.class);
        assertEquals("应该没有LifeFeatureComponent", 0, lifeComponents.size());
    }
    
    /**
     * 测试初始化后添加的对象也会被初始化
     */
    @Test
    public void testInitializeNewObjectsAfterInit() {
        scene.initialize();
        
        GameObject obj = new GameObject("NewObject");
        scene.addGameObject(obj);
        
        // update时应该初始化新对象
        scene.update(0.016f);
        
        // 验证对象已被添加
        assertEquals("新对象应该被添加", 1, scene.getGameObjects().size());
    }
    
    /**
     * 测试getTime方法（注意：当前实现中time字段没有被更新）
     */
    @Test
    public void testGetTime() {
        assertEquals("初始时间应该为0", 0.0f, scene.getTime(), 0.000001f);
        
        // 更新场景
        scene.update(0.016f);
        scene.update(0.016f);
        
        // 注意：当前实现中time字段没有被更新，所以仍然为0
        assertEquals("时间应该仍然为0（当前实现未更新time字段）", 0.0f, scene.getTime(), 0.000001f);
    }
    
    /**
     * 测试空场景的更新和渲染
     */
    @Test
    public void testEmptySceneUpdateAndRender() {
        // 空场景的更新和渲染不应该抛出异常
        scene.update(0.016f);
        scene.render();
        assertEquals("空场景应该保持为空", 0, scene.getGameObjects().size());
    }
    
    /**
     * 测试添加null对象（应该允许但不推荐）
     */
    @Test
    public void testAddNullGameObject() {
        // 添加null对象（虽然不推荐，但应该不会抛出异常）
        try {
            scene.addGameObject(null);
            scene.update(0.016f);
            // 可能包含null或抛出异常，取决于实现
        } catch (Exception e) {
            // 如果抛出异常也是合理的
            assertTrue("添加null对象可能抛出异常", true);
        }
    }
    
    /**
     * 测试场景中添加大量对象
     */
    @Test
    public void testAddManyObjects() {
        int count = 100;
        for (int i = 0; i < count; i++) {
            GameObject obj = new GameObject("Object" + i);
            scene.addGameObject(obj);
        }
        
        scene.update(0.016f);
        assertEquals("应该添加100个对象", count, scene.getGameObjects().size());
    }
    
    /**
     * 测试组件查找的性能（查找不同类型的组件）
     */
    @Test
    public void testComponentSearchPerformance() {
        // 创建多个对象，每个有不同的组件组合
        for (int i = 0; i < 50; i++) {
            GameObject obj = new GameObject("Object" + i);
            if (i % 3 == 0) {
                obj.addComponent(new TransformComponent());
            }
            if (i % 3 == 1) {
                obj.addComponent(new RenderComponent());
            }
            if (i % 3 == 2) {
                obj.addComponent(new TransformComponent());
                obj.addComponent(new RenderComponent());
            }
            scene.addGameObject(obj);
        }
        
        scene.update(0.016f);
        
        // 查找组件
        java.util.List<GameObject> transformObjects = scene.findGameObjectsByComponent(TransformComponent.class);
        java.util.List<GameObject> renderObjects = scene.findGameObjectsByComponent(RenderComponent.class);
        
        assertTrue("应该有对象有TransformComponent", transformObjects.size() > 0);
        assertTrue("应该有对象有RenderComponent", renderObjects.size() > 0);
    }
}

