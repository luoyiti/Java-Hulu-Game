package com.gameengine.input;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import com.gameengine.math.Vector2;

/**
 * InputManager输入管理器类的测试用例
 * 测试键盘输入、鼠标输入、输入状态管理等功能
 */
public class InputManagerTest {
    
    private InputManager inputManager;
    
    /**
     * 测试前的初始化工作
     */
    @Before
    public void setUp() {
        // 获取单例实例
        inputManager = InputManager.getInstance();
        // 清理单例状态，避免测试之间相互影响
        inputManager.reset();
    }
    
    /**
     * 测试单例模式
     */
    @Test
    public void testSingleton() {
        InputManager instance1 = InputManager.getInstance();
        InputManager instance2 = InputManager.getInstance();
        
        // 验证两次获取的是同一个实例
        assertEquals("应该返回同一个实例", instance1, instance2);
    }
    
    /**
     * 测试键盘按下事件
     */
    @Test
    public void testKeyPressed() {
        int keyCode = 65; // 'A'键
        
        // 模拟按键按下
        inputManager.onKeyPressed(keyCode);
        
        // 验证按键被按下
        assertTrue("按键应该被按下", inputManager.isKeyPressed(keyCode));
    }
    
    /**
     * 测试键盘释放事件
     */
    @Test
    public void testKeyReleased() {
        int keyCode = 65; // 'A'键
        
        // 先按下按键
        inputManager.onKeyPressed(keyCode);
        assertTrue("按键应该被按下", inputManager.isKeyPressed(keyCode));
        
        // 释放按键
        inputManager.onKeyReleased(keyCode);
        
        // 验证按键被释放
        assertFalse("按键应该被释放", inputManager.isKeyPressed(keyCode));
    }
    
    /**
     * 测试按键刚刚被按下（只在这一帧为true）
     */
    @Test
    public void testKeyJustPressed() {
        int keyCode = 65; // 'A'键
        
        // 测试初始状态
        assertFalse("按键初始不应该刚刚被按下", inputManager.isKeyJustPressed(keyCode));
        
        // 模拟按键按下
        inputManager.onKeyPressed(keyCode);
        
        // 验证按键刚刚被按下
        assertTrue("按键应该刚刚被按下", inputManager.isKeyJustPressed(keyCode));
        
        // 更新输入状态（清除justPressed状态）
        inputManager.update();
        
        // 验证justPressed状态被清除
        assertFalse("更新后按键不应该刚刚被按下", inputManager.isKeyJustPressed(keyCode));
        // 但按键仍然被按下
        assertTrue("按键应该仍然被按下", inputManager.isKeyPressed(keyCode));
    }
    
    /**
     * 测试鼠标移动事件
     */
    @Test
    public void testMouseMoved() {
        float x = 100.0f;
        float y = 200.0f;
        
        // 模拟鼠标移动
        inputManager.onMouseMoved(x, y);
        
        // 验证鼠标位置
        assertEquals("鼠标X坐标应该正确", x, inputManager.getMouseX(), 0.000001f);
        assertEquals("鼠标Y坐标应该正确", y, inputManager.getMouseY(), 0.000001f);
        
        Vector2 position = inputManager.getMousePosition();
        assertEquals("鼠标位置X应该正确", x, position.x, 0.000001f);
        assertEquals("鼠标位置Y应该正确", y, position.y, 0.000001f);
    }
    
    /**
     * 测试鼠标按下事件
     */
    @Test
    public void testMousePressed() {
        int button = 1; // 左键（Java MouseEvent.BUTTON1 = 1）
        
        // 测试鼠标按键未按下
        assertFalse("鼠标按键初始应该未按下", inputManager.isMouseButtonPressed(button));
        
        // 模拟鼠标按下
        inputManager.onMousePressed(button);
        
        // 验证鼠标按键被按下
        assertTrue("鼠标按键应该被按下", inputManager.isMouseButtonPressed(button));
    }
    
    /**
     * 测试鼠标释放事件
     */
    @Test
    public void testMouseReleased() {
        int button = 1; // 左键
        
        // 先按下鼠标按键
        inputManager.onMousePressed(button);
        assertTrue("鼠标按键应该被按下", inputManager.isMouseButtonPressed(button));
        
        // 释放鼠标按键
        inputManager.onMouseReleased(button);
        
        // 验证鼠标按键被释放
        assertFalse("鼠标按键应该被释放", inputManager.isMouseButtonPressed(button));
    }
    
