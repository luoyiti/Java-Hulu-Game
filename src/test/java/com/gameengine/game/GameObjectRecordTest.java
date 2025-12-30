package com.gameengine.game;

import org.junit.Test;
import static org.junit.Assert.*;

import com.gameengine.components.RenderComponent.RenderType;

/**
 * GameObjectRecord 类测试
 */
public class GameObjectRecordTest {
    
    @Test
    public void testFieldAssignment() {
        GameObjectRecord record = new GameObjectRecord();
        
        record.id = "player-1";
        record.x = 100.0f;
        record.y = 200.0f;
        record.width = 50.0f;
        record.height = 60.0f;
        record.alpha = 1.0f;
        record.rotation = 45.0f;
        record.currentHealth = 80;
        record.maxHealth = 100;
        record.identity = "Player";
        
        assertEquals("player-1", record.id);
        assertEquals(100.0f, record.x, 0.001f);
        assertEquals(200.0f, record.y, 0.001f);
        assertEquals(50.0f, record.width, 0.001f);
        assertEquals(60.0f, record.height, 0.001f);
        assertEquals(1.0f, record.alpha, 0.001f);
        assertEquals(45.0f, record.rotation, 0.001f);
        assertEquals(80, record.currentHealth);
        assertEquals(100, record.maxHealth);
        assertEquals("Player", record.identity);
    }
    
    @Test
    public void testRenderTypeAssignment() {
        GameObjectRecord record = new GameObjectRecord();
        record.rt = RenderType.CIRCLE;
        assertEquals(RenderType.CIRCLE, record.rt);
        
        record.rt = RenderType.RECTANGLE;
        assertEquals(RenderType.RECTANGLE, record.rt);
    }
    
    @Test
    public void testColorFields() {
        GameObjectRecord record = new GameObjectRecord();
        record.r = 1.0f;
        record.g = 0.5f;
        record.b = 0.0f;
        record.a = 0.8f;
        
        assertEquals(1.0f, record.r, 0.001f);
        assertEquals(0.5f, record.g, 0.001f);
        assertEquals(0.0f, record.b, 0.001f);
        assertEquals(0.8f, record.a, 0.001f);
    }
    
    @Test
    public void testSegmentsAndImagePath() {
        GameObjectRecord record = new GameObjectRecord();
        record.segments = 32;
        record.imagePath = "path/to/image.png";
        
        assertEquals(32, record.segments);
        assertEquals("path/to/image.png", record.imagePath);
    }
    
    @Test
    public void testDefaultValues() {
        GameObjectRecord record = new GameObjectRecord();
        assertNull(record.id);
        assertNull(record.rt);
        assertNull(record.imagePath);
        assertNull(record.identity);
    }
    
    @Test
    public void testHealthValues() {
        GameObjectRecord record = new GameObjectRecord();
        record.currentHealth = 50;
        record.maxHealth = 100;
        
        assertEquals(50, record.currentHealth);
        assertEquals(100, record.maxHealth);
        assertTrue(record.currentHealth <= record.maxHealth);
    }
}
