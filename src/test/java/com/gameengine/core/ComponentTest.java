package com.gameengine.core;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import com.gameengine.components.TransformComponent;

/**
 * Component组件基类的测试用例
 * 测试组件的基本功能，包括初始化、更新、渲染、销毁等
 */
public class ComponentTest {
    
    private TransformComponent component;
    private GameObject owner;
    
    /**
     * 测试前的初始化工作
     */
    @Before
    public void setUp() {
        owner = new GameObject("TestObject");
        component = new TransformComponent();
        component.setOwner(owner);
    }
    
    /**
     * 测试组件的初始化
     */
    @Test
    public void testInitialize() {
        // 测试组件初始化不会抛出异常
        component.initialize();
        assertTrue("组件应该被正确初始化", component != null);
    }
    
    /**
     * 测试组件的更新功能
     */
    @Test
    public void testUpdate() {
        // 测试组件更新不会抛出异常
        component.update(0.016f); // 模拟60FPS的deltaTime
        assertTrue("组件更新应该成功", component != null);
    }
    
    /**
     * 测试组件的渲染功能
     */
    @Test
    public void testRender() {
        // 测试组件渲染不会抛出异常
        component.render();
        assertTrue("组件渲染应该成功", component != null);
    }
    
    /**
     * 测试组件的销毁功能
     */
    @Test
    public void testDestroy() {
        // 测试组件销毁
        assertTrue("组件应该处于启用状态", component.isEnabled());
        component.destroy();
        assertFalse("组件应该被禁用", component.isEnabled());
    }
    
    /**
     * 测试组件的所有者设置和获取
     */
    @Test
    public void testOwner() {
        // 测试设置和获取所有者
        component.setOwner(owner);
        assertEquals("所有者应该正确设置", owner, component.getOwner());
    }
    
    /**
     * 测试组件的启用/禁用状态
     */
    @Test
    public void testEnabled() {
        // 测试默认启用状态
        assertTrue("组件默认应该启用", component.isEnabled());
        
        // 测试禁用组件
        component.setEnabled(false);
        assertFalse("组件应该被禁用", component.isEnabled());
        
        // 测试重新启用组件
        component.setEnabled(true);
        assertTrue("组件应该被重新启用", component.isEnabled());
    }
    
    /**
     * 测试组件的名称设置和获取
     */
    @Test
    public void testName() {
        // 测试默认名称
        String defaultName = component.getName();
        assertNotNull("组件应该有默认名称", defaultName);
        
        // 测试设置名称
        String customName = "CustomComponent";
        component.setName(customName);
        assertEquals("组件名称应该正确设置", customName, component.getName());
    }
    
    /**
     * 测试组件的类型获取
     */
    @Test
    public void testGetComponentType() {
        // 测试获取组件类型
        Class<?> componentType = component.getComponentType();
        assertEquals("组件类型应该正确", TransformComponent.class, componentType);
    }
    
    /**
     * 测试组件的记录功能
     */
    @Test
    public void testRecord() {
        // 测试组件记录功能（默认实现返回空字符串）
        String record = component.record();
        assertNotNull("记录结果不应该为null", record);
    }
}