    /**
     * 测试鼠标按键刚刚被按下
     */
    @Test
    public void testMouseButtonJustPressed() {
        int button = 1; // 左键
        
        // 模拟鼠标按下
        inputManager.onMousePressed(button);
        
        // 验证鼠标按键刚刚被按下
        assertTrue("鼠标按键应该刚刚被按下", inputManager.isMouseButtonJustPressed(button));
        
        // 更新输入状态（清除justPressed状态）
        inputManager.update();
        
        // 验证justPressed状态被清除
        assertFalse("更新后鼠标按键不应该刚刚被按下", inputManager.isMouseButtonJustPressed(button));
        // 但鼠标按键仍然被按下
        assertTrue("鼠标按键应该仍然被按下", inputManager.isMouseButtonPressed(button));
    }
    
    /**
     * 测试多个按键同时按下
     */
    @Test
    public void testMultipleKeys() {
        int keyA = 65; // 'A'键
        int keyB = 66; // 'B'键
        int keyC = 67; // 'C'键
        
        // 按下多个按键
        inputManager.onKeyPressed(keyA);
        inputManager.onKeyPressed(keyB);
        inputManager.onKeyPressed(keyC);
        
        // 验证所有按键都被按下
        assertTrue("A键应该被按下", inputManager.isKeyPressed(keyA));
        assertTrue("B键应该被按下", inputManager.isKeyPressed(keyB));
        assertTrue("C键应该被按下", inputManager.isKeyPressed(keyC));
        
        // 释放一个按键
        inputManager.onKeyReleased(keyB);
        
        // 验证只有B键被释放
        assertTrue("A键应该仍然被按下", inputManager.isKeyPressed(keyA));
        assertFalse("B键应该被释放", inputManager.isKeyPressed(keyB));
        assertTrue("C键应该仍然被按下", inputManager.isKeyPressed(keyC));
    }
    
    /**
     * 测试输入状态的更新
     */
    @Test
    public void testUpdate() {
        int keyCode = 65; // 'A'键
        int mouseButton = 1; // 左键
        
        // 先清理可能存在的状态（因为InputManager是单例，之前的测试可能已经按下过这些键）
        // 先释放按键和鼠标，确保它们不在pressed状态
        inputManager.onKeyReleased(keyCode);
        inputManager.onMouseReleased(mouseButton);
        // 清理justPressed状态
        inputManager.update();
        
        // 现在确保按键和鼠标都未被按下
        assertFalse("按键应该未被按下", inputManager.isKeyPressed(keyCode));
        assertFalse("鼠标按键应该未被按下", inputManager.isMouseButtonPressed(mouseButton));
        
        // 按下按键和鼠标
        inputManager.onKeyPressed(keyCode);
        // 验证justPressed状态
        assertTrue("按键应该刚刚被按下", inputManager.isKeyJustPressed(keyCode));
        inputManager.onMousePressed(mouseButton);
        assertTrue("鼠标按键应该刚刚被按下", inputManager.isMouseButtonJustPressed(mouseButton));
        
        // 更新输入状态
        inputManager.update();
        
        // 验证justPressed状态被清除
        assertFalse("更新后按键不应该刚刚被按下", inputManager.isKeyJustPressed(keyCode));
        assertFalse("更新后鼠标按键不应该刚刚被按下", inputManager.isMouseButtonJustPressed(mouseButton));
        
        // 但pressed状态应该保持
        assertTrue("按键应该仍然被按下", inputManager.isKeyPressed(keyCode));
        assertTrue("鼠标按键应该仍然被按下", inputManager.isMouseButtonPressed(mouseButton));
    }
    
    /**
     * 测试无效的鼠标按键
     */
    @Test
    public void testInvalidMouseButton() {
        int invalidButton = 10; // 无效的按键索引
        
        // 测试无效按键应该返回false
        assertFalse("无效的鼠标按键应该返回false", inputManager.isMouseButtonPressed(invalidButton));
        assertFalse("无效的鼠标按键justPressed应该返回false", inputManager.isMouseButtonJustPressed(invalidButton));
    }
}
