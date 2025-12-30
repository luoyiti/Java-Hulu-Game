package com.gameengine.game;

import org.junit.Test;
import org.junit.Before;
import static org.junit.Assert.*;

/**
 * Record 类测试
 */
public class RecordTest {
    
    private Record record;
    
    @Before
    public void setUp() {
        record = new Record();
    }
    
    @Test
    public void testDefaultValues() {
        assertEquals(100, record.getPlayerHealth());
        assertEquals(100, record.getPlayerMaxHealth());
        assertEquals(1.0f, record.getSkillCooldownPercent(), 0.001f);
        assertEquals(1, record.getCurrentLevel());
    }
    
    @Test
    public void testSetRecordTypeInput() {
        record.setRecordType("input");
        assertEquals("input", record.getType());
        assertNull(record.getGameObjectsMove());
    }
    
    @Test
    public void testSetRecordTypeObjectMove() {
        record.setRecordType("object_move");
        assertEquals("object_move", record.getType());
    }
    
    @Test
    public void testSetKey() {
        record.setKey(1.5f);
        assertEquals(1.5f, record.getKey(), 0.001f);
    }
    
    @Test
    public void testSetPlayerHealth() {
        record.setPlayerHealth(50);
        assertEquals(50, record.getPlayerHealth());
    }
    
    @Test
    public void testSetPlayerMaxHealth() {
        record.setPlayerMaxHealth(200);
        assertEquals(200, record.getPlayerMaxHealth());
    }
    
    @Test
    public void testSetSkillCooldownPercent() {
        record.setSkillCooldownPercent(0.5f);
        assertEquals(0.5f, record.getSkillCooldownPercent(), 0.001f);
    }
    
    @Test
    public void testSetCurrentLevel() {
        record.setCurrentLevel(3);
        assertEquals(3, record.getCurrentLevel());
    }
    
    @Test
    public void testGetTypeNull() {
        assertEquals("null", record.getType());
    }
    
    @Test
    public void testGameObjectsMove() {
        assertNotNull(record.getGameObjectsMove());
        assertTrue(record.getGameObjectsMove().isEmpty());
    }
}
