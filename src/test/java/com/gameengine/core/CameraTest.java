package com.gameengine.core;

import org.junit.Test;
import org.junit.Before;
import static org.junit.Assert.*;

import com.gameengine.math.Vector2;

/**
 * Camera 相机类测试
 */
public class CameraTest {
    
    private Camera camera;
    
    @Before
    public void setUp() {
        // 创建一个800x600的视口，2000x1500的世界
        camera = new Camera(800, 600, 2000, 1500);
    }
    
    @Test
    public void testInitialPosition() {
        Vector2 pos = camera.getPosition();
        // 初始位置应该在世界中心
        assertEquals(1000, pos.x, 0.001f);
        assertEquals(750, pos.y, 0.001f);
    }
    
    @Test
    public void testSetPosition() {
        camera.setPosition(new Vector2(500, 400));
        Vector2 pos = camera.getPosition();
        assertEquals(500, pos.x, 0.001f);
        assertEquals(400, pos.y, 0.001f);
    }
    
    @Test
    public void testSetPositionClamping() {
        // 尝试将相机设置到超出边界的位置
        camera.setPosition(new Vector2(0, 0));
        Vector2 pos = camera.getPosition();
        // 应该被限制在视口的一半处（400, 300）
        assertEquals(400, pos.x, 0.001f);
        assertEquals(300, pos.y, 0.001f);
    }
    
    @Test
    public void testSetPositionClampingMaxBound() {
        camera.setPosition(new Vector2(3000, 3000));
        Vector2 pos = camera.getPosition();
        // 应该被限制在世界边界减去视口一半处
        assertEquals(1600, pos.x, 0.001f); // 2000 - 400
        assertEquals(1200, pos.y, 0.001f); // 1500 - 300
    }
    
    @Test
    public void testFollowImmediate() {
        Vector2 target = new Vector2(800, 600);
        camera.followImmediate(target);
        
        Vector2 pos = camera.getPosition();
        assertEquals(800, pos.x, 0.001f);
        assertEquals(600, pos.y, 0.001f);
    }
    
    @Test
    public void testFollowImmediateNull() {
        Vector2 originalPos = camera.getPosition();
        camera.followImmediate(null);
        
        Vector2 pos = camera.getPosition();
        assertEquals(originalPos.x, pos.x, 0.001f);
        assertEquals(originalPos.y, pos.y, 0.001f);
    }
    
    @Test
    public void testWorldToScreen() {
        camera.setPosition(new Vector2(500, 400));
        Vector2 worldPos = new Vector2(500, 400);
        Vector2 screenPos = camera.worldToScreen(worldPos);
        
        // 相机中心点应该映射到屏幕中心
        assertEquals(400, screenPos.x, 0.001f); // 800/2
        assertEquals(300, screenPos.y, 0.001f); // 600/2
    }
    
    @Test
    public void testWorldToScreenOffset() {
        camera.setPosition(new Vector2(500, 400));
        Vector2 worldPos = new Vector2(600, 500);
        Vector2 screenPos = camera.worldToScreen(worldPos);
        
        // 偏移100, 100应该映射到屏幕中心+100
        assertEquals(500, screenPos.x, 0.001f);
        assertEquals(400, screenPos.y, 0.001f);
    }
    
    @Test
    public void testWorldToScreenNull() {
        Vector2 result = camera.worldToScreen(null);
        assertEquals(0, result.x, 0.001f);
        assertEquals(0, result.y, 0.001f);
    }
    
    @Test
    public void testScreenToWorld() {
        camera.setPosition(new Vector2(500, 400));
        Vector2 screenPos = new Vector2(400, 300); // 屏幕中心
        Vector2 worldPos = camera.screenToWorld(screenPos);
        
        // 屏幕中心应该映射到相机位置
        assertEquals(500, worldPos.x, 0.001f);
        assertEquals(400, worldPos.y, 0.001f);
    }
    
    @Test
    public void testScreenToWorldNull() {
        Vector2 result = camera.screenToWorld(null);
        assertEquals(0, result.x, 0.001f);
        assertEquals(0, result.y, 0.001f);
    }
    
    @Test
    public void testIsVisibleCenter() {
        camera.setPosition(new Vector2(1000, 750));
        // 在相机中心的物体应该可见
        assertTrue(camera.isVisible(new Vector2(1000, 750), 50, 50));
    }
    
    @Test
    public void testIsVisibleEdge() {
        camera.setPosition(new Vector2(1000, 750));
        // 在视口边缘的物体应该可见
        assertTrue(camera.isVisible(new Vector2(650, 500), 50, 50));
    }
    
    @Test
    public void testIsVisibleOutside() {
        camera.setPosition(new Vector2(1000, 750));
        // 在视口外的物体不应该可见
        assertFalse(camera.isVisible(new Vector2(0, 0), 50, 50));
    }
    
    @Test
    public void testIsVisibleNull() {
        assertFalse(camera.isVisible(null, 50, 50));
    }
    
    @Test
    public void testIsPointVisible() {
        camera.setPosition(new Vector2(1000, 750));
        assertTrue(camera.isPointVisible(new Vector2(1000, 750)));
        assertFalse(camera.isPointVisible(new Vector2(0, 0)));
    }
    
    @Test
    public void testSetSmoothSpeed() {
        camera.setSmoothSpeed(0.5f);
        // 没有getter，但应该不抛异常
        
        // 测试边界情况
        camera.setSmoothSpeed(-1.0f); // 应该被限制为0
        camera.setSmoothSpeed(2.0f);  // 应该被限制为1
    }
    
    @Test
    public void testGetters() {
        assertEquals(800, camera.getViewportWidth(), 0.001f);
        assertEquals(600, camera.getViewportHeight(), 0.001f);
        assertEquals(2000, camera.getWorldWidth(), 0.001f);
        assertEquals(1500, camera.getWorldHeight(), 0.001f);
    }
    
    @Test
    public void testFollow() {
        camera.setPosition(new Vector2(500, 400));
        Vector2 target = new Vector2(800, 600);
        
        // 平滑跟随应该使相机向目标移动
        camera.follow(target, 0.016f); // 约60fps的一帧
        
        Vector2 pos = camera.getPosition();
        // 相机应该向目标移动，但不会立即到达
        assertTrue(pos.x > 500 && pos.x < 800);
        assertTrue(pos.y > 400 && pos.y < 600);
    }
    
    @Test
    public void testFollowNull() {
        Vector2 originalPos = camera.getPosition();
        camera.follow(null, 0.016f);
        
        Vector2 pos = camera.getPosition();
        assertEquals(originalPos.x, pos.x, 0.001f);
        assertEquals(originalPos.y, pos.y, 0.001f);
    }
    
    @Test
    public void testSmallWorldCamera() {
        // 测试世界比视口小的情况
        Camera smallCamera = new Camera(800, 600, 400, 300);
        Vector2 pos = smallCamera.getPosition();
        
        // 相机应该固定在世界中心
        assertEquals(200, pos.x, 0.001f);
        assertEquals(150, pos.y, 0.001f);
    }
    
    @Test
    public void testWorldToScreenAndBack() {
        camera.setPosition(new Vector2(700, 500));
        Vector2 original = new Vector2(800, 600);
        
        Vector2 screen = camera.worldToScreen(original);
        Vector2 back = camera.screenToWorld(screen);
        
        assertEquals(original.x, back.x, 0.001f);
        assertEquals(original.y, back.y, 0.001f);
    }
}
