package com.gameengine.game;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * InputRecord 类测试
 */
public class InputRecordTest {
    
    @Test
    public void testDefaultConstructor() {
        InputRecord record = new InputRecord();
        assertEquals("INPUT", record.type);
    }
    
    @Test
    public void testParameterizedConstructor() {
        InputRecord record = new InputRecord("192.168.1.1:8080", 1.5f, -2.0f);
        
        assertEquals("INPUT", record.type);
        assertEquals("192.168.1.1:8080", record.addressId);
        assertEquals(1.5f, record.vx, 0.001f);
        assertEquals(-2.0f, record.vy, 0.001f);
    }
    
    @Test
    public void testFieldAssignment() {
        InputRecord record = new InputRecord();
        record.addressId = "test-address";
        record.vx = 10.0f;
        record.vy = 20.0f;
        
        assertEquals("test-address", record.addressId);
        assertEquals(10.0f, record.vx, 0.001f);
        assertEquals(20.0f, record.vy, 0.001f);
    }
    
    @Test
    public void testZeroVelocity() {
        InputRecord record = new InputRecord("addr", 0.0f, 0.0f);
        assertEquals(0.0f, record.vx, 0.001f);
        assertEquals(0.0f, record.vy, 0.001f);
    }
    
    @Test
    public void testNegativeVelocity() {
        InputRecord record = new InputRecord("addr", -5.0f, -10.0f);
        assertEquals(-5.0f, record.vx, 0.001f);
        assertEquals(-10.0f, record.vy, 0.001f);
    }
}
